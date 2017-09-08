package com.solace.kafka.connect;

import java.util.Map;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Range;
import org.apache.kafka.common.config.ConfigDef.Type;
import org.apache.kafka.common.config.ConfigDef.Validator;

public class SolaceConfigDef extends AbstractConfig
{

	public static ConfigDef defaultConfig()
	{
		ConfigDef defs = new ConfigDef();
		// Connection parameters
		defs.define(SolaceConnectorConstants.SOLACE_VPN, Type.STRING, "default", ConfigDef.Importance.MEDIUM, "Message VPN name");
		defs.define(SolaceConnectorConstants.SOLACE_URL, Type.STRING, ConfigDef.Importance.HIGH, "Host and if necessary port number of the Solace Message Router, eg 'localhost:55555'");
		defs.define(SolaceConnectorConstants.SOLACE_USERNAME, Type.STRING, "default", ConfigDef.Importance.MEDIUM, "Client Username");
		defs.define(SolaceConnectorConstants.SOLACE_PASSWORD, Type.PASSWORD, null, ConfigDef.Importance.MEDIUM, "Client Password, optional");
		defs.define(SolaceConnectorConstants.SOLACE_TOPIC, Type.STRING, "default", ConfigDef.Importance.HIGH, "List of topics that are applied as subscriptions, comma separated");
		// Reconnect related parameters
		defs.define(SolaceConnectorConstants.SOLACE_RECONNECT_RETRIES, Type.INT, 
				SolaceConnectorConstants.DEFAULT_SOLACE_RECONNECT_RETRIES, 
				ConfigDef.Importance.MEDIUM, 
				"The number of times to attempt to reconnect to the Solace "
				+ "appliance (or list of appliances) after an initial connected session goes down.");
		defs.define(SolaceConnectorConstants.SOLACE_RECONNECT_RETRY_WAIT, Type.INT, 
				SolaceConnectorConstants.DEFAULT_SOLACE_RECONNECT_RETRY_WAIT, 
				ConfigDef.Importance.MEDIUM, 
				"How much time in milliseconds to wait between each attempt to connect or reconnect to a host.");
		defs.define(SolaceConnectorConstants.SOLACE_COMPRESSION_LEVEL, Type.INT, 
				SolaceConnectorConstants.DEFAULT_SOLACE_COMPRESSION_LEVEL,
				Range.between(0, 9),
				ConfigDef.Importance.LOW, 
				"A compressionLevel setting of 1-9 sets the ZLIB compression level to use; a setting of 0 disables compression entirely.");
		
		defs.define(SolaceConnectorConstants.KAFKA_TOPIC, Type.STRING, 
				"solace_topic",
				ConfigDef.Importance.HIGH, 
				"Destination Kafka topic");

		defs.define(SolaceConnectorConstants.LONG_POLL_INTERVAL, Type.INT, 
				SolaceConnectorConstants.DEFAULT_LONG_POLL_INTERVAL, 
				ConfigDef.Importance.LOW, 
				"How much time in milliseconds to wait when entering task.poll() method if there are no messages being received from Solace.");
		defs.define(SolaceConnectorConstants.SHORT_POLL_INTERVAL, Type.INT, 
				SolaceConnectorConstants.DEFAULT_SHORT_POLL_INTERVAL, 
				ConfigDef.Importance.LOW, 
				"How much time in milliseconds to wait in task.poll() method after receiving a message when there are no further messages being received from Solace.");
		defs.define(SolaceConnectorConstants.POLL_BATCH_SIZE, Type.INT, 
				SolaceConnectorConstants.DEFAULT_POLL_BATCH_SIZE, 
				ConfigDef.Importance.LOW, 
				"How many messages to receive from Solace in one task.poll() invocation.");
		
		return defs;
	}

	public SolaceConfigDef(ConfigDef definition, Map<String, String> originals) {
		super(definition, originals);
		// TODO Auto-generated constructor stub
	}
}
