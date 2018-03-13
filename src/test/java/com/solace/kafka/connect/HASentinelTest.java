package com.solace.kafka.connect;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import com.solacesystems.jcsmp.Context;
import com.solacesystems.jcsmp.ContextProperties;
import com.solacesystems.jcsmp.InvalidPropertiesException;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.JCSMPSession;

public class HASentinelTest {

	Properties config;
	
	@Before
	public void setup() throws Exception {
		Properties prop = new Properties();
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream("unit_test.properties"); 
		try {
			prop.load(inputStream);	
		} catch (IOException e) {
			e.printStackTrace();
		}
		config = prop;
	}
	/**
	 * It's a good idea to use Solace Cloud for the messaging server during the unit tests.
	 * Register on http://cloud.solace.com/, or download a VMR from http://dev.solace.com/downloads/
	 * @throws Exception 
	 *  
	 */
	public JCSMPSession getSession(String name) throws Exception {

		final JCSMPProperties properties = new JCSMPProperties();
		properties.setProperty(JCSMPProperties.HOST, config.getProperty(SolaceConnectorConstants.SOLACE_URL));  // msg-backbone ip:port
		properties.setProperty(JCSMPProperties.VPN_NAME, config.getProperty(SolaceConnectorConstants.SOLACE_VPN)); // message-vpn
		String password = config.getProperty(SolaceConnectorConstants.SOLACE_PASSWORD);
		if(password != null && password.length()>0)
			properties.setProperty(JCSMPProperties.PASSWORD, password);
		properties.setProperty(JCSMPProperties.USERNAME, config.getProperty(SolaceConnectorConstants.SOLACE_USERNAME)); // client-username (assumes no password)

		// Explicitly create a context.
		ContextProperties contextProperties = new ContextProperties();
		contextProperties.setName(name);
		Context context = JCSMPFactory.onlyInstance().createContext(contextProperties);
		JCSMPSession session = JCSMPFactory.onlyInstance().createSession(properties, context);
		session.connect();
		
		return session;
	}

	/**
	 * Check that a single HASentinel is initially not active and becomes active after the start method is called
	 * @throws Exception 
	 */
	@Test
	public void test() throws Exception {
		
		String queueName = config.getProperty(SolaceConnectorConstants.SOLACE_HA_QUEUE);
		HASentinel sen1 = new HASentinel("one", getSession("one"), queueName);
		assertFalse(sen1.isActiveMember());

		sen1.start();
		
		try { Thread.sleep(1000); } catch(InterruptedException e) {}
		
		assertTrue("one should be active", sen1.isActiveMember());

		try {
			sen1.stop();
			try { Thread.sleep(1000); } catch(InterruptedException e) {}
			
			assertTrue(!sen1.isActiveMember());
		} catch (JCSMPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Start 2 instances of the sentinel. The first one to start should become active, when it stops the next becomes active.
	 * @throws Exception 
	 */
	@Test
	public void testMulti() throws Exception {

		String queueName = config.getProperty(SolaceConnectorConstants.SOLACE_HA_QUEUE);
		JCSMPSession session1 = getSession("oneA");
		JCSMPSession session2 = getSession("two");
		HASentinel sen1 = new HASentinel("oneA", session1, queueName);
		HASentinel sen2 = new HASentinel("two", session2, queueName);

		try {
			sen1.start();

			sen2.start();

			Thread.sleep(1000);

		} catch (JCSMPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		assertTrue(sen1.isActiveMember() != sen2.isActiveMember());

		try {
			sen1.stop();
			
			try { Thread.sleep(1000); } catch(InterruptedException e) {}
			
			assertTrue(sen2.isActiveMember());
			sen2.stop();
			try { Thread.sleep(1000); } catch(InterruptedException e) {}
			assertFalse(sen2.isActiveMember());
			
		} catch (JCSMPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

