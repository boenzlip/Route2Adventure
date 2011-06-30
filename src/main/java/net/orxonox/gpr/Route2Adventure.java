package net.orxonox.gpr;

import java.awt.Color;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.orxonox.gpr.http.FileServer;
import net.orxonox.gpr.http.GeoDataServer;
import net.orxonox.gpr.http.GoogleDirectionsServer;
import net.orxonox.gpr.http.TileServer;
import net.orxonox.gpr.http.WebServer;
import net.orxonox.gpr.store.HeightProfileStore;
import net.orxonox.gpr.store.MapTileStore;
import net.orxonox.gpr.store.RouteStore;

import org.geotools.factory.GeoTools;
import org.geotools.styling.ColorMap;
import org.geotools.styling.ColorMapImpl;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;

import com.sun.net.httpserver.HttpServer;

/**
 * Hello world!
 * 
 */
public class Route2Adventure {

  final ArrayBlockingQueue<Runnable> httpServerQueue = new ArrayBlockingQueue<Runnable>(
      5);

  public void start() {

    RouteStore routeStore = new RouteStore();
    routeStore.init();
    MapTileStore mapTileStore = new MapTileStore(routeStore);
    mapTileStore.init();

    HeightProfileStore heightProfileStore = new HeightProfileStore();
    heightProfileStore.init();
    
    // Start the tile server.
    // Multithreaded server for yet another speed increase!
    HttpServer server = null;
    try {
      server = HttpServer.create(new InetSocketAddress(8080), 0);
    } catch (IOException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
    ThreadPoolExecutor threadPool = new ThreadPoolExecutor(4, 8, 100,
        TimeUnit.SECONDS, httpServerQueue);
    server.setExecutor(threadPool);
    server.createContext("/", new WebServer());
    server.createContext("/tiles", new TileServer(mapTileStore));
    server.createContext("/htdoc", new FileServer());
    server.createContext("/geodata", new GeoDataServer(routeStore, heightProfileStore));
    server.createContext("/directions", new GoogleDirectionsServer(routeStore, heightProfileStore));
    server.setExecutor(null); // creates a default executor
    server.start();
    System.out
        .println("Local webserver started, open url: http://localhost:8080/");

    // TileRenderer tr = new TileRenderer();
    // tr.renderTile(0, 1, 0);

    // GraphViewer viewer = new GraphViewer();
    // viewer.setGraph(graph);
    // viewer.setPath(path);

    // JFrame f = new JFrame("A JFrame");
    // f.setSize(600, 600);
    // f.getContentPane().add(BorderLayout.CENTER, viewer);
    // f.setVisible(true);

    // Create a JMapFrame with a menu to choose the display style for the
    // final MapContext map = new DefaultMapContext();
    // frame = new JMapFrame(map);
    // frame.setSize(800, 600);
    // frame.enableStatusBar(true);
    // frame.enableToolBar(true);

    // try {
    // map.setCoordinateReferenceSystem(sphericalMercator);
    // } catch (TransformException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // } catch (FactoryException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }

    // map.setTitle("ImageLab");
    // map.addLayer(coverage, createGreyscaleStyle(reader));
    // map.addLayer(coverage, createColoredStyle());

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

  public static void main(String[] args) {
    System.out.println("Hello GeoTools:" + GeoTools.getVersion());

    Route2Adventure app = new Route2Adventure();
    app.start();
  }

}
