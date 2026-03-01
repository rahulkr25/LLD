package BookMyShow;

import java.util.List;

public class Reservation {
    private String confirmationId;
    private ShowTime showTime;
    private List<String>seatList;
    
    public Reservation(String confirmationId,  ShowTime showTime, List<String>seaList){
        this.confirmationId = confirmationId;
        this.showTime = showTime;
        this.seatList = seaList;
    }

    public String getConfirmationId(){return confirmationId;}
    public ShowTime getShowTime(){return showTime;}
    public List<String>getSeatList(){return seatList;}

    
}
