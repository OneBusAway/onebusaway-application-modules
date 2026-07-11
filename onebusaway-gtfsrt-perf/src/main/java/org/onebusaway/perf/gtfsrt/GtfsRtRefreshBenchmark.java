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
package org.onebusaway.perf.gtfsrt;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Drives GtfsRealtimeSource.refresh() in a timed loop. System properties:
 *   perf.bundleRoot (required), perf.rtDir (required, holds tripupdates.pb/vehiclepositions.pb/alerts.pb),
 *   perf.warmup (default 20), perf.measure (default 100).
 */
public class GtfsRtRefreshBenchmark {
  public static void main(String[] args) throws Exception {
    String bundleRoot = required("perf.bundleRoot");
    String rtDir = required("perf.rtDir");
    int warmup = Integer.getInteger("perf.warmup", 20);
    int measure = Integer.getInteger("perf.measure", 100);

    String tu = new File(rtDir, "tripupdates.pb").toURI().toString();
    String vp = new File(rtDir, "vehiclepositions.pb").toURI().toString();
    String al = new File(rtDir, "alerts.pb").toURI().toString();

    // Pin the feed to these agency ids, as production's GTFS_RT_FEEDS does
    // (default: empty = all agencies). Comma-separated via -Dperf.agencyIds=1.
    List<String> agencyIds = parseCsv(System.getProperty("perf.agencyIds"));

    PerfBundleHarness harness = new PerfBundleHarness(bundleRoot, tu, vp, al, agencyIds);
    harness.open();
    try {
      System.out.println("== warmup " + warmup + " ==");
      for (int i = 0; i < warmup; i++) {
        harness.refreshOnce();
        if (i == 0) System.out.println("first refresh records=" + harness.lastRecordCount());
      }

      System.out.println("== measure " + measure + " ==");
      long[] ns = new long[measure];
      Profiling.start();       // no-op until Task 5 wires the profiler
      long allocBefore = threadAllocatedBytes();
      for (int i = 0; i < measure; i++) {
        long t0 = System.nanoTime();
        harness.refreshOnce();
        ns[i] = System.nanoTime() - t0;
        if (i % 10 == 0)
          System.out.println("iter " + i + " ms=" + (ns[i] / 1_000_000.0) + " records=" + harness.lastRecordCount());
      }
      long allocAfter = threadAllocatedBytes();
      Profiling.stop();
      report(ns);
      reportAllocation(allocBefore, allocAfter, measure);
    } finally {
      harness.close();
    }
  }

  private static void report(long[] ns) {
    long[] sorted = ns.clone();
    Arrays.sort(sorted);
    double mean = Arrays.stream(ns).average().orElse(0) / 1_000_000.0;
    double p50 = sorted[sorted.length / 2] / 1_000_000.0;
    double p95 = sorted[(int) (sorted.length * 0.95)] / 1_000_000.0;
    double total = Arrays.stream(ns).sum() / 1_000_000_000.0;
    System.out.printf("BASELINE n=%d mean=%.2fms p50=%.2fms p95=%.2fms throughput=%.2f refreshes/sec%n",
        ns.length, mean, p50, p95, ns.length / total);
  }

  /**
   * Bytes allocated by the current (measure-loop) thread, via the
   * HotSpot-specific {@code com.sun.management.ThreadMXBean}. Since the loop
   * drives {@code refresh()} synchronously on this thread, this captures the
   * matching + block-location allocation precisely, with no profiler attached.
   * Returns -1 if the extension isn't available (e.g. a non-HotSpot JVM).
   */
  private static long threadAllocatedBytes() {
    try {
      java.lang.management.ThreadMXBean bean = java.lang.management.ManagementFactory.getThreadMXBean();
      if (bean instanceof com.sun.management.ThreadMXBean) {
        com.sun.management.ThreadMXBean sun = (com.sun.management.ThreadMXBean) bean;
        if (sun.isThreadAllocatedMemorySupported())
          return sun.getThreadAllocatedBytes(Thread.currentThread().getId());
      }
    } catch (Throwable ignore) {
      // fall through
    }
    return -1L;
  }

  private static void reportAllocation(long before, long after, int measure) {
    if (before < 0 || after < 0) {
      System.out.println("ALLOC unavailable (needs a HotSpot com.sun.management.ThreadMXBean)");
      return;
    }
    long total = after - before;
    double totalMb = total / (1024.0 * 1024.0);
    double perRefreshMb = totalMb / measure;
    System.out.printf("ALLOC total=%.1fMB per_refresh=%.1fMB over n=%d (measure-thread only)%n",
        totalMb, perRefreshMb, measure);
  }

  private static String required(String key) {
    String v = System.getProperty(key);
    if (v == null) throw new IllegalStateException("missing -D" + key);
    return v;
  }

  /** Splits a comma-separated value into trimmed, non-empty tokens (empty list if null). */
  private static List<String> parseCsv(String value) {
    List<String> result = new ArrayList<>();
    if (value != null) {
      for (String token : value.split(",")) {
        String trimmed = token.trim();
        if (!trimmed.isEmpty()) result.add(trimmed);
      }
    }
    return result;
  }
}
