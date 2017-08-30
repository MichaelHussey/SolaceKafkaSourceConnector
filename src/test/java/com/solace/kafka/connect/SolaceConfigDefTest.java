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

	@Test
	public void testDefaults() {
    	Properties prop = new Properties();
    	InputStream inputStream = getClass().getClassLoader().getResourceAsStream("unit_test.properties"); 
    	try {
    	    prop.load(inputStream);	
    	} catch (IOException e) {
    	    e.printStackTrace();
    	}
		
		Map<String, Object> parsedMap = SolaceConfigDef.defaultConfig().parse(prop);
		
		Integer reconnectRetries = (Integer) parsedMap.get(SolaceConnectorConstants.SOLACE_RECONNECT_RETRIES);
		assertEquals(reconnectRetries.intValue(), SolaceConnectorConstants.DEFAULT_SOLACE_RECONNECT_RETRIES);
		
		Integer reconnectRetryWait = (Integer) parsedMap.get(SolaceConnectorConstants.SOLACE_RECONNECT_RETRY_WAIT);
		assertEquals(reconnectRetryWait.intValue(), SolaceConnectorConstants.DEFAULT_SOLACE_RECONNECT_RETRY_WAIT);
		
	}

}
