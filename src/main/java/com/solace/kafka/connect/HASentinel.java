package com.solace.kafka.connect;

import org.apache.kafka.connect.errors.ConnectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.ConsumerFlowProperties;
import com.solacesystems.jcsmp.DeliveryMode;
import com.solacesystems.jcsmp.EndpointProperties;
import com.solacesystems.jcsmp.FlowReceiver;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.JCSMPStreamingPublishEventHandler;
import com.solacesystems.jcsmp.Queue;
import com.solacesystems.jcsmp.XMLMessageListener;
import com.solacesystems.jcsmp.XMLMessageProducer;

public class HASentinel implements XMLMessageListener, JCSMPStreamingPublishEventHandler {
	
	private static final Logger log = LoggerFactory.getLogger(HASentinel.class);
	
	protected JCSMPSession session;
	
	protected String haQueueName;
	protected Queue haQueue;
	
	protected boolean isActiveMember = false;

	public boolean isActiveMember() {
		return isActiveMember;
	}
	private FlowReceiver consumer;

	private boolean isStopped = true;
	
	public HASentinel(JCSMPSession _session, String queueName){
		session = _session;
		haQueueName = queueName;
	}
	
	public void connect() throws JCSMPException {
		if (haQueueName == null) {
			throw new ConnectException("Cannot create HA Sentinel as no HA Queue Name defined.");
		}
		haQueue = connectHAQ();
		log.info("Provisioned HA Queue named %s on session %s", haQueueName, session.getSessionName());
	}

	protected Queue connectHAQ() throws JCSMPException {
        log.debug("Attempting to provision the queue '%s' on the appliance", haQueueName);
        
        final EndpointProperties endpointProps = new EndpointProperties();
        
        // set queue permissions to "consume" and access-type to "exclusive"
        endpointProps.setPermission(EndpointProperties.PERMISSION_CONSUME);
        endpointProps.setAccessType(EndpointProperties.ACCESSTYPE_EXCLUSIVE);
        
        // We want this Q to be a Last Message Queue.
        endpointProps.setQuota(0);
        
        // create the queue object locally
        Queue _queue = JCSMPFactory.onlyInstance().createQueue(haQueueName);
        // Actually provision it, and do not fail if it already exists
        session.provision(_queue, endpointProps, JCSMPSession.FLAG_IGNORE_ALREADY_EXISTS);
        
        return _queue;
	}
	
	protected void startListening() throws JCSMPException  {
        // Create a Flow be able to bind to and consume messages from the Queue.
        final ConsumerFlowProperties flow_prop = new ConsumerFlowProperties();
        flow_prop.setEndpoint(haQueue);
        flow_prop.setAckMode(JCSMPProperties.SUPPORTED_MESSAGE_ACK_CLIENT);

        EndpointProperties endpoint_props = new EndpointProperties();
        endpoint_props.setAccessType(EndpointProperties.ACCESSTYPE_EXCLUSIVE);

        consumer = session.createFlow(this, flow_prop, endpoint_props);
        
        consumer.start();  
	}
	
	protected void sendHeartbeat() throws JCSMPException {
		XMLMessageProducer prod = session.getMessageProducer(this);
		BytesXMLMessage hbMessage = JCSMPFactory.onlyInstance().createMessage(BytesXMLMessage.class);
		hbMessage.setDeliveryMode(DeliveryMode.PERSISTENT);
		prod.send(hbMessage, haQueue);
		
		log.info("Sent HA heartbeat");
	}
	
	

	@Override
	public void onException(JCSMPException arg0) {
		log.warn(arg0.getMessage());
	}

	@Override
	public void onReceive(BytesXMLMessage arg0) {
		
		arg0.ackMessage();
		isActiveMember = true;
		
	}

	@Override
	public void handleError(String arg0, JCSMPException arg1, long arg2) {
		log.warn("Error received when sending sentinel heartbeat %s %i %s",arg0, arg2, arg1.getMessage());
		
	}

	@Override
	public void responseReceived(String arg0) {
		log.info("Response when sending Sentinel Heartbeat: %s", arg0);
	}

	public void start()  throws JCSMPException {
		sendHeartbeat();
		if (consumer == null)
			startListening();
		else
			consumer.start();
		isStopped = false;
	}
	
	public void stop()  throws JCSMPException {
		consumer.stop();
		
		// Send a HB so that the next member of the group can become active
		sendHeartbeat();
		isActiveMember = false;
		isStopped = true;
	}
	public void close()  throws JCSMPException {
		if (!isStopped )
			stop();
		consumer.close();
		consumer = null;
	}

}
