package pt.uminho.ceb.biosystems.merlin.merlin_biocoiso;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class Teste {
		public static void main( String[] args ) throws IOException, ParseException {
			JSONParser jsonParser = new JSONParser();
	         
	        try (FileReader reader = new FileReader("C:/Users/Joao/Desktop/results_test_3.json"))
	        {
	            //Read JSON file
	            Object obj = (JSONObject) jsonParser.parse(reader);
	            
	            JSONObject jo = (JSONObject) obj; 
	            Map<?, ?> coiso =  (Map<?, ?>) jo.get("M_fictitious");
				Set<String> next = (Set<String>) ((Map<?, ?>) coiso.get("next")).keySet();
				for (String key : next) {
	            	System.out.println(key);
	            	Map<?,?> keyMap =  (Map<?, ?>) ((Map<?, ?>) coiso.get("next")).get(key);
	            	Map<?,?> nextMap =  (Map<?, ?>) ((Map<?, ?>) keyMap.get("next"));
	            	Set<String> next2  =  (Set<String>)  ((HashMap) keyMap.get("next")).keySet();
	            	for (String k : next2) {
	        			ArrayList<String> childrenList = (ArrayList<String>) ((Map<?,?>) nextMap.get(k)).get("children");
	        			System.out.println(childrenList);
	            	}
			}}}}
			
			
			


//			WorkspaceDataTableView newView = new WorkspaceDataTableView(newTable);
			
			
			
//		 }



