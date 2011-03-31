package net.orxonox.gpr;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class TileServer implements HttpHandler {

  private TileRenderer tileRenderer = new TileRenderer();

  @SuppressWarnings("serial")
  private final class AttributeNotFoundException extends Exception {
    public AttributeNotFoundException(String string) {
      super(string);
    }
  }

  public void handle(HttpExchange t) throws IOException {

    parseGetParameters(t);

    // add the required response header for a PDF file
    Headers h = t.getResponseHeaders();

    // /mt?n=404&v=w2.12&x=130&y=93&zoom=9

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
    // File file = new File("image.png");
    // byte[] bytearray = new byte[(int) file.length()];
    // FileInputStream fis = new FileInputStream(file);
    // BufferedInputStream bis = new BufferedInputStream(fis);

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

  private int getIntAttribute(HttpExchange t, String name)
      throws AttributeNotFoundException {

    Map<String, Object> parameters = (Map<String, Object>) t
        .getAttribute("parameters");
    if (parameters == null) {
      throw new AttributeNotFoundException("Attribute " + name + " not found.");
    }

    Object value = parameters.get(name);
    if (!(value instanceof String)) {
      throw new AttributeNotFoundException("Attribute " + name + " not found.");
    }
    Integer xInteger = Integer.valueOf((String) value);
    return xInteger.intValue();
  }

  private void parseGetParameters(HttpExchange exchange)
      throws UnsupportedEncodingException {

    Map<String, Object> parameters = new HashMap<String, Object>();
    URI requestedUri = exchange.getRequestURI();
    String query = requestedUri.getRawQuery();
    parseQuery(query, parameters);
    exchange.setAttribute("parameters", parameters);
  }

  @SuppressWarnings("unchecked")
  private void parseQuery(String query, Map<String, Object> parameters)
      throws UnsupportedEncodingException {

    if (query != null) {
      String pairs[] = query.split("[&]");

      for (String pair : pairs) {
        String param[] = pair.split("[=]");

        String key = null;
        String value = null;
        if (param.length > 0) {
          key = URLDecoder
              .decode(param[0], System.getProperty("file.encoding"));
        }

        if (param.length > 1) {
          value = URLDecoder.decode(param[1],
              System.getProperty("file.encoding"));
        }

        if (parameters.containsKey(key)) {
          Object obj = parameters.get(key);
          if (obj instanceof List<?>) {
            List<String> values = (List<String>) obj;
            values.add(value);
          } else if (obj instanceof String) {
            List<String> values = new ArrayList<String>();
            values.add((String) obj);
            values.add(value);
            parameters.put(key, values);
          }
        } else {
          parameters.put(key, value);
        }
      }
    }
  }

}
