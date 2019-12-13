package pt.uminho.ceb.biosystems.merlin.merlin_biocoiso;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.json.simple.parser.ParseException;

import es.uvigo.ei.aibench.core.Core;
import es.uvigo.ei.aibench.core.clipboard.ClipboardItem;
import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import pt.uminho.ceb.biosystems.merlin.aibench.datatypes.WorkspaceAIB;
import pt.uminho.ceb.biosystems.merlin.aibench.datatypes.WorkspaceTableAIB;
import pt.uminho.ceb.biosystems.merlin.aibench.utilities.AIBenchUtils;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.WorkspaceGenericDataTable;
import pt.uminho.ceb.biosystems.merlin.merlin_biocoiso.datatypes.ValidationBiocoisoAIB;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.pair.Pair;

@Operation(name="BioISO",description="Import results")
public class BiocoisoResultsImporter {
	
	private WorkspaceAIB workspace;
	Icon notProduced = new ImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/notProducing.png")).getImage().getScaledInstance(20, 20, Image.SCALE_DEFAULT));
	Icon produced = new ImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/producing.png")).getImage().getScaledInstance(20, 20, Image.SCALE_DEFAULT));
	

	@Port(direction=Direction.INPUT, name="Workspace",description="", order = 1)
	public void setWorkspace(WorkspaceAIB workspace) {
		
		this.workspace=workspace;
		
	}
	
	
	@Port(direction=Direction.INPUT, name="File",description="", order = 2)
	public void setFile(File file) throws IOException, ParseException {
		
		int table_number = this.workspace.getDatabase().getValidation().getEntities().size() + 1;

		String name = Integer.toString(table_number) + " - " + file.getName();

		String[] columnsName = new String[] {"info","metabolite", "reactions", "role" , "analysis"};

		WorkspaceTableAIB table = new WorkspaceTableAIB(name, columnsName , this.workspace.getName());

		File results_file = file;

		Pair<WorkspaceGenericDataTable, Map<?,?>> filledTableAndNextLevel = 
				BiocoisoUtils.createDataTable(results_file.getAbsolutePath(), 
						Arrays.asList(columnsName), this.workspace.getName(), name,produced,notProduced);

		//		Pair<WorkspaceGenericDataTable, Map<?,?>> filledTableAndNextLevel = 
		//				this.createDataTable("C:/Users/merlin Developer/Desktop/results_biocoiso_2.json", 
		//						Arrays.asList(columnsName), this.project.getName(), name);

		Map<?, ?> entireMap = BiocoisoUtils.readJSON(results_file.getAbsolutePath());

		ValidationBiocoisoAIB biocoiso = new ValidationBiocoisoAIB(table, name, filledTableAndNextLevel.getB(), entireMap);

		biocoiso.setWorkspace(this.workspace);

		biocoiso.setMainTableData(filledTableAndNextLevel.getA());

		biocoiso.setData(filledTableAndNextLevel.getA());


		try {

			this.workspace.getValidation().addEntity(biocoiso);

		}

		catch (Exception e) {

			List<WorkspaceAIB> wspList = AIBenchUtils.getAllProjects();

			for (WorkspaceAIB wsp : wspList) {

				ClipboardItem item = Core.getInstance().getClipboard().getClipboardItem(wsp);

				Core.getInstance().getClipboard().removeClipboardItem(item);

				Core.getInstance().getClipboard().putItem(wsp, item.getName());

			}
		}
		
	}

}
