package com.solace.kafka.connect;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import com.solacesystems.jcsmp.InvalidPropertiesException;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.JCSMPSession;

public class HASentinelTest {

	Properties config;
	JCSMPSession session;

	/**
	 * It's a good idea to use DataGo service for the messaging server during the unit tests.
	 * Register on http://datago.io/, or download a VMR from http://dev.solace.com/downloads/
	 * @throws Exception 
	 *  
	 */
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
		final JCSMPProperties properties = new JCSMPProperties();
		properties.setProperty(JCSMPProperties.HOST, prop.getProperty(SolaceConnectorConstants.SOLACE_URL));  // msg-backbone ip:port
		properties.setProperty(JCSMPProperties.VPN_NAME, prop.getProperty(SolaceConnectorConstants.SOLACE_VPN)); // message-vpn
		String password = prop.getProperty(SolaceConnectorConstants.SOLACE_PASSWORD);
		if(password != null && password.length()>0)
			properties.setProperty(JCSMPProperties.PASSWORD, password);
		properties.setProperty(JCSMPProperties.USERNAME, prop.getProperty(SolaceConnectorConstants.SOLACE_USERNAME)); // client-username (assumes no password)

		session = JCSMPFactory.onlyInstance().createSession(properties);
		session.connect();
	}

	/**
	 * Check that a single HASentinel is initially not active and becomes active after the start method is called
	 */
	@Test
	public void test() {

		String queueName = config.getProperty(SolaceConnectorConstants.SOLACE_HA_QUEUE);
		HASentinel sen1 = new HASentinel(session, queueName);

		try {
			sen1.connect();
			assertTrue(!sen1.isActiveMember);
			sen1.start();

			Thread.sleep(2000);
		} catch (JCSMPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertTrue(sen1.isActiveMember);

		try {
			sen1.stop();
			assertTrue(!sen1.isActiveMember);

			sen1.start();

			Thread.sleep(2000);
			assertTrue(sen1.isActiveMember);

			sen1.close();
		} catch (JCSMPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Start 2 instances of the sentinel. The first one to start should become active, when it stops the next becomes active.
	 */
	@Test
	public void testMulti() {

		String queueName = config.getProperty(SolaceConnectorConstants.SOLACE_HA_QUEUE);
		HASentinel sen1 = new HASentinel(session, queueName);
		HASentinel sen2 = new HASentinel(session, queueName);

		try {
			sen1.connect();
			sen1.start();

			Thread.sleep(2000);

			sen2.connect();
			sen2.start();

			Thread.sleep(2000);

		} catch (JCSMPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		assertTrue(sen1.isActiveMember);
		assertTrue(!sen2.isActiveMember);

		try {
			sen1.stop();
			sen1.close();
			
			Thread.sleep(2000);
			assertTrue(sen2.isActiveMember);
			sen2.stop();
			sen2.close();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JCSMPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

