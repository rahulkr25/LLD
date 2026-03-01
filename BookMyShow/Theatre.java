package BookMyShow;

import java.util.List;

public class Theatre {
    private String id;
    private String name;
    private String address;
    private List<ShowTime>showTimes;

    public Theatre(String id, String name, String address, List<ShowTime>showTimes){
        this.id = id;
        this.name = name;
        this.address = address;
        this.showTimes = showTimes;
    }

    public List<ShowTime> getShowTime(){
        return showTimes;
    }
    
}
