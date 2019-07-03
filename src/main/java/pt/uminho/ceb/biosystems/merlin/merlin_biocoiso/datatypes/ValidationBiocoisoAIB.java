package pt.uminho.ceb.biosystems.merlin.merlin_biocoiso.datatypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import es.uvigo.ei.aibench.core.clipboard.ClipboardItem;
import es.uvigo.ei.aibench.core.datatypes.annotation.Datatype;
import es.uvigo.ei.aibench.core.datatypes.annotation.Structure;
import pt.uminho.ceb.biosystems.merlin.aibench.datatypes.WorkspaceEntityAIB;
import pt.uminho.ceb.biosystems.merlin.aibench.datatypes.WorkspaceTableAIB;
import pt.uminho.ceb.biosystems.merlin.aibench.datatypes.interfaces.IEntityAIB;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.WorkspaceDataTable;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.WorkspaceEntity;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.WorkspaceGenericDataTable;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Connection;
import pt.uminho.ceb.biosystems.merlin.merlin_biocoiso.ValidationBiocoiso;
import pt.uminho.ceb.biosystems.merlin.processes.model.ModelPathwaysProcesses;
import pt.uminho.ceb.biosystems.merlin.services.model.ModelPathwaysServices;

@Datatype(structure= Structure.SIMPLE, namingMethod="getName",removable=true,removeMethod ="remove")
public class ValidationBiocoisoAIB extends ValidationBiocoiso implements IEntityAIB{

	private String workspaceName;
	private Connection connection;


	/**
	 * @param dbt
	 * @param name
	 */
	public ValidationBiocoisoAIB(WorkspaceTableAIB dbt, String name) {

		super(dbt, name);
		workspaceName = dbt.getWorkspaceName();
		this.connection = dbt.getConnection();
	}

	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#getStats()
	 */
	public String[][] getStats() {

		if(super.getStats()==null) {
			
//			Map<Integer, List<Object>> res = ModelPathwaysServices.getMainTableData(workspaceName, this.namesIndex, this.identifiers, connection);
//			super.setMainTableData(ModelPathwaysProcesses.getMainTableData(res));

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
	 *
	 */
	public WorkspaceDataTable[] getRowInfo(int identifier, boolean refresh) {

		refresh = true;

		if(super.getRowInfo()==null || refresh) {
//			Map<String, List<List<String>>> dataList = ModelPathwaysServices.getRowInfo(workspaceName, identifier, connection);
//			super.setRowInfo(ModelPathwaysProcesses.getRowInfo(dataList));
		}

		return super.getRowInfo();
	}

	@Override
	public Connection getConnection() {
		// TODO Auto-generated method stub
		return this.connection;
	}

	@Override
	public void setConnection(Connection connection) {
		this.connection = connection;
		// TODO Auto-generated method stub

	}
}