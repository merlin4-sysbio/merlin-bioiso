package pt.uminho.ceb.biosystems.merlin.merlin_biocoiso;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;

import es.uvigo.ei.aibench.workbench.Workbench;
import es.uvigo.ei.aibench.workbench.utilities.Utilities;
import pt.uminho.ceb.biosystems.merlin.aibench.datatypes.WorkspaceAIB;
import pt.uminho.ceb.biosystems.merlin.utilities.io.FileUtils;

public class BiocoisoHistoryGUI extends JDialog{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private WorkspaceAIB workspace;
	
	public BiocoisoHistoryGUI(WorkspaceAIB workspace) {
		super(Workbench.getInstance().getMainFrame());
		this.workspace = workspace;
		initGUI();
		Utilities.centerOnOwner(this);
		this.setIconImage((new ImageIcon(getClass().getClassLoader().getResource("icons/merlin.png"))).getImage());
		this.setVisible(true);		
		this.setAlwaysOnTop(true);
		this.toFront();

}

	public void initGUI() {
		this.setModal(true);
		JPanel jPanel1;
		{
			this.setTitle("BioISO simulations history");
			jPanel1 = new JPanel(new GridBagLayout());
			getContentPane().add(jPanel1, BorderLayout.CENTER);
			GridBagConstraints c = new GridBagConstraints();
		}
		
		
		String currentDirectory = getWorkDirectory();
		
		File[] biocoisoFilesList = new File(currentDirectory.concat("/biocoiso")).listFiles();
		
		JPanel jPanel11;
		{
			jPanel11 = new JPanel(new GridBagLayout());
			jPanel1.add(jPanel11, new GridBagConstraints(0, 1, 1, biocoisoFilesList.length, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		}
		
		JLabel label = new JLabel("coiso");
		label.setBorder(jPanel11.getBorder());
		label.setForeground(jPanel11.getForeground());
		label.setFont(UIManager.getFont("Label.font"));
		label.setBackground(jPanel11.getBackground());
		label.setVisible(true);
		
		JLabel label1 = new JLabel("coiso");
		label.setBorder(jPanel11.getBorder());
		label.setForeground(jPanel11.getForeground());
		label.setFont(UIManager.getFont("Label.font"));
		label.setBackground(jPanel11.getBackground());
		label.setVisible(true);
		
		jPanel11.add(label, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		
//		for (int i = 0; i<biocoisoFilesList.length;i++) {
//			JPanel jPanel11n;
//			{
//				JTextField label = new JTextField("coiso");
//				label.setBorder(jPanel11.getBorder());
//				label.setEditable(false);
//				label.setForeground(jPanel11.getForeground());
//				label.setFont(UIManager.getFont("Label.font"));
//				label.setBackground(jPanel11.getBackground());
//				label.setVisible(true);
//				
//				jPanel11n = new JPanel();
//				getContentPane().add(jPanel11n, BorderLayout.CENTER);
//				GridBagLayout jPanel11nLayout = new GridBagLayout();
//				jPanel11nLayout.columnWeights = new double[] {0.0, 0.1, 0.0};
//				jPanel11nLayout.columnWidths = new int[] {7, 7, 7};
//				jPanel11nLayout.rowWeights = new double[] {0.1, 0.0, 0.1};
//				jPanel11nLayout.rowHeights = new int[] {7, 7, 7};
//				jPanel11n.setLayout(jPanel11nLayout);
//				
//				jPanel11n.add(label,new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
//				
//				jPanel11.add(label, new GridBagConstraints(0, i, 3, 1, 0.0, 0.0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
//			}
//		}
		
		this.setSize(480, 230);
		
	}
	
	
	private String getWorkDirectory() {
		
		String database = this.workspace.getName();

		Long taxonomyID= this.workspace.getTaxonomyID();

		String path = FileUtils.getWorkspaceTaxonomyFolderPath(database, taxonomyID);

		return path;

	}
		
	}
