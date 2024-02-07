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
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.FSDirectory;
import org.onebusaway.container.refresh.Refreshable;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.model.SearchResult;
import org.onebusaway.transit_data_federation.services.FederatedTransitDataBundle;
import org.onebusaway.transit_data_federation.services.StopSearchIndexConstants;
import org.onebusaway.transit_data_federation.services.StopSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StopSearchServiceImpl implements StopSearchService {

  private static Analyzer _analyzer = new StandardAnalyzer();

  private static String[] CODE_FIELDS = {StopSearchIndexConstants.FIELD_STOP_CODE};

  private static String[] NAME_FIELDS = {StopSearchIndexConstants.FIELD_STOP_NAME};

  private FederatedTransitDataBundle _bundle;

  private IndexSearcher _searcher;

  @Autowired
  public void setBundle(FederatedTransitDataBundle bundle) {
    _bundle = bundle;
  }

  @PostConstruct
  @Refreshable(dependsOn = RefreshableResources.STOP_SEARCH_DATA)
  public void initialize() throws IOException {
    File path = _bundle.getStopSearchIndexPath();

    if (path.exists()) {
      IndexReader reader = DirectoryReader.open(FSDirectory.open(path.toPath()));
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

    Query query = parser.parse(value);

    /* NOTE:  idf changed from
    (float)(Math.log(numDocs/(double)(docFreq+1)) + 1.0) to
    (float)Math.log(1 + (docCount - docFreq + 0.5) / (docFreq + 0.5))
    sometime after version 2.4.1
     */
    _searcher.setSimilarity(new ClassicSimilarity()); // new default is now BM25Similarity but conflicts with MIN_SCORE

    TopDocs top = _searcher.search(query, maxResultCount);

    Map<AgencyAndId, Float> topScores = new HashMap<AgencyAndId, Float>();

    for (ScoreDoc sd : top.scoreDocs) {
      Document document = _searcher.doc(sd.doc);
      if (sd.score < minScoreToKeep)
        continue;
      String agencyId = document.get(StopSearchIndexConstants.FIELD_AGENCY_ID);
      String stopId = document.get(StopSearchIndexConstants.FIELD_STOP_ID);
      AgencyAndId id = new AgencyAndId(agencyId, stopId);

      Float existingScore = topScores.get(id);
      if (existingScore == null || existingScore < sd.score)
        topScores.put(id, sd.score);
    }


    // we can safely cast to int here, maxResultCount can't exceed 2 billion results
    List<AgencyAndId> ids = new ArrayList<AgencyAndId>((int)top.totalHits);
    double[] scores = new double[(int)top.totalHits];

    int index = 0;
    for (AgencyAndId id : topScores.keySet()) {
      ids.add(id);
      scores[index] = topScores.get(id);
      index++;
    }

    return new SearchResult<AgencyAndId>(ids, scores);
  }
}
