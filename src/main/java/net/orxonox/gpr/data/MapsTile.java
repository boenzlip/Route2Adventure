package net.orxonox.gpr.data;

import java.awt.image.BufferedImage;

public class MapsTile {

  private BufferedImage image;

  public MapsTile(BufferedImage image) {
    this.image = image;
  }

  public BufferedImage getImage() {
    return image;
  }

}
