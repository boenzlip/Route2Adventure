package net.orxonox.gpr;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.io.IOException;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.DataSourceException;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.factory.Hints;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.graph.build.basic.BasicDirectedGraphBuilder;
import org.geotools.graph.structure.basic.BasicDirectedGraph;
import org.geotools.graph.structure.line.BasicDirectedXYNode;
import org.geotools.graph.util.delaunay.GraphViewer;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.referencing.CRS;
import org.geotools.referencing.GeodeticCalculator;
import org.geotools.styling.ChannelSelection;
import org.geotools.styling.ColorMap;
import org.geotools.styling.ColorMapImpl;
import org.geotools.styling.ContrastEnhancement;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.SLD;
import org.geotools.styling.SelectedChannelType;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.Symbolizer;
import org.geotools.swing.JMapFrame;
import org.opengis.filter.FilterFactory2;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.opengis.style.ContrastMethod;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.planargraph.Edge;

/**
 * Hello world!
 * 
 */
public class App {

  private StyleFactory sf = CommonFactoryFinder.getStyleFactory(null);
  private FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);
  private JMapFrame frame;

  public void start() {
    URL inputStream = getClass().getClassLoader().getResource("srtm_38_03.tif");

    GeoTiffReader reader = null;
    try {
      reader = new GeoTiffReader(inputStream, new Hints(
          Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE));
    } catch (DataSourceException ex) {
      ex.printStackTrace();
      // return;
    }

    GridCoverage2D coverage = null;
    try {
      coverage = (GridCoverage2D) reader.read(null);
    } catch (IOException ex) {
      ex.printStackTrace();
      // return;
    }

    // Using a GridCoverage2D
    CoordinateReferenceSystem crs = coverage.getCoordinateReferenceSystem2D();
    System.out.println("crs: " + crs);
    int SRID = 0;
    try {
      SRID = CRS.lookupEpsgCode(crs, true).intValue();
    } catch (FactoryException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    System.out.println("srid: " + SRID);
    Envelope env = coverage.getEnvelope();
    System.out.println("evn: " + env.toString());

  }

  public void start2() {

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
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (FactoryException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    GeodeticCalculator calc = new GeodeticCalculator(sphericalMercator);

    BasicDirectedGraph graph;
    // create the graph generator
    BasicDirectedGraphBuilder graphGen = new BasicDirectedGraphBuilder();

    // http://maps.google.ch/maps/mm?ie=UTF8&hl=de&ll=46.545284,6.87212&spn=0.096341,0.187969&t=h&z=13
    // http://maps.google.ch/maps/mm?ie=UTF8&hl=de&ll=46.227828,7.897797&spn=0.096903,0.187969&t=h&z=13\
    // reading height values, example.
    Envelope env = coverage.getEnvelope();
    double x = 7.89779; // env.getMedian(0);
    double y = 46.227828;// env.getMedian(1);
    Double lastPoint = null;
    for (int i = 0; i < 50; i++) {
      double[] dest = new double[3];
      // gridGeometry.toPoint2D(coord), dest
      Double point = new Point2D.Double(x, y);
      coverage.evaluate(point, dest);
      System.out.println(y + ", " + x + ", " + dest[0]);
      x += 0.001;
      calc.setStartingGeographicPoint(point);
      if (lastPoint != null) {
        calc.setDestinationGeographicPoint(lastPoint);
        double distance = calc.getOrthodromicDistance();
        System.out.println("Distance: " + distance);
      }
      lastPoint = point;

      BasicDirectedXYNode node = new BasicDirectedXYNode();
      node.setCoordinate(new Coordinate(x * 10000, y * 10000));
      Edge edge2 = new Edge();
      graphGen.addNode(node);
    }

    graph = (BasicDirectedGraph) graphGen.getGraph();
    GraphViewer viewer = new GraphViewer();
    viewer.setGraph(graph);

    JFrame f = new JFrame("A JFrame");
    f.setSize(600, 600);
    f.getContentPane().add(BorderLayout.CENTER, viewer);
    f.setVisible(true);

    // This can be used to render images.
    // coverage.getRenderedImage();

    // GridGeometry2D geo = coverage.getGridGeometry();

    // Set up a MapContext with the two layers

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
    // frame.setVisible(true);
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

  /**
   * Create a Style to display a selected band of the GeoTIFF image as a
   * greyscale layer
   * 
   * @return a new Style instance to render the image in greyscale
   */
  private Style createGreyscaleStyle(GeoTiffReader reader) {
    GridCoverage2D cov = null;
    try {
      cov = reader.read(null);
    } catch (IOException giveUp) {
      throw new RuntimeException(giveUp);
    }
    int numBands = cov.getNumSampleDimensions();
    Integer[] bandNumbers = new Integer[numBands];
    for (int i = 0; i < numBands; i++) {
      bandNumbers[i] = Integer.valueOf(i + 1);
    }
    Object selection = JOptionPane.showInputDialog(frame.getComponent(0),
        "Band to use for greyscale display", "Select an image band",
        JOptionPane.QUESTION_MESSAGE, null, bandNumbers, Integer.valueOf(1));
    if (selection != null) {
      int band = ((Number) selection).intValue();
      return createGreyscaleStyle(band);
    }
    return null;
  }

  /**
   * Create a Style to display the specified band of the GeoTIFF image as a
   * greyscale layer.
   * <p>
   * This method is a helper for createGreyScale() and is also called directly
   * by the displayLayers() method when the application first starts.
   * 
   * @param band
   *          the image band to use for the greyscale display
   * 
   * @return a new Style instance to render the image in greyscale
   */
  private Style createGreyscaleStyle(int band) {
    ContrastEnhancement ce = sf.contrastEnhancement(ff.literal(0.8),
        ContrastMethod.NONE);
    SelectedChannelType sct = sf.createSelectedChannelType(
        String.valueOf(band), ce);

    RasterSymbolizer sym = sf.getDefaultRasterSymbolizer();
    ChannelSelection sel = sf.channelSelection(sct);
    sym.setChannelSelection(sel);

    Symbolizer[] syms = new Symbolizer[] { sym };

    return SLD.wrapSymbolizers(syms);
  }

  /**
   * This method examines the names of the sample dimensions in the provided
   * coverage looking for "red...", "green..." and "blue..." (case insensitive
   * match). If these names are not found it uses bands 1, 2, and 3 for the red,
   * green and blue channels. It then sets up a raster symbolizer and returns
   * this wrapped in a Style.
   * 
   * @return a new Style object containing a raster symbolizer set up for RGB
   *         image
   */
  private Style createRGBStyle(GeoTiffReader reader) {
    GridCoverage2D cov = null;
    try {
      cov = reader.read(null);
    } catch (IOException giveUp) {
      throw new RuntimeException(giveUp);
    }
    // We need at least three bands to create an RGB style
    int numBands = cov.getNumSampleDimensions();
    if (numBands < 3) {
      return null;
    }
    // Get the names of the bands
    String[] sampleDimensionNames = new String[numBands];
    for (int i = 0; i < numBands; i++) {
      GridSampleDimension dim = cov.getSampleDimension(i);
      sampleDimensionNames[i] = dim.getDescription().toString();
    }
    final int RED = 0, GREEN = 1, BLUE = 2;
    int[] channelNum = { -1, -1, -1 };
    // We examine the band names looking for "red...", "green...", "blue...".
    // Note that the channel numbers we record are indexed from 1, not 0.
    for (int i = 0; i < numBands; i++) {
      String name = sampleDimensionNames[i].toLowerCase();
      if (name != null) {
        if (name.matches("red.*")) {
          channelNum[RED] = i + 1;
        } else if (name.matches("green.*")) {
          channelNum[GREEN] = i + 1;
        } else if (name.matches("blue.*")) {
          channelNum[BLUE] = i + 1;
        }
      }
    }
    // If we didn't find named bands "red...", "green...", "blue..."
    // we fall back to using the first three bands in order
    if (channelNum[RED] < 0 || channelNum[GREEN] < 0 || channelNum[BLUE] < 0) {
      channelNum[RED] = 1;
      channelNum[GREEN] = 2;
      channelNum[BLUE] = 3;
    }
    // Now we create a RasterSymbolizer using the selected channels
    SelectedChannelType[] sct = new SelectedChannelType[cov
        .getNumSampleDimensions()];
    ContrastEnhancement ce = sf.contrastEnhancement(ff.literal(1.0),
        ContrastMethod.NORMALIZE);
    for (int i = 0; i < 3; i++) {
      sct[i] = sf.createSelectedChannelType(String.valueOf(channelNum[i]), ce);
    }
    Symbolizer[] symbolizers = new Symbolizer[0];
    RasterSymbolizer sym = sf.getDefaultRasterSymbolizer();
    ChannelSelection sel = sf.channelSelection(sct[RED], sct[GREEN], sct[BLUE]);
    sym.setChannelSelection(sel);
    symbolizers[0] = sym;

    return SLD.wrapSymbolizers(symbolizers);
  }

  public static void main(String[] args) {
    System.out.println("Hello GeoTools:" + GeoTools.getVersion());

    // FileDataStore store = null;
    // FeatureSource featureSource = null;
    // try {
    // store = FileDataStoreFinder.getDataStore(file);
    // featureSource = store.getFeatureSource();
    // } catch (IOException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }

    // Create a map context and add our shapefile to it

    App app = new App();
    app.start2();
  }

}
