package org.onebusaway.transit_data_federation.impl.offline;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.transit_data_federation.model.RouteCollection;
import org.onebusaway.transit_data_federation.services.RunnableWithOutputPath;
import org.onebusaway.transit_data_federation.services.TransitDataFederationDao;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class GenerateRouteCollectionSearchIndexTask implements RunnableWithOutputPath {

  public static final String FIELD_ROUTE_COLLECTION_AGENCY_ID = "routeCollectionAgencyId";

  public static final String FIELD_ROUTE_COLLECTION_ID = "routeCollectionShortName";

  public static final String FIELD_AGENCY_ID = "agencyId";

  public static final String FIELD_ROUTE_ID = "routeId";

  public static final String FIELD_ROUTE_SHORT_NAME = "shortName";

  public static final String FIELD_ROUTE_LONG_NAME = "longName";

  public static final String FIELD_ROUTE_DESCRIPTION = "description";

  private TransitDataFederationDao _whereDao;

  private File _path;

  public void setOutputPath(File path) {
    _path = path;
  }

  @Autowired
  public void setWhereDao(TransitDataFederationDao dao) {
    _whereDao = dao;
  }

  @Transactional
  public void run() {
    try {
      buildIndex();
    } catch (Exception ex) {
      throw new IllegalStateException("error building route search index", ex);
    }
  }

  private void buildIndex() throws IOException, ParseException {
    IndexWriter writer = new IndexWriter(_path, new StandardAnalyzer(), true, IndexWriter.MaxFieldLength.LIMITED);
    for (RouteCollection routeCollection : _whereDao.getAllRouteCollections()) {
      List<Document> documents = getRouteCollectionAsDocuments(routeCollection);
      for (Document document : documents)
        writer.addDocument(document);
    }
    writer.optimize();
    writer.close();
  }

  private List<Document> getRouteCollectionAsDocuments(RouteCollection routeCollection) {

    List<Document> documents = new ArrayList<Document>();

    AgencyAndId routeCollectionId = routeCollection.getId();

    for (Route route : routeCollection.getRoutes()) {
      Document document = new Document();

      // Route Collection
      document.add(new Field(FIELD_ROUTE_COLLECTION_AGENCY_ID, routeCollectionId.getAgencyId(), Field.Store.YES,
          Field.Index.NO));
      document.add(new Field(FIELD_ROUTE_COLLECTION_ID, routeCollectionId.getId(), Field.Store.YES,
          Field.Index.NO));

      // Id
      AgencyAndId id = route.getId();
      document.add(new Field(FIELD_AGENCY_ID, id.getAgencyId(), Field.Store.YES, Field.Index.NO));
      document.add(new Field(FIELD_ROUTE_ID, id.getId(), Field.Store.YES, Field.Index.ANALYZED));

      if (isValue(route.getShortName()))
        document.add(new Field(FIELD_ROUTE_SHORT_NAME, route.getShortName(), Field.Store.NO, Field.Index.ANALYZED));
      if (isValue(route.getLongName()))
        document.add(new Field(FIELD_ROUTE_LONG_NAME, route.getLongName(), Field.Store.NO, Field.Index.ANALYZED));
      if (isValue(route.getDesc()))
        document.add(new Field(FIELD_ROUTE_DESCRIPTION, route.getDesc(), Field.Store.NO, Field.Index.ANALYZED));

      documents.add(document);
    }

    return documents;
  }

  private static boolean isValue(String value) {
    return value != null && value.length() > 0;
  }
}
