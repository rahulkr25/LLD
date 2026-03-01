package BookMyShow.exception;

public class InvalidShowTimeException extends RuntimeException{
    public InvalidShowTimeException(String msg){
        super(msg);
    }
}
