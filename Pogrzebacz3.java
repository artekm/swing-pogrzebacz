import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.UIManager;

public class Pogrzebacz3 {
	public static final int NUM_TASKS = Runtime.getRuntime().availableProcessors();
	public static final String DOWNLOAD_FOLDER = Paths.get(System.getProperty("user.home"), "Downloads").toString();
	public static final boolean FORWARD = true;
	public static final boolean BACKWARD = false;

	private JButton buttonStartStop;
	private Downloader downloader;

	class MyTransferHandler extends TransferHandler {
		private static final long serialVersionUID = 1L;
		private Image image;
		private JLabel label;
		private String page;

		MyTransferHandler(Image image, JLabel label) {
			super();
			this.image = image;
			this.label = label;
		}

		public boolean canImport(TransferSupport support) {
			return (support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)
					&& support.isDataFlavorSupported(DataFlavor.stringFlavor)
					&& support.isDataFlavorSupported(DataFlavor.fragmentHtmlFlavor));
		}

		@SuppressWarnings("unchecked")
		public boolean importData(TransferSupport support) {
			Transferable tr = support.getTransferable();
			try {
				List<File> list = (List<File>) tr.getTransferData(DataFlavor.javaFileListFlavor);
				String s = (String) tr.getTransferData(DataFlavor.fragmentHtmlFlavor);
				if (s.contains("/thb/")) {
					image = ImageIO.read(list.get(0));
					label.setText("");
					label.setIcon(new ImageIcon(image));
					page = (String) tr.getTransferData(DataFlavor.stringFlavor);
					page = page.replace("medium", "full");
					buttonStartStop.setEnabled(true);
				}
			} catch (UnsupportedFlavorException | IOException e) {
			}

			return true;
		}

		public String getPage() {
			return page;
		}
	}

	public void LokalizujNapisyPL()
    {
        UIManager.put("FileChooser.lookInLabelText","Szukaj w");
        UIManager.put("FileChooser.lookInLabelMnemonic",""+ KeyEvent.VK_W);

        UIManager.put("FileChooser.saveInLabelText","Zapisz w");
        UIManager.put("FileChooser.saveInLabelMnemonic",""+KeyEvent.VK_W);

        UIManager.put("FileChooser.fileNameLabelText","Nazwa pliku:");
        UIManager.put("FileChooser.fileNameLabelMnemonic",""+KeyEvent.VK_N);

        UIManager.put("FileChooser.folderNameLabelText","Nazwa folderu:");
        UIManager.put("FileChooser.folderNameLabelMnemonic",""+KeyEvent.VK_N);

        UIManager.put("FileChooser.filesOfTypeLabelText","Pliki typu:");
        UIManager.put("FileChooser.filesOfTypeLabelMnemonic",""+KeyEvent.VK_P);

        UIManager.put("FileChooser.upFolderToolTipText","Poziom wy¿ej");
        UIManager.put("FileChooser.homeFolderToolTipText","Pulpit");
        UIManager.put("FileChooser.newFolderToolTipText","Nowy folder");
        UIManager.put("FileChooser.listViewButtonToolTipText","Lista");
        UIManager.put("FileChooser.detailsViewButtonToolTipText","Szczegó³y");

        UIManager.put("FileChooser.fileNameHeaderText","Nazwa");
        UIManager.put("FileChooser.fileSizeHeaderText","Rozmiar");
        UIManager.put("FileChooser.fileTypeHeaderText","Typ");
        UIManager.put("FileChooser.fileDateHeaderText","Modyfikacja");
        UIManager.put("FileChooser.fileAttrHeaderText","Atrybuty");

        UIManager.put("FileChooser.newFolderErrorText","B³¹d podczas tworzenia folderu");

        UIManager.put("FileChooser.saveButtonText","Zapisz");
        UIManager.put("FileChooser.saveButtonMnemonic",""+KeyEvent.VK_Z);

        UIManager.put("FileChooser.openButtonText","Otwórz");
        UIManager.put("FileChooser.openButtonMnemonic",""+KeyEvent.VK_O);

        UIManager.put("FileChooser.cancelButtonText","Anuluj");
        UIManager.put("FileChooser.openButtonMnemonic",""+KeyEvent.VK_R);

        UIManager.put("FileChooser.openDialogTitleText","Otwieranie");
        UIManager.put("FileChooser.saveDialogTitleText","Zapisywanie");

        UIManager.put("FileChooser.saveButtonToolTipText","Zapisanie pliku");
        UIManager.put("FileChooser.openButtonToolTipText","Otwarcie pliku");
        UIManager.put("FileChooser.cancelButtonToolTipText","Anuluj");
        UIManager.put("FileChooser.acceptAllFileFilterText","Wszystkie pliki");

        UIManager.put("FileChooser.directoryOpenButtonText", "Otwórz folder");
        UIManager.put("FileChooser.directoryOpenButtonToolTipText", "Otwiera folder");

        UIManager.put("FileChooser.foldersLabelText", "Nazwa folderu: ");
        UIManager.put("FileChooser.pathLabelText", "Œcie¿ka: ");
        UIManager.put("FileChooser.directoryDescriptionText", "Scie¿ka folderu, opis");
        UIManager.put("FileChooser.foldersLabelText", "Foldery");
        UIManager.put("FileChooser.newFolderAccessibleName", "Nowy folder");
        UIManager.put("FileChooser.newFolderToolTipText", "Nowy folder");
        UIManager.put("FileChooser.other.newFolder", "Nowy folder");
        UIManager.put("FileChooser.other.newFolder.subsequent", "Nowy folder");
        UIManager.put("FileChooser.win32.newFolder", "Nowy folder");
        UIManager.put("FileChooser.win32.newFolder.subsequent", "Nowy folder");

    }
	
	private void runGUI() {
		{
			LokalizujNapisyPL();
		    JFrame.setDefaultLookAndFeelDecorated(true);
			JDialog.setDefaultLookAndFeelDecorated(true);

			JFrame frame = new JFrame("Pogrzebacz");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setLocationByPlatform(true);
			frame.setResizable(false);
			frame.setIconImage(new ImageIcon("PogrzebaczIcon.png").getImage());

			JPanel panelNorth = new JPanel();
			panelNorth.setBorder(BorderFactory.createTitledBorder("Przeci¹gnij strony z SWA"));
			frame.add(panelNorth, BorderLayout.NORTH);

			JLabel labelBeginPage = new JLabel("strona pocz¹tkowa", JLabel.CENTER);
			labelBeginPage.setPreferredSize(new Dimension(200, 200));
			labelBeginPage.setBorder(BorderFactory.createLoweredBevelBorder());
			Image imgStart = null;
			labelBeginPage.setTransferHandler(new MyTransferHandler(imgStart, labelBeginPage));
			panelNorth.add(labelBeginPage, BorderLayout.WEST);

			JLabel labelEndPage = new JLabel("strona koñcowa", JLabel.CENTER);
			labelEndPage.setPreferredSize(new Dimension(200, 200));
			labelEndPage.setBorder(BorderFactory.createLoweredBevelBorder());
			Image imgEnd = null;
			labelEndPage.setTransferHandler(new MyTransferHandler(imgEnd, labelEndPage));
			panelNorth.add(labelEndPage, BorderLayout.EAST);

			JPanel panelCenter = new JPanel(new BorderLayout());
			frame.add(panelCenter, BorderLayout.CENTER);

			JPanel panelCenterNorth = new JPanel();
			panelCenterNorth.setBorder(BorderFactory.createTitledBorder("Folder docelowy"));
			panelCenter.add(panelCenterNorth, BorderLayout.NORTH);

			JTextField inputTargetFolder = new JTextField(
					new File(System.getProperty("user.home"), "Downloads").toString(), 30);
			panelCenterNorth.add(inputTargetFolder);

			JButton buttonWybierzFolder = new JButton("Wybierz");
			buttonWybierzFolder.setMnemonic(KeyEvent.VK_W);
			panelCenterNorth.add(buttonWybierzFolder);

			JPanel panelCenterSouth = new JPanel();
			panelCenterSouth.setBorder(BorderFactory.createTitledBorder("Pobieranie"));
			panelCenter.add(panelCenterSouth, BorderLayout.SOUTH);

			JLabel labelProcesy = new JLabel("Procesy");
			panelCenterSouth.add(labelProcesy);

			JSpinner spinerProcesy = new JSpinner(new SpinnerNumberModel(NUM_TASKS, 1, 10, 1));
			panelCenterSouth.add(spinerProcesy);

			buttonStartStop = new JButton("Rozpocznij pobieranie");
			buttonStartStop.setEnabled(false);
			panelCenterSouth.add(buttonStartStop);

			JButton buttonExit = new JButton("Zakoñcz");
			buttonExit.setMnemonic(KeyEvent.VK_Z);
			panelCenterSouth.add(buttonExit);

			JPanel panelSouth = new JPanel();
			panelSouth.setBorder(BorderFactory.createTitledBorder("Postêp pobierania"));
			frame.add(panelSouth, BorderLayout.SOUTH);

			JTextArea textLogger = new JTextArea("", 10, 38);
			textLogger.setEditable(false);

			JScrollPane scrollPane = new JScrollPane(textLogger);
			panelSouth.add(scrollPane, BorderLayout.SOUTH);
			downloader = new Downloader(this, textLogger);

			buttonWybierzFolder.addActionListener(event -> {
				JFileChooser chooser = new JFileChooser();
				chooser.setCurrentDirectory(new File(inputTargetFolder.getText()));
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				chooser.setDialogTitle("Wybierz folder docelowy");
				int result = chooser.showDialog(null, "Wybierz");
				if (result == JFileChooser.APPROVE_OPTION) {
					inputTargetFolder.setText(chooser.getSelectedFile().toString());
				}
			});

			buttonStartStop.addActionListener(event -> {
				if (downloader.isOngoing()) {
					downloader.cancelDownloading();
					buttonStartStop.setText("Rozpocznij pobieranie");
				} else {
					String pageBegin = ((MyTransferHandler) labelBeginPage.getTransferHandler()).getPage();
					String pageEnd = ((MyTransferHandler) labelEndPage.getTransferHandler()).getPage();
					int tasksNumber = (Integer) spinerProcesy.getValue();
					String saveToFolder = inputTargetFolder.getText();
					buttonStartStop.setText("Przerwij pobieranie");
					downloader.startDownloading(pageBegin, pageEnd, saveToFolder, tasksNumber);
				}
			});

			buttonExit.addActionListener(event -> {
				downloader.cancelDownloading();
				SwingUtilities.getWindowAncestor(buttonExit).dispose();
			});

			frame.pack();
			frame.setVisible(true);
		}
	}

	public void downloadingFinished() {
		buttonStartStop.setText("Rozpocznij pobieranie");
	}

	public static void main(String[] args) {
		EventQueue.invokeLater(() -> new Pogrzebacz3().runGUI());
	}
}
