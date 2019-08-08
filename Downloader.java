import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.*;

import javax.net.ssl.HttpsURLConnection;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

public class Downloader {
	public static final String POISON_PILL = "PILL";
	public static final boolean FORWARD = true;
	public static final boolean BACKWARD = false;

	private List<SwingWorker<Void, Void>> tasks = new LinkedList<>();
	private Queue<String> queue = new ConcurrentLinkedQueue<>();
	private JTextArea logger;
	private Pogrzebacz3 control;
	private int numScan;
	private long totalLength;
	private Instant startTime;
	private Instant stopTime;

	Downloader(Pogrzebacz3 control, JTextArea logger) {
		this.control = control;
		this.logger = logger;
	}

	private static String extractValueForKey(String line, String key) {

		Matcher m = Pattern.compile(key + "=\"(.+)\"").matcher(line);
		if (m.find())
			return m.group(1);
		else
			return null;
	}

	class PageReader extends SwingWorker<Void, Void> {
		private String nextHttpsName;
		private String lastHttpsName;
		private boolean forwardDirection;
		private int tasksNum;

		PageReader(String httpsName, String httpsLast, int tasksNum, boolean forwardDirection) {
			super();
			this.nextHttpsName = httpsName;
			this.lastHttpsName = httpsLast;
			this.forwardDirection = forwardDirection;
			this.tasksNum = tasksNum;
		}

		@Override
		protected Void doInBackground() throws Exception {
			String currentHttpsName;
			try {
				do {
					currentHttpsName = nextHttpsName;
					Thread.sleep(50);
					nextHttpsName = parsePage(currentHttpsName);
				} while (nextHttpsName != null && !currentHttpsName.equals(lastHttpsName));
			} catch (InterruptedException e) {
				return null;
			}
			for (int poison = 0; poison < tasksNum; poison++) {
				queue.offer(POISON_PILL);
			}
			logger.append("KONIEC SZUKANIA\n");
			return null;
		}

		@Override
		protected void done() {
			if (!isCancelled())
				tasks.remove(this);

			if (tasks.isEmpty()) {
				downloadingFinished();
			}
		}

		private String parsePage(String httpsName) throws Exception {
			logger.append("[" + Thread.currentThread().getId() + "] Przeszukujê " + httpsName + "\n");
			URL myurl = new URL(httpsName);
			HttpsURLConnection con = (HttpsURLConnection) myurl.openConnection();

			String charsetLine = con.getHeaderField("Content-Type");
			String charsetName = charsetLine.substring(charsetLine.indexOf("=") + 1);

			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), charsetName));

			String nextPage = null;
			String downloadPage = null;

			String line = null;
			while ((line = in.readLine()) != null) {
				if (line.contains("Next") && forwardDirection == FORWARD)
					nextPage = "https://szukajwarchiwach.pl" + extractValueForKey(line, "href");
				if (line.contains("Previous") && forwardDirection == BACKWARD)
					nextPage = "https://szukajwarchiwach.pl" + extractValueForKey(line, "href");
				if (line.contains("Download"))
					downloadPage = "https://szukajwarchiwach.pl" + extractValueForKey(line, "href");
			}
			queue.offer(downloadPage);

			return nextPage;
		}
	}

	class PageDownloader extends SwingWorker<Void, Void> {

		private String folder;

		PageDownloader(String folder) {
			super();
			this.folder = folder;
		}

		@Override
		protected Void doInBackground() {
			String httpsName = null;
			try {
				do {
					httpsName = pollTaskFromQueue();
					if (POISON_PILL.equals(httpsName))
						break;
					downloadAttachement(httpsName);
				} while (true);
			} catch (InterruptedException | IOException e) {
			}
			return null;
		}

		@Override
		protected void done() {
			if (!isCancelled())
				tasks.remove(this);
			if (tasks.isEmpty()) {
				downloadingFinished();
			}
		}

		private String pollTaskFromQueue() throws InterruptedException {
			do {
				Thread.sleep(50);
				String httpsName = queue.poll();
				if (httpsName != null)
					return httpsName;
			} while (true);
		}

		private void downloadAttachement(String httpsName) throws IOException {
			URL myurl = new URL(httpsName);
			HttpsURLConnection con = (HttpsURLConnection) myurl.openConnection();

			String fileNameLine = con.getHeaderField("Content-Disposition");
			String fileName = fileNameLine.substring(fileNameLine.indexOf("=") + 1);

			String fileLengthLine = con.getHeaderField("Content-Length");
			int fileLength = Integer.valueOf(fileLengthLine.substring(fileLengthLine.indexOf("=") + 1));

			Path path = Paths.get(folder, fileName);
			logger.append("[" + Thread.currentThread().getId() + "] Pobieram " + path + "\n");

			InputStream in = con.getInputStream();
			Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
			in.close();

			synchronized (this) {
				numScan++;
				totalLength += fileLength;
			}
			logger.append("[" + Thread.currentThread().getId() + "] Pobra³em " + path + "\n");
		}

	}

	public void cancelDownloading() {
		tasks.forEach(task -> task.cancel(true));
		tasks.clear();
		queue.clear();
		logger.append("PRZERWANO\n");
	}

	public boolean isOngoing() {
		return !tasks.isEmpty();
	}

	public void startDownloading(String startPage, String endPage, String targetFolder, int tasksNum) {
		System.setProperty("com.sun.security.enableAIAcaIssuers", "true");
		tasks.clear();
		queue.clear();
		logger.setText("");
		if (startPage != null)
			tasks.add(new PageReader(startPage, endPage, tasksNum, FORWARD));
		else
			tasks.add(new PageReader(endPage, startPage, tasksNum, BACKWARD));
		for (int cn = 1; cn <= tasksNum; cn++)
			tasks.add(new PageDownloader(targetFolder));
		numScan = 0;
		totalLength = 0;
		startTime = Instant.now();
		tasks.forEach(task -> task.execute());
	}

	public void downloadingFinished() {
		stopTime = Instant.now();
		logger.append("KONIEC POBIERANIA\n");
		double execTime = startTime.until(stopTime, ChronoUnit.SECONDS);
		logger.append(String.format("Czas %.0f sekund (%.2f sekund/skan, %.2f Mbit/sek)%n",
				execTime,
				execTime / numScan,
				totalLength / (128 * 1024 * execTime)));
		control.downloadingFinished();
	}
}
