package net.petronika.fcp.dsl.message

import net.pterodactylus.fcp.FcpMessage

class ListPeerNotes extends FcpMessage {

	ListPeerNotes() {
		this(null)
	}

	ListPeerNotes(String nodeIdentifier) {
		super("ListPeerNotes")
		if ( nodeIdentifier )
			this.nodeIdentifier = nodeIdentifier
	}

	void setNodeIdentifier(String nodeIdentifier) {
		setField("NodeIdentifier", nodeIdentifier)
	}
}