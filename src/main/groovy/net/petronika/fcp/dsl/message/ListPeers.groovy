package net.petronika.fcp.dsl.message

import net.pterodactylus.fcp.FcpMessage

class ListPeers extends FcpMessage {

	ListPeers() {
		this(null, false, false)
	}

	ListPeers(String identifier) {
		this(identifier, false, false)
	}

	ListPeers(String identifier, boolean withMetadata, boolean withVolatile) {
		super("ListPeers")
		if ( identifier )
			this.identifier = identifier
		this.withMetadata = withMetadata
		this.withVolatile = withVolatile
	}

	void setIdentifier(String identifier) {
		setField("Identifier", identifier)
	}

	void setWithMetadata(boolean withMetadata) {
		setField("WithMetadata", String.valueOf(withMetadata))
	}

	void setWithVolatile(boolean withVolatile) {
		setField("WithVolatile", String.valueOf(withVolatile))
	}
}