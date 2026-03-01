package BookMyShow;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import BookMyShow.exception.InvalidSeatException;
import BookMyShow.exception.SeatAlreadyBookedException;
public class ShowTime {
    private String id;
    private Theatre theatre;
    private long showTimeInMillis;
    private Movie movie;
    private String screen;
    private Map<String,Reservation>reservationsByConfirmationId;
    private Set<String>allSeats;
    
    public ShowTime(String id, Theatre theatre, long showTimeInMillis, Movie movie, String screen){
        this.id = id;
        this.showTimeInMillis = showTimeInMillis;
        this.movie = movie;
        this.screen = screen;
        this.theatre = theatre;
        this.reservationsByConfirmationId = new HashMap<>();
        this.allSeats = createAllSeats();
    }
    public String getId(){return id;}
    public String getScreen(){return screen;}
    public Movie getMovie(){return movie;}
    public long getShowTime(){return showTimeInMillis;}

    public synchronized void book(Reservation newReservation){
         List<String> seatIds = newReservation.getSeatList();

        if (seatIds == null || seatIds.isEmpty()) {
            throw new IllegalArgumentException("Must select at least one seat");
        }

        Set<String>existingSelectedSeats = new HashSet<>();
        for(Reservation exitsingReservation: reservationsByConfirmationId.values()){
            for(String existingselectedSeat: exitsingReservation.getSeatList()){
              existingSelectedSeats.add(existingselectedSeat);
            }
        }

        for(String seat: newReservation.getSeatList()){
            if(!allSeats.contains(seat)){
                throw new InvalidSeatException("Selected Seat is Invalid");
            }
            if (existingSelectedSeats.contains(seat)){
                 throw new SeatAlreadyBookedException("Selected Seat is already booked");
            }
           
        }
        reservationsByConfirmationId.put(newReservation.getConfirmationId(), newReservation);


        /**
         Incase of fine grained locking in order to not block customers selecting d
         different seats for a certain showTime:
         class Seat :
           string id
           lock: lock
           bookedBy: reservation(or null)

           book(reservation){
            seatIds = sorted(reservation.getSeatdIds());
            // sorted locking to avoid deadlock

            for seatId in seatIds:
               seat[seatId].lock.acquire()

            try 
               for seatId in seatIds
                  if seats[seatId].bookedBy != null
                    throw SeatUnavailableException(seatId)
                
                for seatId in seatIds
                  seats[seatId].bookedBy = reservation
                
                reservations.add(reservation)
            finally
                for seatId in seatIds
                    seats[seatdId].lock.release();
           }
         */
    }

    public synchronized void cancel(Reservation reservation){
        reservationsByConfirmationId.remove(reservation.getConfirmationId());
    }

    private Set<String>createAllSeats(){
        Set<String>allSeats = new  HashSet<>();
        for(char row = 'A'; row<='Z'; row++){
            for(int i=0;i<=20;i++){
                String seatString = row + String.format("%02d", i);
                allSeats.add(seatString);
            }
        }
        return allSeats;
    }

    
}
