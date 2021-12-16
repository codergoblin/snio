package codergoblin.snio;

import java.nio.channels.SelectionKey;

public class BufferedSessionFactory implements SessionFactory {

	private final SessionManager sessionManager;
	private final IdGenerator idGenerator;

	public BufferedSessionFactory(
			SessionManager sessionManager,
			IdGenerator idGenerator) {
		this.sessionManager = sessionManager;
		this.idGenerator = idGenerator;
	}

	@Override
	public NioSession newSession(SelectionKey key) {
		return new NioBufferedSession(idGenerator.get(), sessionManager.get());
	}

}
