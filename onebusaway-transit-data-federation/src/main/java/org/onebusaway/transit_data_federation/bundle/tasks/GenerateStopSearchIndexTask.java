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

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.ParseException;
import org.onebusaway.container.refresh.RefreshService;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.bundle.model.FederatedTransitDataBundle;
import org.onebusaway.transit_data_federation.impl.RefreshableResources;
import org.onebusaway.transit_data_federation.impl.StopSearchServiceImpl;
import org.onebusaway.transit_data_federation.model.narrative.StopNarrative;
import org.onebusaway.transit_data_federation.services.StopSearchService;
import org.onebusaway.transit_data_federation.services.narrative.NarrativeService;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Generate the underlying Lucene search index for stop searches that will power
 * {@link StopSearchServiceImpl} and {@link StopSearchService}.
 * 
 * @author bdferris
 * @see StopSearchServiceImpl
 * @see StopSearchService
 */
@Component
public class GenerateStopSearchIndexTask implements Runnable {

  public static final String FIELD_AGENCY_ID = "agencyId";

  public static final String FIELD_STOP_ID = "stopId";

  public static final String FIELD_STOP_NAME = "name";

  public static final String FIELD_STOP_CODE = "code";

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

  public void run() {
    try {
      buildIndex();
    } catch (Exception ex) {
      throw new IllegalStateException("error building stop search index", ex);
    }
  }

  private void buildIndex() throws IOException, ParseException {
    IndexWriter writer = new IndexWriter(_bundle.getStopSearchIndexPath(),
        new StandardAnalyzer(), true, IndexWriter.MaxFieldLength.LIMITED);
    for (StopEntry stopEntry : _transitGraphDao.getAllStops()) {
      StopNarrative narrative = _narrativeService.getStopForId(stopEntry.getId());
      Document document = getStopAsDocument(stopEntry, narrative);
      writer.addDocument(document);
    }
    writer.optimize();
    writer.close();
    _refreshService.refresh(RefreshableResources.STOP_SEARCH_DATA);
  }

  private Document getStopAsDocument(StopEntry stopEntry,
      StopNarrative narrative) {

    Document document = new Document();

    // Id
    AgencyAndId id = stopEntry.getId();
    document.add(new Field(FIELD_AGENCY_ID, id.getAgencyId(), Field.Store.YES,
        Field.Index.NO));
    document.add(new Field(FIELD_STOP_ID, id.getId(), Field.Store.YES,
        Field.Index.ANALYZED));

    // Code
    if (narrative.getCode() != null && narrative.getCode().length() > 0)
      document.add(new Field(FIELD_STOP_CODE, narrative.getCode(),
          Field.Store.NO, Field.Index.ANALYZED));
    else
      document.add(new Field(FIELD_STOP_CODE, stopEntry.getId().getId(),
          Field.Store.NO, Field.Index.ANALYZED));

    if (narrative.getName() != null && narrative.getName().length() > 0)
      document.add(new Field(FIELD_STOP_NAME, narrative.getName(),
          Field.Store.YES, Field.Index.ANALYZED));

    return document;
  }
}
