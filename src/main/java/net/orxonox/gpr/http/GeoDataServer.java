package net.orxonox.gpr.http;

import java.awt.geom.Point2D;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;

import org.geotools.graph.structure.line.BasicDirectedXYNode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import net.orxonox.gpr.TileRenderer;
import net.orxonox.gpr.data.MapsTile;
import net.orxonox.gpr.data.MapsTileRequest;
import net.orxonox.gpr.data.MapsTileRouteData;
import net.orxonox.gpr.graph.BasicDirectedXYZNode;
import net.orxonox.gpr.store.MapTileStore;
import net.orxonox.gpr.store.RouteStore;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

public class GeoDataServer extends AbstractServer {

  private RouteStore routeStore;

  public GeoDataServer(RouteStore routeStore) {
    this.routeStore = routeStore;
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
    h.add("Content-Type", "application/json");

    MapsTileRequest descriptor = new MapsTileRequest(x, y, zoom,
        new Point2D.Double(startLng, startLat), new Point2D.Double(endLng,
            endLat));
    MapsTileRouteData routeData = routeStore.aquire(descriptor);
    
    JSONArray pathPoints = new JSONArray();
    
    Iterator<BasicDirectedXYNode> iterator = routeData.getPath().iterator();
    while(iterator.hasNext()) {
      BasicDirectedXYZNode node = (BasicDirectedXYZNode)iterator.next();
      node.getHeight();
      
      JSONObject pathPoint = new JSONObject();
      try {
        pathPoint.append("x", new Double(node.getCoordinate().x));
        pathPoint.append("y", new Double(node.getCoordinate().y));
        pathPoint.append("z", new Double(node.getHeight()));
      } catch (JSONException e) {
        e.printStackTrace();
      }
      pathPoints.put(pathPoint);
    }
    
    String response = pathPoints.toString();
    t.sendResponseHeaders(200, response.length());
    OutputStream os = t.getResponseBody();
    os.write(response.getBytes());
    os.close();
  }

}
