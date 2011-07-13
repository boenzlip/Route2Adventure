package net.orxonox.gpr.graph;


public class GeoLocation {

  double height;
  double latitude;
  double longitude;
  
  public GeoLocation(double latitude, double longitude, double height) {
    this.latitude = latitude;
    this.longitude = longitude;
    this.height = height;
  }

  
  public double getHeight() {
    return height;
  }

  
  public double getLatitude() {
    return latitude;
  }

  
  public double getLongitude() {
    return longitude;
  }
  
}
