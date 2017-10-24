package com.solace.kafka.connect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.solacesystems.jcsmp.BytesMessage;
import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.DeliveryMode;
import com.solacesystems.jcsmp.Destination;
import com.solacesystems.jcsmp.MapMessage;
import com.solacesystems.jcsmp.SDTMap;
import com.solacesystems.jcsmp.TextMessage;

public class SolaceConverter{
	
	private static final Logger log = LoggerFactory.getLogger(SolaceConverter.class);
	
	private static Schema solaceKafkaSchema = getSchema();

	protected SolaceSourceTask parentTask;
	
	public SolaceConverter(SolaceSourceTask solaceSourceTask) {
		parentTask = solaceSourceTask;
	}
	
    private Map<String, String> offsetKey(String key) {
        return Collections.singletonMap("solaceKey", key);
    }

    private Map<String, String> offsetValue(String pos) {
        return Collections.singletonMap(SolaceConnectorConstants.FIELD_SEQUENCE_NUMBER, pos);
    }


	/**
	 * Process messages received from Solace - map to Kafka format
	 */
	//@Override
	public SourceRecord convertMessage(BytesXMLMessage solaceMessage) {
		if (log.isDebugEnabled()) {
			log.debug("Converting: "+solaceMessage.dump());
		}
		String key = solaceMessage.getDestination().getName()+"_"+ solaceMessage.getSenderId();
		
		Struct kafkaStruct = mapSolaceToKafka(solaceMessage);
		SourceRecord kafkaSourceRecord = new SourceRecord(
				offsetKey(key), //Map<String, ?> sourcePartition
				offsetValue(solaceMessage.getMessageId()), //Map<String, ?> sourceOffset
				parentTask.kafkaTopicName, //String topic
				Schema.STRING_SCHEMA, //Schema keySchema
				key, //Object key
				solaceKafkaSchema, //Schema valueSchema
	            kafkaStruct //Object value
	            );
		
		log.debug("Created SourceRecord: "+kafkaSourceRecord.toString());
		return kafkaSourceRecord;
	}

	private static Schema getSchema() {
		SchemaBuilder sb = SchemaBuilder.struct()
				.name(SolaceConnectorConstants.SCHEMA_NAME)
				.version(SolaceConnectorConstants.SCHEMA_VERSION)
				.field(SolaceConnectorConstants.FIELD_APPLICATION_MESSAGE_ID, Schema.OPTIONAL_STRING_SCHEMA)
				.field(SolaceConnectorConstants.FIELD_APPLICATION_DELIVERY_MODE, Schema.STRING_SCHEMA)
				.field(SolaceConnectorConstants.FIELD_ATTACHMENT_CONTENT_LENGTH, Schema.OPTIONAL_INT32_SCHEMA)
				.field(SolaceConnectorConstants.FIELD_CACHE_REQUEST_ID, Schema.OPTIONAL_INT64_SCHEMA)
				.field(SolaceConnectorConstants.FIELD_CONSUMER_ID_LIST, Schema.OPTIONAL_STRING_SCHEMA)
				.field(SolaceConnectorConstants.FIELD_CONTENT_LENGTH, Schema.OPTIONAL_INT32_SCHEMA)
				.field(SolaceConnectorConstants.FIELD_CORRELATION_ID, Schema.OPTIONAL_STRING_SCHEMA)
				.field(SolaceConnectorConstants.FIELD_DESTINATION, Schema.STRING_SCHEMA)
				.field(SolaceConnectorConstants.FIELD_EXPIRATION, Schema.OPTIONAL_INT64_SCHEMA)
				.field(SolaceConnectorConstants.FIELD_HTTP_CONTENT_ENCODING, Schema.OPTIONAL_STRING_SCHEMA)
				.field(SolaceConnectorConstants.FIELD_HTTP_CONTENT_TYPE, Schema.OPTIONAL_STRING_SCHEMA)
				.field(SolaceConnectorConstants.FIELD_MESSAGE_ID, Schema.OPTIONAL_STRING_SCHEMA)
				.field(SolaceConnectorConstants.FIELD_MESSAGE_ID_LONG, Schema.OPTIONAL_INT64_SCHEMA)
				.field(SolaceConnectorConstants.FIELD_RECEIVE_TIMESTAMP, Schema.OPTIONAL_INT64_SCHEMA)
				.field(SolaceConnectorConstants.FIELD_REDELIVERED, Schema.OPTIONAL_BOOLEAN_SCHEMA)
				.field(SolaceConnectorConstants.FIELD_REPLYTO, Schema.OPTIONAL_STRING_SCHEMA)
				.field(SolaceConnectorConstants.FIELD_SENDERID, Schema.OPTIONAL_STRING_SCHEMA)
				.field(SolaceConnectorConstants.FIELD_SENDER_TIMESTAMP, Schema.OPTIONAL_INT64_SCHEMA)
				.field(SolaceConnectorConstants.FIELD_SEQUENCE_NUMBER, Schema.OPTIONAL_INT64_SCHEMA)
				.field(SolaceConnectorConstants.FIELD_USER_DATA, Schema.OPTIONAL_BYTES_SCHEMA)
				.field(SolaceConnectorConstants.FIELD_DATA_TYPE, Schema.OPTIONAL_STRING_SCHEMA)
				.field(SolaceConnectorConstants.FIELD_DATA_TEXT, Schema.OPTIONAL_STRING_SCHEMA)
				.field(SolaceConnectorConstants.FIELD_DATA_BYTES, Schema.OPTIONAL_BYTES_SCHEMA)
				.field(SolaceConnectorConstants.FIELD_DATA_MAP, getMapSchema())
				.field(SolaceConnectorConstants.FIELD_USER_PROPERTIES, getMapSchema());
				
		Schema solMessageSchema = sb.build();
		return solMessageSchema;
	}

	private static Schema getMapSchema() {
		SchemaBuilder sb = SchemaBuilder.struct();
		sb.name(SolaceConnectorConstants.SCHEMA_MAP_NAME);
		sb.version(SolaceConnectorConstants.SCHEMA_VERSION);
		sb.optional();
		return sb.build();
	}

	/**
	 * Extract as much information as possible from the received message and place it in a Record suitable for Kafka use
	 * 
	 * TODO add user property, payload and attachment handling
	 * 
	 * @param solaceMessage
	 * @return
	 */
	private Struct mapSolaceToKafka(BytesXMLMessage solaceMessage) {
		
		Struct kafkaStruct = new Struct(solaceKafkaSchema);
		kafkaStruct.put(SolaceConnectorConstants.FIELD_APPLICATION_MESSAGE_ID, solaceMessage.getApplicationMessageId());
		DeliveryMode deliveryMode = solaceMessage.getDeliveryMode();
		kafkaStruct.put(SolaceConnectorConstants.FIELD_APPLICATION_DELIVERY_MODE, deliveryMode.toString());
		kafkaStruct.put(SolaceConnectorConstants.FIELD_ATTACHMENT_CONTENT_LENGTH, solaceMessage.getAttachmentContentLength());
		kafkaStruct.put(SolaceConnectorConstants.FIELD_CACHE_REQUEST_ID, solaceMessage.getCacheRequestId());
		List<Long> consumerIds = solaceMessage.getConsumerIdList();
		if (consumerIds.size() > 0) {
			
			kafkaStruct.put(SolaceConnectorConstants.FIELD_CONSUMER_ID_LIST, consumerIds.toArray().toString());			
		}
		kafkaStruct.put(SolaceConnectorConstants.FIELD_CONTENT_LENGTH, solaceMessage.getContentLength());
		kafkaStruct.put(SolaceConnectorConstants.FIELD_CORRELATION_ID, solaceMessage.getCorrelationId());
		kafkaStruct.put(SolaceConnectorConstants.FIELD_DESTINATION, solaceMessage.getDestination().getName());
		kafkaStruct.put(SolaceConnectorConstants.FIELD_EXPIRATION, solaceMessage.getExpiration());
		if (solaceMessage.getHTTPContentEncoding() != null) {
			kafkaStruct.put(SolaceConnectorConstants.FIELD_HTTP_CONTENT_ENCODING, solaceMessage.getHTTPContentEncoding());
		}
		if (solaceMessage.getHTTPContentType() != null) {
			kafkaStruct.put(SolaceConnectorConstants.FIELD_HTTP_CONTENT_TYPE, solaceMessage.getHTTPContentType());
		}
		// MessageId is meaningless for DIRECT messages
		if (deliveryMode != DeliveryMode.DIRECT) {
			kafkaStruct.put(SolaceConnectorConstants.FIELD_MESSAGE_ID, solaceMessage.getMessageId());
			kafkaStruct.put(SolaceConnectorConstants.FIELD_MESSAGE_ID_LONG, solaceMessage.getMessageIdLong());
		}
		long receiveTS = solaceMessage.getReceiveTimestamp();
		if (receiveTS > 0) {
			kafkaStruct.put(SolaceConnectorConstants.FIELD_RECEIVE_TIMESTAMP, receiveTS);
		}
		kafkaStruct.put(SolaceConnectorConstants.FIELD_REDELIVERED, solaceMessage.getRedelivered());
		Destination replyToDest = solaceMessage.getReplyTo();
		if (replyToDest != null) {
			kafkaStruct.put(SolaceConnectorConstants.FIELD_REPLYTO, replyToDest.getName());
		}
		String senderId = solaceMessage.getSenderId();
		if (replyToDest != null) {
			kafkaStruct.put(SolaceConnectorConstants.FIELD_SENDERID, senderId);
		}
		Long senderTS = solaceMessage.getSenderTimestamp();
		if (senderTS != null) {
			kafkaStruct.put(SolaceConnectorConstants.FIELD_SENDER_TIMESTAMP, senderTS);
		}
		Long seqNo = solaceMessage.getSequenceNumber();
		if (seqNo != null) {
			kafkaStruct.put(SolaceConnectorConstants.FIELD_SEQUENCE_NUMBER, seqNo);
		}
		byte[] userData = solaceMessage.getUserData();
		if (userData != null && userData.length > 0) {
			kafkaStruct.put(SolaceConnectorConstants.FIELD_USER_DATA, userData);
		}
		SDTMap userProperties = solaceMessage.getProperties();
		if (userProperties != null && !userProperties.isEmpty()) {
			kafkaStruct.put(SolaceConnectorConstants.FIELD_USER_PROPERTIES, mapSDTMap(userProperties));
		}
		
		// Now deal with the payload.
		if(solaceMessage instanceof TextMessage) {
			TextMessage tm = (TextMessage) solaceMessage;
			String data = tm.getText();
			kafkaStruct.put(SolaceConnectorConstants.FIELD_DATA_TYPE, SolaceConnectorConstants.DataType.TEXT.getValue());
			kafkaStruct.put(SolaceConnectorConstants.FIELD_DATA_TEXT, data);
		}
		else if(solaceMessage instanceof MapMessage) {
			MapMessage mm = (MapMessage) solaceMessage;
			SDTMap dataMap = mm.getMap();
			kafkaStruct.put(SolaceConnectorConstants.FIELD_DATA_TYPE, SolaceConnectorConstants.DataType.MAP.getValue());
			kafkaStruct.put(SolaceConnectorConstants.FIELD_DATA_MAP, mapSDTMap(dataMap));
		}
		else if(solaceMessage instanceof BytesXMLMessage || solaceMessage instanceof BytesMessage) {
			BytesMessage bm = (BytesMessage) solaceMessage;
			byte[] data = bm.getData();
			kafkaStruct.put(SolaceConnectorConstants.FIELD_DATA_TYPE, SolaceConnectorConstants.DataType.BYTES.getValue());
			kafkaStruct.put(SolaceConnectorConstants.FIELD_DATA_BYTES, data);
		}
		return kafkaStruct;
	}

	private Struct mapSDTMap(SDTMap dataMap) {
		Struct mapStruct = new Struct(getMapSchema());
		return mapStruct;
	}

}
