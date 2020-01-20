package pt.uminho.ceb.biosystems.merlin.merlin_biocoiso;


import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
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
import pt.uminho.ceb.biosystems.merlin.gui.datatypes.WorkspaceAIB;
import pt.uminho.ceb.biosystems.merlin.gui.jpanels.CustomGUI;
import pt.uminho.ceb.biosystems.merlin.gui.utilities.AIBenchUtils;
import pt.uminho.ceb.biosystems.merlin.gui.utilities.CreateImageIcon;
import pt.uminho.ceb.biosystems.merlin.core.containers.model.MetaboliteContainer;
import pt.uminho.ceb.biosystems.merlin.core.containers.model.ReactionContainer;
import pt.uminho.ceb.biosystems.merlin.services.ProjectServices;
import pt.uminho.ceb.biosystems.merlin.services.model.ModelReactionsServices;
import pt.uminho.ceb.biosystems.merlin.utilities.io.FileUtils;

/**
 * @author João Capela
 *
 */


public class BiocoisoGUI extends AbstractInputJDialog implements InputGUI{
	private static final long serialVersionUID = 1L;

	private ExtendedJComboBox<String> reaction ;
	private ExtendedJComboBox<String> models ;
	private ExtendedJComboBox<String> objective;
//	private JTextField url;
	private ParamsReceiver rec;

	protected Object project;
	private String[] workspaces;


	private JTextArea commit;

	public BiocoisoGUI() {

		super(new JFrame());

		//fill();
	}

	public String getDialogTitle() {
		return "BioISO";
	}

	public String getDescription() {
		return "get results from BioISO";
	}

	public JPanel getInputComponentsPane() {

		List<ClipboardItem> cl = Core.getInstance().getClipboard().getItemsByClass(WorkspaceAIB.class);                    

		workspaces = new String[cl.size()];
		for (int i = 0; i < cl.size(); i++) {

			workspaces[i] = (cl.get(i).getName());
		}
		this.models = new ExtendedJComboBox<String>(workspaces);
		this.objective = new ExtendedJComboBox<String>(new String[0]);
//		this.url = new JTextField("https://bioiso.bio.di.uminho.pt");

		this.reaction = new ExtendedJComboBox<String>(new String[0]);
		this.reaction.setEditable(true);
		AutoCompleteDecorator.decorate(this.reaction);

		this.commit = new JTextArea(); 

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
		okButton.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Ok_bioiso.png")),0.7).resizeImageIcon());
		ActionListener listener= new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispose();

				rec.paramsIntroduced(
						new ParamSpec[]{
								new ParamSpec("workspace", String.class,models.getSelectedItem().toString(),null),
								new ParamSpec("reaction", String.class,reaction.getSelectedItem().toString(),null),
								new ParamSpec("objective", String.class,objective.getSelectedItem().toString(),null),
//								new ParamSpec("url", String.class,url.getText(),null),
								new ParamSpec("commit", boolean.class,commit.getText(),null)

						}
						);

			}
		};
		okButton.addActionListener(listener);

		cancelButton = new JButton("cancel");
		cancelButton.setToolTipText("cancel");
		cancelButton.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Cancel_bioiso.png")),0.7).resizeImageIcon());
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
		InputParameter[] parameters = new InputParameter[4];
		parameters[0] = 

				new InputParameter(
						"workspace", 
						models, 
						"select the workspace"
						);

		parameters[1] = 
				new InputParameter(
						"reaction", 
						reaction, 
						"reaction to be studied"
						);
		parameters[2] = 
				new InputParameter(
						"objective", 
						objective, 
						"objective"
						);
//		parameters[3] = 
//				new InputParameter(
//						"URL", 
//						url, 
//						"BioISO URL"
//						);
		
		parameters[3] = 
				new InputParameter(
						"commit", 
						commit, 
						"commit"
						);
		
		return parameters;
	}

	public void setReactions() {
		
	
		WorkspaceAIB workspace = AIBenchUtils.getProject(models.getSelectedItem().toString());

		String path = FileUtils.getWorkspaceTaxonomyFolderPath(workspace.getName(), workspace.getTaxonomyID());
		File biocoisoFile = new File(path.concat("biocoiso"));
		
		
		try {

			if(!biocoisoFile.exists()) {

				try {
					biocoisoFile.mkdir();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			

			Map<Integer, ReactionContainer> reactions_dic = ModelReactionsServices.getReactionsByReactionId(workspace.getName(), ProjectServices.isCompartmentalisedModel(workspace.getName()));

			ArrayList<String> reactions_list = new ArrayList<String>();

//			Container container = new Container(new ContainerBuilder(workspace.getName(), "model_".concat(workspace.getName()),
//					ProjectServices.isCompartmentalisedModel(workspace.getName()), false, "", "e-biomass"));

			Map<Integer, List<MetaboliteContainer>> reactionMetabolites = 
					this.getStoichiometry(reactions_dic, 
							ProjectServices.isCompartmentalisedModel(workspace.getName()));

			for (int res : reactions_dic.keySet()) {
				
				if(reactionMetabolites.containsKey(res)) {
					
					if (reactions_dic.get(res).getLocalisation()==null) 
						reactions_list.add(buildID("R_",reactions_dic.get(res).getExternalIdentifier(),"cytop"));
					else
						reactions_list.add(buildID("R_",reactions_dic.get(res).getExternalIdentifier(),reactions_dic.get(res).getLocalisation().getAbbreviation()));

					
				}

			}
			
//			Map<String, ReactionCI> dictionary = container.getReactions();

//			Set<String> reactions_test = dictionary.keySet();

			String[] reactions_list_arr =  reactions_list.toArray(new String[0]);
			
			reaction.setModel(new DefaultComboBoxModel<>(reactions_list_arr));
			
			if (reactions_list_arr.length==0){
				Workbench.getInstance().info("Please choose a workspace with reactions.");
			}
		}
		catch (Exception e1) {

			Workbench.getInstance().warn("A problem was found when trying to find the reactions");;
			e1.printStackTrace();
		}
	}

	private Map<Integer, List<MetaboliteContainer>> getStoichiometry(Map<Integer, ReactionContainer> reactions, boolean isCompartmentalized) throws Exception {

		WorkspaceAIB workspace = AIBenchUtils.getProject(models.getSelectedItem().toString());
		
		Map<Integer, List<MetaboliteContainer>> reactionMetabolites = new HashMap<>();
		
		List<String[]> result2 = ModelReactionsServices.getStoichiometryInfo(workspace.getName(), isCompartmentalized);
		if(result2 != null) {

			for(int i=0; i<result2.size(); i++) {

				int idreaction = Integer.parseInt(result2.get(i)[1]);
				int idMetabolite = Integer.parseInt(result2.get(i)[2]);
				double stoichiometry = Double.parseDouble(result2.get(i)[3]);
				String metaboliteName = result2.get(i)[4];
				String formula = result2.get(i)[5];
				String metaboliteExternalIdentifier = result2.get(i)[6];
				String metaboliteCompartmentName = result2.get(i)[8];
				int metaboliteIdCompartment = Integer.valueOf(result2.get(i)[9]);
				String metaboliteCompartmentAbbreviation = result2.get(i)[10];

				//				System.out.println(idreaction+" "+reactionName+" "+this.reactions.containsKey(idreaction));

				if(reactions.containsKey(idreaction)) {

					//					if(!list2[3].contains("m") && !list2[3].contains("n")) {

					List<MetaboliteContainer> metabolitesContainer = new ArrayList<MetaboliteContainer>();

					if(reactionMetabolites.containsKey(idreaction))
						metabolitesContainer = reactionMetabolites.get(idreaction);

					MetaboliteContainer metabolite = new MetaboliteContainer(idMetabolite, metaboliteName, formula, 
							stoichiometry,metaboliteCompartmentName, metaboliteIdCompartment, metaboliteCompartmentAbbreviation, metaboliteExternalIdentifier);
					metabolitesContainer.add(metabolite);

					reactionMetabolites.put(idreaction, metabolitesContainer);
				}
			}
			return reactionMetabolites;
		}
		return null;
	}

	private static String buildID(String prefix, String identifier, String compartment) {

		if(compartment == null)
			System.out.println("null compartment");

		//		System.out.println(compartment);
		//		System.out.println(identifier);
		//		System.out.println(prefix);
		//		
		String output = identifier.concat("__").concat(compartment.toLowerCase());

		if(!output.startsWith(prefix))
			output = prefix.concat(output);

		output = output.replace("-", "_").replace(":", "_").replace(" ", "_").replace("\t", "_").replace(".", "_").replace("+", "_");
		return output;
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
	
	public JTextArea getCommit() {
		return this.commit;
	}

	@Override
	public void finish() {

	}
}
