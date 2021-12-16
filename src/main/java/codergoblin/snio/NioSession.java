package codergoblin.snio;

public interface NioSession extends SessionStream {

	void onDataReceived(byte[] data);

	byte[] getData();

	void close();

}
