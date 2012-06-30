package net.petronika.fcp.dsl.message

import net.pterodactylus.fcp.FcpMessage
import net.pterodactylus.fcp.FcpUtils
import net.pterodactylus.fcp.Persistence
import net.pterodactylus.fcp.Priority
import net.pterodactylus.fcp.ReturnType
import net.pterodactylus.fcp.Verbosity

class ClientGet extends FcpMessage {

	ClientGet() {
		this(null, null)
	}

	ClientGet(String uri, String identifier) {
		super("ClientGet")
		if ( uri != null )
			this.uri = uri
		if ( identifier != null )
			this.identifier = identifier
	}

	void setUri(String uri) {
		setField("URI", uri)
	}

	void setIdentifier(String identifier) {
		setField("Identifier", identifier)
	}

	void setIgnoreDataStore(boolean ignoreDataStore) {
		setField("IgnoreDS", String.valueOf(ignoreDataStore))
	}

	void setDataStoreOnly(boolean dsOnly) {
		setField("DSonly", String.valueOf(dsOnly))
	}

	void setVerbosity(Verbosity verbosity) {
		setField("Verbosity", String.valueOf(verbosity))
	}

	void setMaxSize(long maxSize) {
		setField("MaxSize", String.valueOf(maxSize))
	}

	void setMaxTempSize(long maxTempSize) {
		setField("MaxTempSize", String.valueOf(maxTempSize))
	}

	void setMaxRetries(int maxRetries) {
		setField("MaxRetries", String.valueOf(maxRetries))
	}

	void setPriority(Priority priority) {
		setField("PriorityClass", String.valueOf(priority))
	}

	void setPersistence(Persistence persistence) {
		setField("Persistence", String.valueOf(persistence))
	}

	void setClientToken(String clientToken) {
		setField("ClientToken", clientToken)
	}

	void setGlobal(boolean global) {
		setField("Global", String.valueOf(global))
	}

	void setBinaryBlob(boolean binaryBlob) {
		setField("BinaryBlob", String.valueOf(binaryBlob))
	}

	void setAllowedMimeTypes(String... allowedMimeTypes) {
		setField("AllowedMIMETypes", FcpUtils.encodeMultiStringField(allowedMimeTypes))
	}

	void setFilename(String filename) {
		setField("Filename", filename)
	}

	void setTempFilename(String tempFilename) {
		setField("TempFilename", tempFilename)
	}
}