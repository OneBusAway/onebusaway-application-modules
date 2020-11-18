/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2011 Google, Inc.
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
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
import org.apache.lucene.store.FSDirectory;
import org.onebusaway.container.refresh.Refreshable;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.model.SearchResult;
import org.onebusaway.transit_data_federation.services.FederatedTransitDataBundle;
import org.onebusaway.transit_data_federation.services.RouteCollectionSearchIndexConstants;
import org.onebusaway.transit_data_federation.services.RouteCollectionSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RouteCollectionSearchServiceImpl implements
    RouteCollectionSearchService {

  public static final String[] ENGLISH_STOP_WORDS = {
    "an", "and", "are", "as", "at", "be", "but", "by",
    "for", "if", "in", "into", "is", "it",
    "no", "not", "of", "on", "or", "such",
    "that", "the", "their", "then", "there", "these",
    "they", "this", "to", "was", "will", "with"
  };
  private static Analyzer _analyzer
          = new StandardAnalyzer(new CharArraySet(Arrays.asList(ENGLISH_STOP_WORDS), true));

  private static String[] NAME_FIELDS = {
      RouteCollectionSearchIndexConstants.FIELD_ROUTE_SHORT_NAME,
      RouteCollectionSearchIndexConstants.FIELD_ROUTE_LONG_NAME};

  private FederatedTransitDataBundle _bundle;

  private IndexSearcher _searcher;

  @Autowired
  public void setBundle(FederatedTransitDataBundle bundle) {
    _bundle = bundle;
  }

  @PostConstruct
  @Refreshable(dependsOn = RefreshableResources.ROUTE_COLLECTION_SEARCH_DATA)
  public void initialize() throws IOException {

    File path = _bundle.getRouteSearchIndexPath();

    if (path.exists()) {
      IndexReader reader = DirectoryReader.open(FSDirectory.open(path.toPath()));
      _searcher = new IndexSearcher(reader);
    } else {
      _searcher = null;
    }
  }

  public SearchResult<AgencyAndId> searchForRoutesByName(String value,
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

    Query query = parser.parse(value);
    TopDocs top = _searcher.search(query, maxResultCount);

    Map<AgencyAndId, Float> topScores = new HashMap<AgencyAndId, Float>();

    String lowerCaseQueryValue = value.toLowerCase();

    for (ScoreDoc sd : top.scoreDocs) {
      Document document = _searcher.doc(sd.doc);

      String routeShortName = document.get(RouteCollectionSearchIndexConstants.FIELD_ROUTE_SHORT_NAME);

      Set<String> tokens = new HashSet<String>();
      if (routeShortName != null) {
        for (String token : routeShortName.toLowerCase().split("\\b")) {
          if (!token.isEmpty())
            tokens.add(token);
        }
      }

      // Result must have a minimum score to qualify
      if (sd.score < minScoreToKeep && !tokens.contains(lowerCaseQueryValue))
        continue;

      // Keep the best score for a particular id
      String agencyId = document.get(RouteCollectionSearchIndexConstants.FIELD_ROUTE_COLLECTION_AGENCY_ID);
      String id = document.get(RouteCollectionSearchIndexConstants.FIELD_ROUTE_COLLECTION_ID);
      AgencyAndId routeId = new AgencyAndId(agencyId, id);
      Float score = topScores.get(routeId);
      if (score == null || score < sd.score)
        topScores.put(routeId, sd.score);
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
