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
package org.onebusaway.transit_data_federation.impl.federated;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.onebusaway.transit_data.model.config.BundleMetadata;
import org.onebusaway.transit_data_federation.services.bundle.BundleManagementService;
import org.onebusaway.utility.DateLibrary;

@RunWith(MockitoJUnitRunner.class)
public class TransitDataServiceImplTest {

  private TransitDataServiceImpl _service;

  private BundleManagementService _bundleManagementService;

  @Before
  public void before() {
    _service = new TransitDataServiceImpl();
    _bundleManagementService = Mockito.mock(BundleManagementService.class);
    _service.set_bundleManagementService(_bundleManagementService);
  }

  @Test
  public void testOldBundleSynthesizesLoadTime() {
    // Old bundle: no metadata.json, but the load time is known — the
    // endpoint still gets the timestamp, with the hash omitted.
    Date loadTime = new Date(1793904060000L);
    Mockito.when(_bundleManagementService.bundleIsReady()).thenReturn(true);
    Mockito.when(_bundleManagementService.getBundleMetadata()).thenReturn(null);
    Mockito.when(_bundleManagementService.getBundleLoadTime()).thenReturn(loadTime);

    BundleMetadata meta = _service.getBundleMetadataIfReady();
    assertNotNull(meta);
    assertNull(meta.getStaticGtfsHash());
    assertEquals(
        DateLibrary.getTimeAsIso8601String(loadTime, TimeZone.getTimeZone("UTC")),
        meta.getStaticGtfsLastUpdated());
  }

  @Test
  public void testNoLoadTimeReturnsNull() {
    Mockito.when(_bundleManagementService.bundleIsReady()).thenReturn(true);
    Mockito.when(_bundleManagementService.getBundleMetadata()).thenReturn(null);
    Mockito.when(_bundleManagementService.getBundleLoadTime()).thenReturn(null);

    assertNull(_service.getBundleMetadataIfReady());
  }

  @Test
  public void testNotReadyReturnsNull() {
    Mockito.when(_bundleManagementService.bundleIsReady()).thenReturn(false);

    assertNull(_service.getBundleMetadataIfReady());
  }

  @Test
  public void testUnreadableDelegates() {
    Mockito.when(_bundleManagementService.isBundleMetadataUnreadable()).thenReturn(true);
    assertTrue(_service.isBundleMetadataUnreadable());
    Mockito.when(_bundleManagementService.isBundleMetadataUnreadable()).thenReturn(false);
    assertFalse(_service.isBundleMetadataUnreadable());
  }
}
