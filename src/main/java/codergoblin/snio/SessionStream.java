package codergoblin.snio;

public interface SessionStream {

	String getId();
	void write(byte[] data);
	byte[] read();

}
