package com.solace.solft;

import com.solacesystems.jcsmp.*;
import com.solacesystems.jcsmp.Queue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Copied from https://github.com/koverton/solft
 *
 * This internal class provides the underlying Solace capabilities to implement application layer fault tolerant clustering.
 * Instances of this class are used by the {@link com.solacesystems.poc.FTMgr}. Applications should not need to
 * interact with this directly, they should merely instantiate one according to the available public constructors,
 * and pass the instance into the {@link com.solacesystems.poc.FTMgr#FTMgr(Properties)} constructor.
 */
class SolaceConnection {
    private static final Logger logger = LoggerFactory.getLogger(SolaceConnection.class);

    /**
     * Create a new {@link com.solacesystems.poc.SolaceConnection} wrapper around an existing Solace session instance.
     * @param sharedSession Shared Solace session to be used to join a FT-Cluster. This session is assumed to be already connected.
     * @throws JCSMPException In the event of any error validating the session's capabilities match those required by this object.
     */
    SolaceConnection(JCSMPSession sharedSession) throws JCSMPException {
        session = sharedSession;
        validateCapabilities();
    }

    /**
     * Create a new {@link com.solacesystems.poc.SolaceConnection} from a set of Solace session properties, and connect it to a Solace Message Bus.
     * @param connectionProperties A collection of Solace session properties to use in creating a new Solace session.
     * @throws JCSMPException In the event of any error creating the session, connecting to a Solace Message bus, or validating the session's capabilities match those required by this object.
     */
    SolaceConnection(Properties connectionProperties) throws JCSMPException {
        final JCSMPProperties solprops = new JCSMPProperties();
        for (String name : connectionProperties.stringPropertyNames()) {
            Object value = connectionProperties.getProperty(name);
            solprops.setProperty(name, value);
        }
        session = JCSMPFactory.onlyInstance().createSession(solprops);
        session.connect();
        validateCapabilities();
    }

    void stop() {
        session.closeSession();
    }

    /**
     * Validates that the current Solace session supports: Active Flow Indication, Subscribing to Guaranteed Messages, and Managing Solace Guaranteed Endpoints.
     * @throws JCSMPException In the event of any error validating the session's capabilities match those required by this object.
     */
    private void validateCapabilities() throws JCSMPException {
        boolean valid = (session.isCapable(CapabilityType.ACTIVE_FLOW_INDICATION)
                && session.isCapable(CapabilityType.SUB_FLOW_GUARANTEED)
                && session.isCapable(CapabilityType.ENDPOINT_MANAGEMENT));
        if (!valid)
            throw new JCSMPException("Session has insufficient capabilities to support application Fault Tolerance");
    }

    /**
     * Bind into an exclusive cluster; a leader is selected from all applications bound to a give cluster.
     * Whenever the Leader process unbinds for any reason a new Leader is elected from the rest of the
     * bound participants.
     * @param exclusiveClusterName Exclusive cluster for FT-selection.
     * @param listener Event listener to be invoked for any FT state event changes.
     * @throws JCSMPException In the event of any error joining the FT-cluster.
     */
    void bindExclusive(String exclusiveClusterName, final FTEventListener listener) throws JCSMPException {
        provisionExclusiveQueue(exclusiveClusterName);

        final ConsumerFlowProperties queueProps = new ConsumerFlowProperties();
        queueProps.setEndpoint(JCSMPFactory.onlyInstance().createQueue(exclusiveClusterName));
        queueProps.setActiveFlowIndication(true);
        session.createFlow(
                new XMLMessageListener() {
                    @Override
                    public void onReceive(BytesXMLMessage bytesXMLMessage) {
                        // Reserved for future use
                    }
                    @Override
                    public void onException(JCSMPException e) {
                    }
                },
                queueProps,
                null,
                new FlowEventHandler() {
                    @Override
                    public void handleEvent(Object o, FlowEventArgs args) {
                        if (args.getEvent().equals(FlowEvent.FLOW_ACTIVE)) {
                            listener.onActive(null);
                        }
                        else {
                            listener.onBackup();
                        }
                    }
                })
                .start();
    }

    void bindExclusiveWithState(final String exclusiveClusterName, String outputSubscription, final FTEventListener listener) throws JCSMPException {
        provisionExclusiveQueue(exclusiveClusterName);
        subscribeQueueToTopic(exclusiveClusterName, outputSubscription);

        final ConsumerFlowProperties queueProps = new ConsumerFlowProperties();
        queueProps.setEndpoint(JCSMPFactory.onlyInstance().createQueue(exclusiveClusterName));
        queueProps.setActiveFlowIndication(true);
        queueProps.setStartState(false);
        session.createFlow(
                null,
                queueProps,
                null,
                new FlowEventHandler() {
                    @Override
                    public void handleEvent(Object o, FlowEventArgs args) {
                        if (args.getEvent().equals(FlowEvent.FLOW_ACTIVE)) {
                            final BytesXMLMessage lastMsg = browseLastQueueValue(exclusiveClusterName);
                            listener.onActive(lastMsg);
                        }
                        else {
                            listener.onBackup();
                        }
                    }
                })
                .start();
    }

    /**
     * Create the named queue on the Solace message router, ignoring any errors if the queue already exists.
     * @param queueName Name of the Solace queue to provision on the Solace message router.
     * @throws JCSMPException In the event of any error provisioning the queue on the Solace message router.
     */
    private void provisionExclusiveQueue(String queueName) throws JCSMPException {
        final Queue queue = JCSMPFactory.onlyInstance().createQueue(queueName);
        final EndpointProperties endpointProps = new EndpointProperties();
        endpointProps.setPermission(EndpointProperties.PERMISSION_DELETE);
        endpointProps.setAccessType(EndpointProperties.ACCESSTYPE_EXCLUSIVE);
        endpointProps.setQuota(0);
        session.provision(queue, endpointProps, JCSMPSession.FLAG_IGNORE_ALREADY_EXISTS);
    }

    /**
     * Subscribes the named queue to the topic subscription string so that it will persist messages published to the Solace message router with topics matching that subscription string.
     * @param queueName Name of the queue to subscribe to the topic subscription string.
     * @param subscription Topic subscription string to be mapped to the named queue. This topic subscription string can include wildcards.
     * @throws JCSMPException In the event of any error subscribing the queue on the Solace message router to the provided topic subscription string.
     */
    private void subscribeQueueToTopic(String queueName, String subscription) {
        final Queue queue = JCSMPFactory.onlyInstance().createQueue(queueName);
        final Topic topic = JCSMPFactory.onlyInstance().createTopic(subscription);
        try {
            session.addSubscription(queue, topic, JCSMPSession.WAIT_FOR_CONFIRM);
        }
        catch(JCSMPException e) {
            // If the subscription was already there, no biggie just swallow it
            //e.printStackTrace();
            logger.warn("Error subscribing queue {} to topic {}: {}",  queueName, subscription, e.getMessage());
        }
    }

    /**
     * Attempts to return a value from the named queue; returns null if no message was found.
     * @param queueName Name of the queue to browse.
     * @return First message found on the queue or null if none is found.
     * @throws JCSMPException In the event of any error browsing the named queue.
     */
    private BytesXMLMessage browseLastQueueValue(String queueName) {
        try {
            final BrowserProperties properties = new BrowserProperties();
            properties.setEndpoint(JCSMPFactory.onlyInstance().createQueue(queueName));
            properties.setTransportWindowSize(1);
            properties.setWaitTimeout(1000);
            final Browser browser = session.createBrowser(properties);
            return browser.getNext();
        }
        catch (JCSMPException e) {
            e.printStackTrace();
        }
        return null;
    }

    final private JCSMPSession session;
}
