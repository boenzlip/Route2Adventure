package net.orxonox.gpr.http;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.Iterator;

import net.orxonox.gpr.data.HeightProfileData;
import net.orxonox.gpr.data.HeightProfileData.Waypoint;
import net.orxonox.gpr.data.MapsTileRequest;
import net.orxonox.gpr.data.MapsTileRouteData;
import net.orxonox.gpr.store.HeightProfileStore;
import net.orxonox.gpr.store.RouteStore;

import org.json.JSONArray;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

public class GeoDataServer extends AbstractServer {

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

  public GeoDataServer(RouteStore routeStore,
      HeightProfileStore heightProfileStore) {
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

    MapsTileRequest descriptor = new MapsTileRequest(x, y, zoom,
        new Point2D.Double(startLng, startLat), new Point2D.Double(endLng,
            endLat));
    MapsTileRouteData routeData = routeStore.aquire(descriptor);
    HeightProfileData heightData = heightProfileStore.aquire(routeData);

    JSONArray pathPoints = new JSONArray();

    Iterator<Waypoint> iterator = heightData.iterator();
    while (iterator.hasNext()) {
      Waypoint tuple = iterator.next();

      JSONArray pathPoint = new JSONArray();

      DecimalFormat twoDForm = new DecimalFormat("#.##");
      Double dataValue = null;
      if (dataType.equals(GeoDataType.TIME)) {
        dataValue = Double
        .valueOf(twoDForm.format(tuple.getTime() / 3600.0f)); // Time in [h].
      } else {
        dataValue = Double.valueOf(twoDForm.format(tuple
            .getDistance() / 1000.0)); // Distance in [km].
      }
      
      

      pathPoint.put(dataValue);
      pathPoint.put(new Double(tuple.getHeight()));
      pathPoints.put(pathPoint);
    }

    String response = pathPoints.toString();
    t.sendResponseHeaders(200, response.length());
    OutputStream os = t.getResponseBody();
    os.write(response.getBytes());
    os.close();
  }

}
