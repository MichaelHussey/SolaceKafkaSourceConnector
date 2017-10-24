package com.solace.kafka.connect;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.common.config.ConfigException;
import org.junit.Test;

public class SolaceConfigDefTest {

	@Test
	public void testMissingMandatory() {
		Map<String, Object> propMap = new HashMap<String, Object>();
		
		try {
			Map<String, Object> parsedMap = SolaceConfigDef.defaultConfig().parse(propMap);
		} catch (ConfigException ce) {
			assertNotNull(ce);
			assertTrue(ce.getMessage().startsWith("Missing required configuration"));
		}
	}
	
	protected Properties readMinProperties()
	{
    	Properties prop = new Properties();
    	InputStream inputStream = getClass().getClassLoader().getResourceAsStream("unit_test.properties"); 
    	try {
    	    prop.load(inputStream);	
    	} catch (IOException e) {
    	    e.printStackTrace();
    	}
    	return prop;
	}

	@Test
	public void testDefaults() {
		Properties prop = readMinProperties();
		Map<String, Object> parsedMap = SolaceConfigDef.defaultConfig().parse(prop);
		
		Integer reconnectRetries = (Integer) parsedMap.get(SolaceConnectorConstants.SOLACE_RECONNECT_RETRIES);
		assertEquals(reconnectRetries.intValue(), SolaceConnectorConstants.DEFAULT_SOLACE_RECONNECT_RETRIES);
		
		Integer reconnectRetryWait = (Integer) parsedMap.get(SolaceConnectorConstants.SOLACE_RECONNECT_RETRY_WAIT);
		assertEquals(reconnectRetryWait.intValue(), SolaceConnectorConstants.DEFAULT_SOLACE_RECONNECT_RETRY_WAIT);
		
		Integer compressLevel = (Integer) parsedMap.get(SolaceConnectorConstants.SOLACE_COMPRESSION_LEVEL);
		assertEquals(compressLevel.intValue(), SolaceConnectorConstants.DEFAULT_SOLACE_COMPRESSION_LEVEL);
	}

	@Test
	public void testInvalidCompression() {
		Properties propMap = readMinProperties();
		propMap.put(SolaceConnectorConstants.SOLACE_COMPRESSION_LEVEL, 10);
		
		try {
			Map<String, Object> parsedMap = SolaceConfigDef.defaultConfig().parse(propMap);
		} catch (ConfigException ce) {
			ce.printStackTrace();
			assertNotNull(ce);
		}
		
	}
	@Test
	public void testHAOn() {
		Properties propMap = readMinProperties();
		propMap.put("tasks.max", 1);
		
		try {
			// when tasks=1 then we ignore HA queue
			Map<String, Object> parsedMap = SolaceConfigDef.defaultConfig().parse(propMap);
			
			String qName = (String) parsedMap.get(SolaceConnectorConstants.SOLACE_HA_QUEUE);
		} catch (ConfigException ce) {
			ce.printStackTrace();
		}
		try {
			// when tasks>1 then we require HA queue
			propMap.put("tasks.max", 2);
			Map<String, Object> parsedMap = SolaceConfigDef.defaultConfig().parse(propMap);
			
			String qName = (String) parsedMap.get(SolaceConnectorConstants.SOLACE_HA_QUEUE);
			assertNotNull(qName);
		} catch (ConfigException ce) {
			ce.printStackTrace();
		}
		
	}

}
