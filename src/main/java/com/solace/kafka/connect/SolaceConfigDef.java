package com.solace.kafka.connect;

import java.util.Map;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Range;
import org.apache.kafka.common.config.ConfigDef.Type;
import org.apache.kafka.common.config.ConfigDef.Width;

public class SolaceConfigDef extends AbstractConfig
{

	public static ConfigDef defaultConfig()
	{
		String SOLACE_GROUP = "Solace Connection Parameters";
		String SOLACE_RECONNECT_GROUP = "Solace Re-Connection Parameters";
		String INTERNAL_GROUP = "Connector Internal Behaviour";
		ConfigDef defs = new ConfigDef()
				// Connection parameters
				.define(SolaceConnectorConstants.SOLACE_VPN, //name
						Type.STRING, //type
						"default", //default value
						ConfigDef.Importance.MEDIUM, // importance
						"Message VPN name", //documentation
						SOLACE_GROUP,//group
						2, //order in group
						Width.LONG, //width 
						SolaceConnectorConstants.SOLACE_VPN //display name
						)
				.define(SolaceConnectorConstants.SOLACE_URL, 
						Type.STRING,   
						"localhost:55555",
						ConfigDef.Importance.HIGH,
						"Host and if necessary port number of the Solace Message Router, eg 'localhost:55555'",
						SOLACE_GROUP, 
						1,
						Width.LONG,
						null 
						)
				.define(SolaceConnectorConstants.SOLACE_USERNAME, 
						Type.STRING, 
						"default",
						ConfigDef.Importance.MEDIUM, 
						"Client Username",
						SOLACE_GROUP, 
						3,
						Width.LONG,
						null
						)
				.define(SolaceConnectorConstants.SOLACE_PASSWORD, 
						Type.PASSWORD, 
						null,
						ConfigDef.Importance.MEDIUM,
						"Client Password, optional",
						SOLACE_GROUP, 
						3,
						Width.LONG,
						null
						)
				.define(SolaceConnectorConstants.SOLACE_TOPIC, 
						Type.STRING, 
						"default", 
						ConfigDef.Importance.HIGH, 
						"List of topics that are applied as subscriptions, comma separated",
						SOLACE_GROUP, 
						4,
						Width.LONG,
						null
						)
				.define(SolaceConnectorConstants.SOLACE_HA_QUEUE, 
						Type.STRING, 
						null, 
						ConfigDef.Importance.MEDIUM, 
						"Name of Queue to use as High-Availability sentinel",
						SOLACE_GROUP, 
						4,
						Width.LONG,
						null
						)
		// Reconnect related parameters
				.define(SolaceConnectorConstants.SOLACE_RECONNECT_RETRIES, 
						Type.INT, 
						SolaceConnectorConstants.DEFAULT_SOLACE_RECONNECT_RETRIES, 
						ConfigDef.Importance.MEDIUM, 
						"The number of times to attempt to reconnect to the Solace "
							+ "appliance (or list of appliances) after an initial connected session goes down.",
						SOLACE_RECONNECT_GROUP, 
						1,
						Width.LONG,
						null
						)
				.define(SolaceConnectorConstants.SOLACE_RECONNECT_RETRY_WAIT, 
						Type.INT, 
						SolaceConnectorConstants.DEFAULT_SOLACE_RECONNECT_RETRY_WAIT, 
						ConfigDef.Importance.MEDIUM, 
						"How much time in milliseconds to wait between each attempt to connect or reconnect to a host.",
						SOLACE_RECONNECT_GROUP, 
						2,
						Width.LONG,
						null
						)
				.define(SolaceConnectorConstants.SOLACE_COMPRESSION_LEVEL, 
						Type.INT, 
						SolaceConnectorConstants.DEFAULT_SOLACE_COMPRESSION_LEVEL,
						Range.between(0, 9),
						ConfigDef.Importance.LOW, 
						"A compressionLevel setting of 1-9 sets the ZLIB compression level to use; a setting of 0 disables compression entirely.",
						SOLACE_RECONNECT_GROUP, 
						3,
						Width.LONG,
						null)
		// Internal parameters
				.define(SolaceConnectorConstants.CONNECTOR_INSTANCE, 
						Type.STRING, 
						SolaceConnectorConstants.CONNECTOR_NAME+"_"+SolaceConnectorConstants.CONNECTOR_VERSION+"_0",
						ConfigDef.Importance.LOW, 
						"Unique identifier for the particular instance of the Connector Task. Should not be set in config file.",
						INTERNAL_GROUP, 
						1,
						Width.NONE,
						null)
				.define(SolaceConnectorConstants.KAFKA_TOPIC, 
						Type.STRING, 
						"solace_topic",
						ConfigDef.Importance.HIGH, 
						"Destination Kafka topic",
						INTERNAL_GROUP, 
						1,
						Width.LONG,
						null)

				.define(SolaceConnectorConstants.LONG_POLL_INTERVAL, 
						Type.INT, 
						SolaceConnectorConstants.DEFAULT_LONG_POLL_INTERVAL, 
						ConfigDef.Importance.LOW, 
						"How much time in milliseconds to wait when entering task.poll() method if there are no messages being received from Solace.",
						INTERNAL_GROUP, 
						2,
						Width.LONG,
						null)
				.define(SolaceConnectorConstants.SHORT_POLL_INTERVAL, 
						Type.INT, 
						SolaceConnectorConstants.DEFAULT_SHORT_POLL_INTERVAL, 
						ConfigDef.Importance.LOW, 
						"How much time in milliseconds to wait in task.poll() method after receiving a message when there are no further messages being received from Solace.",
						INTERNAL_GROUP, 
						3,
						Width.LONG,
						null)
				.define(SolaceConnectorConstants.POLL_BATCH_SIZE, 
						Type.INT, 
						SolaceConnectorConstants.DEFAULT_POLL_BATCH_SIZE, 
						ConfigDef.Importance.LOW, 
						"How many messages to receive from Solace in one task.poll() invocation.",
						INTERNAL_GROUP, 
						3,
						Width.LONG,
						null);
		
		return defs;
	}

	public SolaceConfigDef(ConfigDef definition, Map<String, String> originals) {
		super(definition, originals);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Generate configuration documentation in RST format.
	 * @param args
	 */
	public static void main(String[] args) {
		String docs = defaultConfig().toEnrichedRst();
		System.out.println("Solace Source Configuration Options");
		System.out.println("---------------------------------");
		System.out.println("");
		System.out.println(docs);
	}
	
}
