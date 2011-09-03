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
package org.onebusaway.transit_data_federation.bundle.tasks;

import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.ParseException;
import org.onebusaway.container.refresh.RefreshService;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.transit_data_federation.bundle.model.FederatedTransitDataBundle;
import org.onebusaway.transit_data_federation.impl.RefreshableResources;
import org.onebusaway.transit_data_federation.impl.StopSearchServiceImpl;
import org.onebusaway.transit_data_federation.services.StopSearchService;
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

  private GtfsRelationalDao _dao;

  private FederatedTransitDataBundle _bundle;

  private RefreshService _refreshService;

  @Autowired
  public void setGtfsDao(GtfsRelationalDao dao) {
    _dao = dao;
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
    for (Stop stop : _dao.getAllStops()) {
      Document document = getStopAsDocument(stop);
      writer.addDocument(document);
    }
    writer.optimize();
    writer.close();
    _refreshService.refresh(RefreshableResources.STOP_SEARCH_DATA);
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
