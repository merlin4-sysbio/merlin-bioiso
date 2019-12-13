package pt.uminho.ceb.biosystems.merlin.merlin_biocoiso;

import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import pt.uminho.ceb.biosystems.merlin.aibench.datatypes.WorkspaceAIB;

@Operation(name="BioISO Simulation History",description="BioISO Simulation History")
public class BiocoisoHistory {
	
	@Port(direction=Direction.INPUT, name="Workspace",description="", order = 1)
	public void setWorkspace(WorkspaceAIB workspace) {
		
		new BiocoisoHistoryGUI(workspace);
		
	}

}
