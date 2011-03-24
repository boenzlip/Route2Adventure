package net.orxonox.gpr;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.swing.JOptionPane;

import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.DataSourceException;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.factory.Hints;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.referencing.CRS;
import org.geotools.styling.ChannelSelection;
import org.geotools.styling.ContrastEnhancement;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.SLD;
import org.geotools.styling.SelectedChannelType;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.Symbolizer;
import org.geotools.swing.JMapFrame;
import org.opengis.filter.FilterFactory2;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.style.ContrastMethod;

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

    // display a data store file chooser dialog for shapefiles
    // File file = JFileDataStoreChooser.showOpenFile("shp", null);
    URL inputStream = App.class.getClassLoader().getResource("srtm_38_03.tif");
    File file = new File(inputStream.getFile());
    if (!file.exists()) {
      return;
    }

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

    // Set up a MapContext with the two layers

    // Create a JMapFrame with a menu to choose the display style for the
    final MapContext map = new DefaultMapContext();
    frame = new JMapFrame(map);
    frame.setSize(800, 600);
    frame.enableStatusBar(true);
    // frame.enableTool(JMapFrame.Tool.ZOOM, JMapFrame.Tool.PAN,
    // JMapFrame.Tool.RESET);
    frame.enableToolBar(true);

    map.setTitle("ImageLab");
    map.addLayer(coverage, createGreyscaleStyle(reader));

    // Now display the map
    frame.setVisible(true);
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
