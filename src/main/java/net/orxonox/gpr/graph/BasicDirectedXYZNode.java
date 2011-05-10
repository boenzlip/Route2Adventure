package net.orxonox.gpr.graph;

import org.geotools.graph.structure.line.BasicDirectedXYNode;




public class BasicDirectedXYZNode extends BasicDirectedXYNode {

  private double height = 0.0;
  
  public void setHeight(double height) {
    this.height = height;
  }
  
  public double getHeight() {
    return height;
  }
  
}
