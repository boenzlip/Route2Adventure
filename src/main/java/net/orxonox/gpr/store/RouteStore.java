package net.orxonox.gpr.store;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import net.orxonox.gpr.Route2Adventure;
import net.orxonox.gpr.data.MapsTileRequest;
import net.orxonox.gpr.data.MapsTileRouteData;
import net.orxonox.gpr.graph.BasicDirectedXYZNode;
import net.orxonox.gpr.graph.EdgeWeight;
import net.orxonox.gpr.graph.GeoGraph;
import net.orxonox.gpr.graph.GeoLocation;
import net.orxonox.gpr.graph.GeoPath;
import net.orxonox.gpr.graph.GeoRegion;

import org.apache.commons.collections.map.LRUMap;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.DataSourceException;
import org.geotools.factory.Hints;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.graph.build.basic.BasicDirectedGraphBuilder;
import org.geotools.graph.path.DijkstraShortestPathFinder;
import org.geotools.graph.path.Path;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Graphable;
import org.geotools.graph.structure.basic.BasicDirectedEdge;
import org.geotools.graph.structure.basic.BasicGraph;
import org.geotools.graph.structure.line.BasicDirectedXYNode;
import org.geotools.graph.traverse.standard.DijkstraIterator.EdgeWeighter;
import org.geotools.referencing.CRS;
import org.geotools.referencing.GeodeticCalculator;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Coordinate;

import static net.orxonox.gpr.store.ArrayDijkstra.HEIGHT;

public class RouteStore implements IStore<MapsTileRequest, MapsTileRouteData> {

  // private Path path;
//  private BasicGraph graph;
  private BasicDirectedXYZNode[][] nodeMatrix;
  private static final int CACHE_SIZE = 10;
  public static final double GRID_WIDTH = 0.01; // arc degrees.
  private Map<String, MapsTileRouteData> routeCache = new LRUMap(CACHE_SIZE);
  private ArrayDijkstra arrayDijkstra;
  private GeoRegion geoRegion;

  public synchronized MapsTileRouteData aquire(MapsTileRequest descriptor) {

    String routeHash = getRouteHash(descriptor.getStartLocation(),
        descriptor.getDestinationLocation());
    MapsTileRouteData routeDate;
    if (routeCache.containsKey(routeHash)) {
      routeDate = routeCache.get(routeHash);
    } else {
      routeDate = routeGraph(descriptor.getStartLocation(), descriptor.getDestinationLocation());
      routeCache.put(routeHash, routeDate);
    }

    return routeDate;
  }

  private String getRouteHash(Point2D.Double startLocation, Point2D.Double destinationLocation) {
    return startLocation.hashCode() + "-" + destinationLocation.hashCode();
  }

  private MapsTileRouteData routeGraph(Point2D.Double startLocation,
      Point2D.Double destinationLocation) {

    Point startCoordinates = sphereToGrid(startLocation);
    Point destinationCoodiantes = sphereToGrid(destinationLocation);
    
    List<int[]> shortestPath = arrayDijkstra.shortestPath(
      new int[] { startCoordinates.x, startCoordinates.y }, new int[] { destinationCoodiantes.x, destinationCoodiantes.y });
    
//    // create a strategy for weighting edges in the graph
//    EdgeWeighter weighter = new EdgeWeighter() {
//
//      public double getWeight(Edge e) {
//        return ((EdgeWeight) e.getObject()).getWeight();
//      }
//    };
//    DijkstraShortestPathFinder pf = new DijkstraShortestPathFinder(graph,
//        getNearestNode(startLocation), weighter);
//    pf.calculate();

    GeoPath geoPath = new GeoPath();
    for(int i = 0; i < shortestPath.size(); i++) {
      geoPath.appendPoint(gridToSphere(shortestPath.get(i)[0], shortestPath.get(i)[1]) );
    }
    
    GeoGraph geoGraph = new GeoGraph(arrayDijkstra, geoRegion);
    
    return new MapsTileRouteData(geoGraph, geoPath);
  }

  
  private GeoLocation gridToSphere(int x, int y) {
    double dX = geoRegion.getX1() + x * GRID_WIDTH ;
    double dY = geoRegion.getY1() + y * GRID_WIDTH ;
    return new GeoLocation(dX, dY, arrayDijkstra.getHeight(x, y));
  }
  
  private Point sphereToGrid(Point2D.Double location) {
    
    int dX = (int) Math.round((location.x - geoRegion.getX1()) / GRID_WIDTH) ;
    int dY = (int) Math.round((location.y - geoRegion.getY1()) / GRID_WIDTH) ;
    
    return new Point(dX, dY);
    
    // TODO greedy closest neighbor lookup can be done much faster.
//    double minDistance = java.lang.Double.MAX_VALUE;
//    Graphable node = null;
//    for (int i = 1; i < arrayDijkstra.getGraphWidth() - 1; i++) {
//      for (int j = 1; j < arrayDijkstra.getGraphHeight()- 1; j++) {
//        double dx = nodeMatrix[i][j].getCoordinate().x - location.x;
//        double dy = nodeMatrix[i][j].getCoordinate().y - location.y;
//        double distance = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
//        if (distance < minDistance) {
//          node = nodeMatrix[i][j];
//          minDistance = distance;
//        }
//      }
//    }
//
//    return node;
  }

  public void init() {
    // Download data via maven:
    // \http://code.google.com/p/maven-download-plugin/
    // display a data store file chooser dialog for shapefiles
    // File file = JFileDataStoreChooser.showOpenFile("shp", null);
    URL inputStream = Route2Adventure.class.getClassLoader().getResource("srtm_38_03.tif");
    // Can be downloaded from:
    // http://srtm.csi.cgiar.org/SRT-ZIP/SRTM_V41/SRTM_Data_GeoTiff/srtm_38_03.zip

    // Multiple raster image can be loaded via ImageMosaicReader.
    // http://docs.codehaus.org/display/GEOTDOC/Image+Mosaic+Plugin
    // http://osgeo-org.1803224.n2.nabble.com/mosaicBuilder-td1939281.html#a1939283
    // unit test:
    // http://www.javadocexamples.com/java_source/org/geotools/gce/imagemosaic/ImageMosaicReaderTest.java.html
    // http://docs.geoserver.org/stable/en/user/tutorials/imagepyramid/imagepyramid.html
    // http://docs.codehaus.org/display/GEOTDOC/Generating+Image+Pyramid+Guide
    // ImageMosaicReader mosaicReader = null;
    // try {
    // mosaicReader = new ImageMosaicReader(inputStream);
    // // mosaicReader.read(null);
    // } catch (IOException e1) {
    // // TODO Auto-generated catch block
    // e1.printStackTrace();
    // }

    GeoTiffReader reader = null;
    try {
      reader = new GeoTiffReader(inputStream, new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER,
          Boolean.TRUE));
    } catch (DataSourceException ex) {
      ex.printStackTrace();
    }

    GridCoverage2D coverage = null;
    try {
      coverage = (GridCoverage2D) reader.read(null);
    } catch (IOException ex) {
      ex.printStackTrace();
      // return;
    }

    // Use google maps compatible mercator projection EPSG:3785.
    CoordinateReferenceSystem sphericalMercator = null;
    try {
      sphericalMercator = CRS.decode("EPSG:3785");
    } catch (NoSuchAuthorityCodeException e) {
      e.printStackTrace();
    } catch (FactoryException e) {
      e.printStackTrace();
    }

    // create the graph generator
    BasicDirectedGraphBuilder graphGen = new BasicDirectedGraphBuilder();
    GeodeticCalculator calc = new GeodeticCalculator(sphericalMercator);

    // http://maps.google.ch/maps/mm?ie=UTF8&hl=de&ll=46.545284,6.87212&spn=0.096341,0.187969&t=h&z=13
    // http://maps.google.ch/maps/mm?ie=UTF8&hl=de&ll=46.227828,7.897797&spn=0.096903,0.187969&t=h&z=13\
    // reading height values, example.
    Envelope env = coverage.getEnvelope();
    double x = env.getMinimum(0);
    double y = env.getMinimum(1);

    // This calculation is angle constant not distance constant over the globe.
    final int nX = (int) ((env.getMaximum(0) - env.getMinimum(0)) / GRID_WIDTH);
    final int nY = (int) ((env.getMaximum(1) - env.getMinimum(1)) / GRID_WIDTH);
    System.out.println("Array dimensions: " + nX + ", " + nY);
    geoRegion = new GeoRegion(x, y, env.getMaximum(0), env.getMaximum(1));

    
   
    java.lang.Double[][][] graphArray = new java.lang.Double[nX][nY][4];
    for (int xOffset = 0; xOffset < nX; xOffset++) {
      for (int yOffset = 0; yOffset < nY; yOffset++) {
        double currentX = x + xOffset * GRID_WIDTH;
        double currentY = y + yOffset * GRID_WIDTH;

        double latitude = currentX;
        double longitude = currentY;

        double[] height = new double[1];
        coverage.evaluate(new Point2D.Double(latitude, longitude), height);
        graphArray[xOffset][yOffset][ArrayDijkstra.HEIGHT] = new java.lang.Double(height[0]);
      }
    }
    double matrixDistance = matrixNodeDistance(env, calc);
    System.out.println("Matrix Distance: " + matrixDistance + "m");
    arrayDijkstra = new ArrayDijkstra(graphArray, matrixDistance);
    
    
    /*
    // Create the node matrix.
    nodeMatrix = new BasicDirectedXYZNode[nX][nY];
    for (int xOffset = 0; xOffset < nX; xOffset++) {
      for (int yOffset = 0; yOffset < nY; yOffset++) {
        BasicDirectedXYZNode node = new BasicDirectedXYZNode();
        double currentX = x + xOffset * GRID_WIDTH;
        double currentY = y + yOffset * GRID_WIDTH;

        double latitude = currentX * graphScalingFactor;
        double longitude = currentY * graphScalingFactor;
        node.setCoordinate(new Coordinate(latitude, longitude));
        graphGen.addNode(node);

        double[] height = new double[1];
        coverage.evaluate(new Point2D.Double(latitude, longitude), height);
        node.setHeight(height[0]);

        nodeMatrix[xOffset][yOffset] = node;
      }
    }

    // Create weighted edges. ATTENTION: border edges are not correctly
    // connected, don't use those for experimenting.
    for (int xOffset = 1; xOffset < nX - 1; xOffset++) {

      double[] height = new double[1];
      Double point = new Point2D.Double(x, y);
      coverage.evaluate(point, height);

      for (int yOffset = 1; yOffset < nY - 1; yOffset++) {

        // grid
        graphGen.addEdge(createEdge(coverage, nodeMatrix, xOffset, yOffset, xOffset + 1, yOffset,
            calc));
        graphGen.addEdge(createEdge(coverage, nodeMatrix, xOffset, yOffset, xOffset, yOffset + 1,
            calc));
        graphGen.addEdge(createEdge(coverage, nodeMatrix, xOffset, yOffset, xOffset, yOffset - 1,
            calc));
        graphGen.addEdge(createEdge(coverage, nodeMatrix, xOffset, yOffset, xOffset - 1, yOffset,
            calc));
        // diagonals
        graphGen.addEdge(createEdge(coverage, nodeMatrix, xOffset, yOffset, xOffset - 1,
            yOffset - 1, calc));
        graphGen.addEdge(createEdge(coverage, nodeMatrix, xOffset, yOffset, xOffset - 1,
            yOffset + 1, calc));
        graphGen.addEdge(createEdge(coverage, nodeMatrix, xOffset, yOffset, xOffset + 1,
            yOffset + 1, calc));
        graphGen.addEdge(createEdge(coverage, nodeMatrix, xOffset, yOffset, xOffset + 1,
            yOffset + 1, calc));
      }
    }
 

    graph = (BasicGraph) graphGen.getGraph();
   */
  }

  public void teardown() {
    // TODO Auto-generated method stub

  }
  
  private double matrixNodeDistance(Envelope env, GeodeticCalculator calc) {
    
    double x = env.getMinimum(0);
    double y = env.getMinimum(1);

    calc.setStartingGeographicPoint(x, y);
    calc.setDestinationGeographicPoint(x, y + GRID_WIDTH);
    double distance = calc.getOrthodromicDistance();

    return distance;
  }

  private BasicDirectedEdge createEdge(GridCoverage2D coverage,
      BasicDirectedXYNode[][] nodeMatrix,
      int sourceX,
      int sourceY,
      int destX,
      int destY,
      GeodeticCalculator calc) {
    // Add horizontal edge, right.
    BasicDirectedEdge edge = new BasicDirectedEdge(nodeMatrix[destX][destY],
        nodeMatrix[sourceX][sourceY]);

    double[] sourceHeight = new double[1];
    Double sourcePoint = new Point2D.Double(nodeMatrix[sourceX][sourceY].getCoordinate().x,
        nodeMatrix[sourceX][sourceY].getCoordinate().y);
    coverage.evaluate(sourcePoint, sourceHeight);

    double[] destHeight = new double[1];
    Double destPoint = new Point2D.Double(nodeMatrix[destX][destY].getCoordinate().x,
        nodeMatrix[destX][destY].getCoordinate().y);
    coverage.evaluate(destPoint, destHeight);

    calc.setStartingGeographicPoint(nodeMatrix[sourceX][sourceY].getCoordinate().x,
        nodeMatrix[sourceX][sourceY].getCoordinate().y);
    calc.setDestinationGeographicPoint(nodeMatrix[destX][destY].getCoordinate().x,
        nodeMatrix[sourceX][sourceY].getCoordinate().y);
    double distance = calc.getOrthodromicDistance();

    double verticalFactor = 13.0;
    edge.setObject(new EdgeWeight(Math.abs(destHeight[0] - sourceHeight[0]) * verticalFactor
        + distance));

    return edge;
  }

}
