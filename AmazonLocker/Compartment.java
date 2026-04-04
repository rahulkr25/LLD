package AmazonLocker;

public class Compartment {
    String id;
    Size size;
    boolean occupied;

    public Compartment(String id, Size size){
        this.id = id;
        this.size = size;
        this.occupied = false;
    }

    public void markOccupied(){
        this.occupied = true;
    }

    public void markFree(){
        this.occupied = false;
    }
    
    public void open(){
        // DO Something
    }

}
