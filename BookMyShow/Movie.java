package BookMyShow;

public class Movie {
    private String name;
    private String id;

    public Movie(String name, String id){
        this.id  = id;
        this.name = name;
    }
    public String getName(){
        return name;
    }
    public String getId(){
        return id;
    }
}
