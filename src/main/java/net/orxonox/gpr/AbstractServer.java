package net.orxonox.gpr;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public abstract class AbstractServer implements HttpHandler {

  public AbstractServer() {
    super();
  }

  public abstract void handle(HttpExchange arg0) throws IOException;

  @SuppressWarnings("unchecked")
  protected int getIntAttribute(HttpExchange t, String name)
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

  protected void parseGetParameters(HttpExchange exchange)
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