package org.onebusaway.nextbus.actions.api;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.lang.reflect.Type;
import java.net.HttpURLConnection;

import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

import java.util.List;

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.onebusaway.nextbus.model.nextbus.Body;
import org.onebusaway.nextbus.model.transiTime.Predictions;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.opensymphony.xwork2.ModelDriven;

public class PredictionsAction implements
    ModelDriven<Body<List<Predictions>>> {

  private String agencyId = "";

  private String stopId = "";
  
  private String routeTag = "";

  public String getA() {
    return agencyId;
  }
  
  public void setA(String agencyId) {
    this.agencyId = agencyId;
  }
  
  public String getStopId() {
    return stopId;
  }

  public void setStopId(String stopId) {
    this.stopId = stopId;
  }
  
  public String getRouteTag() {
    return routeTag;
  }

  public void setRouteTag(String routeTag) {
    this.routeTag = routeTag;
  }

  public DefaultHttpHeaders index() {
    return new DefaultHttpHeaders("success");
  }

  public Body<List<Predictions>> getModel() {
    String serviceUrl = "http://localhost:8080/api/v1/key/8a3273b0/agency/1/command/predictions?";
    String routeStop = "rs=" + routeTag + "|" + stopId;
    String uri = serviceUrl + routeStop + "&format=json";

    Body<List<Predictions>> body = new Body<List<Predictions>>();

    try {
      JsonArray predictionsJson = getJsonObject(uri).getAsJsonArray(
          "predictions");
      Type listType = new TypeToken<List<Predictions>>() {
      }.getType();
      List<List<Predictions>> predictions = new Gson().fromJson(
          predictionsJson, listType);

      body.getResponse().addAll(predictions);
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }

    return body;

  }

  public String callURL(String myURL) {
    System.out.println("Requeted URL:" + myURL);
    StringBuilder sb = new StringBuilder();
    URLConnection urlConn = null;
    InputStreamReader in = null;
    try {
      URL url = new URL(myURL);
      urlConn = url.openConnection();
      if (urlConn != null)
        urlConn.setReadTimeout(60 * 1000);
      if (urlConn != null && urlConn.getInputStream() != null) {
        in = new InputStreamReader(urlConn.getInputStream(),
            Charset.defaultCharset());
        BufferedReader bufferedReader = new BufferedReader(in);
        if (bufferedReader != null) {
          int cp;
          while ((cp = bufferedReader.read()) != -1) {
            sb.append((char) cp);
          }
          bufferedReader.close();
        }
      }
      in.close();
    } catch (Exception e) {
      throw new RuntimeException("Exception while calling URL:" + myURL, e);
    }

    return sb.toString();
  }

  private JsonObject getJsonObject(String uri) throws Exception {
    URL url = new URL(uri);
    HttpURLConnection request = (HttpURLConnection) url.openConnection();
    request.connect();

    // Convert to a JSON object to print data
    JsonParser jp = new JsonParser(); // from gson
    JsonElement root = jp.parse(new InputStreamReader(
        (InputStream) request.getContent())); // Convert the input stream to a
                                              // json element
    JsonObject rootobj = root.getAsJsonObject(); // May be an array, may be an
                                                 // object.
    return rootobj;
  }

}
