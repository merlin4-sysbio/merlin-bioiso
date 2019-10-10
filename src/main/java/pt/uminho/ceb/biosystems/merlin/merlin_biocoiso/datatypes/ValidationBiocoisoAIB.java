package pt.uminho.ceb.biosystems.merlin.merlin_biocoiso.datatypes;

import java.awt.Image;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.json.simple.parser.ParseException;


import es.uvigo.ei.aibench.core.datatypes.annotation.Datatype;
import es.uvigo.ei.aibench.core.datatypes.annotation.Structure;
import pt.uminho.ceb.biosystems.merlin.aibench.datatypes.WorkspaceTableAIB;
import pt.uminho.ceb.biosystems.merlin.aibench.datatypes.interfaces.IEntityAIB;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.WorkspaceDataTable;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.WorkspaceGenericDataTable;
import pt.uminho.ceb.biosystems.merlin.merlin_biocoiso.ValidationBiocoiso;

@Datatype(structure= Structure.SIMPLE, namingMethod="getName",removable=true,removeMethod ="remove")
public class ValidationBiocoisoAIB extends ValidationBiocoiso implements IEntityAIB{

	private String workspaceName;
	private Icon notProduced; 
	private Icon produced;
	private Icon dontknow;
	private Map<?, ?> nextLevel;
	private Map<String, ArrayList<ArrayList<String>>> reactionsAndCompounds;
	private String metabolite;


	/**
	 * @param dbt
	 * @param name
	 */
	//	public ValidationBiocoisoAIB(WorkspaceTableAIB dbt, String name) {
	//
	//		super(dbt, name);
	//		workspaceName = dbt.getWorkspaceName();
	//		notProduced = new ImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Cancel.png")).getImage().getScaledInstance(20, 20, Image.SCALE_DEFAULT));
	//		produced = new ImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Ok.png")).getImage().getScaledInstance(20, 20, Image.SCALE_DEFAULT));
	//	}


	public ValidationBiocoisoAIB(WorkspaceTableAIB dbt, String name, Map<?, ?> nextLevel) {

		super(dbt, name);
		this.nextLevel = nextLevel;
		workspaceName = dbt.getWorkspaceName();
		notProduced = new ImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Cancel.png")).getImage().getScaledInstance(20, 20, Image.SCALE_DEFAULT));
		produced = new ImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Ok.png")).getImage().getScaledInstance(20, 20, Image.SCALE_DEFAULT));
		dontknow = new ImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/question.png")).getImage().getScaledInstance(20, 20, Image.SCALE_DEFAULT));
		this.reactionsAndCompounds= new HashMap<String, ArrayList<ArrayList<String>>>();
	}




	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#getStats()
	 */
	public String[][] getStats() {


		return super.getStats();
	}

	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#getData()
	 */
	public WorkspaceGenericDataTable getMainTableData() {

		if(super.getMainTableData()==null) {

			this.mainTableData = new WorkspaceGenericDataTable(Arrays.asList(this.getDbt().getColumms()), workspaceName, this.name);
			//this.mainTableData.addLine(line);
			super.setMainTableData(mainTableData);
		}

		return super.getMainTableData();
	}


	public WorkspaceDataTable[] getReactionInfo(String choosenReaction, boolean refresh) throws FileNotFoundException, IOException, ParseException{

		refresh = true;

		ArrayList<ArrayList<Object>> reactions = (ArrayList<ArrayList<Object>>) ((Map<?,?>) nextLevel.get(metabolite)).get("children"); //list with reactions

		WorkspaceDataTable[] results = new WorkspaceDataTable[1];

		String[] columnsNames = new String[] {"metabolite", "role", "analysis"};

		results[0] = new WorkspaceDataTable(columnsNames, "reactions") {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int col){
				if (col==0)
				{
					return true;
				}
				else return false;
			}};

			boolean found =false;
			int i = 0;

			while (!found &&  i< reactions.size()) {

				ArrayList<Object> reaction = reactions.get(i);

				String reactionID =  (String) reaction.get(0); //r10856

				if (choosenReaction.equals(reactionID)) {
					found = true;
					ArrayList<String> reactantsList = (ArrayList<String>) reaction.get(2);

					ArrayList<String> productsList = (ArrayList<String>) reaction.get(3);

					for ( String reactant : reactantsList) {
						Object[] line = createLineFromProductOrReactant(this.metabolite, reactionID, reactant,"Reactant");
						results[0].addLine(line);
					}

					for ( String product : productsList) {
						Object[] line = createLineFromProductOrReactant(this.metabolite, reactionID, product,"Product");
						results[0].addLine(line);
					}
				}

				i++;

			}

			return results;
	}

	private Object[] createLineFromProductOrReactant(String metabolite, String reaction, String reactantOrProduct, String role) {

		Map<?,?> precursors = (Map<?,?>) ((Map<?, ?>) nextLevel.get(metabolite)).get("next");

		if (precursors.get(reactantOrProduct)!=null) {
			boolean analysis =  (boolean) ((Map<?, ?>) precursors.get(reactantOrProduct)).get("flux");

			Object[] res = new Object[4];

			res[0]=reactantOrProduct;

			res[1]= role;

			if (analysis) {
				res[2] = produced;
			}
			else {
				res[2] = notProduced;
			}
			return res;

		}

		else {
			Object[] res = new Object[3];

			res[0]=reactantOrProduct;

			res[1]= role;
			
			res[2] = dontknow;
			
			return res;
		}
	}

	public Map<String, ArrayList<ArrayList<String>>> getReactionsAndCompounds(){
		return (Map<String, ArrayList<ArrayList<String>>>) this.reactionsAndCompounds;
	}

	public void setReactionsAndCompounds(Map<String, ArrayList<ArrayList<String>>> newMap) {
		this.reactionsAndCompounds = newMap;
	}


	/**
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * @throws ParseException 
	 *
	 */
	public WorkspaceDataTable[] getRowInfo(int identifier, boolean refresh) throws FileNotFoundException, IOException, ParseException {

		refresh = true;

		WorkspaceGenericDataTable myTable = this.getMainTableData();

		int row = identifier;

		this.metabolite = (String) myTable.getValueAt(row, 1);

		if(super.getRowInfo()==null || refresh) {

			ArrayList<ArrayList<Object>> reactions = (ArrayList<ArrayList<Object>>) ((Map<?,?>) nextLevel.get(metabolite)).get("children"); //list with reactions

			WorkspaceDataTable[] results = new WorkspaceDataTable[1];

			String[] columnsNames = new String[] {"info","reaction", "reactants", "products", "analysis"};

			results[0] = new WorkspaceDataTable(columnsNames, "reactions") {
				private static final long serialVersionUID = 1L;

				@Override
				public boolean isCellEditable(int row, int col){
					if (col==0)
					{
						return true;
					}
					else return false;
				}};

				for (ArrayList<Object> reaction : reactions) {

					String reactionID =  (String) reaction.get(0); //r10856

					boolean analysis = (boolean) reaction.get(1);

					ArrayList<String> reactantsList = (ArrayList<String>) reaction.get(2);

					ArrayList<String> productsList = (ArrayList<String>) reaction.get(3);

					Object[] line = createLineFromMap(analysis, reactionID, reactantsList, productsList);

					results[0].addLine(line);

					ArrayList<ArrayList<String>> res = new ArrayList<ArrayList<String>>();

					res.add(reactantsList);

					res.add(productsList);

					this.reactionsAndCompounds.put(reactionID, res);



				}

				super.setRowInfo(results);
		}
		return super.getRowInfo();
	}





	private Object[] createLineFromMap(boolean analysis, String reactionID, ArrayList<String> reactantsList, ArrayList<String> productsList) {
		Object[] res = new Object[5];

		res[1]=reactionID;

		res[2]= Integer.toString(reactantsList.size());

		res[3] = Integer.toString(productsList.size());

		if (analysis) {
			res[4] = produced;
		}
		else {
			res[4] = notProduced;
		}
		return res;
	}


	public Map<?, ?> getNextLevel() {
		return nextLevel;
	}


	public void setNextLevel(Map<?, ?> nextLevel) {
		this.nextLevel = nextLevel;
	}



}