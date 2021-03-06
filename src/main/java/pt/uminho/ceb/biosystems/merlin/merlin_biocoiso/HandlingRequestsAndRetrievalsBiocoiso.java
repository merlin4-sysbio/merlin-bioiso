package pt.uminho.ceb.biosystems.merlin.merlin_biocoiso;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**Handle requests and retrievals
 * @author João Capela
 *
 */
public class HandlingRequestsAndRetrievalsBiocoiso {


	final static Logger logger = LoggerFactory.getLogger(HandlingRequestsAndRetrievalsBiocoiso.class);
	
	private File model;

	private String reaction;

	private String objective;
	
	private String url;

	private String email;

	private String fast;

	public HandlingRequestsAndRetrievalsBiocoiso(File model, String reaction, String objective, String url, String email, boolean fast){

		this.setModel(model);
		
		this.reaction=reaction;
		
		this.objective=objective;
		
		this.url = url;
		
		this.email = email;
		
		this.fast = Boolean.toString(fast);
		
		

	}

	/**
	 * Method that makes a connection to the URL http://rosalind.di.uminho.pt:8085 and submit files there.
	 * 
	 * @return docker is a {@code String} which represents the ID of the docker used
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public String postFiles() throws IOException, InterruptedException {

		String uploadUrl = this.url.concat("/submitMerlinPlugin/"+reaction+"/"+objective+"/"+this.email+"/"+this.fast);
		
		String charset = "UTF-8";
		String param = "value";


		String boundary = Long.toHexString(System.currentTimeMillis()); // Just generate some unique random value.
		String CRLF = "\r\n"; // Line separator required by multipart/form-data.

		URL url = new URL(uploadUrl);

		URLConnection connection = url.openConnection();
		connection.setDoOutput(true);
		connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
		try (
				OutputStream output = connection.getOutputStream();
				PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, charset), true);
				) {
				logger.info("File path: " + model.getAbsolutePath());
				// Send normal param.
				writer.append("--" + boundary).append(CRLF);
				writer.append("Content-Disposition: form-data; name=\"param\"").append(CRLF);
				writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF);
				writer.append(CRLF).append(param).append(CRLF).flush();

				writer.append("--" + boundary).append(CRLF);
				writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + model.getName() + "\"").append(CRLF);
				writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF); // Text file itself must be saved in this charset!
				writer.append(CRLF).flush();
				Files.copy(model.toPath(), output);
				output.flush(); // Important before continuing with writer!
				writer.append(CRLF).flush();
			

			writer.append("--" + boundary + "--").append(CRLF).flush();
		}
		catch (Exception e) {
			e.printStackTrace();
			logger.error("Error submitting files");

		}
		

		// Request is lazily fired whenever you need to obtain information about response.
		int responseCode = ((HttpURLConnection) connection).getResponseCode();


		//TimeUnit.SECONDS.sleep(5);

		BufferedReader in = new BufferedReader(new InputStreamReader(((HttpURLConnection) connection).getInputStream()));
		String html = "";
		String docker = null;
		
		logger.info(uploadUrl);
		if (responseCode==202 || responseCode==201)
		{
			while ((html = in.readLine()) != null){
				if (html.contains("submissionID")) {
					String[] parts = html.split(":");
					docker = parts[4].replace("\"", "").trim().replace("}", "");
				}
			}
		}

		else if (responseCode==503) {
			logger.error("The server cannot handle the submission due to capacity overload. Please try again later!");
			return null;
		}
		else if (responseCode==500) {
			logger.error("Something went wrong while processing the request, please try again");
			return null;
		}
		else if (responseCode == 400) {
			logger.error("The submitted files are fewer than expected");
			return null;
		}
		else {
			logger.error("Unkown error");
			return null;
		}
		
		logger.info("The submission {} was assigned to work", docker);
		return docker;
	}

	/**
	 * This method aims to get information about a given docker status in order to determine whether the submission is completed or not.
	 * 
	 * @param submissionID: the parameter is a {@code String} which represents the submission ID whose status will be analyzed
	 * @return Boolean: if the submission status is either 200 or 202 the method returns true or false, respectively, otherwise returns null
	 * @throws IOException
	 */
	public int getStatus(String submissionID) throws IOException {

		String uploadUrl = this.url.concat("/result");

		uploadUrl = uploadUrl.concat("/"+this.email+"/"+submissionID+"/True"); 
		
		URL url = new URL(uploadUrl);

		HttpURLConnection conn = (HttpURLConnection)url.openConnection();

		int responseCode = conn.getResponseCode();

		if (responseCode==200) {
			return responseCode;
		}
		else if (responseCode==202) {
			return responseCode;
		}
		else if (responseCode==201) {
			return responseCode;
		}
		return responseCode;

	}

	/**
	 * Method that enables the download of the file related to the submission previously made by the user.
	 * @param submissionID: This parameter is a {@code String} which is the submission ID that the user has been used
	 * @param path: This parameter is a {@code String} with the output .tar.gz file
	 * @throws IOException 
	 */
	public boolean downloadFile(String submissionID, String path) throws IOException {

		try {
			String uploadUrl = this.url.concat("/download");

			uploadUrl = uploadUrl.concat("/"+this.email+"/"+ submissionID); 
			
			URL downloadUrl = new URL(uploadUrl);

			HttpURLConnection conn = (HttpURLConnection)downloadUrl.openConnection();

			int responseCode = conn.getResponseCode();

			logger.info("The response code was {}", Integer.toString(responseCode));
			
			if (responseCode==200) {

				File f = new File(path);

				FileUtils.copyURLToFile(downloadUrl, f);
				
				logger.info("The file was downloaded successfully.");
				return true;
			}
			else {
				logger.error("Error downloading the files.");
				return false;
			}


		} 
		catch (Exception e) {
			logger.error("Error downloading the files.");
			e.printStackTrace();
			return false;

		}
	}

	public File getModel() {
		return model;
	}

	public void setModel(File model) {
		this.model = model;
	}



}
