package net.orxonox.gpr;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import org.geotools.graph.path.Path;
import org.geotools.graph.structure.basic.BasicGraph;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

public class TileServer extends AbstractServer {

  public TileServer(BasicGraph graph, Path path) {
    this.tileRenderer = new TileRenderer(graph, path);
  }

  private TileRenderer tileRenderer;

  public void handle(HttpExchange t) throws IOException {

    parseGetParameters(t);

    // add the required response header for a PDF file
    Headers h = t.getResponseHeaders();

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

    h.add("Content-Type", "image/png");
    // a PDF (you provide your own!)

    BufferedImage image = tileRenderer.renderTile(x, y, zoom);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(image, "PNG", baos);

    // bis.read(bytearray, 0, bytearray.length);

    // ok, we are ready to send the response.
    t.sendResponseHeaders(200, baos.size());
    OutputStream os = t.getResponseBody();
    os.write(baos.toByteArray(), 0, baos.size());
    os.close();
  }

}
