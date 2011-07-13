package net.orxonox.gpr.graph;

import net.orxonox.gpr.store.ArrayDijkstra;


public class GeoGraph {

  private ArrayDijkstra array;
  private GeoRegion region;
  
  public GeoGraph(ArrayDijkstra array, GeoRegion region) {
    this.array = array;
    this.region = region;
  }

  
  public ArrayDijkstra getArray() {
    return array;
  }

  
  public GeoRegion getRegion() {
    return region;
  }
  
}
