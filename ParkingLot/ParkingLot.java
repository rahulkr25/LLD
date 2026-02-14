package ParkingLot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ParkingLot {
   List<ParkingSpot>parkingSpots;
   Map<String,Ticket>activeTickets;
   Set<String>occupiedSpots;
   long hourlyCents;
   ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

   public ParkingLot(List<ParkingSpot>parkingSpots, long hourlyCents){
       this.parkingSpots = parkingSpots;
       this.activeTickets = new HashMap<>();
       this.occupiedSpots = new HashSet<>();
       this.hourlyCents = hourlyCents;
   }
   public Ticket entry(VechicleType vechicleType){
       while(true){
           SpotType spotType = mapVehicleTypeToSpotType(vechicleType);
          
           ParkingSpot parkingSpot = findParkingSpot(spotType);
           if(parkingSpot == null){
               throw new RuntimeException("No Spots Available at the moment");
           }
           readWriteLock.writeLock().lock();
           try{
               // Double-check: spot might have been taken
               if(!occupiedSpots.contains(parkingSpot.getSpotId())){
                   occupiedSpots.add(parkingSpot.getSpotId());
                   Ticket ticket = new Ticket(UUID.randomUUID().toString(), parkingSpot.getSpotId(), System.currentTimeMillis(), vechicleType);
                   activeTickets.put(ticket.getTicketId(), ticket);
                   return ticket;
               }
           }finally{
               readWriteLock.writeLock().unlock();
           }
       }
  
   }
 

   double exit(Ticket ticket){
       if(ticket==null || !activeTickets.containsKey(ticket.getTicketId())){
           throw new RuntimeException("Invalid Ticket");
       }
       double cost = computeCost(ticket);
       activeTickets.remove(ticket.getTicketId());
       occupiedSpots.remove(ticket.getSpotId());
       return cost;

   }

   private long computeCost(Ticket ticket){
       long exitTime = System.currentTimeMillis();
       long duration = exitTime - ticket.getEntryTime();
       long hours = (duration / (60*60*1000));
       if(duration % (60*60*1000)>0)hours++;

       return (hourlyCents * hours)/(1000);
   }
   private ParkingSpot findParkingSpot(SpotType spotType) {
       readWriteLock.readLock().lock();
       try{
           // Safe to read occupiedSpotIds - no writers can modify
           for(ParkingSpot parkingSpot: parkingSpots){
               if(!occupiedSpots.contains(parkingSpot.getSpotId()) && parkingSpot.getSpotType() == spotType){
                       return parkingSpot;
               }
           }
          return null;
      }finally{
       readWriteLock.readLock().unlock();
      }
   }

   private SpotType mapVehicleTypeToSpotType(VechicleType vechicleType) {
       if(vechicleType == VechicleType.BIKE)return SpotType.BIKE;
       if(vechicleType == VechicleType.CAR)return SpotType.CAR;
       if(vechicleType == VechicleType.BIG_VEHICLE)return SpotType.BIG_VEHICLE;
       throw new RuntimeException("Unsupported Vehicle Type");
   }
   public static void main(String[] args) {
       // Create parking spots
       List<ParkingSpot> spots = new ArrayList<>();
      
       // Add some bike spots
       for (int i = 1; i <= 3; i++) {
           ParkingSpot bikeSpot = new ParkingSpot();
           bikeSpot.spotId = "BIKE-" + i;
           bikeSpot.spotType = SpotType.BIKE;
           spots.add(bikeSpot);
       }
      
       // Add some car spots
       for (int i = 1; i <= 5; i++) {
           ParkingSpot carSpot = new ParkingSpot();
           carSpot.spotId = "CAR-" + i;
           carSpot.spotType = SpotType.CAR;
           spots.add(carSpot);
       }
      
       // Add some big vehicle spots
       for (int i = 1; i <= 2; i++) {
           ParkingSpot bigVehicleSpot = new ParkingSpot();
           bigVehicleSpot.spotId = "BIG-" + i;
           bigVehicleSpot.spotType = SpotType.BIG_VEHICLE;
           spots.add(bigVehicleSpot);
       }
      
       // Create parking lot with hourly rate of $5.00 (500 cents)
       ParkingLot parkingLot = new ParkingLot(spots, 500);
      
       System.out.println("=== Parking Lot Test ===");
      
       try {
           // Test 1: Park a bike
           System.out.println("\n1. Parking a bike...");
           Ticket bikeTicket = parkingLot.entry(VechicleType.BIKE);
           System.out.println("Bike parked successfully! Ticket ID: " + bikeTicket.getTicketId() +
                            ", Spot: " + bikeTicket.getSpotId());
          
           // Test 2: Park a car
           System.out.println("\n2. Parking a car...");
           Ticket carTicket = parkingLot.entry(VechicleType.CAR);
           System.out.println("Car parked successfully! Ticket ID: " + carTicket.getTicketId() +
                            ", Spot: " + carTicket.getSpotId());
          
           // Test 3: Park a big vehicle
           System.out.println("\n3. Parking a big vehicle...");
           Ticket bigVehicleTicket = parkingLot.entry(VechicleType.BIG_VEHICLE);
           System.out.println("Big vehicle parked successfully! Ticket ID: " + bigVehicleTicket.getTicketId() +
                            ", Spot: " + bigVehicleTicket.getSpotId());
          
           // Simulate some time passing (1 second for demo)
           Thread.sleep(1000);
          
           // Test 4: Exit bike
           System.out.println("\n4. Exiting bike...");
           double bikeCost = parkingLot.exit(bikeTicket);
           System.out.println("Bike exit successful! Cost: $" + String.format("%.2f", bikeCost));
          
           // Test 5: Exit car
           System.out.println("\n5. Exiting car...");
           double carCost = parkingLot.exit(carTicket);
           System.out.println("Car exit successful! Cost: $" + String.format("%.2f", carCost));
          
           // Test 6: Try to exit with invalid ticket
           System.out.println("\n6. Testing invalid ticket exit...");
           try {
               parkingLot.exit(bikeTicket); // Already exited
           } catch (RuntimeException e) {
               System.out.println("Expected error: " + e.getMessage());
           }
          
           // Test 7: Park another bike in the now-available spot
           System.out.println("\n7. Parking another bike...");
           Ticket anotherBikeTicket = parkingLot.entry(VechicleType.BIKE);
           System.out.println("Another bike parked successfully! Ticket ID: " + anotherBikeTicket.getTicketId() +
                            ", Spot: " + anotherBikeTicket.getSpotId());
          
           // Test 8: Fill up all bike spots and try to park another
           System.out.println("\n8. Filling remaining bike spots...");
           Ticket bike2 = parkingLot.entry(VechicleType.BIKE);
           Ticket bike3 = parkingLot.entry(VechicleType.BIKE);
           System.out.println("Filled bike spots: " + bike2.getSpotId() + ", " + bike3.getSpotId());
          
           System.out.println("\n9. Trying to park when no bike spots available...");
           try {
               parkingLot.entry(VechicleType.BIKE);
           } catch (RuntimeException e) {
               System.out.println("Expected error: " + e.getMessage());
           }
          
           System.out.println("\n=== Test Complete ===");
          
       } catch (Exception e) {
           System.err.println("Test failed: " + e.getMessage());
           e.printStackTrace();
       }
   }
}