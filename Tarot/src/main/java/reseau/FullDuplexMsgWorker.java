package reseau;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;



/*
 * $Id: FullDuplexMsgWorker.java 30 2016-06-02 13:31:57Z conan $
 */
/**
 * 
 * @author Denis Conan
 * @author Christian Bac
 * @author Éric Lallet
 *
 */
public class FullDuplexMsgWorker implements AutoCloseable {
	/**
	 * boolean allowing to add some debug printing.
	 */
	@SuppressWarnings("unused")
	private boolean debug = true;
	/**
	 * this arrays can contain message headers in the first buffer (fixed size) and
	 * message body which size is described in the header We need two byte buffers
	 * due to asynchronism in input and output. put and get operations are not done
	 * at once.
	 */
	private ByteBuffer[] inBuffers;
	/**
	 * this arrays can contain message headers in the first buffer (fixed size) and
	 * message body which size is described in the header We need two byte buffers
	 * due to asynchronism in input and output. put and get operations are not done
	 * at once.
	 */
	private ByteBuffer[] outBuffers;

	/**
	 * read message status, to describe completeness of data reception.
	 */
	private ReadMessageStatus readState;
	/**
	 * socket connected to the other side.
	 */
	private SocketChannel rwChan = null;
	/**
	 * type of the received message.
	 */
	private int inType;
	/**
	 * size of the received message.
	 */
	private int inSize;

	/**
	 * public ctr for an open channel i.e. after accept.
	 * 
	 * @param clientChan the socketChannel that has been accepted on server
	 */
	public FullDuplexMsgWorker(final SocketChannel clientChan) {
		inBuffers = new ByteBuffer[2]; // inBuffers[0] for header, inBuffers[1] for data
		outBuffers = new ByteBuffer[2]; // outBuffers[0] for header, outBuffers[1] for data
		inBuffers[0] = ByteBuffer.allocate(Integer.BYTES * 2); // header contains 2 int:
		outBuffers[0] = ByteBuffer.allocate(Integer.BYTES * 2); // messType and messSize
		inBuffers[1] = null;
		outBuffers[1] = null;
		readState = ReadMessageStatus.ReadUnstarted; // not yet started to read
		rwChan = clientChan;
	}

	/**
	 * To configure the channel in non blocking mode.
	 * 
	 * @throws IOException the exception thrown in case of IO problem.
	 */
	public void configureNonBlocking() throws IOException {
		rwChan.configureBlocking(false);
	}

	/**
	 * get the current channel of this worker.
	 * 
	 * @return my channel
	 */
	public SocketChannel getChannel() {
		return rwChan;
	}

	/**
	 * send a message using channel.
	 * 
	 * @param type message type
	 * @param s    the message content is a String
	 * @return size of the data send
	 * @throws IOException the exception thrown in case of IO problem.
	 */
	public long sendMsg(final int type, final Serializable s) throws IOException {
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		int size;
		ObjectOutputStream oo = new ObjectOutputStream(bo);
		oo.writeObject(s);
		oo.close();
		size = bo.size();
		setHeader(type, size);

		outBuffers[1] = ByteBuffer.allocate(size);
		outBuffers[1].put(bo.toByteArray());
		bo.close();
		outBuffers[1].flip();
		sendBuffers();
		return size;

	}

	/**
	 * send the buffers through the connection.
	 * 
	 * @throws IOException the exception thrown in case of IO problem.
	 */
	public void sendBuffers() throws IOException {
		rwChan.write(outBuffers);
	}

	/**
	 * Set the header of the send message.
	 * @param type message type
	 * @param size of the data send
	 */
	public void setHeader(final int type, final int size) {
		outBuffers[0].clear();
		outBuffers[0].putInt(type);
		outBuffers[0].putInt(size);
		outBuffers[0].flip();
	}

	/**
	 * initialize content with data to be send.
	 * 
	 * @param outdata data to be send
	 */
	public void setDataByteBuffer(final ByteBuffer outdata) {
		outBuffers[1] = outdata;
	}

	/**
	 * close the channel.
	 * 
	 * @throws IOException the exception thrown in case of IO problem.
	 */
	public void close() throws IOException {
		rwChan.close();
	}

	/**
	 * reads a message.
	 * 
	 * @return a ReadMessageStatus to specify read progress
	 */
	public ReadMessageStatus readMessage() {
		int recvSize;

		// first usage of readMessage()
		if (readState == ReadMessageStatus.ReadUnstarted) {
			inBuffers[0].clear();
			readState = ReadMessageStatus.ReadHeaderStarted;
		}

		// new call readMessage() when data are completed
		// -> start the reception of a new message
		if (readState == ReadMessageStatus.ReadDataCompleted) {
			inBuffers[0].clear();
			inBuffers[1] = null;
			readState = ReadMessageStatus.ReadHeaderStarted;
		}

		if (readState == ReadMessageStatus.ReadHeaderStarted) {
			if (inBuffers[0].position() < inBuffers[0].capacity()) {
				try {
					recvSize = rwChan.read(inBuffers[0]);
					final int size = recvSize;


					// in case of polling mode, read can return 0
					// no data has been read, readState is not changed
					if (recvSize == 0) {
						return readState;
					}

					// the connection has been closed by the other side
					if (recvSize < 0) {
						readState = ReadMessageStatus.ChannelClosed;
						close();
						return readState;
					}

					// the header is not completed: stay in the same state
					if (inBuffers[0].position() < inBuffers[0].capacity()) {
						return readState;
					}
				} catch (IOException ie) {
					readState = ReadMessageStatus.ChannelClosed;
					try {
						close();
					} catch (IOException closeException) {

					}
					return readState;

				}
			}

			// inBuffers[0].position() == inBuffers[0].capacity(): the header is completed

			inBuffers[0].flip();


			inType = inBuffers[0].getInt();
			inSize = inBuffers[0].getInt();
			
			readState = ReadMessageStatus.ReadHeaderCompleted;

		}

		if (readState == ReadMessageStatus.ReadHeaderCompleted) {
			if (inBuffers[1] == null || inBuffers[1].capacity() != inSize) {
				inBuffers[1] = ByteBuffer.allocate(inSize);
			}
			// else : the previous buffer has the correct size, we can
			// reuse it. The rewind has be done in the getData() methode.

			readState = ReadMessageStatus.ReadDataStarted;
		}

		if (readState == ReadMessageStatus.ReadDataStarted) {
			if (inBuffers[1].position() < inBuffers[1].capacity()) {
				try {
					recvSize = rwChan.read(inBuffers[1]);
					final int size = recvSize;


					// in case of polling mode, read can return 0
					// no data has been read, readState is not changed
					if (recvSize == 0) {
						return readState;
					}

					// the connection has been closed by the other side
					if (recvSize < 0) {
						close();
						readState = ReadMessageStatus.ChannelClosed;
						return readState;
					}
				} catch (IOException ie) {
					readState = ReadMessageStatus.ChannelClosed;
					try {
						close();
					} catch (IOException closeException) {
					}
					return readState;
				}
			}

			

			if (inBuffers[1].position() == inBuffers[1].capacity()) {
				readState = ReadMessageStatus.ReadDataCompleted;
			}
		}
		return readState;

	}

	/**
	 * return the Serializable data build out of the data part of the received
	 * message when the readStat is ReadDataCompleted. This operation should be
	 * stateless for the ByteBuffers, meaning that we can getData and after write
	 * the ByteBuffer if necessary
	 * 
	 * @throws IOException the exception thrown in case of IO problem.
	 * @return unserialized data
	 */
	public Serializable getData() throws IOException {
		Serializable res = null;
		if (readState == ReadMessageStatus.ReadDataCompleted) {
			try {
				inBuffers[1].flip();
				byte[] cpBb = new byte[inBuffers[1].limit()];
				inBuffers[1].get(cpBb);
				ByteArrayInputStream bi = new ByteArrayInputStream(inBuffers[1].array());
				ObjectInputStream oi = new ObjectInputStream(bi);
				res = (Serializable) oi.readObject();
				oi.close();
				bi.close();
			} catch (ClassNotFoundException ce) {
				ce.printStackTrace();
			}
		}

		// the inBuffers[1] can be used for the next message, if the data has the same
		// size.
		// so we rewind it.
		inBuffers[1].rewind();
		return res;
	}

	/**
	 * get the message type.
	 * 
	 * @return value of message type
	 */
	public int getMessType() {
		return inType;
	}

	/**
	 * get the message size if the status is at least ReadHeaderCompleted.
	 * 
	 * @return message size
	 */
	public int getMessSize() {
		return inSize;
	}

	/**
	 * get direct access to byteBuffers.
	 * 
	 * @return the ByteBuffers
	 */
	public ByteBuffer[] getByteBuffers() {
		return inBuffers;
	}

	/**
	 * passThroughOutBuffers is used to send prepared byteBuffers through a channel.
	 * 
	 * @param setBbs ByteBuffers to send
	 * @throws IOException the exception thrown in case of IO problem.
	 */
	public void passThroughOutBuffers(final ByteBuffer[] setBbs) throws IOException {
		outBuffers[0].clear();
		setBbs[0].rewind();
		do {
			outBuffers[0].put(setBbs[0].get());
		} while (setBbs[0].position() < setBbs[0].capacity());
		outBuffers[0].flip();

		outBuffers[1] = setBbs[1];
		outBuffers[1].rewind();
		rwChan.write(outBuffers);
	}

}
