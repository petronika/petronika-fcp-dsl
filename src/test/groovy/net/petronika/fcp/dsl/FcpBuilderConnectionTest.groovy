package net.petronika.fcp.dsl

import org.junit.Test
import static org.junit.Assert.*

import net.pterodactylus.fcp.*

class FcpBuilderConnectionTest {

	static final String FREENET_HOST = "localhost"
	static final int FREENET_PORT = 9481

	static final String CLIENT_NAME = "greenet"

	@Test
	void testDefault() {
		new FcpBuilder().communicate {
			clientHello(clientName: CLIENT_NAME) { NodeHello nodeHello ->
				assertNotNull nodeHello.connectionIdentifier
			}
		}
	}

	@Test
	void testSpecific() {
		new FcpBuilder(FREENET_HOST, FREENET_PORT).communicate {
			clientHello(clientName: CLIENT_NAME) { NodeHello nodeHello ->
				assertNotNull nodeHello.connectionIdentifier
			}
		}
	}

	@Test
	void testExternal() {
		FcpConnection connection = openConnection()
		try {
			new FcpBuilder(connection).communicate {
				clientHello(clientName: CLIENT_NAME) { NodeHello nodeHello ->
					assertNotNull nodeHello.connectionIdentifier
				}
			}
		}
		finally {
			connection.close()
		}
	}

	@Test(expected = ProtocolErrorException.class)
	void testProtocolError() {
		new FcpBuilder().communicate {
			clientHello(clientName: CLIENT_NAME)
			clientHello(clientName: CLIENT_NAME)
		}
	}

	@Test(expected = DuplicateClientNameException.class)
	void testDuplicateClientName() {
		new FcpBuilder().communicate {
			clientHello(clientName: CLIENT_NAME)
			FcpConnection connection = createConnection()
			try {
				listPeers(identifier: "testListPeers")
			}
			catch ( DuplicateClientNameException e ) {
				throw e
			}
			finally {
				connection.close()
			}
		}
	}

	protected static FcpConnection openConnection() {
		FcpConnection connection = new FcpConnection(FREENET_HOST, FREENET_PORT)
		connection.connect()
		return connection
	}

	protected static FcpConnection createConnection() {
		FcpConnection connection = openConnection()
		new FcpBuilder().communicate {
			clientHello(clientName: CLIENT_NAME)
		}
		return connection
	}
}