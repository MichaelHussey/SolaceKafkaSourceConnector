package com.solace.kafka.connect;

/**
 * 
 * @author michussey
 *
 */
public class SolaceConnectorConstants {
	
	public static final String CONNECTOR_NAME = "solace_kafka_source";
	public static final String CONNECTOR_VERSION = "0.1";
	public static final String CONNECTOR_INSTANCE = "connector.instance";

	public static final String SOLACE_VPN = "solace.msgVpn";
	
	public static final String SOLACE_USERNAME = "solace.username";
	
	public static final String SOLACE_PASSWORD = "solace.password";
	
	public static final String SOLACE_URL = "solace.smfHost";
	
	public static final String SOLACE_TOPIC = "solace.topic";
	
	public static final String SOLACE_HA_QUEUE = "solace.ha_sentinel_queue";
	
	public static final String SOLACE_RECONNECT_RETRIES = "solace.reconnectRetries";
	public static final int DEFAULT_SOLACE_RECONNECT_RETRIES = 3;
	
	public static final String SOLACE_RECONNECT_RETRY_WAIT = "solace.reconnectRetryWaitInMillis";
	public static final int DEFAULT_SOLACE_RECONNECT_RETRY_WAIT = 3000;
	
	public static final String SOLACE_COMPRESSION_LEVEL = "solace.compressionLevel";
	public static final int DEFAULT_SOLACE_COMPRESSION_LEVEL = 0;
	
	public static final String KAFKA_TOPIC = "topic";
	
	public static final String LONG_POLL_INTERVAL = "polling.long_interval";
	public static final int DEFAULT_LONG_POLL_INTERVAL = 1000;
	
	public static final String SHORT_POLL_INTERVAL = "polling.short_interval";
	public static final int DEFAULT_SHORT_POLL_INTERVAL = 10;
	
	public static final String POLL_BATCH_SIZE = "polling.batch_size";
	public static final int DEFAULT_POLL_BATCH_SIZE = 100;
	

	public static final String SCHEMA_NAME = "com.solace.kafka.message";
	public static final String SCHEMA_MAP_NAME = "com.solace.kafka.map";

	public static final Integer SCHEMA_VERSION = 1;

	/**
	 * Solace message fields
	 */
	public static final String FIELD_APPLICATION_MESSAGE_ID = "ApplicationMessageId";
	public static final String FIELD_APPLICATION_DELIVERY_MODE = "DeliveryMode";
	public static final String FIELD_USER_PROPERTIES = "UserProperties";
	public static final String FIELD_ATTACHMENT = "Attachment";
	public static final String FIELD_ATTACHMENT_CONTENT_LENGTH = "AttachmentContentLength";
	public static final String FIELD_CACHE_REQUEST_ID = "CacheRequestId";
	public static final String FIELD_CONSUMER_ID_LIST = "ConsumerIdList";
	public static final String FIELD_CONTENT_LENGTH = "ContentLength";
	public static final String FIELD_CORRELATION_ID = "CorrelationId";
	public static final String FIELD_CORRELATION_KEY = "CorrelationKey";
	public static final String FIELD_DESTINATION = "Destination";
	public static final String FIELD_EXPIRATION = "Expiration";
	public static final String FIELD_HTTP_CONTENT_ENCODING = "HttpContentEncoding";
	public static final String FIELD_HTTP_CONTENT_TYPE = "HttpContentType";
	public static final String FIELD_MESSAGE_ID = "MessageId";
	public static final String FIELD_MESSAGE_ID_LONG = "MessageIdLong";
	public static final String FIELD_RECEIVE_TIMESTAMP = "ReceiveTimestamp";
	public static final String FIELD_REDELIVERED = "IsRedelivered";
	public static final String FIELD_REPLYTO = "ReplyTo";
	public static final String FIELD_SENDERID = "SenderId";
	public static final String FIELD_SENDER_TIMESTAMP = "SenderTimestamp";
	public static final String FIELD_SEQUENCE_NUMBER = "SequenceNumber";
	public static final String FIELD_USER_DATA = "UserData";
	public static final String FIELD_DATA_TYPE = "DataType";
	public static final String FIELD_DATA_TEXT = "DataText";
	public static final String FIELD_DATA_BYTES = "DataBytes";
	public static final String FIELD_DATA_MAP = "DataMap";
	
	public static enum DataType {
		TEXT("text"),
		MAP("map"),
		BYTES("bytes");
		
		private String value;
		
		DataType(String _val) {
			this.value = _val; 
		}
		
		public String getValue() {
			return value;
		}
	}
	
	
}
