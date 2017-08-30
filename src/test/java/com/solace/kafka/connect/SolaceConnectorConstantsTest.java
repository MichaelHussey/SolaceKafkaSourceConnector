package com.solace.kafka.connect;

import static org.junit.Assert.*;

import org.junit.Test;

public class SolaceConnectorConstantsTest {

	@Test
	public void test() {
		assertEquals(SolaceConnectorConstants.DataType.BYTES.getValue(),"bytes");
		assertEquals(SolaceConnectorConstants.DataType.TEXT.getValue(),"text");
		assertEquals(SolaceConnectorConstants.DataType.MAP.getValue(),"map");
	}

	@Test
	public void test1() {
		assertEquals(SolaceConnectorConstants.CONNECTOR_NAME,"solace_kafka_source");
		assertEquals(SolaceConnectorConstants.CONNECTOR_VERSION,"0.1");
	}

}
