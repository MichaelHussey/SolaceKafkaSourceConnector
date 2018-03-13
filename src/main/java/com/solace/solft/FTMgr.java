package com.solace.solft;

import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPSession;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Copied from https://github.com/koverton/solft

 * <p>Instances of this class bind to a FT-cluster in a Solace Message Broker and manage FT-cluster membership
 * events for that binding. To implement Fault Tolerance within your application, you would typically have
 * several redundant instances of your application binding to the same FT-cluster, and each behave appropriately
 * when the FTMgr instance signals your application to either become Active or become Backup.</p>
 *
 * <p>There are two ways to run this: the first is simply using a shared lock mechanism to elect the current
 * Leader and transition when that Leader gives up the lock. The second does that at well, but when transitioning
 * from Backup to Active it also gathers the last output from the previous Leader so that the current Leader
 * can attempt to synchronize itself to that output.</p>
 *
 * <p>For details see {@link com.solacesystems.poc}. </p>
 */
public class FTMgr {

    final private SolaceConnection connection;
    private FTEventListener listener; // there can only be one!

    /**
     * Creates a new Solace session connected to a Solace Message Bus for use in joining
     * a FT-Cluster for Leader Election and role events.
     * @param solaceConnectionProperties Properties file containing Solace session properties.
     * @throws JCSMPException In the event of any error creating the Solace session and connecting it to a Solace Message Broker.
     */
    public FTMgr(Properties solaceConnectionProperties) throws JCSMPException {
        this.connection = new SolaceConnection(solaceConnectionProperties);
    }

    /**
     * Uses an existing Solace session connected to a Solace Message Bus for use in joining
     * a FT-Cluster for Leader Election and role events.
     * @param solaceSession An existing Solace session.
     * @throws JCSMPException In the event of any error creating the Solace session and connecting it to a Solace Message Broker.
     */
    public FTMgr(JCSMPSession solaceSession) throws JCSMPException {
        this.connection = new SolaceConnection(solaceSession);
    }

    /**
     * Bind to a FT cluster as a cluster member for leader election, listening for FT state change events.
     * @param ftClusterName Exclusive cluster for FT-selection.
     * @param listener Event listener to be invoked for any FT state event changes.
     * @throws JCSMPException In the event of any failures in connecting to Solace or binding to the cluster.
     */
    public void start(String ftClusterName, final FTEventListener listener) throws JCSMPException {
        this.listener = listener;
        // Everyone starts out as slave, Solace doesn't event for listeners initially bound as backup
        this.listener.onBackup();
        connection.bindExclusive(
                ftClusterName,
                new FTEventListener() {
                    @Override
                    public void onActive(BytesXMLMessage msg) {
                        listener.onActive(null);
                    }
                    @Override
                    public void onBackup() {
                        listener.onBackup();
                    }
                }
        );
    }

    /**
     * Bind to a FT cluster as a cluster member for leader election, listening for FT state change events and
     * the latest output from the current Active member of the FT-Cluster.
     * @param ftClusterName Exclusive cluster for FT-selection.
     * @param outputSubscription Subscription to topic matching output from the Active FT-Cluster member.
     * @param listener Event listener to be invoked for any FT state event changes.
     * @throws JCSMPException In the event of any failures in connecting to Solace or binding to the cluster.
     */
    public void startStateful(String ftClusterName, String outputSubscription, final FTEventListener listener) throws JCSMPException {
        // Everyone starts out as slave, Solace doesn't event for listeners initially bound as backup
        listener.onBackup();
        connection.bindExclusiveWithState(
                ftClusterName,
                outputSubscription,
                new FTEventListener() {
                    @Override
                    public void onActive(BytesXMLMessage msg) {
                        listener.onActive(msg);
                    }
                    @Override
                    public void onBackup() {
                        listener.onBackup();
                    }
                }
        );
    }

    /**
     * Terminate a binding to a FT-Cluster. After calling stop() this instance may no longer be used.
     */
    public void stop() {
        connection.stop();
        this.listener.onBackup();
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("    USAGE: <path/to/solace/conn.properties> <solace-exclusive-queue> {optional: output-subscription}");
            System.out.println("");
            System.out.println("");
            System.exit(1);
        }

        Properties props = readPropsFile(args[0]);
        String clusterName = args[1];
        String outputSubscription = null;
        if (args.length == 3)
            outputSubscription = args[2];

        try {
            final FTEventListener listener = new FTEventListener() {
                @Override
                public void onActive(BytesXMLMessage msg) {
                    System.out.println("BECOMING MASTER: " + (msg==null ? "(null)" : msg.dump()));
                }
                @Override
                public void onBackup() {
                    System.out.println("BECOMING BACKUP");
                }
            };

            final FTMgr ftMgr = new FTMgr(props);
            if (outputSubscription != null)
                ftMgr.startStateful(clusterName, outputSubscription, listener);
            else
                ftMgr.start(clusterName, listener);

            while(true) {
                Thread.sleep(Long.MAX_VALUE);
            }
        }
        catch(JCSMPException e) {
            e.printStackTrace();
        }
        catch(InterruptedException iex) {
            iex.printStackTrace();
        }
    }

    static Properties readPropsFile(String name) {
        Properties props = new Properties();
        InputStream input = null;
        try {
            input = new FileInputStream(name);
            props.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return props;
    }
}
