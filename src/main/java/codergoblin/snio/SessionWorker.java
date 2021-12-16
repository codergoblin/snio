package codergoblin.snio;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.logging.Logger;

import static java.lang.String.format;
import static java.lang.System.arraycopy;
import static java.nio.channels.SelectionKey.OP_READ;
import static java.nio.channels.SelectionKey.OP_WRITE;

public class SessionWorker {

    private final Logger logger = Logger.getLogger(getClass().getName());
    private static final int READ_BUFFER_SIZE = 64;
    private final SessionFactory sessionFactory;

    public SessionWorker(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void publish(SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();
        NioSession session = getSession(key);
        try {
            if (key.isReadable()) {
                readKey(key, channel, session);
            }
            if (key.isWritable()) {
                writeKey(key, channel, session);
            }
        } catch (Exception e) {
            closeSession(key, channel, session);
        }
    }

    private NioSession getSession(SelectionKey key) {
        Object attachment = key.attachment();
        if (attachment == null) {
            NioSession session = sessionFactory.newSession(key);
            key.attach(session);
            return session;
        }
        return (NioSession) attachment;
    }

    private void readKey(
            SelectionKey key,
            SocketChannel channel, NioSession session) throws IOException {

        //TODO any way to put key into read mode twice in a row if reading is not finished?
        ByteBuffer buffer = ByteBuffer.allocate(READ_BUFFER_SIZE);
        int numRead = channel.read(buffer);
        if (numRead == -1) {
            closeSession(key, channel, session);
            return;
        }
        try {
            if (numRead == 0) {
                return;
            }
            byte[] data = new byte[numRead];
            arraycopy(buffer.array(), 0, data, 0, numRead);
            session.onDataReceived(data);
        } finally {
            if (numRead == READ_BUFFER_SIZE) {
                key.interestOps(OP_READ);
            } else {
                key.interestOps(OP_WRITE);
            }
        }
    }

    private void writeKey(
            SelectionKey key,
            SocketChannel channel,
            NioSession session) {
        try {
            byte[] data = session.getData();
            if (data.length > 0) {
                channel.write(ByteBuffer.wrap(data));
            }
        } catch (Exception e) {
            logger.warning(e.getMessage());
        } finally {
            key.interestOps(OP_READ);
        }
    }

    private void closeSession(
            SelectionKey key,
            SocketChannel channel,
            NioSession session) {

        session.close();

        Socket socket = channel.socket();
        SocketAddress remoteAddress = socket.getRemoteSocketAddress();
        logger.info(format("Connection closed by client. remoteAddress: %s", remoteAddress));
        key.cancel();
        try {
            channel.close();
        } catch (IOException e) {
            //todo handle failure
        }
    }

}
