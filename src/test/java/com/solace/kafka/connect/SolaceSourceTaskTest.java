/**
 * 
 */
package com.solace.kafka.connect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.json.JsonConverter;
import org.junit.Before;
import org.junit.Test;

/**
 * @author michussey
 *
 */
public class SolaceSourceTaskTest implements Runnable {
	
	Map<String, String> config;
	private SolaceSourceTask task;

	/**
	 * It's a good idea to use DataGo service for the messaging server during the unit tests.
	 * Register on http://datago.io/, or download a VMR from http://dev.solace.com/downloads/
	 *  
	 */
    @Before
    public void setup() {
    	config = new HashMap<String, String>();
    	config.put(SolaceConnectorConstants.SOLACE_URL, "mr-6727eibfb.datago_ecb2-messaging.datago.io:21184");
    	config.put(SolaceConnectorConstants.SOLACE_VPN, "msgvpn-6727f5gdp");
    	config.put(SolaceConnectorConstants.SOLACE_USERNAME, "datago-client-username");
    	config.put(SolaceConnectorConstants.SOLACE_PASSWORD, "u7n7f2ddt3hia005fo29uli64f");
    	config.put(SolaceConnectorConstants.SOLACE_TOPIC, "test/>");
    	config.put(SolaceConnectorConstants.KAFKA_TOPIC, "solace_topic");
    	config.put(SolaceConnectorConstants.LONG_POLL_INTERVAL, "100");
    	config.put(SolaceConnectorConstants.SHORT_POLL_INTERVAL, "1");
    	config.put(SolaceConnectorConstants.POLL_BATCH_SIZE, "10");

    	// Only used in testing to send messages to Solace
    	config.put("REST_URL", "http://mr-6727eibfb.datago_ecb2-messaging.datago.io:21194");
    }
    private static final String SCHEMAS_ENABLE_CONFIG = "schemas.enable";
    private static final String SCHEMAS_CACHE_SIZE_CONFIG = "schemas.cache.size";
	@Test
	public void test() {
		SolaceSourceTask task = new SolaceSourceTask();
		List<SourceRecord> records = new ArrayList<SourceRecord>();
		task.start(config);
		
		try {
			
			doSolaceREST();
			
			records = task.poll();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertEquals(records.size(), 1);
		
		// The Kafka data structure has a handy method to check that all mandatory fields are filled 
		((Struct)records.get(0).value()).validate();
		
		// Make sure that at least the OOTB JSON converter can handle the record we create
		JsonConverter converter = new JsonConverter();
		Map<String, String> cc = new HashMap<String, String>();
		cc.put(SCHEMAS_ENABLE_CONFIG, "true");
		cc.put(SCHEMAS_CACHE_SIZE_CONFIG,"10");
		converter.configure(cc, false);
		
		byte[] jsonData = converter.fromConnectData(config.get(SolaceConnectorConstants.KAFKA_TOPIC), records.get(0).valueSchema(), records.get(0).value());

		assertTrue(jsonData.length > 0);
		
		System.err.println("JSON data: "+new String(jsonData));
	}
	
	/**
	 * @throws IOException 
	 * @throws MalformedURLException 
	 * @throws  
	 * 
	 */
	public int doSolaceREST() throws MalformedURLException, IOException {
		URL url = new URL(config.get("REST_URL")+"/TOPIC/test/foo");
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
	    System.out.println(result);
		return 0;
	}
	
	/** 
	 * To make debugging easier
	 * @param args
	 */
	public static void main(String[] args)
	{
		SolaceSourceTask task = new SolaceSourceTask();
		SolaceSourceTaskTest tester = new SolaceSourceTaskTest();
		tester.setup();
		tester.task = task;
		task.start(tester.config);
		
		(new Thread(tester)).start();
	}
	
	@Override
	public void run() {
		while(true)
		{
			try {
				this.task.poll();
				//Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	
	}


}
