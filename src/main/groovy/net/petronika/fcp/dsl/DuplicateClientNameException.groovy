package net.petronika.fcp.dsl

class DuplicateClientNameException extends RuntimeException {

	DuplicateClientNameException() {
		super("The connection is being closed because another connection has been opened with the same client Name")
	}
}
