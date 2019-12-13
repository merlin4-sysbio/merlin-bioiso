package pt.uminho.ceb.biosystems.merlin.merlin_biocoiso;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.Map;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.SoftBevelBorder;

import es.uvigo.ei.aibench.workbench.Workbench;
import es.uvigo.ei.aibench.workbench.utilities.Utilities;
import pt.uminho.ceb.biosystems.merlin.gui.utilities.ButtonColumn;
import pt.uminho.ceb.biosystems.merlin.gui.utilities.MyJTable;
import pt.uminho.ceb.biosystems.merlin.gui.views.windows.GenericDetailWindow;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.WorkspaceDataTable;
import pt.uminho.ceb.biosystems.merlin.merlin_biocoiso.datatypes.ValidationBiocoisoAIB;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.pair.Pair;

public class BiocoisoDetailWindow extends javax.swing.JDialog{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel jPanel1;
	private JScrollPane jScrollPane1;
	private MyJTable jTable1;
	private JPanel jPanel11;
	private JPanel jPanel12;
	private JComboBox<String> searchComboBox;
	private WorkspaceDataTable[] dataTable;
	private String reaction;
	private ButtonColumn buttonColumn;
	private int infoSelectedRow;
	private ValidationBiocoisoAIB entity;
	private String metabolite;
	private boolean last;
	private Map<?, ?> next;

	//	public BiocoisoDetailWindow(Map<String, ArrayList<ArrayList<String>>> mapReactionsAndCompounds, WorkspaceDataTable[] table, String windowName, String name) {
	//
	//		super(Workbench.getInstance().getMainFrame());
	//		this.dataTable = table;
	//		this.mapReactionsAndCompounds=mapReactionsAndCompounds;
	//		initGUI(table, windowName, name);
	//		Utilities.centerOnOwner(this);
	//		this.setIconImage((new ImageIcon(getClass().getClassLoader().getResource("icons/merlin.png"))).getImage());
	//		this.setVisible(true);		
	//		this.setAlwaysOnTop(true);
	//		this.toFront();
	//		
	//	}

	public BiocoisoDetailWindow(Map<?, ?> next, boolean last, String metabolite, ValidationBiocoisoAIB entity,
			WorkspaceDataTable[] table, String windowName, String name) {

		super(Workbench.getInstance().getMainFrame());
		this.last=last;
		this.metabolite=metabolite;
		this.dataTable = table;
		this.next=next;
		//		this.mapReactionsAndCompounds=mapReactionsAndCompounds;
		this.entity=entity;
		initGUI(table, windowName, name);
		Utilities.centerOnOwner(this);
		this.setIconImage((new ImageIcon(getClass().getClassLoader().getResource("icons/merlin.png"))).getImage());
		this.setVisible(true);		
		this.setAlwaysOnTop(true);
		this.toFront();

	}
	private void initGUI(WorkspaceDataTable[] querydatas, String windowName, String name) {

		try {

			this.setTitle(windowName);

			jPanel1 = new JPanel();
			GridBagLayout jPanel1Layout = new GridBagLayout();
			jPanel1.setLayout(jPanel1Layout);
			getContentPane().add(jPanel1, BorderLayout.CENTER);
			jPanel1.setPreferredSize(new java.awt.Dimension(400, 298));

			GridBagConstraints c = new GridBagConstraints();

			c.fill = GridBagConstraints.BOTH;


			jPanel11 = new JPanel();
			BorderLayout jPanel11Layout = new BorderLayout();
			jPanel11.setLayout(jPanel11Layout);

			c.weightx = 1.0;
			c.weighty = 0.015;
			c.gridx = 0;
			c.gridy = 0;
			jPanel1.add(jPanel11, c);

			if(!name.equals("")) {

				JLabel j = new JLabel(name);
				jPanel1.add(j, c);
			}

			int zad = 1;

			if(this.reaction!=null) {

				c.weightx = 1.0;
				c.weighty = 0.015;
				c.gridx = 0;
				c.gridy = zad;
				zad++;
				JLabel rec = new JLabel(this.reaction);
				jPanel1.add(rec, c);
			}

			c.fill = GridBagConstraints.BOTH;

			c.weightx = 1.0;
			c.weighty = 0.92;
			c.gridx = 0;
			c.gridy = zad;
			zad++;

			if(!name.equals("")) c.insets = new Insets(10, 0, 0, 0);

			jPanel1.add(jPanel11, c);
			jScrollPane1 = new JScrollPane();
			jPanel11.add(jScrollPane1, BorderLayout.CENTER);
			jTable1 = new MyJTable();
			jTable1.setShowGrid(false);
			jScrollPane1.setViewportView(jTable1);
			jTable1.setModel(querydatas[0]);

			buttonColumn =  new ButtonColumn(jTable1,0, new ActionListener(){
				public void actionPerformed(ActionEvent arg0){
					processButton(arg0);
				} },
					new MouseAdapter(){
					public void mouseClicked(MouseEvent e) {
						// {
						// get the coordinates of the mouse click
						Point p = e.getPoint();

						// get the row index that contains that coordinate
						int rowNumber = jTable1.rowAtPoint(p);
						int  columnNumber = jTable1.columnAtPoint(p);
						jTable1.setColumnSelectionInterval(columnNumber, columnNumber);
						// Get the ListSelectionModel of the MyJTable
						ListSelectionModel model = jTable1.getSelectionModel();
						// set the selected interval of rows. Using the "rowNumber"
						// variable for the beginning and end selects only that one row.
						model.setSelectionInterval( rowNumber, rowNumber );
						processButton(e);
					}
				}, new ArrayList<>());

			c.insets = new Insets(0, 0, 0, 0);

			c.fill = GridBagConstraints.BOTH;

			c.weightx = 1.0;
			c.weighty = 0.05;
			c.gridx = 0;
			c.gridy = zad;
			zad++;

			jPanel12 = new JPanel();
			jPanel12.setLayout(new GridBagLayout());
			jPanel12.setBorder(new SoftBevelBorder(SoftBevelBorder.RAISED));

			jPanel1.add(jPanel12, c);

			GridBagConstraints c2 = new GridBagConstraints();

			if(querydatas.length==1) {

				JButton button1 = new JButton("Close");
				c2.weightx = 0.95;
				c2.weighty = 1;
				c2.gridx = 0;
				c2.gridy = 0;
				c2.anchor = GridBagConstraints.CENTER;
				jPanel12.add(button1, c2);
				button1.setPreferredSize(new Dimension(75,33));
				button1.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						finish();
					}
				});
			}
			else {

				JButton button1 = new JButton("Close");
				c2.weightx = 0.95;
				c2.weighty = 1;
				c2.gridx = 0;
				c2.gridy = 0;
				c2.anchor = GridBagConstraints.CENTER;
				jPanel12.add(button1, c2);
				button1.setPreferredSize(new Dimension(75,33));
				button1.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						finish();
					}
				});

				String[] chc = new String[dataTable.length];

				for(int s=0;s<this.dataTable.length;s++)					
					chc[s] = this.dataTable[s].getName();

				ComboBoxModel<String> searchComboBoxModel = new DefaultComboBoxModel<>(chc);
				searchComboBox = new JComboBox<>();
				searchComboBox.setModel(searchComboBoxModel);

				c2.weightx = 0.05;
				c2.weighty = 1;
				c2.gridx = 1;
				c2.gridy = 0;
				c2.anchor = GridBagConstraints.EAST;
				jPanel12.add(searchComboBox, c2);

				searchComboBox.addActionListener(new ActionListener() {

					public void actionPerformed(ActionEvent evt) {

						searchComboBoxActionPerformed(evt);
					}
				});
			}

			this.setModal(true);
			this.setSize(600, 400);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void processButton(EventObject arg0) {

		try {
			JButton button = null;
			if(arg0.getClass()==ActionEvent.class)
				button = (JButton)((ActionEvent) arg0).getSource();

			if(arg0.getClass()==MouseEvent.class)
				button = (JButton) arg0.getSource();

			button.setSelected(true);

			ListSelectionModel model = jTable1.getSelectionModel();
			model.setSelectionInterval( buttonColumn.getSelectIndex(button), buttonColumn.getSelectIndex(button));


			boolean refresh = (this.infoSelectedRow == jTable1.getSelectedRow());

			String reactionID = (String) jTable1.getValueAt(jTable1.getSelectedRow(),1);

			refresh = true;

			Pair<WorkspaceDataTable[],Map<?,?>> reactionInfo;
			WorkspaceDataTable[] results;


			if (!this.last) {
				
				reactionInfo = this.entity.getReactionInfo(last,metabolite,reactionID, refresh);

				results = reactionInfo.getA();
				
				Map<?,?> next2 = reactionInfo.getB();
				
				new MetaboliteDetailWindow(this.entity,results, (String) jTable1.getValueAt(jTable1.getSelectedRow(),1), 
						"reaction: " + jTable1.getValueAt(jTable1.getSelectedRow(),1), next2);
				
			}

			else {
				results = this.entity.getLastReactionInfo(next,metabolite,reactionID, refresh);
				
				new GenericDetailWindow(results, (String) jTable1.getValueAt(jTable1.getSelectedRow(),1), 
						"reaction: " + jTable1.getValueAt(jTable1.getSelectedRow(),1));
			}
		} 
		catch (Exception e) {
			Workbench.getInstance().error(e);
			e.printStackTrace();
		}
	}
	/**
	 * 
	 */
	public void finish() {

		this.setVisible(false);
		this.dispose();
	}

	/**
	 * @param evt
	 */
	private void searchComboBoxActionPerformed(ActionEvent evt) {

		jTable1 = new MyJTable();
		jTable1.setShowGrid(false);
		jTable1.setModel(this.dataTable[this.searchComboBox.getSelectedIndex()]);
		//jTable1.setSortableFalse();
		jScrollPane1.setViewportView(jTable1);
		jTable1.setAutoCreateRowSorter(this.dataTable[this.searchComboBox.getSelectedIndex()].getRowCount()>0);
	}

}
