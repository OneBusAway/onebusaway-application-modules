package org.onebusaway.transit_data_federation.bundle.tasks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.ParseException;
import org.onebusaway.container.refresh.RefreshService;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.transit_data_federation.bundle.model.FederatedTransitDataBundle;
import org.onebusaway.transit_data_federation.impl.RouteCollectionSearchServiceImpl;
import org.onebusaway.transit_data_federation.impl.refresh.RefreshableResources;
import org.onebusaway.transit_data_federation.model.RouteCollection;
import org.onebusaway.transit_data_federation.services.RouteCollectionSearchService;
import org.onebusaway.transit_data_federation.services.TransitDataFederationDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Generate the underlying Lucene search index for route collection searches
 * that will power {@link RouteCollectionSearchServiceImpl} and
 * {@link RouteCollectionSearchService}.
 * 
 * @author bdferris
 * @see RouteCollectionSearchService
 * @see RouteCollectionSearchServiceImpl
 */
@Component
public class GenerateRouteCollectionSearchIndexTask implements Runnable {

  public static final String FIELD_ROUTE_COLLECTION_AGENCY_ID = "routeCollectionAgencyId";

  public static final String FIELD_ROUTE_COLLECTION_ID = "routeCollectionShortName";

  public static final String FIELD_AGENCY_ID = "agencyId";

  public static final String FIELD_ROUTE_ID = "routeId";

  public static final String FIELD_ROUTE_SHORT_NAME = "shortName";

  public static final String FIELD_ROUTE_LONG_NAME = "longName";

  public static final String FIELD_ROUTE_DESCRIPTION = "description";

  private TransitDataFederationDao _whereDao;

  private FederatedTransitDataBundle _bundle;

  private RefreshService _refreshService;

  @Autowired
  public void setWhereDao(TransitDataFederationDao dao) {
    _whereDao = dao;
  }

  @Autowired
  public void setBundle(FederatedTransitDataBundle bundle) {
    _bundle = bundle;
  }
  
  @Autowired
  public void setRefresService(RefreshService refreshService) {
    _refreshService = refreshService;
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
    IndexWriter writer = new IndexWriter(_bundle.getRouteSearchIndexPath(),
        new StandardAnalyzer(), true, IndexWriter.MaxFieldLength.LIMITED);
    for (RouteCollection routeCollection : _whereDao.getAllRouteCollections()) {
      List<Document> documents = getRouteCollectionAsDocuments(routeCollection);
      for (Document document : documents)
        writer.addDocument(document);
    }
    writer.optimize();
    writer.close();
    
    _refreshService.refresh(RefreshableResources.ROUTE_COLLECTION_SEARCH_DATA);
  }

  private List<Document> getRouteCollectionAsDocuments(
      RouteCollection routeCollection) {

    List<Document> documents = new ArrayList<Document>();

    AgencyAndId routeCollectionId = routeCollection.getId();

    for (Route route : routeCollection.getRoutes()) {
      Document document = new Document();

      // Route Collection
      document.add(new Field(FIELD_ROUTE_COLLECTION_AGENCY_ID,
          routeCollectionId.getAgencyId(), Field.Store.YES, Field.Index.NO));
      document.add(new Field(FIELD_ROUTE_COLLECTION_ID,
          routeCollectionId.getId(), Field.Store.YES, Field.Index.NO));

      // Id
      AgencyAndId id = route.getId();
      document.add(new Field(FIELD_AGENCY_ID, id.getAgencyId(),
          Field.Store.YES, Field.Index.NO));
      document.add(new Field(FIELD_ROUTE_ID, id.getId(), Field.Store.YES,
          Field.Index.ANALYZED));

      if (isValue(route.getShortName()))
        document.add(new Field(FIELD_ROUTE_SHORT_NAME, route.getShortName(),
            Field.Store.NO, Field.Index.ANALYZED));
      if (isValue(route.getLongName()))
        document.add(new Field(FIELD_ROUTE_LONG_NAME, route.getLongName(),
            Field.Store.NO, Field.Index.ANALYZED));
      if (isValue(route.getDesc()))
        document.add(new Field(FIELD_ROUTE_DESCRIPTION, route.getDesc(),
            Field.Store.NO, Field.Index.ANALYZED));

      documents.add(document);
    }

    return documents;
  }

  private static boolean isValue(String value) {
    return value != null && value.length() > 0;
  }
}
