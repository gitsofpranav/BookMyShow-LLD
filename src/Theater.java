import java.util.ArrayList;
import java.util.List;

public class Theater {

    private String theaterId;
    private String name;
    private String location;
    private List<Screen> screens;

    public Theater(String theaterId, String name, String location) {
        this.theaterId = theaterId;
        this.name = name;
        this.location = location;
        this.screens = new ArrayList<>();
    }

    public void addScreen(Screen screen) {
        screens.add(screen);
    }

    public void removeScreen(Screen screen) {
        screens.remove(screen);
    }

    public List<Screen> getScreens() {
        return new ArrayList<>(screens);
    }

    public String getTheaterId() {
        return theaterId;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }
}