/**
 * 
 */
package com.solace.kafka.connect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
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
import java.util.Properties;

import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.json.JsonConverter;
import org.apache.kafka.connect.source.SourceRecord;
import org.junit.Before;
import org.junit.Test;

/**
 * Test the Solace side of the Connector (doesn't send any data to Kafka).
 * @author michussey
 *
 */
public class SolaceSourceTaskTest implements Runnable {
	
	Properties config;
	private SolaceSourceTask task;

	/**
	 * It's a good idea to use a Solace Cloud service for the messaging server during the unit tests.
	 * Register on http://cloud.solace.com/, or download a VMR from http://dev.solace.com/downloads/
	 *  
	 */
    @Before
    public void setup() {
    	
    	Properties prop = new Properties();
    	InputStream inputStream = getClass().getClassLoader().getResourceAsStream("unit_test.properties"); 
    	try {
    	    prop.load(inputStream);	
    	} catch (IOException e) {
    	    e.printStackTrace();
    	}
    	config = prop;
    	/**
    	config = new HashMap<String, String>();
    	config.put(SolaceConnectorConstants.SOLACE_URL, prop.getProperty(SolaceConnectorConstants.SOLACE_URL));
    	config.put(SolaceConnectorConstants.SOLACE_VPN, prop.getProperty(SolaceConnectorConstants.SOLACE_VPN));
    	config.put(SolaceConnectorConstants.SOLACE_USERNAME, prop.getProperty(SolaceConnectorConstants.SOLACE_USERNAME));
    	config.put(SolaceConnectorConstants.SOLACE_PASSWORD, prop.getProperty(SolaceConnectorConstants.SOLACE_PASSWORD));
    	config.put(SolaceConnectorConstants.SOLACE_TOPIC, prop.getProperty(SolaceConnectorConstants.SOLACE_TOPIC));
    	config.put(SolaceConnectorConstants.KAFKA_TOPIC, "solace_topic");
    	config.put(SolaceConnectorConstants.LONG_POLL_INTERVAL, "100");
    	config.put(SolaceConnectorConstants.SHORT_POLL_INTERVAL, "1");
    	config.put(SolaceConnectorConstants.POLL_BATCH_SIZE, "10");

    	// Only used in testing to send messages to Solace
    	config.put("REST_URL", prop.getProperty("REST_URL"));
    	*/
    }
    private static final String SCHEMAS_ENABLE_CONFIG = "schemas.enable";
    private static final String SCHEMAS_CACHE_SIZE_CONFIG = "schemas.cache.size";
    
	@SuppressWarnings("unchecked")
	@Test
	public void test() {
		SolaceSourceTask task = new SolaceSourceTask();
		List<SourceRecord> records = new ArrayList<SourceRecord>();
		task.start((Map)config);
		
		try {
			
			RESTHelper.doSolaceREST(config, "test/foo", false);
			
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
		
		byte[] jsonData = converter.fromConnectData((String)config.get(SolaceConnectorConstants.KAFKA_TOPIC), records.get(0).valueSchema(), records.get(0).value());

		assertTrue(jsonData.length > 0);
		
		System.err.println("JSON data: "+new String(jsonData));
		
		task.stop();
	}
	
	/**
	 * Check that the optional properties defaults are being correctly picked up even though they aren't
	 * set in the properties file
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testDefaultProperties() {	
		SolaceSourceTask task = new SolaceSourceTask();
		task.setParameters((Map)config);
		
		assertEquals(task.reconnectRetries, SolaceConnectorConstants.DEFAULT_SOLACE_RECONNECT_RETRIES);
		assertEquals(task.reconnectRetryWaitInMillis, SolaceConnectorConstants.DEFAULT_SOLACE_RECONNECT_RETRY_WAIT);
		
		task.stop();
	}

	
	
	/** 
	 * To make debugging easier
	 * @param args
	 */
	@SuppressWarnings("unchecked")
	public static void main(String[] args)
	{
		SolaceSourceTask task = new SolaceSourceTask();
		SolaceSourceTaskTest tester = new SolaceSourceTaskTest();
		tester.setup();
		tester.task = task;
		task.start((Map)tester.config);
		
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
