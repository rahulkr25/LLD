What is a Logger?
A logger is the in-process library an application uses to record what's happening at runtime. Code calls logger.info("user signed in") from anywhere in the app, and the library timestamps the message, attaches the severity level, and writes it to one or more places like the console, a file, or both. Think Log4j, SLF4J, or Python's logging module. We're designing the library that lives inside one application, not a distributed log aggregation service.

## Requirements
1. Five severity levels: DEBUG < INFO < WARN < ERROR < FATAL.
2. Each record carries timestamp, level, message, emitting thread name.
3. Logger writes each record to one or more destinations, set at startup.
4. Each destination(Console, File) has its own min-level threshold and its own format(JSON/Plain text).
5. Concurrent calls are safe. A record's bytes never interleave with another record's bytes on the same destination.

### Out of Scope
- Hot-reloading config at runtime
- Async / buffered writes
-  Remote / network destinations in v1(design should accomodate)
- Hierarchical / named loggers (com.app.service inherting from com.app)

## Core Entities and Relationship
1. LogRecord - An immutable value object carrying the four pieces of per-call data (timestamp, level, message, thread name). Created in Logger.log(), consumed by every destination.
2. Logger - The orchestrator. Holds the immutable list of destinations, exposes log() and convenience methods, captures per-call data (timestamp, thread name) and builds the LogRecord.
3. Destination - One configured output target. Owns its minimum level threshold, holds a reference to its formatter, and serializes the filter-format-write workflow. Where the per-destination lock will live.
4. Formatter - An interface for serializing a LogRecord to a string. Two implementations exist (plain text and JSON), and new formats become new implementations without touching anything else.


## Class Design
Logger

class Logger:
  - destinations : List<Destination>

  + Logger(destinations: List<Destination>)
  + log(level: LogLevel, message: String)
  + info(message: String)
  + debug(message: String)
  + warn(message: String)
  + fatal(message: String)
  + error(message: String)

LogRecord
class LogRecord:
  - timestamp: Instant
  - level: LogLevel
  - message: String
  - threadName: String

  + LogRecord(timestamp, level, message, threadName)
  + getter for all fields

Formatter
interface Formatter:
  + format(record: LogRecord) -> String

class PlainTextFormat implements Formatter
class JsonFormatter implements Formatter

Destination
Good Solution:
abstract class Destination:
   - formatter: Formatter
   - minLevel: LogLevel
   - lock: Lock

   + write(record):
     if record.level<minLevel : return
     formatted = formatter.format(record)
     lock.acquire()
     try:
        doWrite(formatted)
     finally:
        lock.release()
    
    #doWrite(formatted: String) //abstract - subclass fills this in

class ConsoleDestination extens Destination:
     + doWrite(formatted): stdout.write(formatted)
class FileDestination extens Destination:
     - filePath: String
     + dowWrite(formatted): fileWrite(filePath).write(formatted)

Great Solution:
Composition with a Sink Interface

interface Sink:
    + write(formatted: String)
class ConsoleSink implements Sink
class FileSink implements Sink
    - filePath: String
class Destination:
    - formatter: Formatter
    - minLevel: LogLevel
    - sink : Sink

    + Destination(formatter, minLevel, sink)
    + write(record):
       if record.level < minLevel: return
       formatted = formatter.format(record)
       sink.write(formatted)

enum LogLevel:
    DEBUG
    INFO
    WARN
    ERROR
    FATAL
  
## Implementation
Logger

  + log(level, message):
     record = new LogRecord(
        timestamp = now(),
        level = level,
        message = message,
        threadName = currenThread().name
     )
    for destination in destinations:
       destination.write(record)
    
Destination
    + write(record):
      if record.level < minLevel: return
      formatted = formatter.format(record) #If Thread A formats "payment failed" and Thread B formats "user signed in", each thread gets its own separate string object in memory. They might run at the exact same time, but neither thread is changing the other thread's string.
      lock.acquire()
      try: 
        sink.write(formatted)
      catch e:
        stderr.write("logger: sink write failed: " + e.message)
      finally:
        lock.release()

Formatter 
PlainTextFormatter
    + format(record):
      return record.timestamp + " [" + record.level + "] " +
           "[" + record.threadName + "] " + record.message
JsonFormatter
    +  format()
       return jsonEncode({
        "timestamp" : record.timestamp,
        "level":     record.level,
        "thread":    record.threadName,
        "message":   record.message
        })

Sink
ConsoleSink
   + write(formatted):
     stdout.println(formatted)

FileSink(filePath):
    - this.fileWriter = openFile(filePath, mode = APPEND)
    + write(formatted)
       fileWriter.append(formatted + "\n")

LogRecord
LogRecord(timestamp, level, message, threadName):
    this.timestamp  = timestamp
    this.level      = level
    this.message    = message
    this.threadName = threadName

getTimestamp():  return timestamp
getLevel():      return level
getMessage():    return message
getThreadName(): return threadName


## Extensions
1. "How would you make log() non-blocking?"
Using a blocking Queue