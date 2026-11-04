import java.time.LocalDateTime;

public class Show {
    private String showId;
    private Movie movie;
    private Screen screen;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public Show(String showId, Movie movie, Screen screen, LocalDateTime startTime, LocalDateTime endTime){
        this.showId = showId;
        this.movie = movie;
        this.screen = screen;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getshowId(){
        return showId;
    }

    public Movie getMovie(){
        return movie;
    }

    public Screen getScreen(){
        return screen;
    }

     public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }
}
