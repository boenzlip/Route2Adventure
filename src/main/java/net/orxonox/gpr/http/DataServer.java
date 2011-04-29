package net.orxonox.gpr.http;

import java.io.IOException;


import org.geotools.graph.path.DijkstraShortestPathFinder;
import org.geotools.graph.structure.line.XYNode;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

public class DataServer extends AbstractServer {

  private DijkstraShortestPathFinder pathFinder;
  private XYNode[][] nodes;

  public DataServer(DijkstraShortestPathFinder pathFinder, XYNode[][] nodes) {
    this.pathFinder = pathFinder;
    this.nodes = nodes;
  }

  @Override
  public void handle(HttpExchange t) throws IOException {
    parseGetParameters(t);

    // add the required response header for a PDF file
    Headers h = t.getResponseHeaders();

    int zoom = 0;
    try {
      zoom = getIntAttribute(t, "zoom");
    } catch (AttributeNotFoundException e) {
      // ignore.
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

  }

}