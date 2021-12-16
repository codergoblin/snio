package codergoblin.snio;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.logging.Logger;

import static java.nio.channels.SelectionKey.OP_ACCEPT;
import static java.nio.channels.SelectionKey.OP_WRITE;

public class NioServer {

    private final Logger logger = Logger.getLogger(getClass().getName());
    private final SocketAddress bindAddress;
    private final SessionWorker sessionWorker;

    private ServerSocketChannel serverChannel;
    private Selector selector;

    //todo handler multithreading
    public NioServer(SocketAddress bindAddress, SessionFactory factory) {
        this.bindAddress = bindAddress;
        this.sessionWorker = new SessionWorker(factory);
    }

    public boolean isRunning() {
        return serverChannel != null;
    }

    public synchronized void stop() throws IOException {
        if (isRunning()) {
            logger.info("Stopping server... " + bindAddress);
            serverChannel.close();
            serverChannel = null;
            selector.close();
            selector = null;
        }
    }

    public void start() throws IOException {

        if (isRunning()) {
            stop();
        }
        synchronized (this) {
            logger.info("Starting server on " + bindAddress);
            serverChannel = ServerSocketChannel.open();
            serverChannel.socket().bind(bindAddress);
            serverChannel.configureBlocking(false);

            selector = Selector.open();
            serverChannel.register(selector, OP_ACCEPT);
        }

        mainLoop();
    }

    public void flush() {
        try {
            if (!selector.isOpen()) {
                return;
            }

            boolean shouldWakeUp = false;
            for (SelectionKey key : selector.keys()) {
                if (!key.isValid()) {
                    continue;
                }
                if (key.isWritable()) {
                    logger.info("flushing key to write " + key);
                    key.interestOps(SelectionKey.OP_WRITE);
                    shouldWakeUp = true;
                }
            }
            if (shouldWakeUp) {
                selector.wakeup();
            }
        }
        catch (Throwable e) {
            logger.warning(e.getMessage());
        }
    }

    private void mainLoop() throws IOException {
        while (isRunning()) {
            selector.select(key -> {
                if (!key.isValid()) {
                    return;
                }
                if (key.isAcceptable()) {
                    logger.info("accepting key " + key);
                    acceptConnection();
                } else if (key.isWritable() || key.isReadable()) {
                    sessionWorker.publish(key);
                }
            });
        }
    }

    private void acceptConnection() {
        try {
            SocketChannel channel = serverChannel.accept();
            channel.configureBlocking(false);
            Socket socket = channel.socket();
            SocketAddress remoteAddr = socket.getRemoteSocketAddress();
            logger.info("Connected to: " + remoteAddr);
            channel.register(selector, OP_WRITE);
        }
        catch (Exception e) {
            logger.info(e.getMessage());
        }
    }

}
