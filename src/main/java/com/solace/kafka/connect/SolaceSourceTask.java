package com.solace.kafka.connect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Type;
import org.apache.kafka.common.utils.AppInfoParser;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.apache.kafka.common.config.types.Password;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.JCSMPReconnectEventHandler;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.Topic;
import com.solacesystems.jcsmp.XMLMessageConsumer;
import com.solacesystems.jcsmp.XMLMessageListener;
import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.JCSMPChannelProperties;


public class SolaceSourceTask extends SourceTask {

	private static final Logger log = LoggerFactory.getLogger(SolaceSourceTask.class);

	protected JCSMPSession session;
	protected Topic topic;
	protected XMLMessageConsumer consumer;

	protected String smfHost;
	protected String msgVpnName;
	protected String clientUsername;
	protected Password clientPassword;
	protected String solaceTopicName;
	protected String kafkaTopicName;
	protected int longPollInterval = SolaceConnectorConstants.DEFAULT_LONG_POLL_INTERVAL;
	protected int shortPollInterval = SolaceConnectorConstants.DEFAULT_SHORT_POLL_INTERVAL;
	protected int kafkaBufferSize = SolaceConnectorConstants.DEFAULT_POLL_BATCH_SIZE;
	
	protected SolaceConverter converter;

	protected int reconnectRetries;

	protected int connectTimeoutInMillis;

	protected int connectRetriesPerHost;

	protected int keepAliveIntervalInMillis;

	protected int reconnectRetryWaitInMillis;
	
	protected int compressionLevel;
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

		setParameters(propMap);

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
	
	protected void setParameters(Map<String, String> propMap)
	{
		// Pull the parameters needed to connect to the Message Router
		//Map<String, Object> parsedMap = SolaceConfigDef.defaultConfig().parse(propMap);
		SolaceConfigDef conf = new SolaceConfigDef(SolaceConfigDef.defaultConfig(), propMap);
		
		smfHost = conf.getString(SolaceConnectorConstants.SOLACE_URL);
		msgVpnName  = conf.getString(SolaceConnectorConstants.SOLACE_VPN);
		clientUsername = conf.getString(SolaceConnectorConstants.SOLACE_USERNAME);
		clientPassword = conf.getPassword(SolaceConnectorConstants.SOLACE_PASSWORD);
		kafkaTopicName = conf.getString(SolaceConnectorConstants.KAFKA_TOPIC);
		solaceTopicName = conf.getString(SolaceConnectorConstants.SOLACE_TOPIC);
		longPollInterval = conf.getInt(SolaceConnectorConstants.LONG_POLL_INTERVAL);
		shortPollInterval = conf.getInt(SolaceConnectorConstants.SHORT_POLL_INTERVAL);
		kafkaBufferSize = conf.getInt(SolaceConnectorConstants.POLL_BATCH_SIZE);
		reconnectRetries =  conf.getInt(SolaceConnectorConstants.SOLACE_RECONNECT_RETRIES);
		reconnectRetryWaitInMillis = conf.getInt(SolaceConnectorConstants.SOLACE_RECONNECT_RETRY_WAIT);
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
            properties.setProperty(JCSMPProperties.PASSWORD, clientPassword.value());
        }
        properties.setProperty(JCSMPProperties.APPLICATION_DESCRIPTION, 
        		SolaceConnectorConstants.CONNECTOR_NAME+" Version "+SolaceConnectorConstants.CONNECTOR_VERSION);
        
        // Settings for automatic reconnection to Solace Router
        JCSMPChannelProperties channelProps = (JCSMPChannelProperties) properties.getProperty(JCSMPProperties.CLIENT_CHANNEL_PROPERTIES);
        channelProps.setReconnectRetries(reconnectRetries);
        channelProps.setReconnectRetryWaitInMillis(reconnectRetryWaitInMillis);
        channelProps.setConnectTimeoutInMillis(connectTimeoutInMillis);
        channelProps.setConnectRetriesPerHost(connectRetriesPerHost);
        channelProps.setKeepAliveIntervalInMillis(keepAliveIntervalInMillis);
        
        properties.setProperty(JCSMPProperties.CLIENT_CHANNEL_PROPERTIES, channelProps);
        
        
        log.info("Connecting to Solace Message Router...");
        topic = JCSMPFactory.onlyInstance().createTopic(solaceTopicName);
		session = JCSMPFactory.onlyInstance().createSession(properties);
        session.connect();		
        log.info("Connection succeeded!");
		
	}	
}
