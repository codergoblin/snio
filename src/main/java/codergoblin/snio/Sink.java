package codergoblin.snio;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

import static java.nio.ByteBuffer.allocate;

public class Sink {

	private final AtomicInteger inflation = new AtomicInteger(0);
	private final ByteBuffer buffer;
	private final int size;

	public Sink(int size) {
		this.size = size;
		this.buffer = allocate(size);
	}

	public void push(byte[] data) {
		int newSize = inflation.get() + data.length;
		if (newSize > size) {
			throw new IndexOutOfBoundsException();
		}
		buffer.put(data);
		inflation.set(newSize);
	}

	public byte[] drain() {
		if (inflation.get() == 0) {
			return new byte[] {};
		}
		byte[] destination = new byte[inflation.getAndSet(0)];
		buffer.flip();
		buffer.get(destination);
		buffer.compact();
		buffer.clear();
		return destination;
	}

	public void clear() {
		inflation.set(0);
		buffer.clear();
	}

}
