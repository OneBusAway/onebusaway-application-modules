package org.onebusaway.transit_data_federation.impl.offline;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.transit_data_federation.impl.StopSearchServiceImpl;
import org.onebusaway.transit_data_federation.services.RunnableWithOutputPath;
import org.onebusaway.transit_data_federation.services.StopSearchService;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

/**
 * Generate the underlying Lucene search index for stop searches that will power
 * {@link StopSearchServiceImpl} and {@link StopSearchService}.
 * 
 * @author bdferris
 * @see StopSearchServiceImpl
 * @see StopSearchService
 */
@Component
public class GenerateStopSearchIndexTask implements RunnableWithOutputPath {

  public static final String FIELD_AGENCY_ID = "agencyId";

  public static final String FIELD_STOP_ID = "stopId";

  public static final String FIELD_STOP_NAME = "name";

  public static final String FIELD_STOP_CODE = "code";

  private GtfsRelationalDao _dao;

  private File _path;

  public void setOutputPath(File path) {
    _path = path;
  }

  @Autowired
  public void setGtfsDao(GtfsRelationalDao dao) {
    _dao = dao;
  }

  public void run() {
    try {
      buildIndex();
    } catch (Exception ex) {
      throw new IllegalStateException("error building stop search index", ex);
    }
  }

  private void buildIndex() throws IOException, ParseException {
    IndexWriter writer = new IndexWriter(_path, new StandardAnalyzer(), true,
        IndexWriter.MaxFieldLength.LIMITED);
    for (Stop stop : _dao.getAllStops()) {
      Document document = getStopAsDocument(stop);
      writer.addDocument(document);
    }
    writer.optimize();
    writer.close();
  }

  private Document getStopAsDocument(Stop stop) {

    Document document = new Document();

    // Id
    AgencyAndId id = stop.getId();
    document.add(new Field(FIELD_AGENCY_ID, id.getAgencyId(), Field.Store.YES,
        Field.Index.NO));
    document.add(new Field(FIELD_STOP_ID, id.getId(), Field.Store.YES,
        Field.Index.ANALYZED));

    // Code
    if (stop.getCode() != null && stop.getCode().length() > 0)
      document.add(new Field(FIELD_STOP_CODE, stop.getCode(), Field.Store.NO,
          Field.Index.ANALYZED));
    else
      document.add(new Field(FIELD_STOP_CODE, stop.getId().getId(),
          Field.Store.NO, Field.Index.ANALYZED));

    if (stop.getName() != null && stop.getName().length() > 0)
      document.add(new Field(FIELD_STOP_NAME, stop.getName(), Field.Store.YES,
          Field.Index.ANALYZED));

    return document;
  }
}
