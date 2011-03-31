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

  private final int width = 256;
  private final int height = 256;

  double tileSizeX;
  double tileSizeY;
  double leftUpperCornerX;
  double leftUpperCornerY;
  double rightLowerCornerX;
  double rightLowerCornerY;
  private Graph graph;
  private Path path;
  private double maxLatitude;

  public TileRenderer(Graph graph, Path path) {
    this.graph = graph;
    this.path = path;

    this.maxLatitude = 85.05112866411389;
  }

  public BufferedImage renderTile(int n, int m, int zoom) {

    System.out.println(n + ", " + m + " @ " + zoom);
    tileSizeX = 360.0 / Math.pow(2, zoom);
    tileSizeY = 2 * maxLatitude / Math.pow(2, zoom);
    leftUpperCornerX = tileSizeX * n - 180.0;
    leftUpperCornerY = maxLatitude - tileSizeY * m;
    rightLowerCornerX = leftUpperCornerX + tileSizeX;
    rightLowerCornerY = leftUpperCornerY - tileSizeY;

    // TYPE_INT_ARGB specifies the image format: 8-bit RGBA packed
    // into integer pixels
    BufferedImage bi = new BufferedImage(width, height,
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
    ig2.drawString(message, (width - stringWidth), height - stringHeight);

    ig2.setPaint(Color.BLACK);
    Iterator<XYNode> it = graph.getNodes().iterator();
    paintPoints(it, ig2, 2);

    return bi;
  }

  public double[] mercatorProjection(final double aLat, final double aLon) {
    final double DEG2RAD = Math.PI / 180;

    // I have arrays.
    double[] p = new double[2];
    p[0] = (aLon + 180) / 360;
    p[1] = (1 - Math.log(Math.tan(aLat * DEG2RAD) + 1
        / Math.cos(aLat * DEG2RAD))
        / Math.PI) / 2;

    return p;
  }

  private Point latLngToPixel(double latitude, double longitude) {

    double[] a = mercatorProjection(latitude, longitude);
    System.out.println(a[1] + ", " + a[0]);

    double ratioX = width / (rightLowerCornerX - leftUpperCornerX);
    double ratioY = height / (leftUpperCornerY - rightLowerCornerY);

    int x = (int) ((longitude - leftUpperCornerX) * ratioX);
    int y = (int) (height - (latitude - rightLowerCornerY) * ratioY);

    return new Point(x, y);
  }

  private void paintPoints(Iterator<XYNode> it, Graphics g, int pointSize) {
    while (it.hasNext()) {
      Object next = it.next();
      if (!(next instanceof XYNode)) {
        throw new RuntimeException(
            "I can't draw a node that doesn't have a coordinate.");
      }
      Coordinate coord = ((XYNode) next).getCoordinate();
      // g.setColor(nodeColors[i]);
      // i++; //this works if there are no more than 10 nodes.
      Point p = latLngToPixel(coord.y, coord.x);
      g.fillOval(p.x - pointSize / 2, p.y - pointSize / 2, pointSize, pointSize);

    }

    pointSize = 10;
    g.setColor(Color.BLUE);
    Point p = latLngToPixel(0.0, 0.0);
    g.fillOval(p.x - pointSize / 2, p.y - pointSize / 2, pointSize, pointSize);

    p = latLngToPixel(90.0, 0.0);
    g.setColor(Color.GREEN);
    g.fillOval(p.x - pointSize / 2, p.y - pointSize / 2, pointSize, pointSize);

    System.out.println("a");
    p = latLngToPixel(85.0, 0.0);
    p = latLngToPixel(84.0, 0.0);
    p = latLngToPixel(80.0, 0.0);
    p = latLngToPixel(70.0, 0.0);
    p = latLngToPixel(60.0, 0.0);
    p = latLngToPixel(50.0, 0.0);
    p = latLngToPixel(40.0, 0.0);
    p = latLngToPixel(0.0, 0.0);
    System.out.println("a");
    p = latLngToPixel(0.0, 90.0);
    p = latLngToPixel(0.0, 180.0);

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
