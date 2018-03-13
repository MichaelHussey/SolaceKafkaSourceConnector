package com.solace.kafka.connect;

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.solace.solft.FTEventListener;
import com.solace.solft.FTMgr;
import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPSession;

public class HASentinel implements FTEventListener {
	
	private static final Logger log = LoggerFactory.getLogger(HASentinel.class);
	
	protected JCSMPSession session;
	
	protected String haQueueName;
	
	private boolean isAM;

	protected String id;
	
	public boolean isActiveMember() {
		return isAM;
	}
	
	private FTMgr ftmanager;
		
	public HASentinel(String _id, JCSMPSession _session, String queueName) throws JCSMPException
	{
		this.id = _id;
		session = _session;
		this.haQueueName = queueName;
		//ftmanager = new FTMgr(session);
		
	}
	
	public void start()  throws JCSMPException {
		this.ftmanager = new FTMgr(session);
		this.ftmanager.start(haQueueName, this);
	}
	
	public void stop()  throws JCSMPException {
		this.ftmanager.stop();
	}

	@Override
	public void onActive(BytesXMLMessage msg) {
		log.info("HASentinel:{} became active", id);
		isAM = true;
	}

	@Override
	public void onBackup() {
		log.info("HASentinel:{} became inactive", id);
		isAM = false;
	}

}
