# GTFS-RT Matching CPU Investigation — Findings & Ranked Fix List

**Date:** 2026-07-10
**Branch:** `perf/gtfsrt-matching-investigation`
**Status:** Evidence complete. This report is the deliverable; no matching-engine
code was changed this session.

## TL;DR

A production OBA box (2 GB RAM / 1 vCPU) with a single GTFS-realtime feed wired
(~840 trips, ~30 s poll) shows flat memory but CPU pegged near 85% of its one
core. A prior assessment called this "mostly legitimate work." We built a
repeatable harness that reproduces the poll path at King County Metro (KCM)
scale, captured a CPU flamegraph, an allocation profile, a wall-clock baseline,
and the bundle's stop-count distribution.

**Verdict:** the "mostly legitimate work" claim does **not** hold for the largest
cost we can see. The single dominant hotspot —
`BlockConfigurationEntryImpl$BlockStopTimeList.get()` — accounts for **19.78% of
CPU self-time** and **69.02% of all allocated bytes** (48.8 GB over a 100-refresh
run; `BlockStopTimeEntryImpl` is constructed *only* inside `.get()`, so its entire
allocation share is attributable to this call — of which the 59.20% / 41.8 GB
detailed in §4 is the block-location write-path slice), and it is pure, avoidable
waste: a materialize-on-access `List` view that
constructs a brand-new `BlockStopTimeEntryImpl` on **every** `.get()` call
instead of returning a cached reference. This is algorithmic/allocation waste,
not inherent load, and it is fixable behind an existing regression test.

**However** (stated up front, honestly): on *this* machine one feed's poll path
is ~203 ms of wall time, which at a real 30 s poll is only **~0.7% of one core**.
That does **not** by itself explain a production 85% peg. The waste is real and
worth fixing regardless, but reconciling it with the exact production figure
still needs two facts we did not have: the production **poll cadence** and the
**API read-path traffic** hitting the same box. See §2 and §6.

> **Follow-up (see the Addendum at the end):** a second round rebuilt the
> reproduction to mirror the real production container — the multi-agency
> `rule-set-19` build-56 bundle, a live-feed ERROR flood, the production
> `[%F:%L]` log config, and a `--cpus=1 --memory=2g` box. Two results: (1) the
> tempting **"expensive `[%F:%L]` logging × flood"** hypothesis is **refuted** —
> immaterial at the real flood size; (2) even fully faithful and constrained, one
> 30 s poll is still only **~0.7% of a core**, confirming §2 empirically. The
> per-refresh allocation waste is reconfirmed; the 85% peg still points to a
> **continuous** driver (read traffic / cadence), not the single poll.

---

## 1. Setup

### Bundle (King County Metro, static GTFS)

| Stat | Value |
|---|---|
| Trips | 33,036 |
| Stops | 6,355 |
| Min stops/trip | 2 |
| Median stops/trip | 31 |
| Avg stops/trip | 32 |
| Max stops/trip | 97 |
| Trips with >50 stops | 5,011 (15.2%) |
| Trips with >100 stops | 0 |

Distribution is roughly bell-shaped, peaking at the 20–29-stop bucket (9,108
trips, 27.6%), with a tail to 90–99 (221 trips). Source: `StopCountHistogram`
run against the built KCM bundle (Task 7).

The KCM bundle (33,036 trips) is materially larger than the ~840-trip production
feed described in the ticket. This makes it a **stress** reproduction of the same
code paths, not a size-matched replica of the production box. It is well suited
to exposing per-trip constant factors and complexity class; it is **not** a
direct estimator of the production box's absolute CPU percentage.

### Feed (pinned GTFS-RT snapshot)

- Three KCM `.pb` feeds (trip updates, vehicle positions, alerts), downloaded
  once and pinned so matched counts don't drift run-to-run.
- **Matched records per refresh: 551.** This is the `CountingListener` count of
  `handleVehicleLocationRecord(s)` calls per `refresh()` — used throughout as the
  proxy for "matched vehicle updates per refresh."
  - **Caveat (transparency):** the `GtfsRealtimeSource` INFO log line at
    `GtfsRealtimeSource.java:785-791` (`"Agency … matched=… (unmatched)…"`) was
    **not** captured in any run log this investigation produced (grep for
    `matched=` / `Agency` across all scratchpad logs returned zero hits — most
    likely the harness logging config runs that class below INFO). So `records=551`
    is a *proxy* for the matched-trip-id count, not that exact log value. If the
    precise matched/unmatched split ever matters, it still needs to be captured.

### JVM / hardware / method

- **JVM:** OpenJDK 11, `-Xmx1500m` (deliberately approximating the 2 GB / 1 vCPU
  production box, leaving headroom for non-heap), G1GC.
- Profiling flags: `-XX:+UnlockDiagnosticVMOptions -XX:+DebugNonSafepoints` for
  accurate (non-safepoint-biased) stacks.
- **Hardware:** developer Mac (Apple silicon, macOS). A modern desktop-class
  core — **faster than a cloud 1-vCPU share.** This matters for §2.
- **Method:**
  1. Build the KCM bundle **once**, off to the side (slow, not measured).
  2. Stand up a real `GtfsRealtimeSource` with the full production collaborator
     set (real `VehicleStatusServiceImpl` as the `VehicleLocationListener` and
     occupancy listener, cancel service, alert service, feed `file://` URLs),
     via `PerfBundleHarness`.
  3. `setRefreshInterval(0)` so no background scheduler runs — the loop drives
     `refresh()` synchronously.
  4. **`source.reset()` before every measured `refresh()`** so the
     `_lastVehicleUpdate` dedup is cleared and **both** cost stages (matching +
     block-location write) run every iteration, faithful to production where the
     feed timestamp advances each poll. Confirmed: `records=551` on every logged
     iteration (0, 10, 20, … 90), not just iteration 0.
  5. Warm up 20 refreshes, measure 100. Profile the **measure phase only**
     (profiler started after warmup, stopped before teardown).
  6. async-profiler 3.0 (`event=cpu` and `event=alloc`) as primary; JDK JFR as an
     independently-verified fallback.

### Honesty / transparency gaps (stated plainly)

- **Matched/unmatched log line not captured** — `records=551` used as proxy
  (above).
- **No GC log was captured.** GC cost is *inferred* from G1 frames appearing
  directly in the CPU flat profile (§3), not measured from an `-Xlog:gc` capture.
- Whole-`refresh()` wall time is **not** pure matching — see §2.
- The `.get()`-call-count figure in §5 is an **order-of-magnitude estimate**
  (allocation bytes ÷ estimated object size), not an instrumented count.

Every inference below is labeled as an inference.

---

## 2. Baseline

Canonical run (`benchmark-run3.log`), and two corroborating profiled runs:

| Run | mean | p50 | p95 | throughput | records |
|---|---|---|---|---|---|
| Baseline (canonical) | 203.18 ms | 200.44 ms | 223.40 ms | 4.92 refresh/s | 551 |
| CPU-profiled | 199.07 ms | 197.47 ms | 214.56 ms | 5.02 refresh/s | 551 |
| Alloc-profiled | 193.43 ms | 189.78 ms | 206.86 ms | 5.17 refresh/s | 551 |
| JFR fallback | 221.04 ms | 211.65 ms | 242.79 ms | 4.52 refresh/s | 551 |

Take **~203 ms/refresh** as the headline. async-profiler overhead was negligible;
JFR's was ~10–20 ms/refresh higher, as expected.

**Important caveat on what 203 ms includes.** This is whole-`refresh()` wall time
— it covers matching **plus** the block-location write, the alert diff, and the
position cache write. It is **not** pure matching-engine time. In fact the CPU
attribution (§3) shows the majority of the hot self-time lands on the
block-location *write* path (`getBlockLocation`), not the trip-update matching
path. So "matching" in the loose sense the ticket used is really "the whole poll
path," and that's what 203 ms measures.

### Reconciliation with the production 85%-of-a-core symptom

At a real **30 s poll**, one `refresh()` of 203 ms is:

```
203 ms / 30,000 ms ≈ 0.68% of one core (on this Mac, over a 30 s window)
```

So on this machine, **one feed's poll path alone does not obviously peg a core** —
not even close. We should not overclaim that this reproduction "explains" the
production 85%. Candidate explanations for the gap, none yet confirmed:

1. **Slower cloud core.** A shared 1-vCPU cloud slice is materially slower than
   this desktop-class core. Even a 5–10× slowdown, though, only takes 0.68% to
   ~3–7% — still not 85%. (Inference.)
2. **Higher poll frequency.** If the production feed is actually polled far more
   often than 30 s (or multiple feeds/agencies are wired), the per-poll cost
   multiplies. The design doc's "~30 s" is an assumption worth verifying.
3. **API read-path / query load.** The same box serves arrivals-and-departures
   queries. `AbstractBlockLocationServiceImpl.getBlockLocation` — the #2 CPU
   hotspot here — is *also* on the read path, and it hammers the same
   `BlockStopTimeList.get()` hotspot. A busy read path could dominate CPU
   independently of polling. This is the **most likely** missing contributor and
   is only measured secondarily here. (Inference.)
4. **Other co-located app work** (bundle refresh, alert processing, logging).

**Recommendation:** before attributing the 85% to matching, confirm (a) the
production poll cadence and feed/agency count, and (b) the API read-traffic on
the box. Both are cheap to gather and would close the reconciliation. The fixes
in §7 are worth doing regardless, because the *waste itself* is real and measured
— but the exact 85% attribution is not yet proven and this report does not claim
it.

---

## 3. CPU attribution

Top ~15 self-time frames, async-profiler `flat`, 2,108 measure-phase samples
(Task 5):

| # | Self % | Samples | Frame |
|---|---|---|---|
| 1 | **19.78%** | 417 | `BlockConfigurationEntryImpl$BlockStopTimeList.get` |
| 2 | 6.12% | 129 | `AbstractBlockLocationServiceImpl.getBlockLocation` |
| 3 | 6.02% | 127 | `java.lang.String.equals` |
| 4 | 4.08% | 86 | `G1ParScanThreadState::copy_to_survivor_space` (GC) |
| 5 | 2.94% | 62 | `java.lang.String.coder` |
| 6 | 2.75% | 58 | `BlockConfigurationEntryImpl$BlockStopTimeList.size` |
| 7 | 2.37% | 50 | `G1ScanEvacuatedObjClosure::do_oop_work` (GC) |
| 8 | 2.04% | 43 | `java.util.ArrayList.elementData` |
| 9 | 1.61% | 34 | `java.util.AbstractList$Itr.hasNext` |
| 10 | 1.57% | 33 | `PropertyPathExpression.<init>` |
| 11 | 1.47% | 31 | `java.util.Objects.checkIndex` |
| 12 | 1.33% | 28 | `java.util.HashMap.putVal` |
| 13 | 1.19% | 25 | `java.util.AbstractList$Itr.checkForComodification` |
| 14 | 1.19% | 25 | `G1ScanObjsDuringUpdateRSClosure::do_oop_work` (GC) |
| 15 | 1.19% | 25 | `java.util.ArrayList.get` |

Also relevant to the investigation's named targets:

- `PropertyPathExpression.invokeReturningFullResult` **1.00%** — reflection cost
  inside `MappingLibrary.mapToValue`'s index rebuild.
- `MappingLibrary.mapToValue` self-time only **0.33%** — its cost lives in callees
  (`PropertyPathExpression` / `HashMap`), not the method body.
- `GtfsRealtimeTripLibrary.applyTripUpdatesToRecord` self **0.24%**,
  `getBlockStopTimeForStopTimeUpdate` self **0.09%** — negligible *self*-time;
  both are thin dispatchers whose cost lives in what they call (chiefly
  `mapToValue` and `BlockStopTimeList.get()`).
- **Feed parsing is not a hotspot** — protobuf frames (`CodedInputStream`,
  `GeneratedMessageV3`, …) sum to well under 1% self-time combined.
- **`ScheduledBlockLocationServiceImpl` does not appear at all** in the CPU flat
  profile — it is not a measurable hotspot in this run.

### GC (inferred, not measured)

The three G1 frames above (#4, #7, #14) sum to **~7.64% self-time**. No `-Xlog:gc`
capture was taken, so this is the only GC evidence we have; it is consistent with
the ~706 MB/refresh allocation rate (§4) driving frequent young-gen collections.
**Inference**, flagged as such.

### Call-stack attribution (from `traces=15`)

Essentially all of the 15 hottest stacks (~34% of measure-phase CPU) funnel
through **one path** — the vehicle-position / block-location *write* path, not
the trip-update matching path:

```
GtfsRealtimeSource.refresh
  → handleUpdates → handleCombinedUpdates
    → VehicleStatusServiceImpl.handleVehicleLocationRecord
      → BlockLocationServiceImpl.handleVehicleLocationRecord
        → BlockLocationServiceImpl.putBlockLocationRecord
          → AbstractBlockLocationServiceImpl.getBlockLocation      <- 6.12% self, dispatch hub
            → BlockConfigurationEntryImpl$BlockStopTimeList.get     <- 19.78% self, THE hotspot
            → BlockStopTimeList.size                                <- 2.75% self
            → AgencyAndId.equals / String.equals / String.coder     <- ~9% self, ID matching
            → AbstractList$Itr.next / hasNext / checkForComodification
```

**Key finding:** `BlockStopTimeList.get(index)` is *not* a cheap array lookup. It
is called from inside an `AbstractList$Itr` traversal (once per element while
iterating a trip's stop-time list), and each call allocates a fresh
`BlockStopTimeEntryImpl` (see §4). It is reached from **both** the vehicle-position
write path (dominant) and the trip-update matching path.

---

## 4. Allocation attribution

Top allocation sites, async-profiler `alloc` flat, 134,977 measure-phase samples
(Task 5):

| # | % bytes | Bytes | Type |
|---|---|---|---|
| 1 | **69.02%** | 48.8 GB | `BlockStopTimeEntryImpl` |
| 2 | 5.67% | 4.0 GB | `java.lang.Object[]` |
| 3 | 4.87% | 3.4 GB | `java.util.HashMap$Node` |
| 4 | 3.63% | 2.6 GB | `PropertyInvocationResult` |
| 5 | 2.88% | 2.0 GB | `byte[]` |
| 6 | 2.39% | 1.7 GB | `java.util.HashMap$Node[]` |
| 7 | 2.14% | 1.5 GB | `int[]` |
| 8 | 0.95% | 669 MB | `java.lang.String` |
| 9 | 0.62% | 437 MB | `java.util.regex.Pattern` |
| 10 | 0.53% | 375 MB | `com.google.protobuf.SmallSortedMap$1` |
| 11 | 0.52% | 369 MB | `java.util.regex.Matcher` |
| 12 | 0.38% | 271 MB | `TripUpdate$StopTimeEvent` |

Total allocated over the 100-refresh measure phase: **~70.6 GB** — i.e. **~706
MB/refresh**, of which **~489 MB/refresh is `BlockStopTimeEntryImpl` alone.**

### Where those allocations come from (`traces=10`)

- **59.20%** of *all* allocated bytes (41.8 GB): `BlockStopTimeEntryImpl`
  allocated inside `BlockStopTimeList.get()`, from
  `AbstractBlockLocationServiceImpl.getBlockLocation` (the vehicle-position write
  path). This **confirms** `.get()` manufactures a new object per call instead of
  returning a stored reference.
- **~14.7%** combined (three trace groups): same `BlockStopTimeEntryImpl` via
  `BlockStopTimeList.get()`, but reached from the **trip-update** path
  (`applyTripUpdatesToRecord → createVehicleLocationRecordForUpdate`). Same
  materialize-on-access list, walked from both ingestion paths.
- **4.97%** (3.5 GB): `HashMap$Node` from `MappingLibrary.mapToValue`'s internal
  `HashMap.put`, from `getBlockStopTimeForStopTimeUpdate` — this is exactly the
  **"index rebuilt on every stop-time update"** churn the investigation
  predicted. `mapToValue` builds and discards a fresh index `HashMap` on every
  stop-time update.
- **4.91%** (3.5 GB): `Object[]` boxing inside `PropertyMethodImpl.invoke` /
  `PropertyPathExpression.invokeReturningFullResult`, from the same `mapToValue`
  rebuild — reflection-based property access allocating a per-invocation args
  array.

So the allocation profile independently corroborates the CPU profile: **the
majority of both CPU and memory pressure traces to one root cause** — a
lazily-materializing list view (`BlockStopTimeList`) whose `.get()` rebuilds a
`BlockStopTimeEntryImpl` each call — plus a secondary, smaller churn source (the
per-stop-time-update `mapToValue` index rebuild).

### Why `.get()` reconstructs an object every call (grounded in source)

`BlockConfigurationEntryImpl.BlockStopTimeList` (an inner
`AbstractList<BlockStopTimeEntry>`) implements `get(int)` as:

```java
// BlockConfigurationEntryImpl.java:362-376
public BlockStopTimeEntry get(int index) {
  int tripIndex = tripIndices[index];
  BlockTripEntry blockTrip = trips.get(tripIndex);
  TripEntry trip = blockTrip.getTrip();
  List<StopTimeEntry> stopTimes = trip.getStopTimes();
  int stopTimeIndex = index - accumulatedStopTimeIndices[tripIndex];
  StopTimeEntry stopTime = stopTimes.get(stopTimeIndex);
  boolean hasNextStop = index + 1 < tripIndices.length;
  return new BlockStopTimeEntryImpl(stopTime, index, blockTrip, hasNextStop);  // <- new object every call
}
```

There is no cache. Every `.get(i)` — and every iterator step, since
`AbstractList`'s iterator calls `get(i)` — allocates a new `BlockStopTimeEntryImpl`
wrapping otherwise-immutable underlying data. Because the underlying arrays
(`tripIndices`, `accumulatedStopTimeIndices`, `trips`, per-trip `stopTimes`) are
fixed for the life of the bundle, these wrappers are effectively immutable and
could be built once and cached.

---

## 5. Quadratic-cost evidence

Two independent inner loops over a trip's stop times sit on the poll path, both in
`GtfsRealtimeTripLibrary.applyTripUpdatesToRecord`:

**(a) The `mapToValue` index rebuild** — `getBlockStopTimeForStopTimeUpdate`
(`GtfsRealtimeTripLibrary.java:1132`) calls
`MappingLibrary.mapToValue(stopTimes, "stopTime.gtfsSequence")`, building an
N-entry `HashMap` over **all** of the trip's stop times, and it is called **once
per stop-time update** from the loop at line 958. For a trip with N stop times and
N updates that is N map-builds × N entries = **O(N²)** map insertions per trip,
each insertion doing a reflective property read.

**(b) The `lastStopScheduleTime` inner rescan** — inside that same per-stop-time-
update loop (`GtfsRealtimeTripLibrary.java:964-970`), the code rescans **all** of
`blockTrip.getStopTimes()` to find the maximum arrival time, once per stop-time
update:

```java
// GtfsRealtimeTripLibrary.java:964-970 — runs once PER stopTimeUpdate
List<BlockStopTimeEntry> stopTimes = blockTrip.getStopTimes();
for (BlockStopTimeEntry bste : stopTimes) {
  long scheduleTime = instance.getServiceDate() + bste.getStopTime().getArrivalTime() * 1000;
  if (scheduleTime > lastStopScheduleTime) {
    lastStopScheduleTime = scheduleTime;
  }
}
```

`lastStopScheduleTime` here does **not** depend on the current `stopTimeUpdate` —
it is the trip's max schedule time, invariant across the inner loop — yet it is
recomputed on every iteration, and every iteration walks `blockTrip.getStopTimes()`
(a `BlockStopTimeList`, so every step allocates a `BlockStopTimeEntryImpl`). This
is a textbook O(stops²) rescan that could be hoisted to run once per block-trip.

### Correlating with the histogram

With median = 31 stops and max = 97:

- A typical trip's O(stops²) inner loop ≈ **31² ≈ 961 iterations**.
- The longest trips ≈ **97² ≈ 9,409 iterations**.
- 5,011 trips (15.2%) exceed 50 stops, so a meaningful slice of the bundle carries
  a per-refresh constant factor in the **thousands** of iterations, not tens.

### Order-of-magnitude cross-check (labeled: estimate, not measured)

`BlockStopTimeEntryImpl` holds two references + an `int` + a `boolean` → shallow
size ~32 bytes (compressed oops). At ~489 MB/refresh attributed to it, that's on
the order of **10–15 million `.get()`-driven allocations per refresh**. Against
551 matched records, that's ~18,000–27,000 `.get()` calls per matched vehicle —
one to two orders of magnitude above a single O(stops) pass (31) or even one full
O(stops²) self-scan (961) over a median trip. That gap is consistent with the same
trip's stop-time list being re-walked/re-materialized **multiple times per vehicle
per refresh** (across candidate deviations / repeated block entry), i.e. the cost
is driven by *both* a stop-count-dependent inner loop *and* redundant re-invocation
of it. **This 10–15M figure is an inference from bytes ÷ estimated object size, not
an instrumented count.**

**Does the O(stops²) hypothesis hold?** Yes, with evidence: two concrete O(stops)
inner loops nested inside a per-stop-time-update loop are present in source (a),(b);
the flamegraph puts ~20% self-time + majority of allocations in the per-element
`.get()` those loops hammer; and the histogram shows the stop counts that make the
quadratic term bite. The magnitude cross-check further suggests redundant
re-invocation on top of the quadratic term.

---

## 6. Verdict on "mostly legitimate work"

**Not accurate for the dominant cost.** The evidence:

- **~20% of CPU self-time** and **~69% of all allocated bytes** come from a single
  avoidable pattern — a materialize-on-access list view (`BlockStopTimeList`) that
  reconstructs a `BlockStopTimeEntryImpl` on every `.get()`, hammered by repeated
  stop-time iteration on both the block-location write path and the trip-update
  matching path.
- A second, smaller waste source (the per-stop-time-update `mapToValue` rebuild and
  its reflective property reads) adds several more percent of allocations.
- Neither is inherent to "matching 840 trips" — both are constant-factor /
  algorithmic overhead that a cache or a hoist removes without changing behavior.

So the majority of the *visible* CPU and the vast majority of allocation is
**waste, not load.**

**But be precise about scope.** Two honest qualifications:

1. The waste is proven; the production **85%** figure is **not** reproduced here.
   On this machine one 30 s poll is ~0.7% of a core (§2). The 85% almost certainly
   involves a slower cloud core and/or read-path query load and/or a faster poll
   cadence — factors this poll-path harness doesn't capture. Fixing the waste will
   reduce CPU and (especially) allocation/GC pressure, but we cannot yet promise
   "this fix takes you from 85% to X%" without the cadence + read-traffic data.
2. Because `getBlockLocation` (the #2 hotspot) is *also* the read-path entry point,
   the same fix very likely helps the API query path too — which, if reads are the
   real production driver, could matter more than the poll path. That's an argument
   *for* the fix, but it also means the production win should be **re-measured**,
   not extrapolated from this harness.

Bottom line: **the prior "mostly legitimate work" framing is wrong about the
biggest cost we can measure — it's mostly fixable waste — but confirming the
exact production impact still requires the poll-cadence and read-traffic facts
called out in §2.**

---

## 7. Ranked fix list

Ranked lowest cost/risk & highest reward first. Rewards tie to the measured
self-time / allocation percentages above; each is an **estimate** of the poll-path
CPU/allocation it removes and should be **re-measured** after implementation. The
harness makes that trivial: re-run `GtfsRtRefreshBenchmark` (same warmup/measure
loop) and diff the `BASELINE` line + re-capture the flamegraphs.

### Fix 1 — Cache/memoize `BlockStopTimeList.get()` (highest reward, do first)

> **STATUS: IMPLEMENTED & VALIDATED** (see Addendum §D) — −71 % allocation,
> −24 % median refresh, behavior preserved. Details below were the pre-work plan.

- **What:** Stop reconstructing a `BlockStopTimeEntryImpl` on every `.get(index)`.
  The wrapped data (`tripIndices`, `accumulatedStopTimeIndices`, `trips`, per-trip
  `stopTimes`) is fixed for the bundle's lifetime, so the wrappers are effectively
  immutable. Build them once — lazily populate a `BlockStopTimeEntryImpl[]` (length
  `tripIndices.length`) on first access and return the cached element thereafter,
  or precompute the array when the `BlockConfigurationEntryImpl` is built.
- **Where:** `BlockConfigurationEntryImpl.java:361-376`
  (`BlockStopTimeList.get`), same file's `BlockStopTimeList` inner class.
- **Expected reward:** the largest single item — **~19.78% CPU self-time** and
  **~59% of all allocations (≈489 MB/refresh)** trace directly to this call.
  Removing the per-call allocation should also shrink the ~7.6% G1 GC self-time
  (inferred). Realistically the single highest-leverage change in the codebase for
  this workload.
- **Implementation cost:** **Low–medium.** A few lines in one inner class. Two
  care points: (1) memory — caching one wrapper per block-stop-time across all
  block configurations trades CPU/alloc for retained heap; measure retained size
  on a large bundle (KCM is a good stress case) to confirm it fits the 2 GB box.
  (2) `Serializable` — the list is `Serializable`; a cached array is fine but keep
  it `transient` + lazily rebuilt if bundle serialization must stay stable.
- **Regression risk:** **Low**, if the cached wrapper is byte-for-byte equivalent
  to the freshly-built one. `BlockStopTimeEntryImpl` wraps immutable inputs, so
  identity-vs-equality is the only behavioral subtlety — audit callers that might
  rely on `==` (none should).
- **Covering test:** `ScheduledBlockLocationServiceImplTest` (14 tests) exercises
  the block-location path that dominates `.get()` usage. The Task 6 golden
  `GtfsRealtimeTripLibraryCharacterizationTest` covers the trip-update path's use
  of it. **Coverage note:** neither test directly asserts `BlockStopTimeList`
  element *identity/equality* semantics; add a small targeted unit test on
  `BlockConfigurationEntryImpl.getStopTimes()` asserting `get(i).equals(...)`
  stability and correct values before/after the change.

### Fix 2 — Hoist the `mapToValue` sequence index out of the per-update loop

- **What:** `getBlockStopTimeForStopTimeUpdate` rebuilds
  `MappingLibrary.mapToValue(stopTimes, "stopTime.gtfsSequence")` on **every**
  stop-time update. The index depends only on the block-trip's stop times, not on
  the update, so build it **once per block-trip** and pass it in (or memoize per
  `blockTrip`).
- **Where:** `GtfsRealtimeTripLibrary.java:1132` (the `mapToValue` call), fed from
  the loop at `GtfsRealtimeTripLibrary.java:958`. Hoist the map construction to
  just above line 958 (once per `blockTrip`).
- **Expected reward:** **~5% of allocations** (the 4.97% `HashMap$Node` + part of
  the 4.91% reflective `Object[]` boxing both trace to this rebuild), plus the
  associated `PropertyPathExpression` reflection CPU (~1–2.5% self-time across
  frames #10, and `invokeReturningFullResult` 1.00%). Turns O(N²) map inserts per
  trip into O(N).
- **Implementation cost:** **Low.** Move one statement out of a loop; thread the
  map (or a per-`blockTrip` cache) through `getBlockStopTimeForStopTimeUpdate`'s
  signature.
- **Regression risk:** **Low.** Pure loop-invariant code motion; output identical.
- **Covering test:** Task 6 golden `GtfsRealtimeTripLibraryCharacterizationTest`
  snapshots the produced `VehicleLocationRecord` / predictions for exactly this
  method — it will catch any behavior drift. This is the best-covered fix.

### Fix 3 — Lift the O(stops) `lastStopScheduleTime` inner rescan

- **What:** The `lastStopScheduleTime` max-arrival rescan
  (`GtfsRealtimeTripLibrary.java:964-970`) runs once per stop-time update but is
  invariant across updates. Compute it **once per block-trip** (before the
  `stopTimeUpdate` loop at line 958), or precompute it from the trip's known last
  stop time.
- **Where:** `GtfsRealtimeTripLibrary.java:964-970`, hoist above line 958.
- **Expected reward:** **Modest but non-trivial.** Removes one full O(stops)
  `BlockStopTimeList` walk per stop-time update — and because that walk goes
  through `BlockStopTimeList.get()`, each removed iteration is also a removed
  `BlockStopTimeEntryImpl` allocation. Its reward **overlaps heavily with Fix 1**:
  if Fix 1 lands, `.get()` becomes allocation-free and this rescan gets much
  cheaper on its own, so do Fix 1 first, then re-measure before deciding how much
  Fix 3 still buys. Independently, it also eliminates redundant CPU work.
- **Implementation cost:** **Low.** Loop-invariant hoist.
- **Regression risk:** **Low–medium.** Must confirm `lastStopScheduleTime` is
  genuinely update-independent (it is, by inspection: it only reads
  `bste.getStopTime().getArrivalTime()` and the service date) and that nothing in
  the loop body mutates the stop-time list. Verify against the golden.
- **Covering test:** Task 6 golden `GtfsRealtimeTripLibraryCharacterizationTest`
  (same method under characterization).

### Fix 4 — Match only changed vehicles (dedup **before** match) — evaluate, don't assume

- **What:** Today `handleCombinedUpdates` runs
  `createVehicleLocationRecordForUpdate` for **all** ~feed updates
  (`GtfsRealtimeSource.java:711`) **before** the `_lastVehicleUpdate` timestamp
  dedup (`GtfsRealtimeSource.java:741`). So the full O(feed) match cost is paid
  every poll even when a given vehicle hasn't advanced. If semantics allow,
  dedup/skip unchanged vehicles *before* the expensive match.
- **Where:** `GtfsRealtimeSource.java:711` (match call) vs `:741` (dedup gate).
- **Expected reward:** **Potentially large but conditional** — in the steady state
  where few vehicles changed between polls, this avoids most matching work
  entirely. But in this pinned-snapshot harness, `reset()` forces every vehicle to
  look "changed" every iteration, so the harness **cannot currently quantify** this
  fix; its real-world reward depends on the production per-poll change rate (which
  ties back to the §2 cadence question).
- **Implementation cost:** **Medium.** Requires moving/duplicating the dedup key
  check earlier and reasoning about partial-feed and clock-skew cases.
- **Regression risk:** **Medium–high — the riskiest item.** The current ordering
  may be intentional: matching can have side effects (occupancy records, alerts,
  matched-stop bookkeeping via `MonitoredResult`) that would be skipped if we
  short-circuit before matching. Skipping unchanged vehicles could drop those
  updates or stale out predictions. **Do not ship without** confirming exactly
  what downstream state each pre-dedup match mutates.
- **Covering test:** **Coverage is thin here.** The unit-level golden covers a
  single record's transform, not the `handleCombinedUpdates` dedup ordering across
  a feed. This needs a new test at the `GtfsRealtimeSource.handleCombinedUpdates`
  level (or an integration test in `onebusaway-gtfsrt-integration-tests`) asserting
  that skipping unchanged vehicles preserves occupancy/alert/prediction side
  effects. Treat Fix 4 as a separate, later investigation — not a quick win.

### Recommended order

1. **Fix 1** (cache `.get()`) — highest reward, low risk, well-isolated. Re-measure.
2. **Fix 2** (hoist `mapToValue`) — cheap, best-covered, removes the secondary
   allocation source. Re-measure.
3. **Fix 3** (hoist `lastStopScheduleTime`) — cheap; re-measure *after* Fix 1 to
   see residual benefit.
4. **Fix 4** (pre-match dedup) — defer; needs semantic analysis + new tests +
   the production change-rate data before it can be justified or sized.

After each fix, re-run `GtfsRtRefreshBenchmark` and re-capture the CPU + alloc
flamegraphs to validate the actual (not estimated) delta, and run
`GtfsRealtimeTripLibraryCharacterizationTest` +
`ScheduledBlockLocationServiceImplTest` to prove behavior preservation.

---

## Appendix — provenance

| Artifact | Source |
|---|---|
| Baseline timings | Task 4 (`GtfsRtRefreshBenchmark`), Task 5 profiled runs |
| CPU + alloc top frames, call-stack attribution | Task 5 (async-profiler 3.0, measure-phase only) |
| Stop-count histogram, O(stops²) math | Task 7 (`StopCountHistogram`, KCM bundle) |
| Flamegraphs / JFR / logs (not committed) | scratchpad `kcm/` and `task8-results/` |
| Hot code cited | `BlockConfigurationEntryImpl.java:351-377`, `GtfsRealtimeTripLibrary.java:958-970, 1125-1149` |

All numbers are from single real runs against the real KCM bundle and pinned feed
snapshot. Estimates and inferences are labeled inline. Run artifacts (bundle,
`.pb` feeds, flamegraphs) are git-ignored and were not committed.

---

# Addendum (2026-07-10, follow-up) — Faithful constrained reproduction; the logging hypothesis, tested and refuted

The baseline above ran a **single-agency KCM** bundle on an **unconstrained
desktop** and, honestly, could not reproduce the production 85% peg (§2). This
follow-up rebuilt the reproduction to mirror the **actual production container**
— `agency-dockerfiles/test-organization-1/Dockerfile` — as closely as the harness
allows, to (a) test a specific new hypothesis about logging cost and (b) see
whether a size-and-config-faithful poll pegs a constrained core.

## A. What changed to raise fidelity

| Dimension | Baseline (§1) | This addendum (production-faithful) |
|---|---|---|
| Bundle | bare KCM `google_transit.zip`, 1 agency | **rule-set-19 build 56**, the real prod bundle: 5 agencies (metro `1`, sound_transit `40`, kitsap `20`, everett `97`, monorail `96`) |
| Feed agency pinning | implicit (only agency present) | **`agencyIds:["1"]`**, exactly as `GTFS_RT_FEEDS` pins it (harness now calls `setAgencyId`; otherwise `GtfsRealtimeSource.start()` defaults to *all* agencies) |
| Trip resolution | fresh bundle → nearly all resolve | live KCM feed vs **stale build-56** bundle → a **real per-cycle ERROR flood** (~52 log lines/refresh: 17 `null block … bailing` + 17 `no stoptimes` + ~9 `route not found … defaulting` + summary/alerts) |
| Log config | plain test layout | **the production `tdf-log4j2.xml`**: `Root=info` with the `[%F:%L]` location converter (copied in by the deploy Dockerfile) |
| CPU / RAM | unconstrained desktop | **`docker run --cpus=1 --memory=2g`** (`-Xmx1200m`) |
| Timezone | host (America/Los_Angeles) | **`TZ=America/Los_Angeles`** in-container (the deploy Dockerfile's `TZ`) — required: GTFS service-date math (`BlockFinder`) NPEs under the stock UTC image |

Matching was **byte-identical across every run** (381 active vehicles, **matched
373 / 398** updates), so the only variable between the three log configs below is
the logging. (This also closes a §1 honesty gap: the `matched=…` summary line is
now captured — `matched=373 (46)` for `updates=398`.)

Harness/config artifacts are committed under `onebusaway-gtfsrt-perf/`:
`PerfBundleHarness` agency pinning, `perf/logconfigs/log4j2-{prod,cheap,quiet}.xml`,
and `perf/run-constrained.sh`.

## B. The logging hypothesis

The production log pattern is `[%F:%L]` (file + line) at `Root=info`. The Log4j2
manual is explicit that `%F`/`%L` are *"an expensive operation and not
garbage-free"* — each event walks the stack and allocates. Hypothesis: the
per-cycle ERROR flood, multiplied by that per-line stack walk, is a hidden CPU
cost the plain-log baseline never saw. Three configs, identical in every respect
**except** logging, isolate it:

- **prod** — `[%F:%L]` at `info` (full flood, with location) — faithful to prod.
- **cheap** — same flood, layout drops `[%F:%L]` for the cached logger name.
- **quiet** — the three flooding loggers muted to `OFF` (no flood at all).

`prod − cheap` isolates location-computation cost; `cheap − quiet` isolates the
flood's log-volume cost; `quiet` is the pure matching floor.

### Result (3 repeats each, `--cpus=1 --memory=2g`, warmup 8 / measure 30)

| Config | p50 refresh (range) | mean refresh (range) | p95 spread |
|---|---|---|---|
| **quiet** (no flood) | 194 ms (190–201) | 294 ms (256–331) | 0.34–1.41 s |
| **cheap** (flood, no `%F:%L`) | ~205 ms (198–313\*) | 305 ms (217–371) | 0.29–1.91 s |
| **prod** (`[%F:%L]` + flood) | 205 ms (198–215) | 319 ms (277–356) | 0.51–2.01 s |

\* cheap's 313 ms p50 is a GC-timing outlier in one of three runs.

**Verdict: the logging hypothesis is refuted at the real flood magnitude.** The
config-to-config differences (~10–25 ms on the mean) are *smaller than each single
config's own run-to-run spread* (75–155 ms), which is dominated by G1 pause
timing (note the p95 column swinging 0.3–2.0 s). `%F:%L` **is** expensive
per call, but ~52 flood lines/refresh × a stack walk is a rounding error against
~195 ms of matching. It would only matter if the flood were orders of magnitude
larger. Muting the flood loggers is still worth doing for **log hygiene** (see
[[oba-render-cpu-realtime]]), but it will **not** meaningfully cut CPU.

## C. Does a faithful, constrained poll peg the core? No.

Even with the full production config — multi-agency bundle, real flood,
`[%F:%L]` at info, 1 vCPU, 2 GB, LA time — a warmed poll is **~195–210 ms median
/ ~290–320 ms mean**, with occasional GC spikes to ~2 s. At the production **30 s
cadence**:

```
median 200 ms / 30,000 ms ≈ 0.67% of one core
worst p95 ~2 s / 30,000 ms ≈ 6.7% of one core (and only ~5% of polls)
```

This **confirms §2's reconciliation caveat empirically**, and rules the logging
hypothesis out as the missing factor. A single 30 s feed poll — however
faithfully reproduced — does not peg a 1-vCPU core.

The same runs also show sustained back-to-back throughput of **~3 refresh/s = one
full core when the work runs continuously**. So the arithmetic of an 85% peg
requires the block-location work to run **~127× more often than once/30 s** —
i.e. effectively continuously. That points away from "the 30 s poll" and toward a
**continuous or high-frequency driver of the same hot path**:

1. **API read traffic (leading candidate).** `getBlockLocation` — the #2 CPU
   hotspot (§3) and the same code that hammers `BlockStopTimeList.get()` — is the
   arrivals-and-departures **read** path. Even modest sustained QPS pegs the core
   independently of polling. Not exercised by this poll-only harness. **Verify
   `http_request_count` on the box** (health checks, monitoring pollers, and app
   traffic all count).
2. **Tighter effective cadence / more feeds** than the single 30 s feed. Verify
   the deployed `refreshInterval` and feed count.
3. A weaker cloud vCPU multiplies per-refresh cost — but even 5–10× only reaches
   ~3–7% duty at 30 s; it cannot alone explain 85%.

## D. Fix #1 implemented and validated (memoize `BlockStopTimeList.get()`)

Fix #1 from §7 is now implemented: `BlockStopTimeList.get(index)` populates a
lazy, `transient` `BlockStopTimeEntry[]` cache instead of building a fresh
`BlockStopTimeEntryImpl` on every call (`BlockConfigurationEntryImpl.java`).
Thread-safe without locking because `BlockStopTimeEntryImpl` has only final
fields (a racing reader sees `null`→rebuild or a safely-published instance).
Guarded by `BlockConfigurationEntryImplTest` (values correct + element identity
memoized); `ScheduledBlockLocationServiceImplTest` (14) and the golden
`GtfsRealtimeTripLibraryCharacterizationTest` still pass unchanged.

Measured before/after on the **same constrained harness** (quiet config,
`--cpus=1 --memory=2g`, warmup 8 / measure 40, 3 repeats each; allocation via a
per-measure-thread `com.sun.management.ThreadMXBean` counter added to
`GtfsRtRefreshBenchmark`):

| Metric | Before (rebuild/call) | After (memoized) | Δ |
|---|---|---|---|
| **Allocation / refresh** (measure thread) | **542 MB** | **159 MB** | **−71%** |
| **p50 refresh** | 188 ms | 143 ms | **−24%** |
| mean refresh | 261 ms | 312 ms | within noise |
| p95 refresh | 0.6–1.6 s | 1.2–1.6 s | within noise |

- **Allocation is the decisive, deterministic result: −71 %** (542 → 159 MB per
  refresh), squarely in the ~59–69 % range §4/§6 attributed to this call. This is
  the fix's core claim, proven.
- **Median refresh is a stable −24 %** (188 → 143 ms across all 3 reps) — the
  typical-case latency win from no longer allocating + minor-GC'ing per `.get()`.
- **Mean and p95 are GC-pause-timing noise** on this fast desktop core with a
  1.2 GB heap: both before and after hit occasional ~1.5 s pauses whose timing
  lands randomly in a 40-sample window, so the mean is not a reliable comparator
  here (the median is). The 71 % allocation cut should convert to lower GC CPU on
  a more memory-pressured or slower production box, but this harness on desktop
  hardware cannot demonstrate that cleanly — re-measure on the target box.

## E. Bottom line

- **Fix #1 is done and proven**: −71 % allocation, −24 % median refresh, behavior
  preserved by existing + new tests. It also lightens the arrivals **read** path,
  which shares `getBlockLocation` → `BlockStopTimeList.get()`.
- The **per-refresh waste** identified in the baseline is **reconfirmed** under
  faithful constrained conditions and now **fixed**.
- The **`[%F:%L]` logging hypothesis is refuted** as a material CPU cost.
- The **85 % peg is still not reproduced by any poll-path experiment**, with
  strong evidence it is **not** the single 30 s poll. The next reproduction to
  run is **concurrent read load** against `getBlockLocation` (now cheaper post-Fix
  #1), gated on first confirming the box's real request traffic and poll cadence.

### Addendum provenance

| Artifact | Source |
|---|---|
| Multi-agency bundle | `BuildKcmBundle` over `rule-set-19` build-56 feeds in `<agencyId>/<feed>.zip` layout |
| A/B/C timings (3 reps) | `perf/run-constrained.sh {prod,cheap,quiet}`, `--cpus=1 --memory=2g`, `TZ=America/Los_Angeles` |
| Flood magnitude (~52 lines/refresh) | measure-phase log counts: 17 bailing + 17 no-stoptimes + ~9 route-not-found per refresh |
| `%F`/`%L` cost claim | Log4j2 manual, Pattern Layout (Location/Line converters: "expensive … not garbage-free") |
| Fix #1 before/after (alloc −71 %, p50 −24 %) | `perf/run-constrained.sh quiet`, 3 reps each, memoization stashed/restored via git; alloc from `GtfsRtRefreshBenchmark`'s `ThreadMXBean` counter |

Run artifacts (bundle, `.pb` feeds, per-config logs) are git-ignored and not
committed; the harness, log configs, and runner script are.
