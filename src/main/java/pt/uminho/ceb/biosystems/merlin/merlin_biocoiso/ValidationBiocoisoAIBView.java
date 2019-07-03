package pt.uminho.ceb.biosystems.merlin.merlin_biocoiso;

import pt.uminho.ceb.biosystems.merlin.aibench.datatypes.interfaces.IEntityAIB;
import pt.uminho.ceb.biosystems.merlin.aibench.views.WorkspaceGenericEntityDataView;


public class ValidationBiocoisoAIBView extends WorkspaceGenericEntityDataView{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ValidationBiocoisoAIBView(IEntityAIB entity) {
		super(entity);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void fillList() {

		dataTable = this.entity.getMainTableData();

		jTable.setModel(dataTable);
		jTable.setSortableFalse();
		jTable.setAutoCreateRowSorter(true);
		

		this.searchInEntity.setMyJTable(jTable);
		this.searchInEntity.setMainTableData(this.entity.getMainTableData());
		this.searchInEntity.setSearchTextField("");
	}

}
