package com.solace.kafka.connect;

import java.util.Map;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Type;

public class SolaceConfigDef extends AbstractConfig
{

	public static ConfigDef defaultConfig()
	{
		ConfigDef defs = new ConfigDef();
		defs.define(SolaceConnectorConstants.SOLACE_VPN, Type.STRING, "default", ConfigDef.Importance.MEDIUM, "Message VPN name");
		defs.define(SolaceConnectorConstants.SOLACE_URL, Type.STRING, ConfigDef.Importance.HIGH, "Host and if necessary port number of the Solace Message Router, eg 'localhost:55555'");
		defs.define(SolaceConnectorConstants.SOLACE_USERNAME, Type.STRING, "default", ConfigDef.Importance.MEDIUM, "Client Username");
		defs.define(SolaceConnectorConstants.SOLACE_PASSWORD, Type.STRING, null, ConfigDef.Importance.MEDIUM, "Client Password, optional");
		defs.define(SolaceConnectorConstants.SOLACE_TOPIC, Type.STRING, "default", ConfigDef.Importance.HIGH, "List of topics that are applied as subscriptions, comma separated");
		defs.define(SolaceConnectorConstants.KAFKA_TOPIC, Type.STRING, "solace_topic", ConfigDef.Importance.HIGH, "Destination Kafka topic");
		
		return defs;
	}

	public SolaceConfigDef(ConfigDef definition, Map<String, String> originals) {
		super(definition, originals);
		// TODO Auto-generated constructor stub
	}
}
