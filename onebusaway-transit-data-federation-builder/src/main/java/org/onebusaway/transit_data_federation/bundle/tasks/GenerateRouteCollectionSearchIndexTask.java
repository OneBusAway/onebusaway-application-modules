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
package org.onebusaway.transit_data_federation.bundle.tasks;

import java.io.IOException;
import java.util.Arrays;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.miscellaneous.LimitTokenCountAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.onebusaway.container.refresh.RefreshService;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.impl.RefreshableResources;
import org.onebusaway.transit_data_federation.impl.RouteCollectionSearchServiceImpl;
import org.onebusaway.transit_data_federation.model.narrative.RouteCollectionNarrative;
import org.onebusaway.transit_data_federation.services.FederatedTransitDataBundle;
import org.onebusaway.transit_data_federation.services.RouteCollectionSearchIndexConstants;
import org.onebusaway.transit_data_federation.services.RouteCollectionSearchService;
import org.onebusaway.transit_data_federation.services.StopSearchIndexConstants;
import org.onebusaway.transit_data_federation.services.narrative.NarrativeService;
import org.onebusaway.transit_data_federation.services.transit_graph.RouteCollectionEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
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

  public static final String[] ENGLISH_STOP_WORDS = {
    "an", "and", "are", "as", "at", "be", "but", "by",
    "for", "if", "in", "into", "is", "it",
    "no", "not", "of", "on", "or", "such",
    "that", "the", "their", "then", "there", "these",
    "they", "this", "to", "was", "will", "with"
  };
  private TransitGraphDao _transitGraphDao;

  private NarrativeService _narrativeService;

  private FederatedTransitDataBundle _bundle;

  private RefreshService _refreshService;

  @Autowired
  public void setTransitGraphDao(TransitGraphDao transitGraphDao) {
    _transitGraphDao = transitGraphDao;
  }

  @Autowired
  public void setNarrativeService(NarrativeService narrativeService) {
    _narrativeService = narrativeService;
  }

  @Autowired
  public void setBundle(FederatedTransitDataBundle bundle) {
    _bundle = bundle;
  }

  @Autowired
  public void setRefreshService(RefreshService refreshService) {
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
    LimitTokenCountAnalyzer limitTokenCountAnalyzer
            = new LimitTokenCountAnalyzer(
                    new StandardAnalyzer(new CharArraySet(Arrays.asList(ENGLISH_STOP_WORDS), true)),
            StopSearchIndexConstants.MAX_LIMIT);
    Directory index = FSDirectory.open(_bundle.getRouteSearchIndexPath().toPath());
    IndexWriterConfig config = new IndexWriterConfig(limitTokenCountAnalyzer);
    IndexWriter writer = new IndexWriter(index, config);

    for (RouteCollectionEntry routeCollection : _transitGraphDao.getAllRouteCollections()) {
      RouteCollectionNarrative narrative = _narrativeService.getRouteCollectionForId(routeCollection.getId());
      Document document = getRouteCollectionAsDocument(routeCollection,
          narrative);
      writer.addDocument(document);
    }
    writer.close();

    _refreshService.refresh(RefreshableResources.ROUTE_COLLECTION_SEARCH_DATA);
  }

  private Document getRouteCollectionAsDocument(
      RouteCollectionEntry routeCollection, RouteCollectionNarrative narrative) {

    AgencyAndId routeCollectionId = routeCollection.getId();

    Document document = new Document();

    // Route Collection
    document.add(new StringField(
        RouteCollectionSearchIndexConstants.FIELD_ROUTE_COLLECTION_AGENCY_ID,
        routeCollectionId.getAgencyId(), Field.Store.YES));
    document.add(new StringField(
        RouteCollectionSearchIndexConstants.FIELD_ROUTE_COLLECTION_ID,
        routeCollectionId.getId(), Field.Store.YES));

    if (isValue(narrative.getShortName()))
      document.add(new TextField(
          RouteCollectionSearchIndexConstants.FIELD_ROUTE_SHORT_NAME,
          narrative.getShortName(), Field.Store.YES));
    if (isValue(narrative.getLongName()))
      document.add(new TextField(
          RouteCollectionSearchIndexConstants.FIELD_ROUTE_LONG_NAME,
          narrative.getLongName(), Field.Store.NO));
    if (isValue(narrative.getDescription()))
      document.add(new TextField(
          RouteCollectionSearchIndexConstants.FIELD_ROUTE_DESCRIPTION,
          narrative.getDescription(), Field.Store.NO));

    return document;
  }

  private static boolean isValue(String value) {
    return value != null && value.length() > 0;
  }
}
