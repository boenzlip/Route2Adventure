package net.orxonox.gpr;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class TileRenderer {

  private final int width = 256;
  private final int height = 256;

  double tileSizeX;
  double tileSizeY;
  double leftUpperCornerX;
  double leftUpperCornerY;
  double rightLowerCornerX;
  double rightLowerCornerY;

  public void renderTile(int n, int m, int zoom) {
    try {

      tileSizeX = 360.0 / Math.pow(2, zoom + 1);
      tileSizeY = 180.0 / Math.pow(2, zoom + 1);
      leftUpperCornerX = 360.0 / tileSizeX * n - 180.0;
      leftUpperCornerY = 90.0 - 180.0 / tileSizeY * m;
      rightLowerCornerX = leftUpperCornerX + tileSizeX;
      rightLowerCornerY = leftUpperCornerY + tileSizeY;

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
      ig2.setPaint(Color.black);
      ig2.drawString(message, (width - stringWidth), height - stringHeight);

      ImageIO.write(bi, "PNG", new File("image.png"));

    } catch (IOException ie) {
      ie.printStackTrace();
    }

  }

  private Point latLngToPixel(double latitude, double longitude) {

    double ratioX = width / (rightLowerCornerX - leftUpperCornerX);
    double ratioY = height / (leftUpperCornerY - rightLowerCornerY);

    int x = (int) ((longitude - leftUpperCornerX) * ratioX);
    int y = (int) (height - (latitude - rightLowerCornerY) * ratioY);

    return new Point(x, y);
  }

}
