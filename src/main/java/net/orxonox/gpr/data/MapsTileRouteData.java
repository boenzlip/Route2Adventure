package net.orxonox.gpr.data;

import net.orxonox.gpr.graph.GeoGraph;
import net.orxonox.gpr.graph.GeoPath;


public class MapsTileRouteData {

  private GeoGraph graph;
  private GeoPath path;

  public MapsTileRouteData(GeoGraph graph, GeoPath path) {
    this.graph = graph;
    this.path = path;
  }

  public GeoGraph getGraph() {
    return graph;
  }

  public GeoPath getPath() {
    return path;
  }

}
