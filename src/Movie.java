public class Movie {

    private String movieId;
    private String title;
    private int durationMinutes;
    private String language;
    private String genre;

    public Movie(String movieId, String title,
                 int durationMinutes,
                 String language, String genre) {

        this.movieId = movieId;
        this.title = title;
        this.durationMinutes = durationMinutes;
        this.language = language;
        this.genre = genre;
    }

    public String getMovieId() {
        return movieId;
    }

    public String getTitle() {
        return title;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }
}