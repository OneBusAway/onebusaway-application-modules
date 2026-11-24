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

import java.io.IOException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.onebusaway.api.actions.api.ApiActionSupport;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.transit_data.model.config.BundleMetadata;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.utility.DateLibrary;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * GET /api/v2/metadata.json — static GTFS freshness for Watchdog, matching
 * Maglev's endpoint. Single-feed only (issue #487): {@code staticGtfsHash}
 * is the SHA-256 of the single GTFS zip the bundle was built from.
 * Plain JSON, no code/data envelope. API-key auth is automatic via
 * ApiKeyInterceptor — do NOT add @ApiKeyAuthorization(enabled=false).
 */
public class MetadataAction extends ApiActionSupport {

  private static final int V2 = 2;

  private static final long serialVersionUID = 1L;

  public MetadataAction() {
    super(V2);
  }

  @Autowired
  private TransitDataService _service;

  public void setTransitDataService(TransitDataService service) {
    _service = service;
  }

  /**
   * GET /api/v2/metadata.json. Non-blocking throughout: a bundle swap (or a
   * failed load) mid-request answers 503 instead of waiting. A loaded bundle
   * without metadata (old bundle) is 200 with the load timestamp and the hash
   * omitted. A bundle whose metadata file exists but cannot be read is 503.
   */
  public DefaultHttpHeaders index() throws IOException, ServiceException {
    if (hasErrors())
      return setValidationErrorsResponse();

    Boolean ready = _service == null ? null : _service.bundleIsReady();
    if (ready == null || !ready) {
      return setServiceUnavailableResponseText(
          "{\"status\":\"unavailable\",\"detail\":\"Bundle not loaded\"}");
    }

    // One non-blocking read: getBundleMetadata() would block here if a swap
    // started after the readiness check above. A null return is ambiguous —
    // either the bundle went away mid-request, or the loaded bundle has
    // neither metadata nor a known load time — so re-check readiness to tell
    // them apart. (An old bundle with a known load time is synthesized with
    // a timestamp instead of null; see getBundleMetadataIfReady.)
    BundleMetadata meta = _service.getBundleMetadataIfReady();
    if (meta == null) {
      // Unreadable metadata (the file exists but is corrupt) is a 503: the
      // endpoint cannot report the freshness it exists to report. A missing
      // file (old bundle) is not unreadable.
      if (_service.isBundleMetadataUnreadable()) {
        return setServiceUnavailableResponseText(
            "{\"status\":\"unavailable\",\"detail\":\"Bundle metadata is unreadable\"}");
      }
      Boolean stillReady = _service.bundleIsReady();
      if (stillReady == null || !stillReady) {
        return setServiceUnavailableResponseText(
            "{\"status\":\"unavailable\",\"detail\":\"Bundle not loaded\"}");
      }
    }
    // meta is null only when the bundle has neither metadata nor a known load
    // time. The bundle is still loaded, so answer 200 with the static fields
    // omitted, same as Maglev's no-import case, not a 503. Only "no bundle
    // at all" (checked above via bundleIsReady) and unreadable metadata are
    // 503s.
    Map<String, Object> body = new LinkedHashMap<String, Object>();
    if (meta != null) {
      if (meta.getStaticGtfsLastUpdated() != null)
        body.put("staticGtfsLastUpdated", meta.getStaticGtfsLastUpdated());
      if (meta.getStaticGtfsHash() != null)
        body.put("staticGtfsHash", meta.getStaticGtfsHash());
    }

    Map<String, String> feeds = new LinkedHashMap<String, String>();
    Map<String, Long> updates = _service.getRealtimeFeedLastUpdateTimes();
    if (updates != null) {
      for (Map.Entry<String, Long> entry : updates.entrySet()) {
        feeds.put(entry.getKey(),
            DateLibrary.getTimeAsIso8601String(new Date(entry.getValue())));
      }
    }
    body.put("realtimeFeeds", feeds);

    String json = new ObjectMapper().writeValueAsString(body);
    return setOkResponseText(json);
  }
}
