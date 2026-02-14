

package ParkingLot;

public class Ticket {
   String ticketId;
   String spotId;
   long entryTime;
   VechicleType vechicleType;

   public Ticket(String ticketId, String spotId, long entryTime, VechicleType vechicleType){
       this.ticketId = ticketId;
       this.spotId = spotId;
       this.entryTime = entryTime;
       this.vechicleType = vechicleType;
   }

   String getTicketId(){
       return ticketId;
   }
  
    String getSpotId(){
       return spotId;
   }

    long getEntryTime(){
       return entryTime;
   }

    VechicleType getVechicleType(){
       return vechicleType;
   }
}