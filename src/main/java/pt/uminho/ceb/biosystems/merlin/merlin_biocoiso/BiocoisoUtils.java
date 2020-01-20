package pt.uminho.ceb.biosystems.merlin.merlin_biocoiso;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import javax.swing.Icon;

import org.apache.commons.validator.routines.EmailValidator;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import es.uvigo.ei.aibench.workbench.Workbench;
import pt.uminho.ceb.biosystems.merlin.gui.datatypes.WorkspaceAIB;
import pt.uminho.ceb.biosystems.merlin.gui.utilities.AIBenchUtils;
import pt.uminho.ceb.biosystems.merlin.biocomponents.io.Enumerators.SBMLLevelVersion;
import pt.uminho.ceb.biosystems.merlin.biocomponents.io.readers.ContainerBuilder;
import pt.uminho.ceb.biosystems.merlin.biocomponents.io.writers.SBMLLevel3Writer;
import pt.uminho.ceb.biosystems.merlin.core.containers.model.MetaboliteContainer;
import pt.uminho.ceb.biosystems.merlin.core.containers.model.ReactionContainer;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.WorkspaceGenericDataTable;
import pt.uminho.ceb.biosystems.merlin.services.ProjectServices;
import pt.uminho.ceb.biosystems.merlin.services.model.ModelReactionsServices;
import pt.uminho.ceb.biosystems.merlin.utilities.io.FileUtils;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.JSBMLLevel3Reader;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.pair.Pair;

public class BiocoisoUtils {


	/**
	 * This method generates a checksum key for the downloaded files 
	 * @param digest
	 * @param file
	 * @return String
	 * @throws IOException
	 */
	public static String getFileChecksum(MessageDigest digest, File file) throws IOException
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

	public static String readWordsInFile(String path){

		try {

			BufferedReader reader = new BufferedReader(new FileReader(path));

			String text = "";
			String line;
			while ((line = reader.readLine()) != null) {

				if(!line.isEmpty()) {
					text= text + line + "\n";
				}
			}

			reader.close();
			return text;
		} 
		catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}
	
	public static void writeTextInFile(String message, File file) throws IOException {

		FileWriter writer = new FileWriter(file);

		writer.write(message);

		writer.close();
	}

	public static File getLatestFilefromDir(String dirPath){
		File dir = new File(dirPath);
		File[] files = dir.listFiles();
		if (files == null || files.length == 0) {
			return null;
		}

		File lastModifiedFile = files[0];
		for (int i = 1; i < files.length; i++) {
			if (lastModifiedFile.lastModified() < files[i].lastModified()) {
				lastModifiedFile = files[i];
			}
		}
		return lastModifiedFile;
	}

	public static List<File> getNLatestFilefromDir(String dirPath, int n){
		File dir = new File(dirPath);
		File[] files = dir.listFiles();
		if (files == null || files.length == 0) {
			return null;
		}


		ArrayList<File> orderedList = new ArrayList<>(Arrays.asList(files));


		for (int i = 0; i < orderedList.size(); i++) {
			File lastModifiedFile = orderedList.get(i);
			for (int j=i+1; j<orderedList.size(); j++) {
				if (lastModifiedFile.lastModified() < orderedList.get(j).lastModified()) {

					lastModifiedFile = orderedList.get(j);
					orderedList.set(j, orderedList.get(i));
					orderedList.set(i, lastModifiedFile);

				}

			}

		}
		return orderedList.subList(0, n);
	}

	public static Map<String, Set<String>> getGenesReactionsMetabolites(WorkspaceAIB model) throws Exception{

		Container container = new Container(new ContainerBuilder(model.getName(), "model_".concat(model.getName()),
				ProjectServices.isCompartmentalisedModel(model.getName()), false, "", "e-biomass"));


		//		ArrayList<String> reactions = new ArrayList<String>(Arrays.asList(getReactions(model)));

		Set<String> reactions = container.getReactions().keySet();

		Set<String> metabolites = container.getMetabolites().keySet();

		Set<String> genes = container.getGenes().keySet();
		
		Set<String> genesModel = new HashSet<String>();
		
		for (String gene: genes) {
			
			gene = "G_".concat(gene);
			genesModel.add(gene);
			
		}
		
		Map<String, Set<String>> res = new HashMap<>();
		
		res.put("reactions", reactions);

		res.put("genes",genesModel);

		res.put("metabolites", metabolites);

		return res;


	}

	public static Map<String, Set<String>> getGenesReactionsMetabolitesFromModel(WorkspaceAIB database, File model) throws Exception{


		JSBMLLevel3Reader reader = new JSBMLLevel3Reader(model.getAbsolutePath(),database.getName());
		
		Set<String> genes = reader.getGenes().keySet();
		
		Set<String> metabolites = reader.getMetabolites().keySet();
		
		Set<String> reactions = reader.getReactions().keySet();
		
		
		Map<String, Set<String>> res = new HashMap<>();

		res.put("reactions", reactions);

		res.put("genes",genes);

		res.put("metabolites", metabolites);
		
		return res;


	}

	public static String[] getReactions(WorkspaceAIB models) throws Exception {


		WorkspaceAIB workspace = AIBenchUtils.getProject(models.toString());

		Map<Integer, ReactionContainer> reactions_dic = ModelReactionsServices.getReactionsByReactionId(workspace.getName(), ProjectServices.isCompartmentalisedModel(workspace.getName()));

		ArrayList<String> reactions_list = new ArrayList<String>();

		Map<Integer, List<MetaboliteContainer>> reactionMetabolites = 
				getStoichiometry(reactions_dic, 
						ProjectServices.isCompartmentalisedModel(workspace.getName()),models.toString());

		for (int res : reactions_dic.keySet()) {

			if(reactionMetabolites.containsKey(res)) {

				if (reactions_dic.get(res).getLocalisation()==null) 
					reactions_list.add(buildID("R_",reactions_dic.get(res).getExternalIdentifier(),"cytop"));
				else
					reactions_list.add(buildID("R_",reactions_dic.get(res).getExternalIdentifier(),reactions_dic.get(res).getLocalisation().getAbbreviation()));


			}

		}


		String[] reactions_list_arr =  reactions_list.toArray(new String[0]);

		return reactions_list_arr;

	}

	private static Map<Integer, List<MetaboliteContainer>> getStoichiometry(Map<Integer, ReactionContainer> reactions, boolean isCompartmentalized, String models) throws Exception {

		WorkspaceAIB workspace = AIBenchUtils.getProject(models);

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


	public static void backupWorkspaceFolder(String directory, WorkspaceAIB project) throws IOException {

		String path;
		String destination;

		path = FileUtils.getWorkspaceFolderPath(project.getName());
		destination = directory+"/"+project.getName();
		new File(directory+"/"+project.getName()).mkdirs();

		File p = new File(path);
		File d = new File(destination);

		org.apache.commons.io.FileUtils.copyDirectory(p, d);

	}

	public static void zipBackupFiles(File directory, String pathOutputFile) throws IOException {


		String path = directory.getPath();
		String extension = ".mer";
		String name = "backup" + extension;

		if(FileUtils.existsPath(path)){

			FileUtils.createZipFile(path, pathOutputFile + "/" + name, 1);

		}
		else
			throw new IllegalArgumentException("Error while exporting.");

		org.apache.commons.io.FileUtils.deleteDirectory(directory);
	}

	public static Map<?,?> readJSON(String file) {
		JSONParser jsonParser = new JSONParser();

		try (FileReader reader = new FileReader(file))
		{
			//Read JSON file
			Object obj = (JSONObject) jsonParser.parse(reader);

			JSONObject jo = (JSONObject) obj; 
			Map<?, ?> res = (Map<?, ?>) jo;
			return res;
		}
		catch (Exception e) {
			Workbench.getInstance().error(e);
			e.printStackTrace();

		}
		return null;
	}

	public static void exportModel(WorkspaceAIB project, String folder) throws IOException, Exception {

		Container container = new Container(new ContainerBuilder(project.getName(), "model_".concat(project.getName()),
				ProjectServices.isCompartmentalisedModel(project.getName()), false, "", "e-biomass"));

		SBMLLevel3Writer merlinSBML3Writer = new SBMLLevel3Writer(folder.concat("/model.xml"), 
				container, "", false, null, true, SBMLLevelVersion.L3V1, true);

		merlinSBML3Writer.writeToFile();
	}

	public static Pair<WorkspaceGenericDataTable, Map<?,?>> createDataTable(String file, List<String> columnsNames, String name, 
			String windowName, Icon produced, Icon notProduced) throws IOException, ParseException {


		Map<?,?> resultMap = BiocoisoUtils.readJSON(file);
		Pair<WorkspaceGenericDataTable, Map<?,?>> tableAndNextLevel = tableCreator(resultMap, name, windowName, "M_fictitious"
				,produced,notProduced);
		return tableAndNextLevel;
	}


	public static Pair<WorkspaceGenericDataTable, Map<?,?>> tableCreator(Map<?,?> level, String name, String windowName, 
			String metabolite, Icon produced, Icon notProduced) {

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

				Object[] res = createLineFromMap(level2,key,produced,notProduced);

				newTable.addLine(res);

			}
			Pair<WorkspaceGenericDataTable, Map<?,?>> res = new Pair<WorkspaceGenericDataTable, Map<?,?>>(newTable,nextLevel);
			return res;
		}
		return null;
	}


	public static String getEmail() {

		String confEmail = "";
		ArrayList<String> listLines = new ArrayList<>();
		String confPath = FileUtils.getConfFolderPath().concat("email.conf");
		File configFile = new File(confPath);
		try {
			Scanner file = new Scanner(configFile);
			while(file.hasNextLine()==true) {
				listLines.add(file.nextLine());
			}
			file.close();	
		} catch (FileNotFoundException e) {
			e.printStackTrace();

		}

		for (String item : listLines) {
			if(item.startsWith("email")) {

				String[]parts=item.split(":");
				confEmail = parts[1].trim();
			}
		}

		boolean verify = EmailValidator.getInstance().isValid(confEmail);


		if (verify)
			return confEmail;
		else
			return null;


	}

	//palsson.di.uminho.pt:7475
	public static Object[] createLineFromMap(Map<?,?> keyMap, String key, Icon produced, Icon notProduced) {

		Object[] res = new Object[5];

		res[1]=key;


		Boolean flux = (Boolean) keyMap.get("analysis");

		@SuppressWarnings("unchecked")
		ArrayList<ArrayList<String>> childrenList = (ArrayList<ArrayList<String>>) keyMap.get("reactions");

		res[2]= Integer.toString(childrenList.size());

		res[3]=keyMap.get("role");

		if (flux) {
			res[4] = produced;
		}
		else {
			res[4] = notProduced;
		}
		return res;
	}

}
