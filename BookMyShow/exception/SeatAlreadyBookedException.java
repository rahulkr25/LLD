package BookMyShow.exception;

public class SeatAlreadyBookedException  extends RuntimeException{
    public SeatAlreadyBookedException(String msg){
        super(msg);
    }
}
