package net.orxonox.gpr.http;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

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

    String waypoints = null;
    try {
      waypoints = getStringAttribute(t, "waypoints");
    } catch (AttributeNotFoundException e) {
    }
    List<Point2D.Double> waypointCoorindates = new ArrayList<Point2D.Double>();
    waypointCoorindates.add(new Point2D.Double(startLng, startLat));
    if (waypoints != null) {
      String[] waypointElements = waypoints.split("/");

      for (String waypoint : waypointElements) {
        String[] coordinates = waypoint.split(",");
        double lat = Double.valueOf(coordinates[0]).doubleValue();
        double lng = Double.valueOf(coordinates[1]).doubleValue();

        waypointCoorindates.add(new Point2D.Double(lng, lat));
      }
    }
    waypointCoorindates.add(new Point2D.Double(endLng, endLat));

    // Assemble the map tile requests.
    List<MapsTileRequest> mapTileRequests = new ArrayList<MapsTileRequest>();
    for (int i = 0; i < waypointCoorindates.size() - 1; i++) {
      mapTileRequests.add(new MapsTileRequest(x, y, zoom, waypointCoorindates.get(i),
          waypointCoorindates.get(i + 1)));
    }

    // Execute each map tile request separately.
    List<HeightProfileData> profileData = new ArrayList<HeightProfileData>();
    for (MapsTileRequest request : mapTileRequests) {
      MapsTileRouteData routeData = routeStore.aquire(request);
      HeightProfileData heightData = heightProfileStore.aquire(routeData);
      profileData.add(heightData);
    }

    // Now concatenate the result to one height profile data object.
    HeightProfileData heightData = new HeightProfileData();
    for (HeightProfileData data : profileData) {

      Iterator<Waypoint> iterator = data.iterator();
      while (iterator.hasNext()) {
        Waypoint waypoint = iterator.next();
        heightData.put(waypoint.getCoordinate(), waypoint.getDistance(), waypoint.getTime(),
            waypoint.getHeight());
      }

    }

    h.add("Content-Type", "application/json");
    DecimalFormat twoDForm = new DecimalFormat("#.##");
    JSONObject root = new JSONObject();
    try {
      root.put("status", "OK");

      // Evaluate total distance.
      Iterator<Waypoint> iterator = heightData.iterator();
      double totalDistance = 0;
      double totalTime = 0;
      while (iterator.hasNext()) {
        Waypoint tuple = iterator.next();

        double time = tuple.getTime();; // Time in [s].
        double distance = tuple.getDistance(); // Distance in [m].
        totalTime += time;
        totalDistance += distance;
      }
      root.put("summary", "Distanz: " + twoDForm.format(totalDistance / 1000.0) + "km, Zeit: "
          + twoDForm.format(totalTime / 3600.0) + "h");
      JSONArray routes = new JSONArray();
      root.put("route", routes);

      JSONObject route = new JSONObject();
      routes.put(route);

      JSONArray legs = new JSONArray();
      route.put("leg", legs);


      // Create one leg for each waypoint segment.
      for (HeightProfileData data : profileData) {
        legs.put(createLeg(data, twoDForm));
      }


      // JSONObject bounds = new JSONObject();
      // bounds.put("southwest", startLocation);
      // bounds.put("northeast", endLocation);
      // route.put("bounds", bounds);


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

  private JSONObject createLeg(HeightProfileData heightData, DecimalFormat twoDForm)
      throws JSONException {
    JSONObject leg = new JSONObject();

    Track trak = new Track();
    Iterator<Waypoint> iterator = heightData.iterator();
    while (iterator.hasNext()) {
      Waypoint tuple = iterator.next();
      trak.addTrackpoint(new Trackpoint(tuple.getCoordinate().y, tuple.getCoordinate().x));
    }
    HashMap createEncodings = PolylineEncoder.createEncodings(trak, 17, 1);
    JSONObject overviewPolyline = new JSONObject();
    overviewPolyline.put("points", createEncodings.get("encodedPoints"));
    overviewPolyline.put("levels", createEncodings.get("encodedLevels"));
    leg.put("overview_polyline", overviewPolyline);


    JSONArray steps = new JSONArray();

    iterator = heightData.iterator();
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
        totalTime += time;
        totalDistance += distance;

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

    JSONObject distance = new JSONObject();
    distance.put("value", totalDistance); // distance in [m]
    distance.put("text", twoDForm.format(totalDistance) + "m");
    leg.put("distance", distance);

    return leg;
  }

}
