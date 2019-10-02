package pt.uminho.ceb.biosystems.merlin.merlin_biocoiso.datatypes;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

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
			
			Map<?,?> nextTable = (Map<?,?>) ((Map<?,?>) nextLevel.get(metabolite)).get("next");

			@SuppressWarnings("unchecked")
			Set<String> keys = (Set<String>) nextTable.keySet();

			WorkspaceDataTable[] results = new WorkspaceDataTable[1];

			String[] columnsNames = new String[] {"metabolite","flux", "children", "description"};

			results[0] = new WorkspaceDataTable(columnsNames, "reactions");

			for (String key : keys) {
				String[] line = this.createLineFromMap(nextTable, key);

				results[0].addLine(line);

			}
			super.setRowInfo(results);
		}

		return super.getRowInfo();
	} 



	private String[] createLineFromMap(Map<?,?> keyMap, String key) {
		String[] res = new String[4];
		
		Map<?, ?> level2 =  (Map<?, ?>) ((Map<?,?>) keyMap.get(key));

		res[0]=key;

		res[1]= level2.get("flux").toString();


		@SuppressWarnings("unchecked")
		ArrayList<String> childrenList = (ArrayList<String>) level2.get("children");

		String children = "";

		int i = 0;

		while (i<childrenList.size()) {
			if (i==childrenList.size()-1) {
				children = children + childrenList.get(i);
			}
			else {
				children = children + childrenList.get(i) + ", ";
			}
			i++;
		}

		res[2]=children;

		if (Float.parseFloat(res[1]) >0) {
			res[3] = "OK, this metabolite is being produced";
		}
		else {
			res[3] = "To review. This metabolite is not being produced";
		}
		return res;
	}

}