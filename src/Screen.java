import java.util.*;

public class Screen {
    private String screenId;
    private String name;
    private List<Seat> seats;

    public Screen(String screenId, String name){
        this.screenId = screenId;
        this.name = name;
        this.seats = new ArrayList<>();
    }

    public void addSeat(Seat seat){
        seats.add(seat);
    }

    public List<Seat> getSeat(){
        return seats;
    }

    public String getScreenId(){
        return screenId;
    }

    public String getName(){
        return name;
    }
}
