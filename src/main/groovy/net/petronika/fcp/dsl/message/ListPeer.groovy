package net.petronika.fcp.dsl.message

import net.pterodactylus.fcp.FcpMessage

class ListPeer extends FcpMessage {

	ListPeer() {
		this(null, false, false)
	}

	ListPeer(String nodeIdentifier) {
		this(nodeIdentifier, false, false)
	}

	ListPeer(String nodeIdentifier, boolean withMetadata, boolean withVolatile) {
		super("ListPeer")
		if ( nodeIdentifier )
			this.nodeIdentifier = nodeIdentifier
		this.withMetadata = withMetadata
		this.withVolatile = withVolatile
	}

	void setNodeIdentifier(String nodeIdentifier) {
		setField("NodeIdentifier", nodeIdentifier)
	}

	void setWithMetadata(boolean withMetadata) {
		setField("WithMetadata", String.valueOf(withMetadata))
	}

	void setWithVolatile(boolean withVolatile) {
		setField("WithVolatile", String.valueOf(withVolatile))
	}
}