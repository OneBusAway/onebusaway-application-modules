package org.onebusaway.transit_data_federation.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TopDocCollector;
import org.apache.lucene.search.TopDocs;
import org.onebusaway.container.refresh.Refreshable;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.bundle.model.FederatedTransitDataBundle;
import org.onebusaway.transit_data_federation.bundle.tasks.GenerateRouteCollectionSearchIndexTask;
import org.onebusaway.transit_data_federation.impl.refresh.RefreshableResources;
import org.onebusaway.transit_data_federation.model.SearchResult;
import org.onebusaway.transit_data_federation.services.RouteCollectionSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RouteCollectionSearchServiceImpl implements
    RouteCollectionSearchService {

  private static Analyzer _analyzer = new StandardAnalyzer();

  private static String[] NAME_FIELDS = {
      GenerateRouteCollectionSearchIndexTask.FIELD_ROUTE_SHORT_NAME,
      GenerateRouteCollectionSearchIndexTask.FIELD_ROUTE_LONG_NAME};

  private FederatedTransitDataBundle _bundle;

  private Searcher _searcher;

  @Autowired
  public void setBundle(FederatedTransitDataBundle bundle) {
    _bundle = bundle;
  }

  @PostConstruct
  @Refreshable(dependsOn = RefreshableResources.ROUTE_COLLECTION_SEARCH_DATA)
  public void initialize() throws IOException {

    File path = _bundle.getRouteSearchIndexPath();

    if (path.exists()) {
      IndexReader reader = IndexReader.open(path);
      _searcher = new IndexSearcher(reader);
    } else {
      _searcher = null;
    }
  }

  public SearchResult<AgencyAndId> searchForRoutesByShortName(String value,
      int maxResultCount, double minScoreToKeep) throws IOException,
      ParseException {

    return search(new MultiFieldQueryParser(NAME_FIELDS, _analyzer), value,
        maxResultCount, minScoreToKeep);
  }

  private SearchResult<AgencyAndId> search(QueryParser parser, String value,
      int maxResultCount, double minScoreToKeep) throws IOException,
      ParseException {

    if (_searcher == null)
      return new SearchResult<AgencyAndId>();

    TopDocCollector collector = new TopDocCollector(maxResultCount);

    Query query = parser.parse(value);
    _searcher.search(query, collector);

    TopDocs top = collector.topDocs();

    Map<AgencyAndId, Float> topScores = new HashMap<AgencyAndId, Float>();
    
    String lowerCaseQueryValue = value.toLowerCase();

    for (ScoreDoc sd : top.scoreDocs) {
      Document document = _searcher.doc(sd.doc);

      String agencyId = document.get(GenerateRouteCollectionSearchIndexTask.FIELD_ROUTE_COLLECTION_AGENCY_ID);
      String routeShortName = document.get(GenerateRouteCollectionSearchIndexTask.FIELD_ROUTE_COLLECTION_ID);
      AgencyAndId id = new AgencyAndId(agencyId, routeShortName);
      
      String lowerCaseRouteShortName = routeShortName.toLowerCase();
      
      // Result must have a minimum score to qualify
      if (sd.score < minScoreToKeep && !lowerCaseRouteShortName.equals(lowerCaseQueryValue))
        continue;

      // Keep the best score for a particular id
      Float score = topScores.get(id);
      if (score == null || score < sd.score)
        topScores.put(id, sd.score);
    }

    List<AgencyAndId> ids = new ArrayList<AgencyAndId>(topScores.size());
    double[] scores = new double[topScores.size()];

    int index = 0;
    for (AgencyAndId id : topScores.keySet()) {
      ids.add(id);
      scores[index] = topScores.get(id);
      index++;
    }

    return new SearchResult<AgencyAndId>(ids, scores);
  }
}
