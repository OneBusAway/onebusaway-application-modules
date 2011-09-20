/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.transit_data_federation.impl;

import org.onebusaway.container.refresh.Refreshable;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.bundle.model.FederatedTransitDataBundle;
import org.onebusaway.transit_data_federation.bundle.tasks.GenerateStopSearchIndexTask;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

@Component
public class StopSearchServiceImpl implements StopSearchService {

  private static Analyzer _analyzer = new StandardAnalyzer();

  private static String[] CODE_FIELDS = {GenerateStopSearchIndexTask.FIELD_STOP_CODE};

  private static String[] NAME_FIELDS = {GenerateStopSearchIndexTask.FIELD_STOP_NAME};

  private FederatedTransitDataBundle _bundle;

  private Searcher _searcher;

  @Autowired
  public void setBundle(FederatedTransitDataBundle bundle) {
    _bundle = bundle;
  }

  @PostConstruct
  @Refreshable(dependsOn = RefreshableResources.STOP_SEARCH_DATA)
  public void initialize() throws IOException {
    File path = _bundle.getStopSearchIndexPath();

    if (path.exists()) {
      IndexReader reader = IndexReader.open(path);
      _searcher = new IndexSearcher(reader);
    } else {
      _searcher = null;
    }
  }

  public SearchResult<AgencyAndId> searchForStopsByCode(String id,
      int maxResultCount, double minScoreToKeep) throws IOException,
      ParseException {
    return search(new MultiFieldQueryParser(CODE_FIELDS, _analyzer), id,
        maxResultCount, minScoreToKeep);
  }

  public SearchResult<AgencyAndId> searchForStopsByName(String name,
      int maxResultCount, double minScoreToKeep) throws IOException,
      ParseException {
    return search(new MultiFieldQueryParser(NAME_FIELDS, _analyzer), name,
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

    for (ScoreDoc sd : top.scoreDocs) {
      Document document = _searcher.doc(sd.doc);
      if (sd.score < minScoreToKeep)
        continue;
      String agencyId = document.get(GenerateStopSearchIndexTask.FIELD_AGENCY_ID);
      String stopId = document.get(GenerateStopSearchIndexTask.FIELD_STOP_ID);
      AgencyAndId id = new AgencyAndId(agencyId, stopId);

      Float existingScore = topScores.get(id);
      if (existingScore == null || existingScore < sd.score)
        topScores.put(id, sd.score);
    }

    List<AgencyAndId> ids = new ArrayList<AgencyAndId>(top.totalHits);
    double[] scores = new double[top.totalHits];

    int index = 0;
    for (AgencyAndId id : topScores.keySet()) {
      ids.add(id);
      scores[index] = topScores.get(id);
      index++;
    }

    return new SearchResult<AgencyAndId>(ids, scores);
  }
}
