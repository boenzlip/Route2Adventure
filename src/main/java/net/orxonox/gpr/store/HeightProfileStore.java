package net.orxonox.gpr.store;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.orxonox.gpr.data.HeightProfileData;
import net.orxonox.gpr.data.MapsTileRouteData;
import net.orxonox.gpr.graph.BasicDirectedXYZNode;

import org.geotools.referencing.CRS;
import org.geotools.referencing.GeodeticCalculator;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class HeightProfileStore implements
    IStore<MapsTileRouteData, HeightProfileData> {

  public void init() {

  }

  public void teardown() {

  }

  public HeightProfileData aquire(MapsTileRouteData descriptor) {
    HeightProfileData profileData = new HeightProfileData();

    // Use google maps compatible mercator projection EPSG:3785.
    CoordinateReferenceSystem sphericalMercator = null;
    try {
      sphericalMercator = CRS.decode("EPSG:3785");
    } catch (NoSuchAuthorityCodeException e) {
      e.printStackTrace();
    } catch (FactoryException e) {
      e.printStackTrace();
    }
    GeodeticCalculator calc = new GeodeticCalculator(sphericalMercator);

    double totalDistance = 0.0;
    double totalTime = 0.0;
    BasicDirectedXYZNode lastNode = null;

    @SuppressWarnings("unchecked")
    Iterator<BasicDirectedXYZNode> points = descriptor.getPath().iterator();
    List<BasicDirectedXYZNode> reversePoints = new ArrayList<BasicDirectedXYZNode>(
        descriptor.getPath().size());
    while (points.hasNext()) {
      reversePoints.add(0, points.next());
    }

    points = reversePoints.iterator();
    while (points.hasNext()) {

      BasicDirectedXYZNode node = (BasicDirectedXYZNode) points.next();
      double distance = 0; // meters.
      double time = 0; // seconds.
      double horizWalkingSpeed = 1000.0 / (60.0 * 60.0); // 1000m horizontal [m/s]
      double vertWalkingSpeed = 400.0 / (60.0 * 60.0); // 400m vertical path [m/s]
      if (lastNode != null) {
        calc.setStartingGeographicPoint(lastNode.getCoordinate().x,
            lastNode.getCoordinate().y);
        calc.setDestinationGeographicPoint(node.getCoordinate().x,
            node.getCoordinate().y);
        distance = calc.getOrthodromicDistance();

        double deltaHeight = Math.abs(lastNode.getHeight() - node.getHeight()); // meters
        time = distance / horizWalkingSpeed + deltaHeight / vertWalkingSpeed;
      }
      totalDistance += distance;
      totalTime += time;

      profileData.put(node.getCoordinate(), totalDistance, totalTime, node.getHeight());
      lastNode = node;
    }

    return profileData;
  }

}
