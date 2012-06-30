package net.petronika.fcp.dsl

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import net.pterodactylus.fcp.*

class FCPBuilder {

	private static final Logger logger = LoggerFactory.getLogger(FCPBuilder.class)

	static final String DEFAULT_HOST = 'localhost'
	static final int DEFAULT_PORT = FcpConnection.DEFAULT_PORT

	private final Map<Class<? extends BaseMessage>, Set<Closure>> listeners =
		new HashMap<Class<? extends BaseMessage>, Set<Closure>>()

	private String host = DEFAULT_HOST
	private int port = DEFAULT_PORT

	private FcpConnection connection
	private boolean communication

	private ProtocolError protocolError
	private UnknownNodeIdentifier unknownNodeIdentifier
	private CloseConnectionDuplicateClientName closeConnectionDuplicateClientName

	FCPBuilder() {
	}

	FCPBuilder(String host, int port) {
		assert host
		assert port > 0
		this.host = host
		this.port = port
	}

	FCPBuilder(FcpConnection connection) {
		assert connection
		this.connection = connection
	}

	synchronized void communicate(Closure closure) {
		assert closure

		boolean connectionCreated
		if ( !connection ) {
			FcpConnection tmpConnection = new FcpConnection(host, port)
			tmpConnection.connect()
			connection = tmpConnection
			connectionCreated = true
		}
		communication = true

		FCPListener listener = new FCPListener()
		connection.addFcpListener(new FCPListener())

		try {
			doClosure(closure, this, null)
		}
		finally {
			removeAllListeners()
			connection.removeFcpListener(listener)

			if ( connectionCreated ) {
				connection.close()
				connection = null
			}
			communication = false

			protocolError = null
			unknownNodeIdentifier = null
			closeConnectionDuplicateClientName = null
		}
	}

	void clientHello(Map properties, Closure callback = {}) {
		checkCommunicationMode()
		doMessage(net.petronika.fcp.dsl.message.ClientHello.class, properties, callback, NodeHello.class)
	}

	void onNodeHello(Closure closure) {
		checkCommunicationMode()
		addListener(NodeHello.class, closure)
	}

	void listPeers(Map properties, Closure callback = {}) {
		checkCommunicationMode()
		doMessage(net.petronika.fcp.dsl.message.ListPeers.class, properties, callback, Peer.class, EndListPeers.class)
	}

	void listPeer(Map properties, Closure callback = {}) {
		checkCommunicationMode()
		doMessage(net.petronika.fcp.dsl.message.ListPeer.class, properties, callback, Peer.class)
	}

	void onPeer(Closure closure) {
		checkCommunicationMode()
		addListener(Peer.class, closure)
	}

	void onEndListPeers(Closure closure) {
		checkCommunicationMode()
		addListener(EndListPeers.class, closure)
	}

	void listPeerNotes(Map properties, Closure callback = {}) {
		checkCommunicationMode()
		doMessage(net.petronika.fcp.dsl.message.ListPeerNotes.class, properties, callback, PeerNote.class, EndListPeerNotes.class)
	}

	void onPeerNote(Closure closure) {
		checkCommunicationMode()
		addListener(PeerNote.class, closure)
	}

	void onEndListPeerNotes(Closure closure) {
		checkCommunicationMode()
		addListener(EndListPeerNotes.class, closure)
	}

	void clientGet(Map properties) {
		checkCommunicationMode()
		doMessage(net.petronika.fcp.dsl.message.ClientGet.class, properties)
	}

	void onDataFound(Closure closure) {
		checkCommunicationMode()
		addListener(DataFound.class, closure)
	}

	void onAllData(Closure closure) {
		checkCommunicationMode()
		addListener(AllData.class, closure)
	}

	void onGetFailed(Closure closure) {
		checkCommunicationMode()
		addListener(GetFailed.class, closure)
	}

	protected doMessage(
		Class<? extends FcpMessage> messageType,
		Map properties = null,
		Closure callback = null,
		Class<? extends BaseMessage> responseType = null,
		Class<? extends BaseMessage> endListResponseType = null)
	{
		assert messageType

		if ( closeConnectionDuplicateClientName ) {
			throw new DuplicateClientNameException()
		}

		FcpMessage message
		if ( !properties ) {
			message = messageType.newInstance()
		} else {
			message = messageType.newInstance(properties)
		}

		if ( !callback ) {
			connection.sendMessage(message)
		}
		else {
			assert responseType
			addListener(responseType, callback)
			if ( messageType in net.petronika.fcp.dsl.message.ListPeer ) {
				addListener(UnknownNodeIdentifier, callback)
			}
			def endListCallback
			if ( endListResponseType ) {
				endListCallback = {}
				addListener(endListResponseType, endListCallback)
			}
			try {
				if ( !endListCallback ) {
					synchronized ( callback ) {
						connection.sendMessage(message)
						callback.wait()
					}
				} else {
					synchronized ( endListCallback ) {
						connection.sendMessage(message)
						endListCallback.wait()
					}
				}
				if ( protocolError ) {
					throw new ProtocolErrorException(protocolError)
				}
				if ( unknownNodeIdentifier ) {
					throw new UnknownNodeIdentifierException(unknownNodeIdentifier)
				}
			}
			finally {
				removeListener(responseType, callback)
				if ( messageType in net.petronika.fcp.dsl.message.ListPeer ) {
					removeListener(UnknownNodeIdentifier, callback)
				}
				if ( endListResponseType ) {
					removeListener(endListResponseType, endListCallback)
				}
				protocolError = null
				unknownNodeIdentifier = null
			}
		}
	}

	protected addListener(Class<? extends BaseMessage> messageType, Closure closure) {
		assert closure
		Set<Closure> messageListeners = listeners[messageType]
		if ( !messageListeners ) {
			messageListeners = new HashSet<Closure>()
			assert !listeners.put(messageType, messageListeners)
		}
		assert messageListeners.add(closure)
	}

	protected removeListener(Class<? extends BaseMessage> messageType, Closure closure) {
		Set<Closure> messageListeners = listeners[messageType]
		assert messageListeners
		assert messageListeners.remove(closure)
	}

	protected removeAllListeners() {
		listeners.clear()
	}

	protected fireReceived(BaseMessage message) {
		if ( message instanceof ProtocolError ||
			message instanceof UnknownNodeIdentifier ||
			message instanceof CloseConnectionDuplicateClientName )
		{
			listeners.each { Class<? extends BaseMessage> messageType,  Set<Closure> messageListeners ->
				messageListeners.each { Closure closure ->
					synchronized ( closure ) {
						closure.notify()
					}
				}
			}
		}
		else {
			Set<Closure> messageListeners = listeners[message.getClass()]
			if ( messageListeners ) {
				messageListeners.each { Closure closure ->
					synchronized ( closure ) {
						closure.call(message)
						closure.notify()
					}
				}
			}
		}
	}

	protected void doClosure(Closure callback, Object delegate, Object arguments) {
		Object oldDelegate = callback.delegate
		if ( delegate ) {
			callback.delegate = delegate
		}
		try {
			if ( arguments ) {
				callback.call(arguments)
			} else {
				callback.call()
			}
		}
		finally {
			callback.delegate = oldDelegate
		}
	}

	protected checkCommunicationMode() {
		if ( !communication ) {
			throw new IllegalStateException("The call is allowed in the communication mode only")
		}
	}

	protected class FCPListener implements FcpListener {
		@Override
		public void receivedNodeHello(FcpConnection fcpConnection, NodeHello nodeHello) {
			fireReceived(nodeHello)
		}
		@Override
		public void receivedSSKKeypair(FcpConnection fcpConnection, SSKKeypair sskKeypair) {
			fireReceived(sskKeypair)
		}
		@Override
		public void receivedPeer(FcpConnection fcpConnection, Peer peer) {
			fireReceived(peer)
		}
		@Override
		public void receivedEndListPeers(FcpConnection fcpConnection, EndListPeers endListPeers) {
			fireReceived(endListPeers)
		}
		@Override
		public void receivedPeerNote(FcpConnection fcpConnection, PeerNote peerNote) {
			fireReceived(peerNote)
		}
		@Override
		public void receivedEndListPeerNotes(FcpConnection fcpConnection, EndListPeerNotes endListPeerNotes) {
			fireReceived(endListPeerNotes)
		}
		@Override
		public void receivedPeerRemoved(FcpConnection fcpConnection, PeerRemoved peerRemoved) {
			fireReceived(peerRemoved)
		}
		@Override
		public void receivedNodeData(FcpConnection fcpConnection, NodeData nodeData) {
			fireReceived(nodeData)
		}
		@Override
		public void receivedTestDDAReply(FcpConnection fcpConnection, TestDDAReply testDDAReply) {
			fireReceived(testDDAReply)
		}
		@Override
		public void receivedTestDDAComplete(FcpConnection fcpConnection, TestDDAComplete testDDAComplete) {
			fireReceived(testDDAComplete)
		}
		@Override
		public void receivedPersistentGet(FcpConnection fcpConnection, PersistentGet persistentGet) {
			fireReceived(persistentGet)
		}
		@Override
		public void receivedPersistentPut(FcpConnection fcpConnection, PersistentPut persistentPut) {
			fireReceived(persistentPut)
		}
		@Override
		public void receivedEndListPersistentRequests(FcpConnection fcpConnection, EndListPersistentRequests endListPersistentRequests) {
			fireReceived(endListPersistentRequests)
		}
		@Override
		public void receivedURIGenerated(FcpConnection fcpConnection, URIGenerated uriGenerated) {
			fireReceived(uriGenerated)
		}
		@Override
		public void receivedDataFound(FcpConnection fcpConnection, DataFound dataFound) {
			fireReceived(dataFound)
		}
		@Override
		public void receivedAllData(FcpConnection fcpConnection, AllData allData) {
			fireReceived(allData)
		}
		@Override
		public void receivedSimpleProgress(FcpConnection fcpConnection, SimpleProgress simpleProgress) {
			fireReceived(simpleProgress)
		}
		@Override
		public void receivedStartedCompression(FcpConnection fcpConnection, StartedCompression startedCompression) {
			fireReceived(startedCompression)
		}
		@Override
		public void receivedFinishedCompression(FcpConnection fcpConnection, FinishedCompression finishedCompression) {
			fireReceived(finishedCompression)
		}
		@Override
		public void receivedUnknownPeerNoteType(FcpConnection fcpConnection, UnknownPeerNoteType unknownPeerNoteType) {
			fireReceived(unknownPeerNoteType)
		}
		@Override
		public void receivedUnknownNodeIdentifier(FcpConnection fcpConnection, UnknownNodeIdentifier unknownNodeIdentifier) {
			FCPBuilder.this.unknownNodeIdentifier = unknownNodeIdentifier
			fireReceived(unknownNodeIdentifier)
		}
		@Override
		public void receivedConfigData(FcpConnection fcpConnection, ConfigData configData) {
			fireReceived(configData)
		}
		@Override
		public void receivedGetFailed(FcpConnection fcpConnection, GetFailed getFailed) {
			fireReceived(getFailed)
		}
		@Override
		public void receivedPutFailed(FcpConnection fcpConnection, PutFailed putFailed) {
			fireReceived(putFailed)
		}
		@Override
		public void receivedIdentifierCollision(FcpConnection fcpConnection, IdentifierCollision identifierCollision) {
			fireReceived(identifierCollision)
		}
		@Override
		public void receivedPersistentPutDir(FcpConnection fcpConnection, PersistentPutDir persistentPutDir) {
			fireReceived(persistentPutDir)
		}
		@Override
		public void receivedPersistentRequestRemoved(FcpConnection fcpConnection, PersistentRequestRemoved persistentRequestRemoved) {
			fireReceived(persistentRequestRemoved)
		}
		@Override
		public void receivedSubscribedUSKUpdate(FcpConnection fcpConnection, SubscribedUSKUpdate subscribedUSKUpdate) {
			fireReceived(subscribedUSKUpdate)
		}
		@Override
		public void receivedPluginInfo(FcpConnection fcpConnection, PluginInfo pluginInfo) {
			fireReceived(pluginInfo)
		}
		@Override
		public void receivedFCPPluginReply(FcpConnection fcpConnection, FCPPluginReply fcpPluginReply) {
			fireReceived(fcpPluginReply)
		}
		@Override
		public void receivedPersistentRequestModified(FcpConnection fcpConnection, PersistentRequestModified persistentRequestModified) {
			fireReceived(persistentRequestModified)
		}
		@Override
		public void receivedPutSuccessful(FcpConnection fcpConnection, PutSuccessful putSuccessful) {
			fireReceived(putSuccessful)
		}
		@Override
		public void receivedPutFetchable(FcpConnection fcpConnection, PutFetchable putFetchable) {
			fireReceived(putFetchable)
		}
		@Override
		public void receivedSentFeed(FcpConnection source, SentFeed sentFeed) {
			fireReceived(sentFeed)
		}
		@Override
		public void receivedBookmarkFeed(FcpConnection fcpConnection, ReceivedBookmarkFeed receivedBookmarkFeed) {
			fireReceived(receivedBookmarkFeed)
		}
		@Override
		public void receivedProtocolError(FcpConnection fcpConnection, ProtocolError protocolError) {
			logger.error("Protocol error: " + protocolError.codeDescription)
			FCPBuilder.this.protocolError = protocolError
			fireReceived(protocolError)
		}
		@Override
		public void receivedCloseConnectionDuplicateClientName(FcpConnection fcpConnection, CloseConnectionDuplicateClientName closeConnectionDuplicateClientName) {
			logger.warn("Received CloseConnectionDuplicateClientName")
			FCPBuilder.this.closeConnectionDuplicateClientName = closeConnectionDuplicateClientName
			fireReceived(closeConnectionDuplicateClientName)
		}
		@Override
		public void receivedMessage(FcpConnection fcpConnection, FcpMessage fcpMessage) {
			logger.warn("Received unknown FCP message: {}", fcpMessage.name)
		}
		@Override
		public void connectionClosed(FcpConnection fcpConnection, Throwable throwable) {
			logger.debug("Connection closed")
		}
	}
}