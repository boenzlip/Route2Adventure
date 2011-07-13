package net.orxonox.gpr.graph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;


public class GeoPath {

  List<GeoLocation> points = new ArrayList<GeoLocation>();
  
  public void appendPoint(GeoLocation coordinate) {
    points.add(coordinate);
  }
  
  public Iterator<GeoLocation> iterator() {
    return points.iterator();
  }
  
  public int size() {
    return points.size();
  }
  
}
