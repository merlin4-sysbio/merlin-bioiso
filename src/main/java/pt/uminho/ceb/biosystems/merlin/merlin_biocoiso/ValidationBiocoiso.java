package pt.uminho.ceb.biosystems.merlin.merlin_biocoiso;

import java.util.HashMap;

import pt.uminho.ceb.biosystems.merlin.core.datatypes.WorkspaceEntity;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.WorkspaceGenericDataTable;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.WorkspaceTable;
import pt.uminho.ceb.biosystems.merlin.core.interfaces.IEntity;


/**
 * @author João Capela
 *
 */
public class ValidationBiocoiso extends WorkspaceEntity implements IEntity {

	public ValidationBiocoiso(WorkspaceTable dbt, String name) {
		super(dbt, name);
		// TODO Auto-generated constructor stub
	}

	


	private HashMap<String, String> names;
	protected WorkspaceGenericDataTable data;
	private HashMap<Integer, Integer[]> searchData;
	
	/**
	 * @param dbt
	 * @param name
	 */
	
	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#getData()
	 */
	public WorkspaceGenericDataTable getData() {
		
		return this.data;
	}

	public HashMap<Integer,Integer[]> getSearchData() {
		
		return this.searchData;
	}

	
	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#hasWindow()
	 */
	public boolean hasWindow() {
		return true;
	}

	public String getName(String id) {
		
		return this.names.get(id);
	}

	public String getSingular() {
		
		return "pathway: ";
	}

	/**
	 * @param data the data to set
	 */
	public void setData(WorkspaceGenericDataTable data) {
		this.data = data;
	}

	/**
	 * @param searchData the searchData to set
	 */
	public void setSearchData(HashMap<Integer, Integer[]> searchData) {
		this.searchData = searchData;
	}

}