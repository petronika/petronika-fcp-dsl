package net.petronika.fcp.dsl

import org.junit.BeforeClass
import org.junit.AfterClass
import org.junit.Test
import static org.junit.Assert.*

import net.pterodactylus.fcp.*

class FcpBuilderOperationsTest {

	static final String FREENET_HOST = "localhost"
	static final int FREENET_PORT = 9481

	static final String CLIENT_NAME = "greenet"

	static FcpConnection connection

	@BeforeClass
	static void setUp() {
		connection = new FcpConnection(FREENET_HOST, FREENET_PORT)
		connection.connect()
		new FcpBuilder(connection).communicate {
			clientHello(clientName: CLIENT_NAME)
		}
	}

	@AfterClass
	static void tearDown() {
		connection.close()
		connection = null
	}

	@Test
	void testListPeers() {
		new FcpBuilder(connection).communicate {
			def peers = []
			listPeers(identifier: "testListPeers") { Peer peer ->
				assertNotNull peer.identity
				peers << peer.identity
			}
			assertFalse peers.isEmpty()
		}
	}

	@Test
	void testListPeer() {
		new FcpBuilder(connection).communicate {
			def peers = []
			listPeers(identifier: "testListPeers") { Peer peer ->
				peers << peer.identity
			}
			peers.each {
				listPeer(nodeIdentifier: it) { Peer peer ->
					assertNotNull peer.identity
				}
			}
		}
	}

	@Test
	void testListPeerNotes() {
		new FcpBuilder(connection).communicate {
			def peers = []
			listPeers(identifier: "testListPeers") { Peer peer ->
				if ( !peer.opennet )
					peers << peer.identity
			}
			peers.each {
				listPeerNotes(nodeIdentifier: it) { Peer peer ->
					assertNotNull peer.identity
				}
			}
		}
	}

	@Test(expected = UnknownNodeIdentifierException.class)
	void testUnknownNodeIdentifier() {
		new FcpBuilder(connection).communicate {
			listPeer(nodeIdentifier: "peer")
		}
	}
}