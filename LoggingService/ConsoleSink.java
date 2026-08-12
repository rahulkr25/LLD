package LoggingService;

public class ConsoleSink implements Sink{

    @Override
    public void write(String formatted) throws Exception {
       System.out.println(formatted);
    }
    
}
