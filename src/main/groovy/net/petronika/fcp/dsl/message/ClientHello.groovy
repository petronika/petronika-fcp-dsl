package net.petronika.fcp.dsl.message

import net.pterodactylus.fcp.FcpMessage

class ClientHello extends FcpMessage {

	ClientHello() {
		this(null, "2.0")
	}

	ClientHello(String clientName) {
		this(clientName, "2.0")
	}

	ClientHello(String clientName, String expectedVersion) {
		super("ClientHello")
		if ( clientName )
			this.clientName = clientName
		this.expectedVersion = expectedVersion
	}

	void setClientName(String clientName) {
		setField("Name", clientName)
	}

	void setExpectedVersion(String expectedVersion) {
		setField("ExpectedVersion", expectedVersion)
	}
}