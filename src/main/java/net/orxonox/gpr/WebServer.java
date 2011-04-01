package net.orxonox.gpr;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

public class WebServer extends AbstractServer {

  public void handle(HttpExchange t) throws IOException {

    // add the required response header for a PDF file
    Headers h = t.getResponseHeaders();

    h.add("Content-Type", "text/html");
    // a PDF (you provide your own!)

    URL indexPageURL = WebServer.class.getClassLoader().getResource(
        "web/index.html");
    File indexPage = new File(indexPageURL.getFile());

    byte[] content = new byte[(int) indexPage.length()];
    FileInputStream fis = new FileInputStream(indexPage);
    BufferedInputStream bis = new BufferedInputStream(fis);
    bis.read(content, 0, content.length);

    // bis.read(bytearray, 0, bytearray.length);

    // ok, we are ready to send the response.
    t.sendResponseHeaders(200, content.length);
    OutputStream os = t.getResponseBody();
    os.write(content, 0, content.length);
    os.close();

  }

}
