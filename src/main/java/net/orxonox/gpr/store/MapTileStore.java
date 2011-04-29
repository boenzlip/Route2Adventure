package net.orxonox.gpr.store;

import net.orxonox.gpr.MapsTile;
import net.orxonox.gpr.MapsTileRequest;
import net.orxonox.gpr.MapsTileRouteData;
import net.orxonox.gpr.TileRenderer;

public class MapTileStore implements IStore<MapsTileRequest, MapsTile> {

  private RouteStore routeStore;
  private TileRenderer tileRenderer;

  public MapsTile aquire(MapsTileRequest descriptor) {
    MapsTile tile = null;
    try {
      MapsTileRouteData data = routeStore.aquire(descriptor);
      tile = tileRenderer.renderTile(data.getGraph(), data.getPath(),
          descriptor.getxTile(), descriptor.getyTile(),
          descriptor.getZoomTile());
    } catch (Exception e) {
      e.printStackTrace();
    }
    return tile;
  }

  public void init() {
    routeStore = new RouteStore();
    routeStore.init();

    tileRenderer = new TileRenderer();
  }

  public void teardown() {
    routeStore.teardown();
  }

}
