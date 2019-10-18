package pt.uminho.ceb.biosystems.merlin.merlin_biocoiso;


import java.awt.Image;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
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
import pt.uminho.ceb.biosystems.merlin.aibench.datatypes.WorkspaceAIB;
import pt.uminho.ceb.biosystems.merlin.aibench.datatypes.WorkspaceTableAIB;
import pt.uminho.ceb.biosystems.merlin.aibench.utilities.AIBenchUtils;
import pt.uminho.ceb.biosystems.merlin.aibench.utilities.TimeLeftProgress;
import pt.uminho.ceb.biosystems.merlin.biocomponents.io.Enumerators.SBMLLevelVersion;
import pt.uminho.ceb.biosystems.merlin.biocomponents.io.readers.ContainerBuilder;
import pt.uminho.ceb.biosystems.merlin.biocomponents.io.writers.SBMLLevel3Writer;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.WorkspaceGenericDataTable;
import pt.uminho.ceb.biosystems.merlin.core.utilities.Enumerators.SequenceType;
import pt.uminho.ceb.biosystems.merlin.merlin_biocoiso.datatypes.ValidationBiocoisoAIB;
import pt.uminho.ceb.biosystems.merlin.services.ProjectServices;
import pt.uminho.ceb.biosystems.merlin.services.model.ModelSequenceServices;
import pt.uminho.ceb.biosystems.merlin.utilities.io.FileUtils;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.pair.Pair;


/**Aibench operation for BioISO
 * @author João Capela
 *
 */
@Operation(name="BioISO",description="Get results from BioISO")
public class BiocoisoRetriever implements Observer {

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
	private Map<?,?> resultMap;
	Icon notProduced = new ImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Cancel.png")).getImage().getScaledInstance(20, 20, Image.SCALE_DEFAULT));
	Icon produced = new ImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Ok.png")).getImage().getScaledInstance(20, 20, Image.SCALE_DEFAULT));
	private String objective;



	@Port(direction=Direction.INPUT, name="Reaction",description="", order = 2)
	public void setReaction (String reaction) throws Exception{

		this.reaction=reaction.replaceAll("^(R_)", "");
	}	

	@Port(direction=Direction.INPUT, name="Objective",description="", order = 3)
	public void setObjective (String objective){
		try {
			this.objective=objective;

			creationOfRequiredFiles();

			this.startTime = GregorianCalendar.getInstance().getTimeInMillis();

			this.progress.setTime(GregorianCalendar.getInstance().getTimeInMillis() - this.startTime, 0, 4, "submitting files...");


			boolean submitted = submitFiles();

			if (submitted && !this.cancel.get()) {

				this.progress.setTime(GregorianCalendar.getInstance().getTimeInMillis() - this.startTime, 4, 4, "Rendering results...");

				logger.info("The files for BioISO were submitted successfully");

				Workbench.getInstance().info("The files for BioISO were submitted successfully");

				executeOperation();
			}
			else if(this.cancel.get()) {
				Workbench.getInstance().warn("operation canceled!");
			}
			else {

				logger.error("");
				Workbench.getInstance().error("error while doing the operation! please try again");

				//				executeOperation();
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

		Pair<WorkspaceGenericDataTable, Map<?,?>> filledTableAndNextLevel = 
				this.createDataTable(this.getWorkDirectory().concat("/biocoiso/results/results_").concat(BIOCOISO_FILE_NAME).concat(".json"), 
						Arrays.asList(columnsName), this.project.getName(), name);

		//		Pair<WorkspaceGenericDataTable, Map<?,?>> filledTableAndNextLevel = 
		//				this.createDataTable("C:/Users/merlin Developer/Desktop/results_biocoiso_2.json", 
		//						Arrays.asList(columnsName), this.project.getName(), name);

		ValidationBiocoisoAIB biocoiso = new ValidationBiocoisoAIB(table, name, filledTableAndNextLevel.getB());

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
				else if(this.project.getTaxonomyID()<0) {

					throw new IllegalArgumentException("please enter the taxonomic identification from NCBI taxonomy");
				}

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

		HandlingRequestsAndRetrievalsBiocoiso post = new HandlingRequestsAndRetrievalsBiocoiso(model, this.reaction, this.objective);

		String submissionID = "";

		boolean verify = false;

		try {

			submissionID = post.postFiles();

			if(submissionID!=null) {

				try {
					logger.info("SubmissionID attributed: {}", submissionID);
					int responseCode = -1;

					this.progress.setTime(GregorianCalendar.getInstance().getTimeInMillis() - this.startTime, 1, 4, 
							"files submitted, waiting for results...");

					while (responseCode!=200 &&  !this.cancel.get()) {

						responseCode = post.getStatus(submissionID);

						System.out.println(responseCode);

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

						TimeUnit.SECONDS.sleep(3);
					}


					this.progress.setTime(GregorianCalendar.getInstance().getTimeInMillis() - this.startTime, 2, 4, "downloading BioISO results");

					if(!this.cancel.get())
						verify = post.downloadFile(submissionID, getWorkDirectory().concat("/"+BIOCOISO_FILE_NAME).concat("/results.zip"));


					this.progress.setTime(GregorianCalendar.getInstance().getTimeInMillis() - this.startTime, 3, 4, "verifying...");

					biocoisoResultsFile = getWorkDirectory().concat("/"+BIOCOISO_FILE_NAME).concat("/results/");

					FileUtils.extractZipFile(getWorkDirectory().concat("/"+BIOCOISO_FILE_NAME).concat("/results.zip"), biocoisoResultsFile);

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

		String path = getWorkDirectory().concat("/"+BIOCOISO_FILE_NAME+"/results");

		MessageDigest md5Digest = MessageDigest.getInstance("MD5");

		File file = new File(path.concat("/results_".concat(BIOCOISO_FILE_NAME).concat(".json")));

		String checksum = getFileChecksum(md5Digest, file);

		String key = readWordInFile(path.concat("/checksum.md5"));

		if(checksum.equals(key)) {
			return true;
		}
		else {
			logger.error("While verifying the checksum of the xml file, a error was found.");
			return false;
		}
	}

	/**
	 * This method read a word in a file
	 * @param path: {@code String} with the path for the file
	 * @return String which is the word in the file
	 */

	public static String readWordInFile(String path){

		try {

			BufferedReader reader = new BufferedReader(new FileReader(path));

			String line;

			while ((line = reader.readLine()) != null) {

				if(!line.isEmpty() &&  !line.contains("**")) {

					reader.close();
					return line.trim();
				}
			}

			reader.close();

		} 
		catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * This method generates a checksum key for the downloaded files 
	 * @param digest
	 * @param file
	 * @return String
	 * @throws IOException
	 */
	private static String getFileChecksum(MessageDigest digest, File file) throws IOException
	{
		//Get file input stream for reading the file content
		FileInputStream fis = new FileInputStream(file);

		//Create byte array to read data in chunks
		byte[] byteArray = new byte[1024];
		int bytesCount = 0;

		//Read file data and update in message digest
		while ((bytesCount = fis.read(byteArray)) != -1) {
			digest.update(byteArray, 0, bytesCount);
		};

		//close the stream; We don't need it now.
		fis.close();

		//Get the hash's bytes
		byte[] bytes = digest.digest();

		//This bytes[] has bytes in decimal format;
		//Convert it to hexadecimal format
		StringBuilder sb = new StringBuilder();
		for(int i=0; i< bytes.length ;i++)
		{
			sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
		}

		//return complete hash
		return sb.toString();
	}

	/**
	 * This method generates the required files for BioISO to run. A file with the biomass reaction id, another with the protein's name and the model.
	 * @return List<File> with the required files.
	 * @throws Exception
	 */
	private File creationOfRequiredFiles() throws Exception {

		File biocoisoFolder = new File(getWorkDirectory().concat("/biocoiso"));

		File model = new File(biocoisoFolder.toString().concat("/model.xml"));

		if(model.exists()) {
			FileUtils.delete(model);
		}
		if(biocoisoFolder.exists()) {

			try {
				FileUtils.deleteDirectory(biocoisoFolder);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		biocoisoFolder.mkdir(); //creation of a directory to put the required files

		Container container = new Container(new ContainerBuilder(this.project.getName(), "model_".concat(this.project.getName()),
				ProjectServices.isCompartmentalisedModel(this.project.getName()), false, "", "e-biomass"));

		SBMLLevel3Writer merlinSBML3Writer = new SBMLLevel3Writer(biocoisoFolder.toString().concat("/model.xml"), 
				container, "", false, null, true, SBMLLevelVersion.L3V1, true);

		merlinSBML3Writer.writeToFile();


		if (model.exists() ) {

			return model;

		}
		else
			return null;

	}

	/**
	 * This method saves words in a given file.
	 * @param path: {@code String} with the file path
	 * @param words: {@code List<String>} with words to put in the given file
	 */
	public static void saveWordsInFile(String path, List<String> words){

		try {

			PrintWriter writer = new PrintWriter(path, "UTF-8");

			for(String word : words)
				writer.println(word);

			writer.close();

		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();

		} 
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public static void saveWordInFile(String path, String word) {

		PrintWriter writer;

		try {

			writer = new PrintWriter(path, "UTF-8");

			writer.print(word);

			writer.close();

		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

	/**
	 * This method creates the data table with the results. This table will be rendered in BioISO's view.
	 * @param file
	 * @param columnsNames
	 * @param name
	 * @param windowName
	 * @return WorkspaceGenericDataTable with the results.
	 * @throws IOException
	 * @throws ParseException 
	 */

	private Pair<WorkspaceGenericDataTable, Map<?,?>> createDataTable(String file, List<String> columnsNames, String name, String windowName) throws IOException, ParseException {

		JSONParser jsonParser = new JSONParser();

		try (FileReader reader = new FileReader(file))
		{
			//Read JSON file
			Object obj = (JSONObject) jsonParser.parse(reader);

			JSONObject jo = (JSONObject) obj; 
			this.resultMap = (Map<?, ?>) jo; //level 1
			Pair<WorkspaceGenericDataTable, Map<?,?>> tableAndNextLevel = this.tableCreator(resultMap, name, windowName, "M_fictitious");

			return tableAndNextLevel;
		}}


	private Pair<WorkspaceGenericDataTable, Map<?,?>> tableCreator(Map<?,?> level, String name, String windowName, String metabolite) {

		String[] columnsName = new String[] {"info","metabolite", "reaction", "role", "analysis"};

		WorkspaceGenericDataTable newTable = new WorkspaceGenericDataTable(Arrays.asList(columnsName) , name , windowName) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int col){
				if (col==0)
				{
					return true;
				}
				else return false;
			}
		}; 


		for (Object met : level.keySet()) {

			Map<?,?>  nextMap = (Map<?, ?>) level.get(met);

			Map<?,?> nextLevel = (Map<?, ?>) nextMap.get("next");

			@SuppressWarnings("unchecked")
			Set<String> next = (Set<String>) nextLevel.keySet();
			for (String key : next) {

				Map<?, ?> level2 = (Map<?, ?>) ((Map<?, ?>) nextLevel.get(key));

				Object[] res = createLineFromMap(level2,key);

				newTable.addLine(res);

			}
			Pair<WorkspaceGenericDataTable, Map<?,?>> res = new Pair<WorkspaceGenericDataTable, Map<?,?>>(newTable,nextLevel);
			return res;
		}
		return null;
	}

	private Object[] createLineFromMap(Map<?,?> keyMap, String key) {

		Object[] res = new Object[5];

		res[1]=key;

		boolean flux = (boolean) keyMap.get("flux");

		@SuppressWarnings("unchecked")
		ArrayList<ArrayList<String>> childrenList = (ArrayList<ArrayList<String>>) keyMap.get("children");

		res[2]= Integer.toString(childrenList.size());

		res[3]=keyMap.get("side");

		if (flux) {
			res[4] = produced;
		}
		else {
			res[4] = notProduced;
		}
		return res;
	}



	/**
	 * @return the progress
	 */
	@Progress(progressDialogTitle = "BioISO", modal = false, workingLabel = "BioISO is running...", preferredWidth = 400, preferredHeight=300)
	public TimeLeftProgress getProgress() {

		return progress;
	}

	/**
	 * @param cancel the cancel to set
	 */
	@Cancel
	public void setCancel() {

		progress.setTime(0, 0, 0);
		this.cancel.set(true);
	}

	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(Observable o, Object arg) {

		this.progress.setTime(GregorianCalendar.getInstance().getTimeInMillis() - this.startTime, this.counter.get(), this.querySize.get(), message);
	}




	//	public void propertyChange(PropertyChangeEvent evt) {
	//		// TODO Auto-generated method stub
	//		
	//	}

}

