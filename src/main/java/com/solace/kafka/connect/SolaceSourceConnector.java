package com.solace.kafka.connect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.utils.AppInfoParser;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.source.SourceConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SolaceSourceConnector extends SourceConnector {
	
	private static final Logger log = LoggerFactory.getLogger(SolaceSourceConnector.class);
		
	private Map<String, String> connectorProperties;
	protected String[] listenTopicNames;
		
	@Override
	public ConfigDef config() {
		return SolaceConfigDef.defaultConfig();
	}	

	@Override
	public void start(Map<String, String> propMap) {
		connectorProperties = propMap;

		String topicName = propMap.get(SolaceConnectorConstants.SOLACE_TOPIC);
		String[] listenTopicNames = topicName.split("'");
				
		// TODO - validate the config here, using SolaceConfigDef?
		if (topicName == null || topicName.isEmpty())
            throw new ConnectException("SolaceSourceConnector missing required parameter '"+SolaceConnectorConstants.SOLACE_TOPIC+"'");

		log.info("Solace Kafka Source connector started. Listening to "+listenTopicNames.length+" Solace topics. "+topicName);
	}

	@Override
	public void stop() {
	}

	@Override
	public Class<? extends Task> taskClass() {
		// TODO Auto-generated method stub
		return SolaceSourceTask.class;
	}

	@Override
	public List<Map<String, String>> taskConfigs(int maxTasks) {
		ArrayList<Map<String, String>> configs = new ArrayList<Map<String, String>>();
		
		// TODO - does it make sense to allow more than one task when listening to topics?
	 	for (int i=0; i<maxTasks; i++)
		{
	 		Map<String, String> config = new HashMap<String, String>(connectorProperties);
		    configs.add(config);
		}
	    return configs;
	}

	@Override
	public String version() {
		return AppInfoParser.getVersion();
	}
}
