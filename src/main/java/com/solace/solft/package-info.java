/**
 * <p>This Java library leverages Solace's guaranteed delivery capabilities to provide simple
 * Leader Election events to applications requiring fault tolerance, by
 * creating an instance of the {@link com.solacesystems.poc.FTMgr} and binding
 * a listener to it to receive FT role event updates.
 * </p>
 *
 * <p>To use it you must have a Solace Message Broker available for your application to
 * connect to for this library to work.
 * </p>
 *
 * <p>Here's a basic example of a program using the library to join a FT-cluster, then
 * print to the console any time its cluster membership role changes:</p>
 * <pre>
 * <code>
 *    new FTMgr(solaceConnectionProperties)
 *        .start(ftClusterName,
 *            new FTEventListener() {
 *                {@literal @}Override
 *                public void onActive(BytesXMLMessage m) {
 *                    // in this case m is always null
 *                    logger.info("STATE CHANGE TO ACTIVE");
 *                }
 *
 *                {@literal @}Override
 *                public void onBackup() {
 *                    logger.info("STATE CHANGE TO BACKUP");
 *                }
 *            });
 *    // ...
 * </code></pre>
 * <p><strong>Stateless Event Listeners</strong></p>
 *
 * <p>First you will need a connected Solace session. You can either create a new session
 * just for <em>solft</em> by passing an instance of {@link java.util.Properties}, or more likely you'd pass a
 * shared Solace session, into your {@link com.solacesystems.poc.FTMgr} constructor.
 * You use the {@link com.solacesystems.poc.FTMgr} to join the
 * FT cluster and listen for events when cluster leadership state changes occur.</p>
 *
 * <p>To listen for these events you must implement a simple {@link com.solacesystems.poc.FTEventListener} interface
 * and pass the listener into the {@link com.solacesystems.poc.FTMgr#start(java.lang.String, com.solacesystems.poc.FTEventListener)}
 * method when starting the {@link com.solacesystems.poc.FTMgr}. All applications joining the same named cluster on
 * the same Solace broker will be part of the same FT-cluster, where a single application is elected leader until
 * it unbinds from the cluster for any reason. In that event, the cluster elects a new member and an
 * {@link com.solacesystems.poc.FTEventListener#onActive(com.solacesystems.jcsmp.BytesXMLMessage)} event
 * is raised for that instance notifying it of this role change.</p>
 *
 * <p>For stateless listeners, the message object will always be <code>null</code>.</p>
 *
 * <p><strong>Stateful Event Listeners</strong></p>
 *
 * <p>With a Stateful Event Listener, an Active event is received with the last output message sent by the
 * previous Active member. This can be used to synchronize your newly-elected Active member to that state.</p>
 *
 * <p>To run a Stateful Event Listener you start the {@link com.solacesystems.poc.FTMgr} instance by calling
 * {@link com.solacesystems.poc.FTMgr#startStateful(java.lang.String, java.lang.String, com.solacesystems.poc.FTEventListener)}
 * rather than {@link com.solacesystems.poc.FTMgr#start(java.lang.String, com.solacesystems.poc.FTEventListener)}  and
 * pass into it a topic subscription string that subscribes to all output from this application. Each instance must
 * be configured with this to support Stateful Listeners therefore must all be able to publish output messages on a
 * topic that can be subscribed to in this manner.</p>
 *
 * <p>Here's an example.</p>
 *
 * <p>Let's say I have an application that is setup to publish output messages to a _topic hierarchy_ with an
 * agreed structure. In our example, let's say the agreed structure is:</p>
 *
 * <pre>
 * [SOURCE] / [SRC INST] / [DESTINATION] / [EVENT TYPE]
 * </pre>
 *
 * <p>In our case, when my instance (#3) of application AKNA sends an OTD measurement to IGALUK the topic might look like this:</p>
 *
 * <pre>
 * AKNA / 3 / IGALUK / OTD
 * </pre>
 *
 * <p>This allows us to configure all the instances of AKNA to subscribe to all output from any of the instances (and NOT
 * from any other application instances) by starting our {@link com.solacesystems.poc.FTMgr} instance with an output subscription like:</p>
 *
 * <pre>
 * AKNA/&gt;
 * </pre>
 *
 * <p>The code example becomes:</p>
 * <pre><code>
 *     new FTMgr(solaceConnection)
 *         .startStateful(ftClusterName, "AKNA/%gt;",
 *             new FTEventListener() {
 *                 {@literal @}Override
 *                 public void onActive(BytesXMLMessage m) {
 *                     logger.info("STATE CHANGE TO ACTIVE with state: " + m.dump());
 *                 }
 *
 *                 {@literal @}Override
 *                 public void onBackup() {
 *                     logger.info("STATE CHANGE TO BACKUP");
 *                 }
 *             });
 *     // ...
 * </code></pre>
 *
 **/

package com.solace.solft;
