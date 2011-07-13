package net.orxonox.gpr.data;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.orxonox.gpr.graph.GeoLocation;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * @author pboenzli
 *
 */
public class HeightProfileData {

  /**
   * Data structure representing one point in a walking path characterized by:
   * 
   * <lu> <li>distance to the first point in meters</li> <li>walking time needed
   * to get to this point from the starting point in seconds</li> <li>height of
   * this point in meters</li> </lu>
   */
  public static final class Waypoint {
    private GeoLocation coordinate;
    private double distance;
    private double height;
    private double time;
    private 

    Waypoint(GeoLocation coordinate, double distance, double height, double time) {
      this.coordinate = coordinate;
      this.distance = distance;
      this.height = height;
      this.time = time;
    }

    /**
     * @return time to walk from previous point to this in seconds.
     */
    public double getTime() {
      return time;
    }

    /**
     * @return distance from the starting point meters.
     */
    public double getDistance() {
      return distance;
    }

    /**
     * @return height above sea level of this point in meters.
     */
    public double getHeight() {
      return height;
    }

    /**
     * @return the coordinate of this waypoint in lat,lng.
     */
    public GeoLocation getLocation() {
      return coordinate;
    }
  }

  List<Waypoint> dataValues = new LinkedList<Waypoint>();

  public HeightProfileData() {

  }

  /**
   * Adds a new height-distance-time triple to the height profile data
   * container.
   * 
   * @param distance
   *          distance from the first point in meters.
   * @param time
   *          from the first point in seconds.
   * @param height
   *          height of this point in meters.
   */
  public void put(GeoLocation coordinate, double distance, double time, double height) {
    dataValues.add(new Waypoint(coordinate, distance, height, time));
  }

  /**
   * @return iterator to traverse through the data points.
   */
  public Iterator<Waypoint> iterator() {
    return dataValues.iterator();
  }
  
  /**
   * @return the first waypoint in the path.
   */
  public Waypoint first() {
    return dataValues.get(0);
  }
  
  /**
   * @return the last waypoint in the path.
   */
  public Waypoint last() {
    return dataValues.get(dataValues.size() - 1);
  }
}
