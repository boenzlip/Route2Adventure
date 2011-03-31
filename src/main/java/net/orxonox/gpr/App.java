package net.orxonox.gpr;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;

import javax.swing.JFrame;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.DataSourceException;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.factory.Hints;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.graph.build.basic.BasicDirectedGraphBuilder;
import org.geotools.graph.path.DijkstraShortestPathFinder;
import org.geotools.graph.path.Path;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.basic.BasicDirectedEdge;
import org.geotools.graph.structure.basic.BasicGraph;
import org.geotools.graph.structure.line.BasicDirectedXYNode;
import org.geotools.graph.traverse.standard.DijkstraIterator.EdgeWeighter;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.referencing.CRS;
import org.geotools.referencing.GeodeticCalculator;
import org.geotools.styling.ColorMap;
import org.geotools.styling.ColorMapImpl;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.StyleFactory;
import org.geotools.swing.JMapFrame;
import org.opengis.filter.FilterFactory2;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

import com.sun.net.httpserver.HttpServer;
import com.vividsolutions.jts.geom.Coordinate;

/**
 * Hello world!
 * 
 */
public class App {

  private StyleFactory sf = CommonFactoryFinder.getStyleFactory(null);
  private FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);
  private JMapFrame frame;

  public void start() {

    // Download data via maven:
    // \http://code.google.com/p/maven-download-plugin/
    // display a data store file chooser dialog for shapefiles
    // File file = JFileDataStoreChooser.showOpenFile("shp", null);
    URL inputStream = App.class.getClassLoader().getResource("srtm_38_03.tif");
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
      reader = new GeoTiffReader(inputStream, new Hints(
          Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE));
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

    BasicGraph graph;
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
    final double gridWidth = 0.1; // arc degrees.
    final int nX = (int) ((env.getMaximum(0) - env.getMinimum(0)) / gridWidth);
    final int nY = (int) ((env.getMaximum(1) - env.getMinimum(1)) / gridWidth);
    System.out.println("Array dimensions: " + nX + ", " + nY);

    final int graphScalingFactor = 1;

    // Create the node matrix.
    BasicDirectedXYNode[][] nodeMatrix = new BasicDirectedXYNode[nX][nY];
    for (int xOffset = 0; xOffset < nX; xOffset++) {
      for (int yOffset = 0; yOffset < nY; yOffset++) {
        BasicDirectedXYNode node = new BasicDirectedXYNode();
        double currentX = x + xOffset * gridWidth;
        double currentY = y + yOffset * gridWidth;
        node.setCoordinate(new Coordinate(currentX * graphScalingFactor,
            currentY * graphScalingFactor));
        graphGen.addNode(node);

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

        graphGen.addEdge(createEdge(coverage, nodeMatrix, xOffset, yOffset,
            xOffset + 1, yOffset, calc));
        graphGen.addEdge(createEdge(coverage, nodeMatrix, xOffset, yOffset,
            xOffset, yOffset + 1, calc));
        graphGen.addEdge(createEdge(coverage, nodeMatrix, xOffset, yOffset,
            xOffset, yOffset - 1, calc));
        graphGen.addEdge(createEdge(coverage, nodeMatrix, xOffset, yOffset,
            xOffset - 1, yOffset, calc));
      }
    }

    // create a strategy for weighting edges in the graph
    EdgeWeighter weighter = new EdgeWeighter() {
      public double getWeight(Edge e) {
        return ((EdgeWeight) e.getObject()).getWeight();
      }
    };

    graph = (BasicGraph) graphGen.getGraph();

    DijkstraShortestPathFinder pf = new DijkstraShortestPathFinder(graph,
        nodeMatrix[3][4], weighter);
    pf.calculate();

    final Path path = pf
        .getPath(nodeMatrix[nodeMatrix.length - 2][nodeMatrix[0].length - 2]);

    // Start the tile server.
    HttpServer server = null;
    try {
      server = HttpServer.create(new InetSocketAddress(8080), 0);
    } catch (IOException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
    server.createContext("/tiles", new TileServer(graph, path));
    server.createContext("/", new WebServer());
    server.setExecutor(null); // creates a default executor
    server.start();
    System.out
        .println("Local webserver started, open url: http://localhost:8080/");

    // TileRenderer tr = new TileRenderer();
    // tr.renderTile(0, 1, 0);

    GraphViewer viewer = new GraphViewer();
    viewer.setGraph(graph);
    viewer.setPath(path);

    JFrame f = new JFrame("A JFrame");
    f.setSize(600, 600);
    f.getContentPane().add(BorderLayout.CENTER, viewer);
    f.setVisible(true);

    // Create a JMapFrame with a menu to choose the display style for the
    final MapContext map = new DefaultMapContext();
    frame = new JMapFrame(map);
    frame.setSize(800, 600);
    frame.enableStatusBar(true);
    // frame.enableTool(JMapFrame.Tool.ZOOM, JMapFrame.Tool.PAN,
    // JMapFrame.Tool.RESET);
    frame.enableToolBar(true);

    try {
      map.setCoordinateReferenceSystem(sphericalMercator);
    } catch (TransformException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (FactoryException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    map.setTitle("ImageLab");
    // map.addLayer(coverage, createGreyscaleStyle(reader));
    map.addLayer(coverage, createColoredStyle());

    // Now display the map
    frame.setVisible(true);
  }

  private BasicDirectedEdge createEdge(GridCoverage2D coverage,
      BasicDirectedXYNode[][] nodeMatrix, int sourceX, int sourceY, int destX,
      int destY, GeodeticCalculator calc) {
    // Add horizontal edge, right.
    BasicDirectedEdge edge = new BasicDirectedEdge(nodeMatrix[destX][destY],
        nodeMatrix[sourceX][sourceY]);

    double[] sourceHeight = new double[1];
    Double sourcePoint = new Point2D.Double(
        nodeMatrix[sourceX][sourceY].getCoordinate().x,
        nodeMatrix[sourceX][sourceY].getCoordinate().y);
    coverage.evaluate(sourcePoint, sourceHeight);

    double[] destHeight = new double[1];
    Double destPoint = new Point2D.Double(
        nodeMatrix[destX][destY].getCoordinate().x,
        nodeMatrix[destX][destY].getCoordinate().y);
    coverage.evaluate(destPoint, destHeight);

    calc.setStartingGeographicPoint(
        nodeMatrix[sourceX][sourceY].getCoordinate().x,
        nodeMatrix[sourceX][sourceY].getCoordinate().y);
    calc.setDestinationGeographicPoint(
        nodeMatrix[destX][destY].getCoordinate().x,
        nodeMatrix[sourceX][sourceY].getCoordinate().y);
    double distance = calc.getOrthodromicDistance();

    double verticalFactor = 14.0;
    edge.setObject(new EdgeWeight(Math.abs(destHeight[0] - sourceHeight[0])
        * verticalFactor + distance));

    return edge;
  }

  private Style createColoredStyle() {
    StyleBuilder sb = new StyleBuilder();
    ColorMap cm = sb.createColorMap(new String[] { "1", "2", "3", "4", "5" },
        new double[] { 0, 1500, 2500, 3500, 4400 }, new Color[] {
            new Color(0, 255, 0), new Color(255, 255, 0),
            new Color(255, 127, 0), new Color(191, 127, 63),
            new Color(255, 255, 255) }, ColorMapImpl.TYPE_RAMP);
    RasterSymbolizer rsDem = sb.createRasterSymbolizer(cm, 1.0);
    Style demStyle = sb.createStyle(rsDem);

    // Put the data into a map:

    return demStyle;
  }

  public static void main(String[] args) {
    System.out.println("Hello GeoTools:" + GeoTools.getVersion());

    App app = new App();
    app.start();
  }

}
