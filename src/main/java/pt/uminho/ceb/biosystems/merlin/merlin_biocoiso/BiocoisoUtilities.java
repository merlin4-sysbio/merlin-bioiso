package pt.uminho.ceb.biosystems.merlin.merlin_biocoiso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import pt.uminho.ceb.biosystems.merlin.core.datatypes.WorkspaceGenericDataTable;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.pair.Pair;

public class BiocoisoUtilities {

	public static String[] createLineFromMap(Map<?,?> keyMap, String key) {
		String[] res = new String[5];

		res[1]=key;

		res[2]=keyMap.get("flux").toString();

		@SuppressWarnings("unchecked")
		ArrayList<String> childrenList = (ArrayList<String>) keyMap.get("children");

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

		res[3]=children;

		if (Float.parseFloat(res[2]) >0) {
			res[4] = "OK, this metabolite is being produced";
		}
		else {
			res[4] = "To review. This metabolite is not being produced";
		}
		return res;
	}

	public static Pair<WorkspaceGenericDataTable, Map<?,?>> tableMaker(Map<?,?> level, String name, 
			String windowName) {

		String[] columnsName = new String[] {"info","metabolite","flux", "children", "description"};

		WorkspaceGenericDataTable newTable = new WorkspaceGenericDataTable(Arrays.asList(columnsName) , name , windowName) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int col){
				if (col==0)
				{
					return true;
				}
				else return false;
			}
		}; 


		@SuppressWarnings("unchecked")
		Set<String> next = (Set<String>) level.keySet();
		for (String key : next) {

			Map<?, ?> level2 = (Map<?, ?>) ((Map<?, ?>) level.get(key));

			String[] res = createLineFromMap(level2,key);

			newTable.addLine(res);

		}

		Pair<WorkspaceGenericDataTable, Map<?,?>> res = new Pair<WorkspaceGenericDataTable, Map<?,?>>(newTable,level);

		return res;

	}

	public static Pair<WorkspaceGenericDataTable, Map<?,?>> tableCreator(Map<?,?> level, String name, String windowName, String metabolite) {

		String[] columnsName = new String[] {"info","metabolite","flux", "children", "description"};

		WorkspaceGenericDataTable newTable = new WorkspaceGenericDataTable(Arrays.asList(columnsName) , name , windowName) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int col){
				if (col==0)
				{
					return true;
				}
				else return false;
			}
		}; 

		Map<?,?> nextLevel = (Map<?, ?>) ((Map<?, ?>) level.get(metabolite)).get("next");

		@SuppressWarnings("unchecked")
		Set<String> next = (Set<String>) nextLevel.keySet();
		for (String key : next) {

			Map<?, ?> level2 = (Map<?, ?>) ((Map<?, ?>) nextLevel.get(key));

			String[] res = createLineFromMap(level2,key);

			newTable.addLine(res);

		}

		Pair<WorkspaceGenericDataTable, Map<?,?>> res = new Pair<WorkspaceGenericDataTable, Map<?,?>>(newTable,nextLevel);

		return res;

	}

}
