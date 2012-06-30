package net.petronika.fcp.dsl

import net.pterodactylus.fcp.UnknownNodeIdentifier

class UnknownNodeIdentifierException extends RuntimeException {

	private String nodeIdentifier

	public UnknownNodeIdentifierException(UnknownNodeIdentifier unknownNodeIdentifier) {
		super("Unknown node identifier: ${unknownNodeIdentifier.nodeIdentifier}")
		nodeIdentifier = unknownNodeIdentifier.nodeIdentifier
	}

	String getNodeIdentifier() {
		return nodeIdentifier
	}
}