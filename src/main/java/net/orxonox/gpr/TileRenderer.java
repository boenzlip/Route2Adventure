package net.orxonox.gpr;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
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
  private Graph graph;
  private Path path;
  private double maxLatitude;
  private int tileX;
  private int tileY;
  private int tileZoom;

  public TileRenderer(Graph graph, Path path) {
    this.graph = graph;
    this.path = path;

    this.maxLatitude = 85.05112866411389;
  }

  public BufferedImage renderTile(int n, int m, int zoom) {

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

    it = path.iterator();
    paintPoints(it, ig2, 10, Color.GREEN);

    return bi;
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

  private double inverseMercatorProject(double mercatorY) {
    final double n = Math.PI - 2.0 * Math.PI * mercatorY;
    return 180.0 / Math.PI * Math.atan(0.5 * (Math.exp(n) - Math.exp(-n)));
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
      g.fillOval(p.x - pointSize / 2, p.y - pointSize / 2, pointSize, pointSize);
    }

  }

  private Coordinate pixelToLatLng(int x, int y, int zoom) {

    int numberOfTilesPerSide = (int) Math.pow(2, zoom);
    int totalPixelX = numberOfTilesPerSide * tileWidth;
    int totalPixelY = numberOfTilesPerSide * tileHeight;

    // Coordinate system middle of the map.
    double leftUpperCornerX = x * tileWidth - totalPixelX / 2;
    double leftUpperCornerY = y * tileHeight;

    double rightLowerCornerX = (x + 1) * tileWidth - totalPixelX / 2;
    double rightLowerCornerY = (y + 1) * tileHeight;

    // Now normalize the numbers to 0..1, where 1 is total length.
    leftUpperCornerX /= totalPixelX / 2;
    leftUpperCornerY /= totalPixelY;

    rightLowerCornerX /= totalPixelX / 2;
    rightLowerCornerY /= totalPixelY;

    double leftUpperCornerLatitude = inverseMercatorProject(leftUpperCornerY);
    double leftUpperCornerLongitude = leftUpperCornerX * 180.0;
    double rightLowerCornerLatitude = inverseMercatorProject(rightLowerCornerY);
    double rightLowerCornerLongitude = rightLowerCornerX * 180.0;

    System.out.println("pixelToLatLng: " + x + ", " + y + ", " + zoom);
    System.out.println(leftUpperCornerX + ", " + leftUpperCornerY);
    System.out.println(leftUpperCornerLongitude + ", "
        + leftUpperCornerLatitude);
    System.out.println(rightLowerCornerX + ", " + rightLowerCornerY);
    System.out.println(rightLowerCornerLongitude + ", "
        + rightLowerCornerLatitude);

    return null;
  }
}
