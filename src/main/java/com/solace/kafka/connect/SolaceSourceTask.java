package com.solace.kafka.connect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.kafka.common.utils.AppInfoParser;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.Topic;
import com.solacesystems.jcsmp.XMLMessageConsumer;
import com.solacesystems.jcsmp.XMLMessageListener;
import com.solacesystems.jcsmp.BytesXMLMessage;

public class SolaceSourceTask extends SourceTask {

	private static final Logger log = LoggerFactory.getLogger(SolaceSourceTask.class);

	protected JCSMPSession session;
	protected Topic topic;
	protected XMLMessageConsumer consumer;

	protected String smfHost;
	protected String msgVpnName;
	protected String clientUsername;
	protected String clientPassword;
	protected String solaceTopicName;
	protected String kafkaTopicName;
	protected int longPollInterval = 1000;
	protected int shortPollInterval = 10;
	protected int kafkaBufferSize = 100;
	
	protected SolaceConverter converter;
	
	@Override
	public String version() {
        return AppInfoParser.getVersion();
	}

	/**
	 * This is where the main work is done. Grab a bunch of messages from the Solace topic and put in 
	 * a list which will be consumed by Kafka.
	 * 
	 * Uses the Solace JCSMP API in synchronous mode with a combination of short and long polling. 
	 *  - When no messages are available this method blocks for the longPollInterval.
	 *  - When messages are available we try to assemble kafkaBufferSize records together to pass to Kafka, 
	 * allowing max shortPollInterval between consecutive messages.
	 */
	@Override
	public List<SourceRecord> poll() throws InterruptedException {
		System.err.println("in poll()");
		ArrayList<SourceRecord> records = new ArrayList<SourceRecord>();
		
		try {
			BytesXMLMessage msg = consumer.receive(longPollInterval);
			if (msg == null)
				return records;

			records.add(converter.convertMessage(msg));
			
			//Now fast poll as long as we keep getting messages
			int i=0;
			while(i < kafkaBufferSize-1) {
				i++;
				msg = consumer.receive(shortPollInterval);
				if (msg == null) break;
				records.add(converter.convertMessage(msg));
				
			}
		} catch (JCSMPException e) {
			e.printStackTrace();
		}
		log.info("poll() found "+records.size()+" records");
		return records;
	}

	@Override
	public void start(Map<String, String> propMap) {
		// Pull the parameters needed to connect to the Message Router
		smfHost = propMap.get(SolaceConnectorConstants.SOLACE_URL);
		msgVpnName  = propMap.get(SolaceConnectorConstants.SOLACE_VPN);
		clientUsername = propMap.get(SolaceConnectorConstants.SOLACE_USERNAME);
		clientPassword = propMap.get(SolaceConnectorConstants.SOLACE_PASSWORD);
		kafkaTopicName = propMap.get(SolaceConnectorConstants.KAFKA_TOPIC);
		solaceTopicName = propMap.get(SolaceConnectorConstants.SOLACE_TOPIC);
		longPollInterval = Integer.parseInt(propMap.get(SolaceConnectorConstants.LONG_POLL_INTERVAL));
		shortPollInterval = Integer.parseInt(propMap.get(SolaceConnectorConstants.SHORT_POLL_INTERVAL));
		kafkaBufferSize = Integer.parseInt(propMap.get(SolaceConnectorConstants.POLL_BATCH_SIZE));

		// TODO - validate the config here, using SolaceConfigDef?
		if (kafkaTopicName == null || kafkaTopicName.isEmpty())
            throw new ConnectException("SolaceSourceConnector missing required parameter '"+SolaceConnectorConstants.KAFKA_TOPIC+"'");

		log.info("Solace Kafka Source connector started. Will connect to router at url:"
				+smfHost+" vpn:"+msgVpnName+" user:"+clientUsername+" pass:"+clientPassword
				+" Solace topic:"+solaceTopicName+" Kafka topic:"+kafkaTopicName);
		
		// Now start the subscribers
		try {
			connect();
		} catch (JCSMPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
            throw new ConnectException("SolaceSourceTask failed to connect.", e);
		}
		
		// Consume messages synchronously
		converter = new SolaceConverter(this);
		try {
			consumer = session.getMessageConsumer((XMLMessageListener)null);
	        session.addSubscription(topic);
	        consumer.start();
		} catch (JCSMPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
            throw new ConnectException("SolaceSourceTask failed to start listener.", e);
		}
	}

	@Override
	public void stop() {
		consumer.close();
		session.closeSession();
	}

	public void connect() throws JCSMPException {
        final JCSMPProperties properties = new JCSMPProperties();
        properties.setProperty(JCSMPProperties.HOST, smfHost);
        properties.setProperty(JCSMPProperties.VPN_NAME, msgVpnName);
        properties.setProperty(JCSMPProperties.USERNAME, clientUsername);
        if (clientPassword != null)
        {
            properties.setProperty(JCSMPProperties.PASSWORD, clientPassword);
        }
        log.info("Connecting to Solace Message Router...");
        topic = JCSMPFactory.onlyInstance().createTopic(solaceTopicName);
		session = JCSMPFactory.onlyInstance().createSession(properties);
        session.connect();		
        log.info("Connection succeeded!");
		
	}
}
