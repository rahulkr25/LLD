package LoggingService;

public class PlainTextFormatter  implements Formatter{

    @Override
    public String format(LogRecord record) {
        return record.getTimeStamp() + " [" + record.getLevel() + "] ["
            + record.getThreadName() + "] " + record.getMessage();
    }
    
}
