package net.orxonox.gpr.http;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

public class FileServer extends AbstractServer {

  public void handle(HttpExchange t) throws IOException {

    parseGetParameters(t);
    // add the required response header for a PDF file
    Headers h = t.getResponseHeaders();


    String directoryMarker = "/htdoc/";
    String fileName = t.getRequestURI()
        .getPath()
        .substring(t.getRequestURI().getPath().indexOf(directoryMarker) + directoryMarker.length());

    String contentType = "image/png";
    try {
      contentType = getStringAttribute(t, "contentType");
    } catch (AttributeNotFoundException e) {
    }

    //
    URL imageUrl = FileServer.class.getClassLoader().getResource("web/" + fileName);
    File indexPage = new File(imageUrl.getFile());
    h.add("Content-Type", contentType);

    byte[] content = new byte[(int) indexPage.length()];
    FileInputStream fis = new FileInputStream(indexPage);
    BufferedInputStream bis = new BufferedInputStream(fis);
    bis.read(content, 0, content.length);


    // ok, we are ready to send the response.
    t.sendResponseHeaders(200, content.length);
    OutputStream os = t.getResponseBody();
    os.write(content, 0, content.length);
    os.close();

  }

}
