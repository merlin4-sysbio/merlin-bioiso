package pt.uminho.ceb.biosystems.merlin.merlin_biocoiso;


import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
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
import pt.uminho.ceb.biosystems.merlin.biocomponents.io.readers.ContainerBuilder;
import pt.uminho.ceb.biosystems.merlin.biocomponents.io.writers.SBMLLevel3Writer;
import pt.uminho.ceb.biosystems.merlin.biocomponents.io.writers.SBMLWriter;
import pt.uminho.ceb.biosystems.merlin.core.containers.model.ReactionContainer;
import pt.uminho.ceb.biosystems.merlin.services.ProjectServices;
import pt.uminho.ceb.biosystems.merlin.services.model.ModelReactionsServices;
import pt.uminho.ceb.biosystems.merlin.utilities.io.FileUtils;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionCI;

/**
 * @author João Capela
 *
 */


public class BiocoisoGUI extends AbstractInputJDialog implements InputGUI{
	private static final long serialVersionUID = 1L;

	private ExtendedJComboBox<String> reaction ;
	private ExtendedJComboBox<String> models ;
	private ExtendedJComboBox<String> objective;
	private JTextField url;
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
				
		workspaces = new String[cl.size()];
		for (int i = 0; i < cl.size(); i++) {

			workspaces[i] = (cl.get(i).getName());
		}
		this.models = new ExtendedJComboBox<String>(workspaces);
		this.objective = new ExtendedJComboBox<String>(new String[0]);
		this.url = new JTextField("https://bioiso.bio.di.uminho.pt");
		
		this.reaction = new ExtendedJComboBox<String>(new String[0]);
		this.reaction.setEditable(true);
		AutoCompleteDecorator.decorate(this.reaction);
		
		
		if(this.models.getModel().getSize()>0)
			this.setReactions();
		
		this.reaction.setEditable(true);
        AutoCompleteDecorator.decorate(this.reaction);
		
		String[] items = {
				"maximize",
				"minimize", 
		};
		this.objective.setModel(new DefaultComboBoxModel<>(items));

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
		okButton.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Ok.png")),0.7).resizeImageIcon());
		ActionListener listener= new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispose();
				
				rec.paramsIntroduced(
						new ParamSpec[]{
								new ParamSpec("Workspace", String.class,models.getSelectedItem().toString(),null),
								new ParamSpec("Reaction", String.class,reaction.getSelectedItem().toString(),null),
								new ParamSpec("Objective", String.class,objective.getSelectedItem().toString(),null),
								new ParamSpec("url", String.class,url.getText(),null)

						}
						);
				
			}
		};
		okButton.addActionListener(listener);

		cancelButton = new JButton("cancel");
		cancelButton.setToolTipText("cancel");
		cancelButton.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Cancel.png")),0.7).resizeImageIcon());
		cancelButton.addActionListener(event -> {

			//String[] options = new String[2];
			//options[0] = "yes";
			//options[1] = "no";

			//int result = CustomGUI.stopQuestion("cancel confirmation", "are you sure you want to cancel the operation?", options);

			//if(result == 0) {
				canceled = true;
				dispose();
			//}

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
		InputParameter[] parameters = new InputParameter[4];
		parameters[0] = 

				new InputParameter(
						"Workspace", 
						models, 
						"Select the workspace"
						);

		parameters[1] = 
				new InputParameter(
						"Reaction", 
						reaction, 
						"Reaction to be studied"
						);
		parameters[2] = 
				new InputParameter(
						"Objective", 
						objective, 
						"Objective"
						);
		parameters[3] = 
				new InputParameter(
						"URL", 
						url, 
						"BioISO URL"
						);
		
		return parameters;
	}

	public void setReactions() {

		WorkspaceAIB workspace = AIBenchUtils.getProject(models.getSelectedItem().toString());

		String path = FileUtils.getWorkspaceTaxonomyFolderPath(workspace.getName(), workspace.getTaxonomyID());
		File biocoisoFile = new File(path.concat("biocoiso"));

		try {
			
			if(biocoisoFile.exists()) {

				try {
					FileUtils.deleteDirectory(biocoisoFile);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			biocoisoFile.mkdir();
			
			Container container = new Container(new ContainerBuilder(workspace.getName(), "model_".concat(workspace.getName()),
					ProjectServices.isCompartmentalisedModel(workspace.getName()), false, "", "e-biomass"));
			
			
//			merlinSBML3Writer.writeToFile();
			
			Map<String, ReactionCI> dictionary = container.getReactions();
			
			String[] reactions = dictionary.keySet().toArray(new String[dictionary.size()]);
			
//			System.out.println("tamanho:"+reactions.length);
			reaction.setModel(new DefaultComboBoxModel<>(reactions));
			
//			String databaseName = workspace.getDatabase().getWorkspaceName();
//			
//			List<ReactionContainer> reactionsNames = ModelReactionsServices.getReactions(databaseName, ProjectServices.isCompartmentalisedModel(workspace.getName()));
//			
//			System.out.println("--------tamanho: " + reactionsNames.size());
//			for (String reactionName : reactionsNames) {
//				
//				int reactionId = ModelReactionsServices.getReactionID(reactionName, databaseName).get(0);
//				
//				System.out.println("----");
//				
//				System.out.println(reactionName);
//				
//				System.out.println(ModelReactionsServices.getReactionCompartment(databaseName, reactionId));
//				
//			}
			
			
		}
		catch (Exception e1) {

			Workbench.getInstance().warn("A problem was found when trying to export the model");;
			e1.printStackTrace();
		}
		
	
	}

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
