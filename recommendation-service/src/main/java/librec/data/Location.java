package librec.data;

/**
 * store the location information(latitude and longitude)
 *
 * @author Keqiang Wang
 */
public class Location {
    /**
     * the latitude of this locaiton
     */
    private double latitude;

    /**
     * the longitude of this location
     */
    private double longitude;

    /**
     * Construction of location with latitude and longitude
     *
     * @param latitude  the latitude of location to set
     * @param longitude the longitude of location to set
     */
    public Location(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * @return the longitude of this location
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * @param longitude the latitude of this location to set
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * @return the latitude of this location
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * @param latitude the latitude of location to set
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
}
