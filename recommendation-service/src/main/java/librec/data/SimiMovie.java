package librec.data;

import service.data.domain.entity.Product;

import java.util.List;

/**
 * Created by wangkeqiang on 17-6-24.
 */
public class SimiMovie {
    public String  ratedId;
    public List<Movie> movies;

    public SimiMovie() {
    }

    public SimiMovie(String ratedId, List<Movie> movies) {
        this.ratedId = ratedId;
        this.movies = movies;
    }

    public String getRatedId() {
        return ratedId;
    }

    public void setRatedId(String ratedId) {
        this.ratedId = ratedId;
    }

    public List<Movie> getMovies() {
        return movies;
    }

    public void setMovies(List<Movie> movies) {
        this.movies = movies;
    }
}
