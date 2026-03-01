package BookMyShow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import BookMyShow.exception.InvalidBookingException;
import BookMyShow.exception.InvalidShowTimeException;

public class BookMyShow {
    private List<Theatre> theatres;
    private Map<String,Movie>movieById;
    private Map<String, List<ShowTime>>showTimesByMovieId;
    private Map<String, ShowTime>showTimeById;
    private Map<String, Reservation>reservationByConfirmationId;

    public BookMyShow(List<Theatre> theatres){
        this.theatres = theatres;
        this.movieById = new HashMap<>();
        this.showTimesByMovieId = new HashMap<>();
        this.showTimeById = new HashMap<>();
        this.reservationByConfirmationId = new HashMap<>();
        for(Theatre theatre: theatres){
            for(ShowTime showTime: theatre.getShowTime()){
                Movie movie = showTime.getMovie();
                movieById.put(movie.getId(), movie);
                showTimeById.put(showTime.getId(), showTime);
                showTimesByMovieId
                .computeIfAbsent(movie.getId(), k -> new ArrayList<>())
                .add(showTime);
            }
        }
    }

    public Reservation book(String showTimeId, List<String>seats){
        ShowTime showTime = showTimeById.get(showTimeId);
        if (showTime == null || seats == null || seats.isEmpty()) {  
                      throw new InvalidShowTimeException("ShowTime is Invalid");
        }
        
        Reservation reservation = new Reservation(UUID.randomUUID().toString(), showTime, seats);
        showTime.book(reservation);
        reservationByConfirmationId.put(reservation.getConfirmationId(), reservation);
        return reservation;
    }

    public boolean cancel(String confirmationId){
        if(!reservationByConfirmationId.containsKey(confirmationId)){
            throw new InvalidBookingException("Booking doesnt exist");
        }

        Reservation reservation = reservationByConfirmationId.get(confirmationId);
        ShowTime showTime = reservation.getShowTime();
        showTime.cancel(reservation);
        reservationByConfirmationId.remove(confirmationId);

        return true;

    }

    public List<ShowTime>searchByMovieTitle(String movieTitle){
        if (movieTitle == null || movieTitle.isEmpty()) {
            return new ArrayList<>();
        }
        List<ShowTime>availableShowTimes = new ArrayList<>();
        String searchLower = movieTitle.toLowerCase();
        for(Movie movie: movieById.values()){
            if(movie.getName().toLowerCase().contains(searchLower)){
                for(ShowTime showTime : showTimesByMovieId.get(movie.getId())){
                    if(showTime.getShowTime()>System.currentTimeMillis()){
                        availableShowTimes.add(showTime);
                    }
                }
            }
        }
        return availableShowTimes;
    }

    public List<ShowTime>searchByTheatre(Theatre theatre){
         if (theatre == null) {
            return new ArrayList<>();
        }
        List<ShowTime>availableShowTimes = new ArrayList<>();
        for(ShowTime showTime: theatre.getShowTime()){
            if(showTime.getShowTime()>System.currentTimeMillis()){
                    availableShowTimes.add(showTime);
            }
        }
        return availableShowTimes;
    }
    
}
