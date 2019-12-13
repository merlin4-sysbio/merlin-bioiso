package pt.uminho.ceb.biosystems.merlin.merlin_biocoiso;

import java.awt.Image;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import es.uvigo.ei.aibench.workbench.Workbench;
import pt.uminho.ceb.biosystems.merlin.gui.datatypes.WorkspaceAIB;
import pt.uminho.ceb.biosystems.merlin.biocomponents.io.Enumerators.SBMLLevelVersion;
import pt.uminho.ceb.biosystems.merlin.biocomponents.io.readers.ContainerBuilder;
import pt.uminho.ceb.biosystems.merlin.biocomponents.io.writers.SBMLLevel3Writer;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.WorkspaceGenericDataTable;
import pt.uminho.ceb.biosystems.merlin.services.ProjectServices;
import pt.uminho.ceb.biosystems.merlin.utilities.io.FileUtils;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
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

	public static Object[] createLineFromMap(Map<?,?> keyMap, String key, Icon produced, Icon notProduced) {

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

}
