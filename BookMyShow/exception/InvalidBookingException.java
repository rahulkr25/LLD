package BookMyShow.exception;

public class InvalidBookingException extends RuntimeException{
    public InvalidBookingException(String msg){
        super(msg);
    }
}
