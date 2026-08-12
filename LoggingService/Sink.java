package LoggingService;

public interface Sink {
    void write(String formatted) throws Exception;
}
