package net.orxonox.gpr;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Iterator;

import org.geotools.graph.path.Path;
import org.geotools.graph.structure.Graph;
import org.geotools.graph.structure.line.XYNode;

import com.vividsolutions.jts.geom.Coordinate;

public class TileRenderer {

  private final int tileWidth = 256;
  private final int tileHeight = 256;

  double tileSizeX;
  double tileSizeY;

  private int tileX;
  private int tileY;
  private int tileZoom;

  public TileRenderer() {
  }

  @SuppressWarnings("unchecked")
  public MapsTile renderTile(Graph graph, Path path, int n, int m, int zoom) {

    // System.out.println(n + ", " + m + " @ " + zoom);

    tileX = n;
    tileY = m;
    tileZoom = zoom;

    // TYPE_INT_ARGB specifies the image format: 8-bit RGBA packed
    // into integer pixels
    BufferedImage bi = new BufferedImage(tileWidth, tileHeight,
        BufferedImage.TYPE_INT_ARGB);

    Graphics2D ig2 = bi.createGraphics();

    // Draw debug string.
    Font font = new Font("Arial", Font.BOLD, 10);
    ig2.setFont(font);
    String message = "tile: " + n + ", " + m + " @ z = " + zoom;
    FontMetrics fontMetrics = ig2.getFontMetrics();
    int stringWidth = fontMetrics.stringWidth(message);
    int stringHeight = fontMetrics.getAscent();
    ig2.setPaint(Color.RED);
    ig2.drawString(message, (tileWidth - stringWidth), tileHeight
        - stringHeight);

    ig2.setPaint(Color.BLACK);
    Iterator<XYNode> it = graph.getNodes().iterator();
    paintPoints(it, ig2, 2);

    if (path != null) {
      it = path.iterator();
      paintPoints(it, ig2, 10, Color.GREEN);
    }

    MapsTile tile = new MapsTile(bi);
    return tile;
  }

  /**
   * @param x
   *          tile number x (longitude direction)
   * @param y
   *          tile number y (latitude direction)
   * @param zoom
   *          discret zoom level starting at 0.
   * @return
   */
  private Point latLngToRelativePixel(double latitude, double longitude, int x,
      int y, int zoom) {

    double mercY = mercatorProject(latitude);
    double mercX = longitude / 360.0 + 0.5;

    int numberOfTilesPerSide = (int) Math.pow(2, zoom);
    int totalPixelX = numberOfTilesPerSide * tileWidth;
    int totalPixelY = numberOfTilesPerSide * tileHeight;

    int absoluteY = (int) (mercY * totalPixelY);
    int absoluteX = (int) (mercX * totalPixelX);

    // Coordinate system middle of the map.
    int leftUpperCornerX = x * tileWidth;
    int leftUpperCornerY = y * tileHeight;

    Point p = new Point(absoluteX - leftUpperCornerX, absoluteY
        - leftUpperCornerY);

    return p;
  }

  private double mercatorProject(double latitude) {
    final double DEG2RAD = Math.PI / 180;
    double y = (1 - Math.log(Math.tan(latitude * DEG2RAD) + 1
        / Math.cos(latitude * DEG2RAD))
        / Math.PI) / 2;
    return y;
  }

  private void paintPoints(Iterator<XYNode> it, Graphics g, int pointSize) {
    paintPoints(it, g, pointSize, new Color(0.0f, 0.0f, 0.0f, 0.2f));
  }

  private void paintPoints(Iterator<XYNode> it, Graphics g, int pointSize,
      Color color) {
    while (it.hasNext()) {
      Object next = it.next();
      if (!(next instanceof XYNode)) {
        throw new RuntimeException(
            "I can't draw a node that doesn't have a coordinate.");
      }
      g.setColor(color);

      Coordinate coord = ((XYNode) next).getCoordinate();
      Point p = latLngToRelativePixel(coord.y, coord.x, tileX, tileY, tileZoom);
      if (new Rectangle(0, 0, tileWidth, tileHeight).contains(p)) {
        g.fillRect(p.x - pointSize / 2, p.y - pointSize / 2, pointSize,
            pointSize);
      }
    }

  }

}
