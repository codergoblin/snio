package codergoblin.snio;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public interface IdGenerator extends Supplier<String> {

	static IdGenerator sequential() {
		AtomicInteger reference = new AtomicInteger(1);
		return () -> String.valueOf(reference.getAndIncrement());
	}

}
