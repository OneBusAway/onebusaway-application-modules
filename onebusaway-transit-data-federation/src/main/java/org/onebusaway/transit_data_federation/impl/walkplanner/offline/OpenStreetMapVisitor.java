package org.onebusaway.transit_data_federation.impl.walkplanner.offline;

import edu.washington.cs.rse.geospatial.latlon.CoordinateRectangle;
import edu.washington.cs.rse.geospatial.osm.OSMContentHandler;
import edu.washington.cs.rse.geospatial.osm.OSMLibrary;

import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class OpenStreetMapVisitor {

  private NumberFormat _format = new DecimalFormat("0.0000");

  private double _latStep = 0.04;

  private double _lonStep = 0.04;

  private double _overlap = 0.001;

  private Set<String> _keys = new HashSet<String>();

  private File _cacheDirectory;

  private OSMContentHandler _contentHandler;

  private Date _updateIfOlderThanDate;

  public void setLatStep(double latStep) {
    _latStep = latStep;
  }

  public void setLonStep(double lonStep) {
    _lonStep = lonStep;
  }

  public void setOverlap(double overlap) {
    _overlap = overlap;
  }

  public void setCacheDirectory(File cacheDirectory) {
    _cacheDirectory = cacheDirectory;
  }

  public void setContentHandler(OSMContentHandler contentHandler) {
    _contentHandler = contentHandler;
  }

  public void setUpdateIfOlderThanDate(Date updateIfOlderThanDate) {
    _updateIfOlderThanDate = updateIfOlderThanDate;
  }

  public void visitRegion(CoordinateRectangle rectangle) throws IOException,
      SAXException {

    double latFrom = floor(rectangle.getMinLat(), _latStep);
    double latTo = ceil(rectangle.getMaxLat(), _latStep);
    double lonFrom = floor(rectangle.getMinLon(), _lonStep);
    double lonTo = ceil(rectangle.getMaxLon(), _lonStep);

    for (double lat = latFrom; lat < latTo; lat += _latStep) {
      for (double lon = lonFrom; lon < lonTo; lon += _lonStep) {
        String key = getKey(lat, lon);
        if (!_keys.add(key))
          continue;
        File path = getPathToMapTile(lat, lon, key);
        OSMLibrary.parseMap(path, _contentHandler);
      }
    }
  }

  private double floor(double value, double step) {
    return step * Math.floor(value / step);
  }

  private double ceil(double value, double step) {
    return step * Math.ceil(value / step);
  }

  private String getKey(double lat, double lon) {
    return _format.format(lat) + "_" + _format.format(lon) + "_"
        + _format.format(_latStep) + "_" + _format.format(_lonStep) + "_"
        + _format.format(_overlap);
  }

  private File getPathToMapTile(double lat, double lon, String key)
      throws IOException {
    File path = new File(_cacheDirectory, "map-" + key + ".osm");

    if (needsUpdate(path)) {
      CoordinateRectangle r = new CoordinateRectangle(lat - _overlap, lon
          - _overlap, lat + _latStep + _overlap, lon + _lonStep + _overlap);

      URL url = constructUrl(r);
      String value = getUrlAsString(url);
      PrintWriter writer = new PrintWriter(new FileWriter(path));
      writer.println(value);
      writer.close();
    }

    return path;
  }

  private boolean needsUpdate(File path) {
    return !path.exists()
        || (_updateIfOlderThanDate != null && path.lastModified() < _updateIfOlderThanDate.getTime());
  }

  private String getUrlAsString(URL url) throws IOException {
    InputStream in = url.openStream();
    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
    String line = null;
    StringBuilder b = new StringBuilder();
    while ((line = reader.readLine()) != null) {
      b.append(line);
      b.append("\n");
    }
    reader.close();
    return b.toString();
  }

  private URL constructUrl(CoordinateRectangle r) {
    double left = r.getMinLon();
    double right = r.getMaxLon();
    double bottom = r.getMinLat();
    double top = r.getMaxLat();
    try {
      return new URL("http://api.openstreetmap.org/api/0.6/map?bbox=" + left
          + "," + bottom + "," + right + "," + top);
    } catch (MalformedURLException e) {
      throw new IllegalStateException(e);
    }
  }
}
