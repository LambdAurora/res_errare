package dev.lambdaurora.res_errare.system;

public enum Platform {
	LINUX,
	MACOSX,
	WINDOWS;

	private static final Platform current;

	/**
	 * {@return the platform on which the library is running}
	 */
	public static Platform get() {
		return current;
	}

	static {
		var osName = System.getProperty("os.name");
		if (osName.startsWith("Windows")) {
			current = WINDOWS;
		} else if (osName.startsWith("Linux") || osName.startsWith("FreeBSD")
				|| osName.startsWith("SunOS") || osName.startsWith("Unix")) {
			current = LINUX;
		} else if (osName.startsWith("Mac OS X") || osName.startsWith("Darwin")) {
			current = MACOSX;
		} else {
			throw new LinkageError("Unknown platform: " + osName);
		}
	}
}
