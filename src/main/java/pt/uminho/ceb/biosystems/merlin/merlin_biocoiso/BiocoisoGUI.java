package pt.uminho.ceb.biosystems.merlin.merlin_biocoiso;


import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import org.apache.jute.compiler.JBoolean;
import org.sing_group.gc4s.dialog.AbstractInputJDialog;
import org.sing_group.gc4s.input.InputParameter;
import org.sing_group.gc4s.input.InputParametersPanel;
import org.sing_group.gc4s.input.combobox.ExtendedJComboBox;

import es.uvigo.ei.aibench.core.Core;
import es.uvigo.ei.aibench.core.ParamSpec;
import es.uvigo.ei.aibench.core.clipboard.ClipboardItem;
import es.uvigo.ei.aibench.core.operation.OperationDefinition;
import es.uvigo.ei.aibench.workbench.InputGUI;
import es.uvigo.ei.aibench.workbench.ParamsReceiver;
import es.uvigo.ei.aibench.workbench.Workbench;
import pt.uminho.ceb.biosystems.merlin.aibench.datatypes.WorkspaceAIB;
import pt.uminho.ceb.biosystems.merlin.aibench.gui.CustomGUI;
import pt.uminho.ceb.biosystems.merlin.aibench.utilities.AIBenchUtils;
import pt.uminho.ceb.biosystems.merlin.aibench.utilities.CreateImageIcon;
import pt.uminho.ceb.biosystems.merlin.biocomponents.io.Enumerators.SBMLLevelVersion;
import pt.uminho.ceb.biosystems.merlin.biocomponents.io.writers.SBMLWriter;
import pt.uminho.ceb.biosystems.merlin.services.ProjectServices;
import pt.uminho.ceb.biosystems.merlin.utilities.io.FileUtils;

/**
 * @author João Capela
 *
 */


public class BiocoisoGUI extends AbstractInputJDialog implements InputGUI{
	private static final long serialVersionUID = 1L;

	private ExtendedJComboBox<String> objective ;
	private ExtendedJComboBox<String> models ;
	private ExtendedJComboBox<String> biomass;
	private static final String[] biomass_values = {
			"true",
			"false",
		};
	private ParamsReceiver rec;

	protected Object project;
	private String[] workspaces;




	public BiocoisoGUI() {

		super(new JFrame());

		//fill();
	}

	public String getDialogTitle() {
		return "BioCoISO";
	}

	public String getDescription() {
		return "Get results from BioCoISO";
	}

	public JPanel getInputComponentsPane() {

		List<ClipboardItem> cl = Core.getInstance().getClipboard().getItemsByClass(WorkspaceAIB.class);                    
				
		this.biomass = new ExtendedJComboBox<String>(biomass_values);
		
		workspaces = new String[cl.size()];
		for (int i = 0; i < cl.size(); i++) {

			workspaces[i] = (cl.get(i).getName());
		}
		this.models = new ExtendedJComboBox<String>(workspaces);
		this.objective = new ExtendedJComboBox<String>(new String[0]);
		if(this.models.getModel().getSize()>0)
			this.setReactions();

		this.models.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				
				setReactions();
			}
		});

		InputParameter[] inPar = getInputParameters();
		return new InputParametersPanel(inPar);
	}

	@Override
	protected Component getButtonsPane() {
		final JPanel buttonsPanel = new JPanel(new FlowLayout());

		okButton = new JButton("proceed");
		okButton.setEnabled(true);
		okButton.setToolTipText("proceed");
		okButton.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Ok.png")),0.1).resizeImageIcon());
		ActionListener listener= new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispose();
				rec.paramsIntroduced(
						new ParamSpec[]{
								new ParamSpec("new model", String.class, models.getSelectedItem().toString(), null),
								new ParamSpec("e_Biomass", String.class,biomass.getSelectedItem().toString(),null),
								new ParamSpec("Objective", String.class,objective.getSelectedItem().toString(),null)
								
						}
						);
				
			}
		};
		okButton.addActionListener(listener);

		cancelButton = new JButton("cancel");
		cancelButton.setToolTipText("cancel");
		cancelButton.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Cancel.png")),0.1).resizeImageIcon());
		cancelButton.addActionListener(event -> {

			String[] options = new String[2];
			options[0] = "yes";
			options[1] = "no";

			int result = CustomGUI.stopQuestion("cancel confirmation", "are you sure you want to cancel the operation?", options);

			if(result == 0) {
				canceled = true;
				dispose();
			}

		});


		buttonsPanel.add(okButton);
		buttonsPanel.add(cancelButton);

		getRootPane().setDefaultButton(okButton);
		InputMap im = okButton.getInputMap();
		im.put(KeyStroke.getKeyStroke("ENTER"), "pressed");
		im.put(KeyStroke.getKeyStroke("released ENTER"), "released");

		return buttonsPanel;


	}
	
	private InputParameter[] getInputParameters() {
		InputParameter[] parameters = new InputParameter[3];
		parameters[2] = 
				new InputParameter(
						"objective", 
						objective, 
						""
						);
		parameters[1] = 
				new InputParameter(
						"e_Biomass",
						biomass,
						""
						);
		parameters[0] = 
				new InputParameter(
						"new model", 
						models, 
						""
						);

		return parameters;
	}

	public void setReactions() {

		WorkspaceAIB workspace = AIBenchUtils.getProject(models.getSelectedItem().toString());

		String path = FileUtils.getWorkspaceTaxonomyFolderPath(workspace.getName(), workspace.getTaxonomyID());
		File biocoisoFile = new File(path.concat("biocoiso"));

		SBMLWriter sBMLWriter = null;
		try {
			biocoisoFile.mkdir();
			sBMLWriter = new SBMLWriter(workspace.getDatabase().getDatabaseAccess(), 
					biocoisoFile.toString().concat("/model.xml"),
					workspace.getName(),
					ProjectServices.isCompartmentalisedModel(workspace.getDatabase().getDatabaseName()), 
					false,
					null, 
					SBMLLevelVersion.L2V1);
			
			
			sBMLWriter.getDataFromDatabase();
			
			sBMLWriter.toSBML(true);
			
			Map<String, String> dictionary = sBMLWriter.getReactionLabels();
			
			String[] reactions = dictionary.keySet().toArray(new String[dictionary.size()]);
			
			objective.setModel(new DefaultComboBoxModel<>(reactions));
		}
		catch (Exception e1) {

			Workbench.getInstance().warn("A problem was found when trying to export the model");;
			e1.printStackTrace();
		}
		
	
	}
//
//	public void fill () {
//		for (String obj : reactions) {
//			objective.setSelectedItem(obj);
//		}
//	}
	@Override
	public void setVisible(boolean b) {
		this.pack();
		super.setVisible(b);
	}

	@Override
	public void init(ParamsReceiver arg0, OperationDefinition<?> arg1) {
		this.rec = arg0;
		this.setTitle(arg1.getName());
		this.setVisible(true);		
	}

	/* (non-Javadoc)
	 * @see es.uvigo.ei.aibench.workbench.InputGUI#onValidationError(java.lang.Throwable)
	 */
	public void onValidationError(Throwable arg0) {

		Workbench.getInstance().error(arg0);
	}

	@Override
	public void finish() {

	}
}
