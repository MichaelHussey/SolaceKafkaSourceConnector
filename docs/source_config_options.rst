Solace Source Configuration Options
---------------------------------

Solace Connection Parameters
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

``solace.smfHost``
  Host and if necessary port number of the Solace Message Router, eg 'localhost:55555'

  * Type: string
  * Default: localhost:55555
  * Importance: high

``solace.msgVpn``
  Message VPN name

  * Type: string
  * Default: default
  * Importance: medium

``solace.password``
  Client Password, optional

  * Type: password
  * Default: null
  * Importance: medium

``solace.username``
  Client Username

  * Type: string
  * Default: default
  * Importance: medium

``solace.topic``
  List of topics that are applied as subscriptions, comma separated

  * Type: string
  * Default: default
  * Importance: high

Solace Re-Connection Parameters
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

``solace.reconnectRetries``
  The number of times to attempt to reconnect to the Solace appliance (or list of appliances) after an initial connected session goes down.

  * Type: int
  * Default: 3
  * Importance: medium

``solace.reconnectRetryWaitInMillis``
  How much time in milliseconds to wait between each attempt to connect or reconnect to a host.

  * Type: int
  * Default: 3000
  * Importance: medium

``solace.compressionLevel``
  A compressionLevel setting of 1-9 sets the ZLIB compression level to use; a setting of 0 disables compression entirely.

  * Type: int
  * Default: 0
  * Valid Values: [0,...,9]
  * Importance: low

Connector Internal Behaviour
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

``topic``
  Destination Kafka topic

  * Type: string
  * Default: solace_topic
  * Importance: high

``polling.long_interval``
  How much time in milliseconds to wait when entering task.poll() method if there are no messages being received from Solace.

  * Type: int
  * Default: 1000
  * Importance: low

``polling.batch_size``
  How many messages to receive from Solace in one task.poll() invocation.

  * Type: int
  * Default: 100
  * Importance: low

``polling.short_interval``
  How much time in milliseconds to wait in task.poll() method after receiving a message when there are no further messages being received from Solace.

  * Type: int
  * Default: 10
  * Importance: low


