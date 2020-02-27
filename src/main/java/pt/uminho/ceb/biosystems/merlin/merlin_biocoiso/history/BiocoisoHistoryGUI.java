package pt.uminho.ceb.biosystems.merlin.merlin_biocoiso.history;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventObject;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.json.simple.parser.ParseException;

import es.uvigo.ei.aibench.core.Core;
import es.uvigo.ei.aibench.core.clipboard.ClipboardItem;
import es.uvigo.ei.aibench.workbench.Workbench;
import es.uvigo.ei.aibench.workbench.utilities.Utilities;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.WorkspaceDataTable;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.WorkspaceGenericDataTable;
import pt.uminho.ceb.biosystems.merlin.gui.datatypes.WorkspaceAIB;
import pt.uminho.ceb.biosystems.merlin.gui.datatypes.WorkspaceTableAIB;
import pt.uminho.ceb.biosystems.merlin.gui.utilities.AIBenchUtils;
import pt.uminho.ceb.biosystems.merlin.gui.views.windows.GenericDetailWindow;
import pt.uminho.ceb.biosystems.merlin.merlin_biocoiso.BiocoisoUtils;
import pt.uminho.ceb.biosystems.merlin.merlin_biocoiso.datatypes.ValidationBiocoisoAIB;
import pt.uminho.ceb.biosystems.merlin.utilities.io.FileUtils;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.pair.Pair;


/*
 * @author João Capela
 *
 */
public class BiocoisoHistoryGUI extends JDialog{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private WorkspaceAIB workspace;
	private JTable jTable;
	private BiocoisoButtonColumn buttonColumnCommit;
	private BiocoisoButtonColumn buttonColumnCompare;
	Icon notProduced = new ImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/notProducing.png")).getImage().getScaledInstance(20, 20, Image.SCALE_DEFAULT));
	Icon produced = new ImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/producing.png")).getImage().getScaledInstance(20, 20, Image.SCALE_DEFAULT));
	Icon unknown = new ImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/question.png")).getImage().getScaledInstance(20, 20, Image.SCALE_DEFAULT));
	private Map<String, Set<String>> actualModelData;


	public BiocoisoHistoryGUI(WorkspaceAIB workspace) {
		super(Workbench.getInstance().getMainFrame());
		this.workspace = workspace;
		try {
			this.actualModelData = BiocoisoUtils.getGenesReactionsMetabolites(this.workspace);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
		}


		GridBagLayout jPanel1Layout = new GridBagLayout();
		//		jPanel1Layout.columnWeights = new double[] {0.0, 0.1, 0.0};
		//		jPanel1Layout.columnWidths = new int[] {7, 7, 7,7,7};
		//		jPanel1Layout.rowWeights = new double[] {5, 3.5, 0.0, 0.1, 0.0};
		//		jPanel1Layout.rowHeights = new int[] {5, 5, 5, 5, 5};
		this.setLayout(jPanel1Layout);

		{

			JPanel jPanel11 = new JPanel();
			BorderLayout jPanel11Layout = new BorderLayout();
			jPanel11.setLayout(jPanel11Layout);

			JLabel jLabel1 = new JLabel("BioISO History of simulations");
			jPanel11.add(jLabel1);
			this.add(jPanel11, new GridBagConstraints(0, 0, 3, 1, 1, 2, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

		}

		{
			JPanel jPanel2 = new JPanel();
			BorderLayout jPanel2Layout = new BorderLayout();
			jPanel2.setLayout(jPanel2Layout);


			File folder = new File(this.getWorkDirectory().concat("/biocoiso"));

			if (!folder.exists()) {
				folder.mkdir();
			}

			File[] biocoisoFilesList = folder.listFiles();
			Object[][] dados2 = new Object[biocoisoFilesList.length][6];

			//			"commit","compare models", "reaction", "hour", "day", "select"

			for (int i = 0; i<biocoisoFilesList.length;i++) {

				String[] reactionHourDay = biocoisoFilesList[i].getName().split("\\.");


				dados2[i][2] = reactionHourDay[0];

				dados2[i][3] = reactionHourDay[1];

				dados2[i][4] = reactionHourDay[2];

				dados2[i][5] = false;

			}
			{
				this.jTable = new JTable();

				this.jTable.setModel(new BiocoisoDefaultTableModel(dados2));
				
				TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(jTable.getModel());
				jTable.setRowSorter(sorter);

				this.buttonColumnCommit = new BiocoisoButtonColumn(jTable,0, new ActionListener(){
					public void actionPerformed(ActionEvent arg0){
						processButtonCommit(arg0);
					} },
						new MouseAdapter(){
						public void mouseClicked(MouseEvent e) {
							// {
							// get the coordinates of the mouse click
							Point p = e.getPoint();

							// get the row index that contains that coordinate
							int rowNumber = jTable.rowAtPoint(p);
							int  columnNumber = jTable.columnAtPoint(p);
							jTable.setColumnSelectionInterval(columnNumber, columnNumber);
							// Get the ListSelectionModel of the MyJTable
							ListSelectionModel model = jTable.getSelectionModel();
							// set the selected interval of rows. Using the "rowNumber"
							// variable for the beginning and end selects only that one row.
							model.setSelectionInterval( rowNumber, rowNumber );
							processButtonCommit(e);
						}
					}, new ArrayList<>(),"commit");


				this.buttonColumnCompare = new BiocoisoButtonColumn(jTable,1, new ActionListener(){
					public void actionPerformed(ActionEvent arg0){
						try {
							processButtonCompare(arg0);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} },
						new MouseAdapter(){
						public void mouseClicked(MouseEvent e) {
							// {
							// get the coordinates of the mouse click
							Point p = e.getPoint();

							// get the row index that contains that coordinate
							int rowNumber = jTable.rowAtPoint(p);
							int  columnNumber = jTable.columnAtPoint(p);
							jTable.setColumnSelectionInterval(columnNumber, columnNumber);
							// Get the ListSelectionModel of the MyJTable
							ListSelectionModel model = jTable.getSelectionModel();
							// set the selected interval of rows. Using the "rowNumber"
							// variable for the beginning and end selects only that one row.
							model.setSelectionInterval( rowNumber, rowNumber );
							try {
								processButtonCompare(e);
							} catch (Exception e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}


					}, new ArrayList<>(),"model");


				jTable.getTableHeader().setReorderingAllowed(false);

				// Turn off JTable's auto resize so that JScrollPane will show a
				// horizontal scroll bar.

				jTable.getColumnModel().getColumn(0).setPreferredWidth(5);
				jTable.getColumnModel().getColumn(1).setPreferredWidth(40);
				jTable.getColumnModel().getColumn(2).setPreferredWidth(50);
				jTable.getColumnModel().getColumn(3).setPreferredWidth(5);
				jTable.getColumnModel().getColumn(4).setPreferredWidth(5);
				jTable.getColumnModel().getColumn(5).setPreferredWidth(5);

//				jTable.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);


				JScrollPane pane = new JScrollPane(jTable);
				jPanel2.add(pane);
				this.add(jPanel2, new GridBagConstraints(2, 2, 3, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
			}

			{
				JPanel jPanelButtons = new JPanel();

				jPanelButtons.setBorder(BorderFactory.createTitledBorder("menu"));
				//				BorderLayout jPanelButtonsLayout = new BorderLayout();
				//				jPanelButtons.setLayout(jPanelButtonsLayout);
				JButton deleteButton = new JButton("delete");
				deleteButton.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {


						ArrayList<Integer> indexes = new ArrayList<Integer>();

						for(int i=0;i<jTable.getRowCount();i++)
						{
							Boolean checked=Boolean.valueOf(jTable.getValueAt(i, 5).toString());

							if (checked) {
								indexes.add(i);
								String fileName = (String) jTable.getValueAt(i, 2) + "." + jTable.getValueAt(i, 3) + "." + jTable.getValueAt(i, 4);
								File folder = new File(getWorkDirectory().concat("/biocoiso/").concat(fileName));
								try {
									FileUtils.deleteDirectory(folder);
								} catch (IOException e1) {
									e1.printStackTrace();
								}
							}

						}
						((BiocoisoDefaultTableModel) jTable.getModel()).removeRow(indexes);

					}


				});


				JButton showButton =  new JButton("show");
				showButton.addActionListener(new ActionListener(){

					@Override
					public void actionPerformed(ActionEvent e) {

						for(int i=0;i<jTable.getRowCount();i++)
						{
							Boolean checked=Boolean.valueOf(jTable.getValueAt(i, 5).toString());

							if (checked) {

								String fileName = (String) jTable.getValueAt(i, 2) + "." + jTable.getValueAt(i, 3) + "." + jTable.getValueAt(i, 4);

								File file = new File(getWorkDirectory().concat("/biocoiso/").concat(fileName).concat("/results/results_biocoiso.json"));
								
								System.out.println(file);
								int table_number = getProject().getDatabase().getValidation().getEntities().size() + 1;

								String name = Integer.toString(table_number) + " - " + fileName;

								String[] columnsName = new String[] {"info","metabolite", "reactions", "role" , "analysis"};

								WorkspaceTableAIB table = new WorkspaceTableAIB(name, columnsName , getProject().getName());

								File results_file = file;

								Pair<WorkspaceGenericDataTable, Map<?, ?>> filledTableAndNextLevel;
								try {
									filledTableAndNextLevel = BiocoisoUtils.createDataTable(results_file.getAbsolutePath(), 
											Arrays.asList(columnsName), getProject().getName(), name,produced,notProduced,unknown);


									Map<?, ?> entireMap = BiocoisoUtils.readJSON(results_file.getAbsolutePath());

									ValidationBiocoisoAIB biocoiso = new ValidationBiocoisoAIB(table, name, filledTableAndNextLevel.getB(), entireMap);

									biocoiso.setWorkspace(getProject());

									biocoiso.setMainTableData(filledTableAndNextLevel.getA());

									biocoiso.setData(filledTableAndNextLevel.getA());

									getProject().getValidation().addEntity(biocoiso);

								} catch (IOException e2) {
									e2.printStackTrace();
								} catch (ParseException e2) {
									e2.printStackTrace();
								}

								catch (Exception e1) {

									List<WorkspaceAIB> wspList = AIBenchUtils.getAllProjects();

									for (WorkspaceAIB wsp : wspList) {

										ClipboardItem item = Core.getInstance().getClipboard().getClipboardItem(wsp);

										Core.getInstance().getClipboard().removeClipboardItem(item);

										Core.getInstance().getClipboard().putItem(wsp, item.getName());

									}
								}
							}
						}




					}});
				jPanelButtons.add(showButton);
				jPanelButtons.add(deleteButton);
				this.add(jPanelButtons,new GridBagConstraints(2, 3, 2, 4, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

			}




			this.revalidate();
			this.repaint();

		}

		this.setSize(620, 600);

	}


	protected void processButtonCompare(EventObject e) throws Exception {
		JButton button = null;
		if(e.getClass()==ActionEvent.class)
			button = (JButton)((ActionEvent) e).getSource();

		if(e.getClass()==MouseEvent.class)
			button = (JButton) e.getSource();

		button.setSelected(true);

		ListSelectionModel model1 = jTable.getSelectionModel();
		model1.setSelectionInterval( buttonColumnCompare.getSelectIndex(button), buttonColumnCompare.getSelectIndex(button));

		String database = this.workspace.getDatabase().getWorkspaceName();

		Long taxonomyID= this.workspace.getTaxonomyID();

		int i = jTable.getSelectedRow();

		String path = FileUtils.getWorkspaceTaxonomyFolderPath(database, taxonomyID).concat("/biocoiso/");

		String fileName = (String) jTable.getValueAt(i, 2) + "." + jTable.getValueAt(i, 3) + "." + jTable.getValueAt(i, 4);

		String modelPath = path.concat(fileName.concat("/model.xml"));

		File model = new File(modelPath);

		Map<String, Set<String>> oldModel = BiocoisoUtils.getGenesReactionsMetabolitesFromModel(this.workspace, model);

		ArrayList<String> genesDeleted = new ArrayList<String>();
		ArrayList<String> genesInserted = new ArrayList<String>();

		ArrayList<String> metabolitesDeleted = new ArrayList<String>();
		ArrayList<String> metabolitesInserted = new ArrayList<String>();

		ArrayList<String> reactionsDeleted = new ArrayList<String>();
		ArrayList<String> reactionsInserted = new ArrayList<String>();

		Set<String> actualGenes = this.actualModelData.get("genes");
		Set<String> oldGenes = oldModel.get("genes");

		Set<String> actualMetabolites = this.actualModelData.get("metabolites");
		Set<String> oldMetabolites = oldModel.get("metabolites");

		Set<String> actualReactions = this.actualModelData.get("reactions");
		Set<String> oldReactions = oldModel.get("reactions");

		for (String gene : oldGenes ) {

			if (!actualGenes.contains(gene))
				genesDeleted.add(gene);

		}

		for (String gene : actualGenes ) {

			if (!oldGenes.contains(gene))
				genesInserted.add(gene);
		}

		for (String metabolite : oldMetabolites ) {

			if (!actualMetabolites.contains(metabolite))
				metabolitesDeleted.add(metabolite);

		}

		for (String metabolite : actualMetabolites ) {

			if (!oldMetabolites.contains(metabolite))
				metabolitesInserted.add(metabolite);
		}

		for (String reaction : oldReactions ) {

			if (!actualReactions.contains(reaction))
				reactionsDeleted.add(reaction);

		}

		for (String reaction : actualReactions) {

			if (!oldReactions.contains(reaction))
				reactionsInserted.add(reaction);

		}


		WorkspaceDataTable[] results = new WorkspaceDataTable[3];

		String[] columnsNames = new String[] {"inserted","deleted"};



		i = 0;

		results[0] = new WorkspaceDataTable(columnsNames, "genes");
		while (i<genesDeleted.size() || i<genesInserted.size()) {
			String[] genes = new String[2]; 
			if (i>=genesDeleted.size()) {
				genes[1] = "";
				genes[0] = genesInserted.get(i);
			}
			else if (i>=genesInserted.size()) {
				genes[0] = "";
				genes[1] = genesDeleted.get(i);
			}
			else {
				genes[0] = genesInserted.get(i);
				genes[1] = genesDeleted.get(i);
			}
			results[0].addLine(genes);
			i++;
		}

		i=0;
		results[1] = new WorkspaceDataTable(columnsNames, "metabolites");
		while (i<metabolitesDeleted.size() || i<metabolitesInserted.size()) {
			String[] metabolites = new String[2]; 
			if (i>=metabolitesDeleted.size()) {
				metabolites[1] = "";
				metabolites[0] = metabolitesInserted.get(i);
			}
			else if (i>=metabolitesInserted.size()) {
				metabolites[0] = "";
				metabolites[1] = metabolitesDeleted.get(i);
			}
			else {
				metabolites[0] = metabolitesInserted.get(i);
				metabolites[1] = metabolitesDeleted.get(i);
			}
			results[1].addLine(metabolites);
			i++;
		}

		i=0;
		results[2] = new WorkspaceDataTable(columnsNames, "reactions");
		while (i<reactionsDeleted.size() || i<reactionsInserted.size()) {
			String[] reactions = new String[2]; 
			if (i>=reactionsDeleted.size()) {
				reactions[1] = "";
				reactions[0] = reactionsInserted.get(i);
			}
			else if (i>=reactionsInserted.size()) {
				reactions[0] = "";
				reactions[1] = reactionsDeleted.get(i);
			}
			else {
				reactions[0] = reactionsInserted.get(i);
				reactions[1] = reactionsDeleted.get(i);
			}
			results[2].addLine(reactions);
			i++;
		}

		new GenericDetailWindow(results, "", "");


	}

	protected String getWorkDirectory() {

		String database = this.workspace.getName();

		Long taxonomyID= this.workspace.getTaxonomyID();

		String path = FileUtils.getWorkspaceTaxonomyFolderPath(database, taxonomyID);

		return path;

	}


	protected void processButtonCommit(EventObject arg0) {

		JButton button = null;
		if(arg0.getClass()==ActionEvent.class)
			button = (JButton)((ActionEvent) arg0).getSource();

		if(arg0.getClass()==MouseEvent.class)
			button = (JButton) arg0.getSource();

		button.setSelected(true);

		ListSelectionModel model = jTable.getSelectionModel();
		model.setSelectionInterval( buttonColumnCommit.getSelectIndex(button), buttonColumnCommit.getSelectIndex(button));

		int i = jTable.getSelectedRow();
		
		String path = getWorkDirectory().concat("/biocoiso/");
		
		String fileName = (String) jTable.getValueAt(i, 2) + "." + jTable.getValueAt(i, 3) + "." + jTable.getValueAt(i, 4);

		String commitPath = path.concat(fileName.concat("/commit.txt"));

		String message = BiocoisoUtils.readWordsInFile(commitPath);

		Workbench.getInstance().info(message);
	}

	public WorkspaceAIB getProject() {
		return this.workspace;
	}

}


