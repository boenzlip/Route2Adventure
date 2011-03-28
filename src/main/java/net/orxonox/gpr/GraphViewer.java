/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2006-2008, Open Source Geospatial Foundation (OSGeo)
 *        
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package net.orxonox.gpr;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JPanel;

import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Graph;
import org.geotools.graph.structure.line.XYNode;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * 
 * @author jfc173
 * 
 * @source $URL:
 *         http://svn.osgeo.org/geotools/tags/2.6.6/modules/extension/graph
 *         /src/main/java/org/geotools/graph/util/delaunay/GraphViewer.java $
 */
public class GraphViewer extends JPanel {

  Graph graph;
  Collection nodes;
  double minX, minY;
  int xScaling = 4;
  int yScaling = 4;
  int xOffset = 0;
  int yOffset = 0;
  boolean colorEdges = false;
  Color[] nodeColors = new Color[] { Color.RED, Color.ORANGE, Color.YELLOW,
      Color.GREEN, Color.BLUE, Color.MAGENTA, Color.LIGHT_GRAY, Color.GRAY,
      Color.DARK_GRAY, Color.BLACK };
  Vector shortEdges, longEdges, otherEdges;
  private double maxX;
  private double maxY;

  /** Creates a new instance of GraphViewer */
  public GraphViewer() {
  }

  public void setGraph(Graph gr) {
    graph = gr;
    nodes = graph.getNodes();
    Iterator it = nodes.iterator();
    minX = Double.MAX_VALUE;
    minY = Double.MAX_VALUE;
    maxX = 0;
    maxY = 0;
    while (it.hasNext()) {
      Object next = it.next();
      if (!(next instanceof XYNode)) {
        throw new RuntimeException(
            "I can't draw a node that doesn't have a coordinate.");
      }
      Coordinate coord = ((XYNode) next).getCoordinate();
      if (coord.x < minX) {
        minX = coord.x;
      }
      if (coord.y < minY) {
        minY = coord.y;
      }
      if (coord.x > maxX) {
        maxX = coord.x;
      }
      if (coord.y > maxY) {
        maxY = coord.y;
      }
    }
  }

  public void paintComponent(Graphics g) {

    Rectangle viewRect = g.getClipBounds();

    int border = 20;
    xScaling = (int) ((viewRect.width - border) / (maxX - minX));
    yScaling = (int) ((viewRect.height - border) / (maxY - minY));
    xScaling = Math.min(xScaling, yScaling);
    yScaling = xScaling;

    xOffset = (int) -(minX * xScaling) + border / 2;
    yOffset = (int) -(minY * yScaling) + border / 2;

    Iterator it = nodes.iterator();
    while (it.hasNext()) {
      Object next = it.next();
      if (!(next instanceof XYNode)) {
        throw new RuntimeException(
            "I can't draw a node that doesn't have a coordinate.");
      }
      Coordinate coord = ((XYNode) next).getCoordinate();
      // g.setColor(nodeColors[i]);
      // i++; //this works if there are no more than 10 nodes.
      int x = (int) Math.round(xOffset + coord.x * xScaling - 2);
      int y = (int) Math.round(yOffset + coord.y * yScaling - 2);
      g.fillOval(x, y, 4, 4);
    }

    double minWeight = Double.MAX_VALUE;
    double maxWeight = 0;
    Collection edges = graph.getEdges();
    Iterator edgeIt = edges.iterator();
    while (edgeIt.hasNext()) {
      Edge next = (Edge) edgeIt.next();
      double weight = ((EdgeWeight) next.getObject()).getWeight();

      if (weight > maxWeight) {
        maxWeight = weight;
      }
      if (weight < minWeight) {
        minWeight = weight;
      }
    }

    g.setColor(Color.RED);
    edges = graph.getEdges();
    edgeIt = edges.iterator();
    while (edgeIt.hasNext()) {
      Edge next = (Edge) edgeIt.next();
      if (!((next.getNodeA() instanceof XYNode) && (next.getNodeB() instanceof XYNode))) {
        throw new RuntimeException(
            "I can't draw an edge without endpoint coordinates.");
      }

      double weight = ((EdgeWeight) next.getObject()).getWeight();
      Color c = Color.getHSBColor(
          (float) ((maxWeight - minWeight) / (weight - minWeight)), 1.0f, 1.0f);
      g.setColor(c);

      Coordinate coordA = ((XYNode) next.getNodeA()).getCoordinate();
      Coordinate coordB = ((XYNode) next.getNodeB()).getCoordinate();

      g.drawLine((int) Math.round(xOffset + coordA.x * xScaling),
          (int) Math.round(yOffset + coordA.y * yScaling),
          (int) Math.round(xOffset + coordB.x * xScaling),
          (int) Math.round(yOffset + coordB.y * yScaling));
    }
  }
}
