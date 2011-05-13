package net.orxonox.gpr.data;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


public class HeightProfileData {

  public static final class HeightTuple {
    private double distance;
    private double height;
    
    HeightTuple(double distance, double height) {
      this.distance = distance;
      this.height = height;
    }

    
    public double getDistance() {
      return distance;
    }

    
    public double getHeight() {
      return height;
    }
  }
  
  List<HeightTuple> dataValues = new LinkedList<HeightTuple>();
  
  public HeightProfileData() {
    
  }
  
  
  public void put(double distance, double height) {
    dataValues.add(new HeightTuple(distance, height));
  }
  
  public Iterator<HeightTuple> iterator() {
    return dataValues.iterator();
  }
}
