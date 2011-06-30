package net.orxonox.gpr.http;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;

import net.orxonox.google.PolylineEncoder;
import net.orxonox.google.Track;
import net.orxonox.google.Trackpoint;
import net.orxonox.gpr.data.HeightProfileData;
import net.orxonox.gpr.data.HeightProfileData.Waypoint;
import net.orxonox.gpr.data.MapsTileRequest;
import net.orxonox.gpr.data.MapsTileRouteData;
import net.orxonox.gpr.store.HeightProfileStore;
import net.orxonox.gpr.store.RouteStore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

public class GoogleDirectionsServer extends AbstractServer {

  private RouteStore routeStore;
  private HeightProfileStore heightProfileStore;

  public static final String GEODATA_TIME = "geodata-time";

  public static enum GeoDataType {
    TIME("geodata-time"), DISTANCE("geodata-distance");

    private String typeName;

    GeoDataType(String typeName) {
      this.typeName = typeName;
    }

    @Override
    public String toString() {
      return typeName;
    }

    public String getName() {
      return typeName;
    }
  }

  public GoogleDirectionsServer(RouteStore routeStore, HeightProfileStore heightProfileStore) {
    this.routeStore = routeStore;
    this.heightProfileStore = heightProfileStore;
  }

  public void handle(HttpExchange t) throws IOException {

    parseGetParameters(t);

    // add the required response header for a PDF file
    Headers h = t.getResponseHeaders();

    System.out.println(t.getRequestURI());

    // TODO correct error handling.
    int x = 0;
    int y = 0;
    int zoom = 0;
    try {
      x = getIntAttribute(t, "x");
      y = getIntAttribute(t, "y");
      zoom = getIntAttribute(t, "zoom");
    } catch (AttributeNotFoundException e) {
      e.printStackTrace();
      String response = "Attribute not fun: " + e.toString();
      t.sendResponseHeaders(200, response.length());
      OutputStream os = t.getResponseBody();
      os.write(response.getBytes());
      os.close();
      return;
    }

    double startLat = 0;
    double startLng = 0;
    try {
      startLat = getDoubleAttribute(t, "startLat");
      startLng = getDoubleAttribute(t, "startLng");
    } catch (AttributeNotFoundException e) {
    }

    double endLat = 0;
    double endLng = 0;
    try {
      endLat = getDoubleAttribute(t, "endLat");
      endLng = getDoubleAttribute(t, "endLng");
    } catch (AttributeNotFoundException e) {
    }

    String dataType = GeoDataType.DISTANCE.getName();
    try {
      dataType = getStringAttribute(t, "dataType");
    } catch (AttributeNotFoundException e) {
    }

    h.add("Content-Type", "application/json");

    MapsTileRequest descriptor = new MapsTileRequest(x, y, zoom, new Point2D.Double(startLng,
        startLat), new Point2D.Double(endLng, endLat));
    MapsTileRouteData routeData = routeStore.aquire(descriptor);
    HeightProfileData heightData = heightProfileStore.aquire(routeData);

    DecimalFormat twoDForm = new DecimalFormat("#.##");
    JSONObject root = new JSONObject();
    try {
      root.put("status", "OK");


      JSONArray steps = new JSONArray();

      Iterator<Waypoint> iterator = heightData.iterator();
      double totalDistance = 0;
      double totalTime = 0;
      double lastTime = 0;
      double lastDistance = 0;
      Waypoint lastWaypoint = null;
      while (iterator.hasNext()) {
        Waypoint tuple = iterator.next();

        double time = tuple.getTime();; // Time in [s].
        double distance = tuple.getDistance(); // Distance in [m].
        if (lastWaypoint != null) {
          totalTime = time;
          totalDistance = distance;

          JSONObject step = new JSONObject();

          JSONObject duration = new JSONObject();
          duration.put("value", time - lastTime);
          double stepDuration = (time - lastTime);
          String stepUnit = "sec";
          if (stepDuration >= 3600.0) {
            stepDuration /= 3600.0;
            stepUnit = "h";
          } else if (stepDuration >= 60.0) {
            stepDuration /= 60.0;
            stepDuration = Math.ceil(stepDuration);
            stepUnit = "min";
          } else {
            stepDuration = Math.ceil(stepDuration);
          }
          duration.put("text", twoDForm.format(stepDuration) + stepUnit);
          step.put("duration", duration);

          JSONObject stepDistance = new JSONObject();
          stepDistance.put("value", distance - lastDistance);
          stepDistance.put("text", twoDForm.format((distance - lastDistance) / 1000.0) + "m");
          step.put("distance", stepDistance);

          JSONObject startStep = new JSONObject();
          startStep.put("lng", lastWaypoint.getCoordinate().x);
          startStep.put("lat", lastWaypoint.getCoordinate().y);
          step.put("start_location", startStep);

          JSONObject endStep = new JSONObject();
          endStep.put("lng", tuple.getCoordinate().x);
          endStep.put("lat", tuple.getCoordinate().y);
          step.put("end_location", endStep);

          steps.put(step);
        }

        lastTime = time;
        lastDistance = distance;
        lastWaypoint = tuple;
      }

      root.put("summary", "Distanz: " + twoDForm.format(totalDistance / 1000.0) + "km, Zeit: "
          + twoDForm.format(totalTime / 3600.0) + "h");
      JSONArray routes = new JSONArray();
      JSONObject route = new JSONObject();
      routes.put(route);
      JSONObject leg = new JSONObject();
      JSONArray legs = new JSONArray();
      legs.put(leg);
      root.put("route", routes);
      route.put("leg", legs);
      
      Track trak = new Track();
      iterator = heightData.iterator();
      while (iterator.hasNext()) {
        Waypoint tuple = iterator.next();
        trak.addTrackpoint(new Trackpoint(tuple.getCoordinate().y, tuple.getCoordinate().x));
      }
      HashMap createEncodings = PolylineEncoder.createEncodings(trak, 17, 1);
      System.out.println(PolylineEncoder.createEncodings(trak, 17, 1));
      JSONObject overviewPolyline = new JSONObject();
      overviewPolyline.put("points", createEncodings.get("encodedPoints"));
      overviewPolyline.put("levels", createEncodings.get("encodedLevels"));
      route.put("overview_polyline", overviewPolyline);
      
      
//      <bounds> 
//      <southwest> 
//       <lat>34.0523600</lat> 
//       <lng>-118.2435600</lng> 
//      </southwest> 
//      <northeast> 
//       <lat>41.8781100</lat> 
//       <lng>-87.6297900</lng> 
//      </northeast> 
//     </bounds>  
      
      
      leg.put("step", steps);
      leg.put("travel_mode", "WALKING");

      // Start location.
      JSONObject startLocation = new JSONObject();
      startLocation.put("lat", heightData.first().getCoordinate().y);
      startLocation.put("lng", heightData.first().getCoordinate().x);
      leg.put("start_location", startLocation);

      JSONObject endLocation = new JSONObject();
      endLocation.put("lat", heightData.last().getCoordinate().y);
      endLocation.put("lng", heightData.last().getCoordinate().x);
      leg.put("end_location", endLocation);


      JSONObject duration = new JSONObject();
      duration.put("value", totalTime); // time in [s]
      duration.put("text", twoDForm.format(totalTime) + "h");
      leg.put("duration", duration);

      
      
      JSONObject bounds = new JSONObject();
      bounds.put("southwest", startLocation);
      bounds.put("northeast", endLocation);
      route.put("bounds", bounds);
      

    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    String response = root.toString();
    t.sendResponseHeaders(200, response.length());
    OutputStream os = t.getResponseBody();
    os.write(response.getBytes());
    os.close();
  }

}
