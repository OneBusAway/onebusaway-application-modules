/**
 * Copyright (C) 2026 OneBusAway
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
package org.onebusaway.api.actions.api.v2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.rest.DefaultHttpHeaders;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.onebusaway.api.model.ResponseBean;
import org.onebusaway.transit_data.model.config.BundleMetadata;
import org.onebusaway.transit_data.services.TransitDataService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opensymphony.xwork2.ActionContext;

@RunWith(MockitoJUnitRunner.class)
public class MetadataActionTest {

  private static final String HASH = "2135b4e143a1491856026f86ca3078102c83c6a4eaacf20acb76b3f88e71957c";

  private MetadataAction _action;

  private TransitDataService _service;

  @Mock
  HttpServletRequest request;

  @Mock
  HttpServletResponse servletResponse;

  private ObjectMapper _mapper = new ObjectMapper();

  @Before
  public void before() {
    ActionContext.setContext(new ActionContext(new HashMap<>()));

    ServletActionContext.setRequest(request);
    ServletActionContext.setResponse(servletResponse);

    _action = new MetadataAction();

    _service = Mockito.mock(TransitDataService.class);
    _action.setTransitDataService(_service);
  }

  private BundleMetadata meta(String hash, String lastUpdated) {
    BundleMetadata meta = new BundleMetadata();
    meta.setId("test-bundle");
    meta.setName("test-bundle");
    meta.setStaticGtfsHash(hash);
    meta.setStaticGtfsLastUpdated(lastUpdated);
    return meta;
  }

  private Map<String, Object> bodyOf(ResponseBean response) throws Exception {
    assertTrue(response.isString());
    return _mapper.readValue((String) response.getData(), Map.class);
  }

  @Test
  public void testHashAndFeedsReturned() throws Exception {
    Mockito.when(_service.bundleIsReady()).thenReturn(true);
    Mockito.when(_service.getBundleMetadataIfReady()).thenReturn(
        meta(HASH, "2026-11-06T00:11:00Z"));
    Map<String, Long> updates = new HashMap<String, Long>();
    updates.put("trip_updates", 1793904060000L);
    Mockito.when(_service.getRealtimeFeedLastUpdateTimes()).thenReturn(updates);

    DefaultHttpHeaders headers = _action.index();
    assertEquals(200, headers.getStatus());

    ResponseBean response = _action.getModel();
    assertEquals(200, response.getCode());
    assertEquals(2, response.getVersion());
    Map<String, Object> body = bodyOf(response);
    assertEquals(HASH, body.get("staticGtfsHash"));
    assertEquals("2026-11-06T00:11:00Z", body.get("staticGtfsLastUpdated"));
    assertTrue(body.get("realtimeFeeds") instanceof Map);
    assertTrue(((Map<?, ?>) body.get("realtimeFeeds")).containsKey("trip_updates"));
    // The value is the update instant formatted in the system default
    // timezone (yyyy-MM-dd'T'HH:mm:ssXXX), not just the key's presence.
    // Note: SimpleDateFormat takes uppercase XXX here; lowercase xxx is a
    // java.time pattern letter and throws IllegalArgumentException.
    String expectedFeedTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
        .format(new Date(1793904060000L));
    assertEquals(expectedFeedTime,
        ((Map<?, ?>) body.get("realtimeFeeds")).get("trip_updates"));
  }

  @Test
  public void testOldBundleOmitsHash() throws Exception {
    Mockito.when(_service.bundleIsReady()).thenReturn(true);
    Mockito.when(_service.getBundleMetadataIfReady()).thenReturn(
        meta(null, "2026-11-06T00:11:00Z"));
    Mockito.when(_service.getRealtimeFeedLastUpdateTimes()).thenReturn(
        new HashMap<String, Long>());

    DefaultHttpHeaders headers = _action.index();
    assertEquals(200, headers.getStatus());

    Map<String, Object> body = bodyOf(_action.getModel());
    assertFalse(body.containsKey("staticGtfsHash"));
    assertEquals("2026-11-06T00:11:00Z", body.get("staticGtfsLastUpdated"));
    assertTrue(body.get("realtimeFeeds") instanceof Map);
  }

  @Test
  public void testNoBundleReturns503() throws Exception {
    Mockito.when(_service.bundleIsReady()).thenReturn(false);

    DefaultHttpHeaders headers = _action.index();
    assertEquals(503, headers.getStatus());

    ResponseBean response = _action.getModel();
    assertEquals(503, response.getCode());
    String body = (String) response.getData();
    assertTrue(body.contains("\"status\":\"unavailable\""));
    assertFalse(body.contains("staticGtfsHash"));
  }

  @Test
  public void testBundleGoesAwayMidRequestReturns503() throws Exception {
    // Readiness was true, but the bundle went away before the metadata read
    // (swap started / load failed): the non-blocking read returns null and
    // the re-check sees not-ready, so this is a 503, not a 200.
    Mockito.when(_service.bundleIsReady()).thenReturn(true, false);
    Mockito.when(_service.getBundleMetadataIfReady()).thenReturn(null);

    DefaultHttpHeaders headers = _action.index();
    assertEquals(503, headers.getStatus());
  }

  @Test
  public void testRealtimeFeedsEmptyObjectNotNull() throws Exception {
    Mockito.when(_service.bundleIsReady()).thenReturn(true);
    Mockito.when(_service.getBundleMetadataIfReady()).thenReturn(
        meta(HASH, null));
    Mockito.when(_service.getRealtimeFeedLastUpdateTimes()).thenReturn(
        new HashMap<String, Long>());

    _action.index();
    Map<String, Object> body = bodyOf(_action.getModel());
    assertTrue(body.get("realtimeFeeds") instanceof Map);
    assertTrue(((Map<?, ?>) body.get("realtimeFeeds")).isEmpty());
    // and the raw JSON is {} not null:
    assertTrue(((String) _action.getModel().getData()).contains("\"realtimeFeeds\":{}"));
  }

  @Test
  public void testNullMetadataReturns200WithoutStaticFields() throws Exception {
    // Old bundle: loaded, but built before metadata.json existed, so
    // BundleConfigDao returns null. Still 200, static fields omitted.
    Mockito.when(_service.bundleIsReady()).thenReturn(true);
    Mockito.when(_service.getBundleMetadataIfReady()).thenReturn(null);
    Mockito.when(_service.getRealtimeFeedLastUpdateTimes()).thenReturn(
        new HashMap<String, Long>());

    DefaultHttpHeaders headers = _action.index();
    assertEquals(200, headers.getStatus());

    Map<String, Object> body = bodyOf(_action.getModel());
    assertFalse(body.containsKey("staticGtfsHash"));
    assertFalse(body.containsKey("staticGtfsLastUpdated"));
    assertTrue(body.get("realtimeFeeds") instanceof Map);
  }

  @Test
  public void testLastUpdatedOmittedWhenUnknown() throws Exception {
    Mockito.when(_service.bundleIsReady()).thenReturn(true);
    Mockito.when(_service.getBundleMetadataIfReady()).thenReturn(meta(HASH, null));
    Mockito.when(_service.getRealtimeFeedLastUpdateTimes()).thenReturn(
        new HashMap<String, Long>());

    _action.index();
    Map<String, Object> body = bodyOf(_action.getModel());
    assertFalse(body.containsKey("staticGtfsLastUpdated"));
    assertEquals(HASH, body.get("staticGtfsHash"));
  }
}
