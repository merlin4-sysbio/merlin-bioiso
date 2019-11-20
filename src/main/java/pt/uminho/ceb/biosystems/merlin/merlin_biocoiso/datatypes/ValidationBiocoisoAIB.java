package pt.uminho.ceb.biosystems.merlin.merlin_biocoiso.datatypes;

import java.awt.Image;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.pair.Pair;

@Datatype(structure= Structure.SIMPLE, namingMethod="getName",removable=true,removeMethod ="remove")
public class ValidationBiocoisoAIB extends ValidationBiocoiso implements IEntityAIB{

	private String workspaceName;
	private Icon notProduced; 
	private Icon produced;
	private Icon dontknow;
	private Map<?, ?> nextLevel;
	private Map<?, ?> entireMap;


	/**
	 * @param dbt
	 * @param name
	 */
	

	public ValidationBiocoisoAIB(WorkspaceTableAIB dbt, String name, Map<?, ?> nextLevel, Map<?, ?> entireMap) {

		super(dbt, name);
		this.nextLevel = nextLevel;
		this.entireMap = entireMap;
		workspaceName = dbt.getWorkspaceName();
		notProduced = new ImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/notProducing.png")).getImage().getScaledInstance(20, 20, Image.SCALE_DEFAULT));
		produced = new ImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/producing.png")).getImage().getScaledInstance(20, 20, Image.SCALE_DEFAULT));
		dontknow = new ImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/question.png")).getImage().getScaledInstance(20, 20, Image.SCALE_DEFAULT));
	}


	public Map<?, ?> getEntireMap() {
		return this.entireMap;
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

	
	public WorkspaceDataTable[] getLastReactionInfo(Map<?,?> next2,String metabolite, 
			String choosenReaction, boolean refresh) {
		
		String[] columnsNames = new String[] {"metabolite", "role"};
		
		
		ArrayList<ArrayList<Object>> reactions = (ArrayList<ArrayList<Object>>) next2.get("children"); //list with reactions

		WorkspaceDataTable[] results = new WorkspaceDataTable[1];
		
		results[0] = new WorkspaceDataTable(columnsNames, "reactions");
		
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
					Object[] line = createLineFromProductOrReactant(true,next2,metabolite, reactionID, reactant,"Reactant");
					results[0].addLine(line);
				}

				for ( String product : productsList) {
					Object[] line = createLineFromProductOrReactant(true, next2,metabolite, reactionID, product,"Product");
					results[0].addLine(line);
				}
			}

			i++;

		}
		return results;
		
		
	}
	public Pair<WorkspaceDataTable[],Map<?,?>> getReactionInfo(boolean last, String metabolite, 
			String choosenReaction, boolean refresh) throws FileNotFoundException, IOException, ParseException{

		String[] columnsNames;
		refresh = true;

		Map<?,?> next2 = (Map<?, ?>) ((Map<?,?>) nextLevel.get(metabolite)).get("next");
		

		ArrayList<ArrayList<Object>> reactions = (ArrayList<ArrayList<Object>>) ((Map<?,?>) nextLevel.get(metabolite)).get("children"); //list with reactions

		WorkspaceDataTable[] results = new WorkspaceDataTable[1];

			
		columnsNames = new String[] {"info","metabolite","reactions", "role", "analysis"};


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
						Object[] line = createLineFromProductOrReactant(last, next2,metabolite, reactionID, reactant,"Reactant");
						results[0].addLine(line);
					}

					for ( String product : productsList) {
						Object[] line = createLineFromProductOrReactant(last, next2,metabolite, reactionID, product,"Product");
						results[0].addLine(line);
					}
				}

				i++;

			}

			Pair<WorkspaceDataTable[],Map<?,?>> res = new Pair<WorkspaceDataTable[],Map<?,?>>(results, next2);

			return res;
	}

	private Object[] createLineFromProductOrReactant(boolean last, Map<?, ?> next, String metabolite, String reaction, String reactantOrProduct, String role) {

		if (!last) {
			if (next.get(reactantOrProduct)!=null) {

				ArrayList<ArrayList<String>> reactions = (ArrayList<ArrayList<String>>) ((Map<?, ?>) next.get(reactantOrProduct)).get("children");
				boolean analysis =  (boolean) ((Map<?, ?>) next.get(reactantOrProduct)).get("flux");

				Object[] res = new Object[5];

				res[1]=reactantOrProduct;

				res[2]= Integer.toString(reactions.size());

				res[3]= role;

				if (analysis) {
					res[4] = produced;
				}
				else {
					res[4] = notProduced;
				}
				return res;

			}

			else {
				Object[] res = new Object[5];

				res[1]=reactantOrProduct;

				res[2]=Integer.toString(0);

				res[3]= role;

				res[4] = dontknow;

				return res;
			}}

		else {
			Object[] res = new Object[2];

			res[0]=reactantOrProduct;

			res[1]=role;

			return res;
		}
	}


	public Pair<WorkspaceDataTable[],Map<?,?>> getMetaboliteInfo(String metabolite, boolean refresh, Map<?,?> next) throws FileNotFoundException, IOException, ParseException {

		refresh = true;

		//		int row = identifier;

		Map<?,?> nextToReturn = (Map<?, ?>) ((Map<?,?>) next.get(metabolite));
		
		ArrayList<ArrayList<Object>> reactions = (ArrayList<ArrayList<Object>>) ((Map<?,?>) next.get(metabolite)).get("children"); //list with reactions

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



			}

			Pair<WorkspaceDataTable[],Map<?,?>> res = new Pair<WorkspaceDataTable[],Map<?,?>>(results,nextToReturn);
			return res;
	}


	/**
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * @throws ParseException 
	 *
	 */
	public WorkspaceDataTable[] getRowInfo(int identifier, boolean refresh) throws FileNotFoundException, IOException, ParseException {

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