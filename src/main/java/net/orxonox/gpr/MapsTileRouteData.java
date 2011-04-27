package net.orxonox.gpr;

import org.geotools.graph.path.Path;
import org.geotools.graph.structure.Graph;

public class MapsTileRouteData {

  private Graph graph;
  private Path path;

  public MapsTileRouteData(Graph graph, Path path) {
    this.graph = graph;
    this.path = path;
  }

  public Graph getGraph() {
    return graph;
  }

  public Path getPath() {
    return path;
  }

}
