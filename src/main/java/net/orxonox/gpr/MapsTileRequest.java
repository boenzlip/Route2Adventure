package net.orxonox.gpr;

import java.awt.geom.Point2D;

/**
 * Request to create a new Google Maps tile.
 * 
 * Contains all information needed to create the map tile.
 */
public class MapsTileRequest {

  private int xTile;
  private int yTile;
  private int zoomTile;
  private Point2D.Double startLocation;
  private Point2D.Double destinationLocation;

  public MapsTileRequest(int xTile, int yTile, int zoomTile,
      Point2D.Double startLocation, Point2D.Double destiationLocation) {
    this.xTile = xTile;
    this.yTile = yTile;
    this.zoomTile = zoomTile;
    this.startLocation = startLocation;
    this.destinationLocation = destiationLocation;
  }

  public int getxTile() {
    return xTile;
  }

  public int getyTile() {
    return yTile;
  }

  public int getZoomTile() {
    return zoomTile;
  }

  public Point2D.Double getStartLocation() {
    return startLocation;
  }

  public Point2D.Double getDestinationLocation() {
    return destinationLocation;
  }

}
