package org.onebusaway.transit_data_federation.impl;

import org.onebusaway.container.spring.PostConstruct;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.impl.offline.GenerateStopSearchIndexTask;
import org.onebusaway.transit_data_federation.model.SearchResult;
import org.onebusaway.transit_data_federation.services.StopSearchService;

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
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class StopSearchServiceImpl implements StopSearchService {

  private static Analyzer _analyzer = new StandardAnalyzer();

  private static String[] CODE_FIELDS = {GenerateStopSearchIndexTask.FIELD_STOP_CODE};

  private static String[] NAME_FIELDS = {GenerateStopSearchIndexTask.FIELD_STOP_NAME};

  private File _indexPath;

  private Searcher _searcher;

  public void setIndexPath(File indexPath) {
    _indexPath = indexPath;
  }

  @PostConstruct
  public void initialize() throws IOException {
    IndexReader reader = IndexReader.open(_indexPath);
    _searcher = new IndexSearcher(reader);
  }

  public SearchResult<AgencyAndId> searchForStopsByCode(String id, int maxResultCount) throws IOException,
      ParseException {
    return search(new MultiFieldQueryParser(CODE_FIELDS, _analyzer), id, maxResultCount);
  }

  public SearchResult<AgencyAndId> searchForStopsByName(String name, int maxResultCount) throws IOException,
      ParseException {
    return search(new MultiFieldQueryParser(NAME_FIELDS, _analyzer), name, maxResultCount);
  }

  private SearchResult<AgencyAndId> search(QueryParser parser, String value, int maxResultCount) throws IOException,
      ParseException {

    TopDocCollector collector = new TopDocCollector(maxResultCount);

    Query query = parser.parse(value);
    _searcher.search(query, collector);

    TopDocs top = collector.topDocs();
    List<AgencyAndId> ids = new ArrayList<AgencyAndId>(top.totalHits);
    double[] scores = new double[top.totalHits];

    int index = 0;
    for (ScoreDoc sd : top.scoreDocs) {
      Document document = _searcher.doc(sd.doc);
      String agencyId = document.get(GenerateStopSearchIndexTask.FIELD_AGENCY_ID);
      String stopId = document.get(GenerateStopSearchIndexTask.FIELD_STOP_ID);
      AgencyAndId id = new AgencyAndId(agencyId, stopId);
      ids.add(id);
      scores[index++] = sd.score;
    }

    return new SearchResult<AgencyAndId>(ids, scores);
  }
}
