/**
 * Copyright (C) 2026 Open Transit Software Foundation
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
package org.onebusaway.alerts.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onebusaway.gtfs.model.AgencyAndId;

/**
 * A realtime alerts refresh writes changed alerts to the database. Readers
 * (arrivals, trip details, vehicles) must not wait on that database work.
 */
public class ServiceAlertsServiceImplConcurrencyTest {

  private ServiceAlertsServiceImpl _service;
  private BlockingPersistence _persister;
  private ExecutorService _executor;

  @Before
  public void setup() {
    _persister = new BlockingPersistence();
    _service = new ServiceAlertsServiceImpl();
    _service.setServiceAlertsCache(new ServiceAlertsCacheInMemoryImpl());
    _service.setServiceAlertsPersistence(_persister);
    _executor = Executors.newFixedThreadPool(2);

    _service.createOrUpdateServiceAlerts("1", Collections.singletonList(stopAlert("A", "1_100")));
  }

  @After
  public void teardown() {
    _persister.release.countDown();
    _executor.shutdownNow();
    _service.stop();
  }

  @Test
  public void readersDoNotWaitForBulkUpdateDatabaseWork() throws Exception {
    _persister.blockWrites();
    Future<?> writer = _executor.submit(() ->
        _service.createOrUpdateServiceAlerts("1", Collections.singletonList(stopAlert("B", "1_100"))));
    assertTrue("writer never reached the database", _persister.writing.await(5, TimeUnit.SECONDS));

    List<ServiceAlertRecord> alerts = readWithin(() ->
        _service.getServiceAlertsForStopId(-1, new AgencyAndId("1", "100")));
    assertTrue(alerts.size() >= 1);

    _persister.release.countDown();
    writer.get(5, TimeUnit.SECONDS);
    assertEquals(2, _service.getServiceAlertsForStopId(-1, new AgencyAndId("1", "100")).size());
  }

  @Test
  public void readersDoNotWaitForRemovalDatabaseWork() throws Exception {
    _persister.blockLookups();
    Future<?> writer = _executor.submit(() ->
        _service.removeServiceAlerts(Collections.singletonList(new AgencyAndId("1", "A"))));
    assertTrue("writer never reached the database", _persister.writing.await(5, TimeUnit.SECONDS));

    readWithin(() -> _service.getServiceAlertsForStopId(-1, new AgencyAndId("1", "100")));

    _persister.release.countDown();
    writer.get(5, TimeUnit.SECONDS);
    assertEquals(0, _service.getServiceAlertsForStopId(-1, new AgencyAndId("1", "100")).size());
  }

  @Test
  public void readersDoNotReloadAStaleCacheThemselves() throws Exception {
    _persister.rows.add(stopAlert("C", "1_100")); // written by another instance
    _persister.stale = true;
    _persister.blockReads();

    readWithin(() -> _service.getServiceAlertsForStopId(-1, new AgencyAndId("1", "100")));
    assertTrue("reload never reached the database", _persister.writing.await(5, TimeUnit.SECONDS));

    _persister.release.countDown();
    long deadline = System.currentTimeMillis() + 5000;
    while (_service.getServiceAlertsForStopId(-1, new AgencyAndId("1", "100")).size() < 2
        && System.currentTimeMillis() < deadline) {
      Thread.sleep(10);
    }
    assertEquals(2, _service.getServiceAlertsForStopId(-1, new AgencyAndId("1", "100")).size());
  }

  @Test
  public void aFailedSaveLeavesTheCacheMatchingTheDatabase() throws Exception {
    _persister.failWrites = true;
    try {
      _service.createOrUpdateServiceAlerts("1", Collections.singletonList(stopAlert("B", "1_100")));
      fail("expected the save to fail");
    } catch (RuntimeException expected) {
    }

    assertNull(_service.getServiceAlertForId(new AgencyAndId("1", "B")));
    assertEquals(1, _service.getServiceAlertsForStopId(-1, new AgencyAndId("1", "100")).size());
  }

  private <T> T readWithin(java.util.concurrent.Callable<T> read) throws Exception {
    Future<T> reader = _executor.submit(read);
    try {
      return reader.get(1, TimeUnit.SECONDS);
    } catch (TimeoutException e) {
      throw new AssertionError("reader blocked on database work");
    }
  }

  private static ServiceAlertRecord stopAlert(String alertId, String stopId) {
    ServiceAlertRecord record = new ServiceAlertRecord();
    record.setAgencyId("1");
    record.setServiceAlertId(alertId);
    ServiceAlertsSituationAffectsClause affects = new ServiceAlertsSituationAffectsClause();
    affects.setStopId(stopId);
    record.setAllAffects(new HashSet<>(Collections.singletonList(affects)));
    return record;
  }

  /**
   * In-memory persistence whose writes or lookups can be held open, standing
   * in for a slow database round trip. {@link #stale} makes the next
   * cachedNeedsSync report a change made by another instance, and
   * {@link #failWrites} makes bulk saves throw.
   */
  private static class BlockingPersistence implements ServiceAlertsPersistence {
    final CountDownLatch writing = new CountDownLatch(1);
    final CountDownLatch release = new CountDownLatch(1);
    private volatile boolean blockWrites = false;
    private volatile boolean blockLookups = false;
    private volatile boolean blockReads = false;
    volatile boolean stale = false;
    volatile boolean failWrites = false;
    final List<ServiceAlertRecord> rows = Collections.synchronizedList(new ArrayList<>());

    void blockWrites() { blockWrites = true; }

    void blockLookups() { blockLookups = true; }

    void blockReads() { blockReads = true; }

    private void maybeBlock(boolean block) {
      if (!block) return;
      writing.countDown();
      try {
        release.await(10, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }

    @Override
    public void delete(ServiceAlertRecord record) { rows.remove(record); }

    @Override
    public List<ServiceAlertRecord> getAlerts() {
      maybeBlock(blockReads);
      synchronized (rows) {
        return new ArrayList<>(rows);
      }
    }

    @Override
    public void saveOrUpdate(ServiceAlertRecord record) {
      maybeBlock(blockWrites);
      if (!rows.contains(record)) rows.add(record);
    }

    @Override
    public void saveOrUpdate(List<ServiceAlertRecord> records) {
      maybeBlock(blockWrites);
      if (failWrites) throw new RuntimeException("database unavailable");
      for (ServiceAlertRecord record : records) {
        if (!rows.contains(record)) rows.add(record);
      }
    }

    @Override
    public ServiceAlertRecord getServiceAlertRecordByAlertId(String agencyId, String serviceAlertId) {
      maybeBlock(blockLookups);
      synchronized (rows) {
        for (ServiceAlertRecord row : rows) {
          if (agencyId.equals(row.getAgencyId()) && serviceAlertId.equals(row.getServiceAlertId())) return row;
        }
      }
      return null;
    }

    @Override
    public boolean needsSync() { return false; }

    @Override
    public boolean cachedNeedsSync() {
      boolean wasStale = stale;
      stale = false;
      return wasStale;
    }

    @Override
    public boolean deleteOrphans() { return true; }
  }
}
