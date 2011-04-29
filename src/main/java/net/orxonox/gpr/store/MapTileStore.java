package net.orxonox.gpr.store;

import net.orxonox.gpr.TileRenderer;
import net.orxonox.gpr.data.MapsTile;
import net.orxonox.gpr.data.MapsTileRequest;
import net.orxonox.gpr.data.MapsTileRouteData;

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
