package com.solace.solft;

import com.solacesystems.jcsmp.BytesXMLMessage;

/**
 * Copied from https://github.com/koverton/solft
 * 
 * Interface for applications interested in joining a shared Solace FT-Cluster
 * and receiving FT state change events reflecting the state of your application's
 * membership in the cluster.
 *
 * See also {@link com.solacesystems.poc.FTMgr}
 */
public interface FTEventListener {
    /**
     * Invoked by the {@link FTMgr} when this listening application becomes the Active member of the cluster.
     * @param msg When {@link FTMgr} is started stateless via {@link FTMgr#start(String, FTEventListener)}
     *            this object is always <code>null</code>. When started statefully
     *            via {@link FTMgr#startStateful(String, String, FTEventListener)} it contains the last message
     *            matching the <code>outputSubscription</code> passed into that <code>startStateful()</code> method.
     *            When setup properly, it should be the last message published by the previous Active member.
     */
    public void onActive(BytesXMLMessage msg);

    /**
     * Invoked by the {@link FTMgr} when this listening application changes to become a Backup member of the cluster.
     */
    public void onBackup();
}
