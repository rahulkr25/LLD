package BookMyShow.exception;

public class InvalidSeatException extends RuntimeException{
    public InvalidSeatException(String msg){
        super(msg);
    }
}
