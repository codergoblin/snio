package codergoblin.snio;

public interface SessionActor {

	void init(SessionStream stream);

	void accept(SessionStream stream);

	void destroy(SessionStream SessionStream);

}
