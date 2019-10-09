package pt.uminho.ceb.biosystems.merlin.merlin_biocoiso.datatypes;

import java.awt.Image;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

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
	private Map<?, ?> nextLevel;
	Icon notProduced = new ImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Cancel.png")).getImage().getScaledInstance(20, 20, Image.SCALE_DEFAULT));
	Icon produced = new ImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Ok.png")).getImage().getScaledInstance(20, 20, Image.SCALE_DEFAULT));


	/**
	 * @param dbt
	 * @param name
	 */
	public ValidationBiocoisoAIB(WorkspaceTableAIB dbt, String name) {

		super(dbt, name);
		workspaceName = dbt.getWorkspaceName();
	}

	
	public ValidationBiocoisoAIB(WorkspaceTableAIB dbt, String name, Map<?,?> nextLevel) {

		super(dbt, name);
		this.nextLevel = nextLevel;
		workspaceName = dbt.getWorkspaceName();
	}
	
	
	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#getStats()
	 */
	public String[][] getStats() {

		if(super.getStats()==null) {

		}

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

		String metabolite = (String) myTable.getValueAt(row, 1);

		if(super.getRowInfo()==null || refresh) {
			
			Map<?,?> nextTable = (Map<?,?>) ((Map<?,?>) nextLevel.get(metabolite)).get("next"); //level 2 udp-alpha...

			@SuppressWarnings("unchecked")
			Set<String> keys = (Set<String>) nextTable.keySet();

			WorkspaceDataTable[] results = new WorkspaceDataTable[1];

			String[] columnsNames = new String[] {"metabolite","role", "reaction", "reactants", "products","analysis"};

			results[0] = new WorkspaceDataTable(columnsNames, "reactions");

			for (String key : keys) {
				
				Map<?, ?> level2 =  (Map<?, ?>) ((Map<?,?>) nextTable.get(key)); //udp-alpha

				@SuppressWarnings("unchecked")
				ArrayList<ArrayList<String>> reactions = (ArrayList<ArrayList<String>>) level2.get("children");
				
				for (int i=0; i<reactions.size();i++) {
					
					ArrayList<String> reaction = reactions.get(i);
					
					String reactionID = reaction.get(0);
					
					ArrayList<String> reactantsList = ((ArrayList<ArrayList<ArrayList<String>>>) level2.get("children")).get(i).get(2);
					
					ArrayList<String> productsList = ((ArrayList<ArrayList<ArrayList<String>>>) level2.get("children")).get(i).get(3);
					
					Object[] line = this.createLineFromMap(level2, key, reactionID,reactantsList, productsList); //key = udp-alpha 

					results[0].addLine(line);
					
				}
				
				

			}
			super.setRowInfo(results);
		}

		return super.getRowInfo();
	} 



	private Object[] createLineFromMap(Map<?,?> keyMap, String key, String reactionID, ArrayList<String> reactantsList, ArrayList<String> productsList) {
		Object[] res = new Object[6];
		
		res[0]=key;

		boolean production = (boolean) keyMap.get("flux");
		
		String side = (String) keyMap.get("side");
		
		res[1] = side;
		
		res[2]=reactionID;
		
		@SuppressWarnings("unchecked")
		
		
		String reactants = "";

		int i = 0;

		while (i<reactantsList.size()) {
			if (i==reactantsList.size()-1) {
				reactants = reactants + reactantsList.get(i);
			}
			else {
				reactants = reactants + reactantsList.get(i) + ", ";
			}
			i++;
		}
		
		res[3] = reactants;
		
		@SuppressWarnings("unchecked")
		
		
		String products = "";

		i = 0;

		while (i<productsList.size()) {
			if (i==productsList.size()-1) {
				products = products + productsList.get(i);
			}
			else {
				products = products + productsList.get(i) + ", ";
			}
			i++;
		}
		
		res[4] = products;

		if (production) {
			res[5] = produced;
		}
		else {
			res[5] = notProduced;
		}
		return res;
	}



}