package org.onebusaway.gtfs_diff.impl.serialization;

import org.onebusaway.gtfs_diff.model.GtfsDifferences;
import org.onebusaway.gtfs_diff.services.GtfsDifferencesSerializationService;

import com.thoughtworks.xstream.XStream;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class XStreamGtfsDifferencesSerializationServiceImpl implements
    GtfsDifferencesSerializationService {

  private File _path;
  private GtfsDifferences _differences;

  public XStreamGtfsDifferencesSerializationServiceImpl(File path) {
    _path = path;
  }

  public void serializeDifferences(GtfsDifferences differences) {
    _differences = differences;
    try {
      Writer writer = new BufferedWriter(new FileWriter(_path));
      XStream xstream = createXStream();
      xstream.toXML(differences, writer);
      writer.close();
    } catch (IOException ex) {
      throw new IllegalStateException(ex);
    }
  }

  protected XStream createXStream() {
    GtsDifferencesXStreamSourceImpl source = new GtsDifferencesXStreamSourceImpl(
        _differences);
    return source.createXStream();
  }
  
  
}
