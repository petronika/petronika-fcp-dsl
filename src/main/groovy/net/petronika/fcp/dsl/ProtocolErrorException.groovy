package net.petronika.fcp.dsl

import net.pterodactylus.fcp.ProtocolError

class ProtocolErrorException extends RuntimeException {

	private String identifier
	private int code
	private String codeDescription
	private String extraDescription
	private boolean fatal
	private boolean global

	ProtocolErrorException(ProtocolError error) {
		super("(${error.code}) ${error.codeDescription}: ${error.extraDescription}")
		identifier = error.identifier
		code = error.code
		codeDescription = error.codeDescription
		extraDescription = error.extraDescription
		fatal = error.fatal
		global = error.global
	}

	String getIdentifier() {
		return
	}

	int getCode() {
		return
	}

	String getCodeDescription() {
		return
	}

	String getExtraDescription() {
		return
	}

	boolean getFatal() {
		return
	}

	boolean getGlobal() {
		return
	}
}