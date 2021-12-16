package codergoblin.snio;

import java.nio.channels.SelectionKey;

public interface SessionFactory {

	NioSession newSession(SelectionKey key);

}
