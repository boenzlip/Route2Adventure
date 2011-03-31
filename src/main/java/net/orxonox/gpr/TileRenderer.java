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
  double leftUpperCornerX;
  double leftUpperCornerY;
  double rightLowerCornerX;
  double rightLowerCornerY;
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

  /**
   * @param x
   *          tile number x (longitude direction)
   * @param y
   *          tile number y (latitude direction)
   * @param zoom
   *          discret zoom level starting at 0.
   * @return
   */
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

    System.out.println("lat2: ");
    System.out.println(p.x + ", " + p.y);

    return p;
  }

  private double mercatorProject(double latitude) {
    return mercatorProjection(latitude, 0.0)[0];
  }

  public BufferedImage renderTile(int n, int m, int zoom) {

    System.out.println(n + ", " + m + " @ " + zoom);

    tileX = n;
    tileY = m;
    tileZoom = zoom;

    leftUpperCornerX = Math.pow(2, zoom) - 1;

    tileSizeX = 360.0 / Math.pow(2, zoom);
    tileSizeY = 2 * maxLatitude / Math.pow(2, zoom);
    leftUpperCornerX = tileSizeX * n - 180.0;
    leftUpperCornerY = maxLatitude - tileSizeY * m;
    rightLowerCornerX = leftUpperCornerX + tileSizeX;
    rightLowerCornerY = leftUpperCornerY - tileSizeY;

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

    return bi;
  }

  public double[] mercatorProjection(final double aLat, final double aLon) {
    final double DEG2RAD = Math.PI / 180;

    // I have arrays.
    double[] p = new double[2];
    p[1] = (aLon + 180) / 360;
    p[0] = (1 - Math.log(Math.tan(aLat * DEG2RAD) + 1
        / Math.cos(aLat * DEG2RAD))
        / Math.PI) / 2;

    return p;
  }

  private Point latLngToPixel(double latitude, double longitude) {

    double[] a = mercatorProjection(latitude, longitude);
    System.out.println(a[0] + ", " + a[1]);

    double b = inverseMercatorProject(a[0]);

    latitude = 85.0 * (a[0]);

    double ratioX = tileWidth / (rightLowerCornerX - leftUpperCornerX);
    double ratioY = tileHeight / (leftUpperCornerY - rightLowerCornerY);

    int x = (int) ((longitude - leftUpperCornerX) * ratioX);
    int y = (int) (tileHeight - (latitude - rightLowerCornerY) * ratioY);

    return new Point(x, y);
  }

  private double inverseMercatorProject(double mercatorY) {
    final double n = Math.PI - 2.0 * Math.PI * mercatorY;
    return 180.0 / Math.PI * Math.atan(0.5 * (Math.exp(n) - Math.exp(-n)));
  }

  private void paintPoints(Iterator<XYNode> it, Graphics g, int pointSize) {
    while (it.hasNext()) {
      Object next = it.next();
      if (!(next instanceof XYNode)) {
        throw new RuntimeException(
            "I can't draw a node that doesn't have a coordinate.");
      }
      Coordinate coord = ((XYNode) next).getCoordinate();
      Point p = latLngToRelativePixel(coord.y, coord.x, tileX, tileY, tileZoom);
      g.fillOval(p.x - pointSize / 2, p.y - pointSize / 2, pointSize, pointSize);
    }

    // pointSize = 10;
    // g.setColor(Color.BLUE);
    // Point p = latLngToPixel(0.0, 0.0);
    // g.fillOval(p.x - pointSize / 2, p.y - pointSize / 2, pointSize,
    // pointSize);
    //
    // p = latLngToPixel(90.0, 0.0);
    // g.setColor(Color.GREEN);
    // g.fillOval(p.x - pointSize / 2, p.y - pointSize / 2, pointSize,
    // pointSize);

    // System.out.println("a");
    // p = latLngToPixel(85.0, 0.0);
    // p = latLngToPixel(84.0, 0.0);
    // p = latLngToPixel(80.0, 0.0);
    // p = latLngToPixel(70.0, 0.0);
    // p = latLngToPixel(60.0, 0.0);
    // p = latLngToPixel(50.0, 0.0);
    // p = latLngToPixel(40.0, 0.0);
    // p = latLngToPixel(0.0, 0.0);
    // System.out.println("a");
    // p = latLngToPixel(0.0, 90.0);
    // p = latLngToPixel(0.0, 180.0);

  }
  //
  // public void setGraph(Graph gr) {
  // graph = gr;
  // // nodes = graph.getNodes();
  // // Iterator it = nodes.iterator();
  // // minX = Double.MAX_VALUE;
  // // minY = Double.MAX_VALUE;
  // // maxX = 0;
  // // maxY = 0;
  // // while (it.hasNext()) {
  // // Object next = it.next();
  // // if (!(next instanceof XYNode)) {
  // // throw new RuntimeException(
  // // "I can't draw a node that doesn't have a coordinate.");
  // // }
  // // Coordinate coord = ((XYNode) next).getCoordinate();
  // // if (coord.x < minX) {
  // // minX = coord.x;
  // // }
  // // if (coord.y < minY) {
  // // minY = coord.y;
  // // }
  // // if (coord.x > maxX) {
  // // maxX = coord.x;
  // // }
  // // if (coord.y > maxY) {
  // // maxY = coord.y;
  // // }
  // // }
  // }

}
