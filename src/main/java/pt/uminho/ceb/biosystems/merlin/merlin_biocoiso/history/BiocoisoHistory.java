package pt.uminho.ceb.biosystems.merlin.merlin_biocoiso.history;

import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import pt.uminho.ceb.biosystems.merlin.gui.datatypes.WorkspaceAIB;
import pt.uminho.ceb.biosystems.merlin.merlin_biocoiso.BiocoisoUtils;
import pt.uminho.ceb.biosystems.merlin.utilities.io.FileUtils;

@Operation(name="BioISO Simulation History",description="BioISO Simulation History")
public class BiocoisoHistory {
	
	@Port(direction=Direction.INPUT, name="Workspace",description="", order = 1)
	public void setWorkspace(WorkspaceAIB workspace) {
		
		new BiocoisoHistoryGUI(workspace);
		
		
		
	}
	
}
