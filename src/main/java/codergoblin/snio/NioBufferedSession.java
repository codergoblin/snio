package codergoblin.snio;

public class NioBufferedSession implements NioSession {

	//todo configure buffer size
	private static final int OUTPUT_SIZE = 8192;
	private static final int INPUT_SIZE = 1024;

	private final Sink output = new Sink(OUTPUT_SIZE);
	private final Sink input = new Sink(INPUT_SIZE);

	private final String id;
	private final SessionActor sessionActor;

	public NioBufferedSession(
			String id,
			SessionActor sessionActor) {
		this.id = id;
		this.sessionActor = sessionActor;
		sessionActor.init(this);
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void write(byte[] data) {
		//todo another limit protection...
		output.push(data);
	}

	@Override
	public byte[] read() {
		return input.drain();
	}

	@Override
	public void onDataReceived(byte[] data) {
		//todo add limit protection.. circular buffer?
		input.push(data);
	}

	@Override
	public byte[] getData() {
		sessionActor.accept(this);
		input.clear();
		return output.drain();
	}

	@Override
	public void close() {
		sessionActor.destroy(this);
		input.clear();
		output.clear();
	}

}
