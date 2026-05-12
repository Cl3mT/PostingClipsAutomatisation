package reseau;

/*
 * $Id: ReadMessageStatus.java 30 2016-06-02 13:31:57Z conan $
 */
public enum ReadMessageStatus {
	/**
	 * read has not started, yet.
	 */
	ReadUnstarted("ReadUnstarted"),
	/**
	 * reading the header.
	 */
	ReadHeaderStarted("ReadHeaderStarted"),
	/**
	 * the reading of the header has been completed. The header is available for
	 * analysis.
	 */
	ReadHeaderCompleted("ReadHeaderCompleted"),
	/**
	 * reading the body of the message.
	 */
	ReadDataStarted("ReadDataStarted"),
	/**
	 * the reading of the body has been completed. The message is entirely
	 * available.
	 */
	ReadDataCompleted("ReadDataCompleted"),
	/**
	 * the channel has been closed while reading.
	 */
	ChannelClosed("ChannelClosed");

	/**
	 * string name used in toString().
	 */
	private final String name;

	/**
	 * Constructor: initialise the string name.
	 * 
	 * @param name
	 */
	ReadMessageStatus(final String name) {
		this.name = name;
	}

	/**
	 * 
	 * @return string name of the state
	 */
	@Override
	public String toString() {
		return this.name;
	}

}
