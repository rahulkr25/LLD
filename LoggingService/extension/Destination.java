package LoggingService.extension;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import LoggingService.Formatter;
import LoggingService.LogLevel;
import LoggingService.LogRecord;
import LoggingService.Sink;

public class Destination implements AutoCloseable{
    private final Formatter formatter;
    private final LogLevel minLevel;
    private final Sink sink;
    private final BlockingQueue<LogRecord>queue;
    private final Thread worker;
    private volatile boolean running = true;

    public Destination(Formatter formatter, LogLevel minLevel, Sink sink, int capacity) {
        this.formatter = formatter;
        this.minLevel = minLevel;
        this.sink = sink;
        this.queue = new ArrayBlockingQueue<>(capacity);
        this.worker = new Thread(this::drain,"logger-worker");
        this.worker.start();
    }

    public void write(LogRecord record) throws InterruptedException{
        if(!record.getLevel().isAtLeast(minLevel)){
            return;
        }
        queue.put(record);
    }

    private void drain(){
        try{
            while(running || !queue.isEmpty()){
                 LogRecord record = queue.take();
                 String formatted = formatter.format(record);
                 sink.write(formatted);
            }
        }catch(Exception e){
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void close() throws Exception {
        running = false;
        worker.interrupt();
        worker.join();
    }
}
