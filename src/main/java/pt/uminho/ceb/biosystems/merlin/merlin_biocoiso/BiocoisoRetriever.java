package pt.uminho.ceb.biosystems.merlin.merlin_biocoiso;


import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.uvigo.ei.aibench.core.Core;
import es.uvigo.ei.aibench.core.clipboard.ClipboardItem;
import es.uvigo.ei.aibench.core.operation.annotation.Cancel;
import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.aibench.core.operation.annotation.Progress;
import es.uvigo.ei.aibench.workbench.Workbench;
import pt.uminho.ceb.biosystems.merlin.gui.datatypes.WorkspaceAIB;
import pt.uminho.ceb.biosystems.merlin.gui.datatypes.WorkspaceTableAIB;
import pt.uminho.ceb.biosystems.merlin.gui.jpanels.CustomGUI;
import pt.uminho.ceb.biosystems.merlin.gui.utilities.AIBenchUtils;
import pt.uminho.ceb.biosystems.merlin.gui.utilities.TimeLeftProgress;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.WorkspaceGenericDataTable;
import pt.uminho.ceb.biosystems.merlin.core.utilities.Enumerators.SequenceType;
import pt.uminho.ceb.biosystems.merlin.merlin_biocoiso.datatypes.ValidationBiocoisoAIB;
import pt.uminho.ceb.biosystems.merlin.processes.WorkspaceProcesses;
import pt.uminho.ceb.biosystems.merlin.services.model.ModelSequenceServices;
import pt.uminho.ceb.biosystems.merlin.utilities.io.FileUtils;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.pair.Pair;



/**Aibench operation for BioISO
 * @author João Capela
 *
 */
@Operation(name="BioISO",description="Get results from BioISO")
public class BiocoisoRetriever implements PropertyChangeListener {

	private WorkspaceAIB project;
	private TimeLeftProgress progress = new TimeLeftProgress();
	private AtomicBoolean cancel = new AtomicBoolean(false);
	private AtomicInteger querySize;
	private AtomicInteger counter = new AtomicInteger(0);
	private long startTime;
	private String message;
	public static final String BIOCOISO_FILE_NAME = "biocoiso";
	private String biocoisoResultsFile;
	private String reaction;
	final static Logger logger = LoggerFactory.getLogger(BiocoisoRetriever.class);
	Icon notProduced = new ImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/notProducing.png")).getImage().getScaledInstance(20, 20, Image.SCALE_DEFAULT));
	Icon produced = new ImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/producing.png")).getImage().getScaledInstance(20, 20, Image.SCALE_DEFAULT));
	Icon unknown = new ImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/question.png")).getImage().getScaledInstance(20, 20, Image.SCALE_DEFAULT));
	private String objective;
	private String commit;
	private String email;
	private boolean fast;
	private String url;



	@Port(direction=Direction.INPUT, name="Reaction",description="", order = 2)
	public void setReaction (String reaction) throws Exception{

		this.reaction=reaction.replaceAll("^(R_)", "");
	}	

	@Port(direction=Direction.INPUT, name="Objective",description="", order = 3)
	public void setObjective (String objective){
		this.objective=objective;
	}

	@Port(direction=Direction.INPUT, name="Fast BioISO",description="", order = 4)
	public void setFast(String fast){
		if (fast.equals("true")) 
			this.fast=true;
		
		else 
			this.fast=false;
		
		
	}

	@Port(direction=Direction.INPUT, name="Backup",description="Backup model", order = 5)
	public void setCommit(String commit) throws Exception {
		
		
		this.url = FileUtils.readBioisoConfFile().get("host");
		
		this.commit = commit;
		
		try {
			
			this.startTime = GregorianCalendar.getInstance().getTimeInMillis();
			
			if (this.fast) {
//				Workbench.getInstance().info("as you are executing Fast BioISO, \nif the tested metabolites have more than 20 reactions,"
//					+ " be aware that only 10% of them will be tested" );
				setCancelFast();
			}
			
			boolean submitted = false;
			
			if (!this.cancel.get())
				submitted = submitFiles();
				

			if (submitted && !this.cancel.get()) {

				this.progress.setTime(GregorianCalendar.getInstance().getTimeInMillis() - this.startTime, 5, 5, "Rendering results...");

				logger.info("The files for BioISO were submitted successfully");

				Workbench.getInstance().info("The files for BioISO were submitted successfully");

				if (!this.cancel.get())
					executeOperation();
				else
					Workbench.getInstance().warn("operation canceled!");
			}
			else if(this.cancel.get()) {
				Workbench.getInstance().warn("operation canceled!");
			}
			else {

				logger.error("");
				Workbench.getInstance().error("error while doing the operation! please try again");
			}
		}
		catch(Exception e) {
			logger.error(e.getMessage());
			Workbench.getInstance().error(e);
			e.printStackTrace();
		}
	}

	@Port(direction=Direction.INPUT, name="Workspace",description="select the new model workspace",validateMethod="checkNewProject", order = 1)
	public void setNewProject(String projectName) throws Exception {


		this.project = AIBenchUtils.getProject(projectName);
		

	}

	private void executeOperation() throws IOException, ParseException {
		
		int table_number = this.project.getDatabase().getValidation().getEntities().size() + 1;

		String name = Integer.toString(table_number) + " - " + this.reaction;

		String[] columnsName = new String[] {"info","metabolite", "reactions", "role" , "analysis"};

		WorkspaceTableAIB table = new WorkspaceTableAIB(name, columnsName , this.project.getName());

		File results_file = BiocoisoUtils.getLatestFilefromDir(getWorkDirectory().concat("/"+BIOCOISO_FILE_NAME));

		Pair<WorkspaceGenericDataTable, Map<?,?>> filledTableAndNextLevel = 
				BiocoisoUtils.createDataTable(results_file.getAbsolutePath().concat("/results/results_").concat(BIOCOISO_FILE_NAME).concat(".json"), 
						Arrays.asList(columnsName), this.project.getName(), name,produced,notProduced,unknown);

		
		Map<?, ?> entireMap = BiocoisoUtils.readJSON(results_file.getAbsolutePath().concat("/results/results_").concat(BIOCOISO_FILE_NAME).concat(".json"));

		System.out.println(entireMap);
		
		ValidationBiocoisoAIB biocoiso = new ValidationBiocoisoAIB(table, name, filledTableAndNextLevel.getB(), entireMap);

		biocoiso.setWorkspace(this.project);

		biocoiso.setMainTableData(filledTableAndNextLevel.getA());

		biocoiso.setData(filledTableAndNextLevel.getA());


		try {

			this.project.getValidation().addEntity(biocoiso);

		}

		catch (Exception e) {

			List<WorkspaceAIB> wspList = AIBenchUtils.getAllProjects();

			for (WorkspaceAIB wsp : wspList) {

				ClipboardItem item = Core.getInstance().getClipboard().getClipboardItem(wsp);

				Core.getInstance().getClipboard().removeClipboardItem(item);

				Core.getInstance().getClipboard().putItem(wsp, item.getName());

			}
		}
		//		this.project.getDatabase().getValidation().setEntities(newList);

	}

	//////////////////////////ValidateMethods/////////////////////////////
	/**
	 * @param project
	 */
	public void checkNewProject(String workspaceName) {

		if(workspaceName == "") {

			throw new IllegalArgumentException("no workspace selected!");
		}
		else {

			this.project = AIBenchUtils.getProject(workspaceName);;

			try {

				if(!ModelSequenceServices.checkGenomeSequences(workspaceName, SequenceType.PROTEIN)) {
					throw new IllegalArgumentException("please set the project fasta ('.faa' or '.fna') files");
				}

				WorkspaceProcesses.createFaaFile(this.project.getName(), this.project.getTaxonomyID()); // method creates ".faa" files only if they do not exist


			} catch (Exception e) {
				Workbench.getInstance().error(e);
				e.printStackTrace();
			}
		}
	}



	/////////////////////////////////////////////////////

	/**This method allows the submission of the required files for BioISO into the web server, the download of the results and the verification 
	 * of the md5 key as well as show error and warning messages
	 * @return boolean informing whether the submission went well or not.
	 * @throws Exception 
	 */
	public boolean submitFiles() throws Exception {


		File model = creationOfRequiredFiles();

		if (model == null) {
			return false;
		}
		
		this.email = BiocoisoUtils.getEmail();
		

		HandlingRequestsAndRetrievalsBiocoiso post = new HandlingRequestsAndRetrievalsBiocoiso(model, this.reaction, this.objective, this.url, this.email, this.fast);

		String submissionID = "";

		boolean verify = false;

		try {

			this.progress.setTime(GregorianCalendar.getInstance().getTimeInMillis() - this.startTime, 1, 5, "submitting files...");

			submissionID = post.postFiles();

			if(submissionID!=null) {

				try {
					logger.info("SubmissionID attributed: {}", submissionID);
					int responseCode = -1;

					this.progress.setTime(GregorianCalendar.getInstance().getTimeInMillis() - this.startTime, 2, 5, 
							"files submitted, waiting for results...");

					while (responseCode!=200 &&  !this.cancel.get()) {

						responseCode = post.getStatus(submissionID);

						this.showWorkbechMessagesIfError(responseCode);

						TimeUnit.SECONDS.sleep(3);
					}

					File results_file = BiocoisoUtils.getLatestFilefromDir(getWorkDirectory().concat("/"+BIOCOISO_FILE_NAME));

					this.progress.setTime(GregorianCalendar.getInstance().getTimeInMillis() - this.startTime, 3, 5, "downloading BioISO results");

					if(!this.cancel.get()) {
						verify = post.downloadFile(submissionID, results_file.getAbsolutePath().concat("/results.zip"));

						this.progress.setTime(GregorianCalendar.getInstance().getTimeInMillis() - this.startTime, 4, 5, "verifying...");


						biocoisoResultsFile = results_file.getAbsolutePath().concat("/results/");

						FileUtils.extractZipFile(results_file.getAbsolutePath().concat("/results.zip"), biocoisoResultsFile);

						File checksumFile = new File(biocoisoResultsFile.concat("/checksum.md5"));

						if (!checksumFile.exists()) {

							File folder = new File(biocoisoResultsFile);
							File[] listOfFiles = folder.listFiles();

							boolean stop=false;

							int i = 0;

							//The following code will show different error and warning messages to merlin users depending on the error founded

							while (!stop && i<listOfFiles.length) {
								if (listOfFiles[i].getName().equals("error_message") ) {
									BufferedReader reader = new BufferedReader(new FileReader(folder+"/error_message"));

									String line;

									line = reader.readLine();

									reader.close();

									Workbench.getInstance().warn(line);
									stop = true;
									verify=false;
								}
								i++;
							}
						}
						else if (verify) {
							verify=verifyKeys();
							logger.info("The result of the verification of md5 file was {}", Boolean.toString(verify));
						}

						return verify;
					}
				}

				catch (Exception e) {
					e.printStackTrace();
				}

			}
			else
				throw new Exception("No dockerID attributed!");
		} 
		catch (Exception e) {
			e.printStackTrace();
			logger.error("Error submitting files");

		}
		return verify;
	}


	private void showWorkbechMessagesIfError(int responseCode) throws Exception {

		if(responseCode == -1) { 
			logger.error("Error!");
			throw new Exception("Error!");

		}
		else if (responseCode==503) {
			logger.error("The server cannot handle the submission due to capacity overload. Please try again later!");
			throw new Exception("The server cannot handle the submission due to capacity overload. Please try again later!");
		}
		else if (responseCode==500) {
			logger.error("Something went wrong while processing the request, please try again");
			throw new Exception("Something went wrong while processing the request, please try again");
		}
		else if (responseCode == 400) {
			logger.error("The submitted files are fewer than expected");
			throw new Exception("The submitted files are fewer than expected");
		}

	}

	/**
	 * This method gives the name of the current working directory
	 * @return a {@code String} with the path for the working directory
	 */
	private  String getWorkDirectory() {
		String database = this.project.getDatabase().getWorkspaceName();

		Long taxonomyID= this.project.getTaxonomyID();

		String path = FileUtils.getWorkspaceTaxonomyFolderPath(database, taxonomyID);

		return path;

	}

	/**
	 * This method verifies the key in the md5 file and checks whether the results were corrupted or not.
	 * @return boolean informing whether the results were corrupted or not.
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	private boolean verifyKeys() throws IOException, NoSuchAlgorithmException {

		try {

			File results_file = BiocoisoUtils.getLatestFilefromDir(getWorkDirectory().concat("/"+BIOCOISO_FILE_NAME));

			String path = results_file.getAbsolutePath() +"/results";

			MessageDigest md5Digest = MessageDigest.getInstance("MD5");

			File file = new File(path.concat("/results_".concat(BIOCOISO_FILE_NAME).concat(".json")));

			String checksum = BiocoisoUtils.getFileChecksum(md5Digest, file);

			String key = BiocoisoUtils.readWordInFile(path.concat("/checksum.md5"));

			if(checksum.equals(key)) {
				return true;
			}
			else {
				logger.error("While verifying the checksum of the xml file, a error was found.");
				return false;
			} 
		}
		catch (Exception e) {
			return false;
		}

	}


	/**
	 * This method generates the required files for BioISO to run. A file with the biomass reaction id, another with the protein's name and the model.
	 * @return List<File> with the required files.
	 * @throws Exception
	 */
	private File creationOfRequiredFiles() throws Exception {

		File biocoisoFolder = new File(getWorkDirectory().concat("/biocoiso"));

		if (!biocoisoFolder.exists())
			biocoisoFolder.mkdir();

		LocalDateTime currentTime = LocalDateTime.now();

		String date = "." + currentTime.getDayOfMonth() + "-" +currentTime.getMonthValue() + "-" + currentTime.getYear()+ "." + currentTime.getHour() + "h" + currentTime.getMinute() + "m" + currentTime.getSecond() + "s";

		String newFileBiocoiso = biocoisoFolder.getAbsolutePath().concat("/"+this.reaction+date);
		
		FileUtils.createFoldersFromPath(newFileBiocoiso);
		
		BiocoisoUtils.writeTextInFile(this.commit, new File(newFileBiocoiso+"/commit.txt"));
		File model = new File(newFileBiocoiso.concat("/model.xml"));

		this.progress.setTime(GregorianCalendar.getInstance().getTimeInMillis() - this.startTime, 0, 5, "exporting the model...");

		BiocoisoUtils.exportModel(this.project, newFileBiocoiso);

		if (model.exists() ) {

			return model;

		}
		else
			return null;

	}
	

//	private void createBackup(String newFileBiocoiso) throws Exception {
//		
//		BiocoisoUtils.backupWorkspaceFolder(newFileBiocoiso, this.project);
//
//		String backupXmlTables = newFileBiocoiso +"/"+this.project.getName() +"/tables/";
//
//		File newFile = new File(backupXmlTables);
//
//		if(!newFile.exists())
//			newFile.mkdirs();
//
//
//		DatabaseServices.databaseToXML(this.project.getName(), backupXmlTables,this);
//		
//		String file = newFileBiocoiso.concat("/"+this.project.getName());
//
//		BiocoisoUtils.zipBackupFiles(new File(file),newFileBiocoiso);
//	}
	

	@SuppressWarnings("unchecked")
	public static void exportJSON(Map<?,?> entireMap) {

		JSONObject json = new JSONObject();
		json.putAll( entireMap );
	}

	

	/**
	 * @return the progress
	 */
	@Progress(progressDialogTitle = "BioISO", modal = false, workingLabel = "BioISO is running...", preferredWidth = 400, preferredHeight=300)
	public TimeLeftProgress getProgress() {

		return progress;
	}
	
	
	public void setCancelFast() {

		String[] options = new String[2];
		options[0] = "yes";
		options[1] = "no";

		int result = CustomGUI.stopQuestion("BioISO Fast", "as you are executing Fast BioISO, \nif the tested metabolites have more than 20 reactions,\n" + 
									"be aware that only 10% of them will be tested. \n"
									+ "do you want to proceed anyway?", options);

		if(result == 1) {
			this.cancel.set(true);
		}
		

		progress.setTime(0, 0, 0);


	}

	/**
	 * @param cancel the cancel to set
	 */
	@Cancel
	public void setCancel() {

		String[] options = new String[2];
		options[0] = "yes";
		options[1] = "no";

		int result = CustomGUI.stopQuestion("cancel confirmation", "are you sure you want to cancel the operation?", options);

		if(result == 0) {
			this.cancel.set(true);
		}
		

		progress.setTime(0, 0, 0);


	}

	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(Observable o, Object arg) {

		this.progress.setTime(GregorianCalendar.getInstance().getTimeInMillis() - this.startTime, this.counter.get(), this.querySize.get(), message);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// TODO Auto-generated method stub

	}




	//	public void propertyChange(PropertyChangeEvent evt) {
	//		// TODO Auto-generated method stub
	//		
	//	}
	

}

