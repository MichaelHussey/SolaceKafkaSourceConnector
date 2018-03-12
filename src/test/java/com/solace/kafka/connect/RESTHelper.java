package com.solace.kafka.connect;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public class RESTHelper {

	/**
	 * @throws IOException 
	 * @throws MalformedURLException 
	 * @throws  
	 * 
	 */
	public static int doSolaceREST(Properties config, String destinationName, boolean isQueue) throws MalformedURLException, IOException {

		String baseUrl = config.getProperty("REST_URL");
		String destType = "TOPIC";
		if (isQueue)
			destType = "QUEUE";
		URL url = new URL(baseUrl+"/"+destType+"/"+destinationName);
	    Map<String,Object> params = new LinkedHashMap<String,Object>();
	    params.put("param1", "value1");
	    params.put("param2", "value2");

	    StringBuilder postData = new StringBuilder();
	    for (Map.Entry<String,Object> param : params.entrySet()) {
	        if (postData.length() != 0) postData.append('&');
	        postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
	        postData.append('=');
	        postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
	    }
	    String urlParameters = postData.toString();
	    URLConnection conn = url.openConnection();

	    conn.setDoOutput(true);
	    
	    // DataGo uses Basic Auth
	    String authString = config.get(SolaceConnectorConstants.SOLACE_USERNAME)+":"+config.get(SolaceConnectorConstants.SOLACE_PASSWORD);
	    String encodedAuth = Base64.getEncoder().encodeToString(authString.getBytes());
	    conn.setRequestProperty  ("Authorization", "Basic " + encodedAuth);
	    
	    OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());

	    writer.write("");
	    writer.write(urlParameters);
	    writer.flush();

	    String result = "";
	    String line;
	    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

	    while ((line = reader.readLine()) != null) {
	        result += line;
	    }
	    writer.close();
	    reader.close();
	    //System.err.println(result);
		return 0;
	}
	
	
	public static int getConnectorStatus(Properties config) {
		String baseUrl = config.getProperty("REST_URL");
		
		return 0;

	}
	
	

}
