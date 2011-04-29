package net.orxonox.gpr.http;

import java.awt.geom.Point2D;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import net.orxonox.gpr.TileRenderer;
import net.orxonox.gpr.data.MapsTile;
import net.orxonox.gpr.data.MapsTileRequest;
import net.orxonox.gpr.store.MapTileStore;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

public class TileServer extends AbstractServer {

  private MapTileStore mapTileStore;

  public TileServer(MapTileStore mapTileStore) {
    this.mapTileStore = mapTileStore;
  }

  private TileRenderer tileRenderer;

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
    h.add("Content-Type", "image/png");

    MapsTileRequest descriptor = new MapsTileRequest(x, y, zoom,
        new Point2D.Double(startLng, startLat), new Point2D.Double(endLng,
            endLat));
    MapsTile tile = mapTileStore.aquire(descriptor);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(tile.getImage(), "PNG", baos);

    // ok, we are ready to send the response.
    t.sendResponseHeaders(200, baos.size());
    OutputStream os = t.getResponseBody();
    os.write(baos.toByteArray(), 0, baos.size());
    os.close();
  }

}
