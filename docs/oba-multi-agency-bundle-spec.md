# OBA Multi-Agency Bundle Build — Technical Spec

> **Status:** draft 2 — reviewed against the source tree; corrections in §1.1,
> §4.3, §6.1, §6.2.2, §6.3, §6.4, §7.2, §7.4, §8.2
> **Audience:** the engineer (human or Claude agent) building the
> `onebusawaycloud` GTFS merge pipeline that produces bundles for OBA's
> `onebusaway-transit-data-federation-builder`.
> **Goal:** explain the two OBA-native mechanisms that let a multi-agency
> bundle keep per-agency identity *and* consolidate physically shared stops,
> with enough detail to implement them at build time.

---

## 1. Purpose & target reader

This document is a build-time spec, not a runtime spec. It assumes:

- The merge pipeline currently produces a single pre-merged GTFS zip
  (e.g. `rule-set-4-merged-gtfs.zip`) and hands it to OBA's bundle builder
  as a single positional argument. The reader is replacing that step.
- The reader can produce per-agency GTFS zips upstream (one per feed source)
  rather than merging them itself.
- The reader has read access to the `onebusaway-application-modules` source
  tree; every claim in this doc cites the file and line range that backs it,
  so the reader can verify behavior directly.

Out of scope: real-time configuration, federation-webapp deployment, bundle
hosting/distribution. Those layers are unaffected by the changes described
here.

### 1.1 Versions and runtime assumptions

The behavior in this spec is verified against the current
`onebusaway-application-modules` checkout (`pom.xml` lists version
**2.7.1**, Java **11**). The internal interfaces (`GtfsBundle`,
`GtfsReadingSupport`, `EntityReplacementStrategy`,
`GtfsMultiReaderImpl.StoreImpl`) are stable across the 2.x line, but the
external `onebusaway-gtfs-modules` `GtfsReader` is a separate library
with its own version axis. Pin both.

The current Docker container uses `tomcat:8.5.100-jdk11-temurin-focal`
(see top of `Dockerfile`), and invokes `gtfstidy` (latest at build time, from
`github.com/patrickbr/gtfstidy`). The builder jar
(`onebusaway-transit-data-federation-builder-${OBA_VERSION}-withAllDependencies.jar`)
is **not downloaded**: `build_bundle.sh:54-55` copies it out of the
container's local Maven repository
(`/root/.m2/repository/org/onebusaway/onebusaway-transit-data-federation-builder/…`)
into `/oba/builder.jar`, and it lands in that repository by building the
source tree inside the container (`bin/make`). If you build a different
image, replicate those dependencies — none of the consolidation behavior
in this spec works without the builder jar that produced it.

---

## 2. Background: why a single pre-merged GTFS flattens agencies

Standard GTFS (`stops.txt`) has no `agency_id` column. Stops are linked to
agencies only transitively — a stop belongs to whichever routes' trips
call at it, and routes carry an explicit `agency_id` via `routes.txt`.

OBA's GTFS loader (the `onebusaway-gtfs-modules` library, wrapped by
`GtfsReader`) handles this by stamping a **default agency id** onto every
entity that doesn't carry an explicit one. Stops, shapes, fare_attributes,
and similar agency-less entities all receive the default. The default
itself comes from one of two places:

1. Whatever the calling code passes to `GtfsReader.setDefaultAgencyId`.
2. If nothing is set, the first agency in the feed's `agency.txt`.

When the bundle builder is invoked with a single positional zip path —
`FederatedTransitDataBundleCreatorMain.java:124-139` — it constructs a
single anonymous `GtfsBundle` with **no `defaultAgencyId`** set
(`onebusaway-transit-data-federation-builder/src/main/java/org/onebusaway/transit_data_federation/bundle/FederatedTransitDataBundleCreatorMain.java:124-139`).
The fallback in `GtfsReader` then picks the first agency listed in
`agency.txt`, and every stop in the feed gets stamped with that agency id.

**Observed effect on the current `rule-set-4-merged-gtfs.zip` build:**

| API endpoint | Result |
|---|---|
| `/api/where/agencies-with-coverage.json` | All 4 agencies listed (good) |
| `/api/where/route-ids-for-agency/{MTS,NCTD,SAN,UCSD}` | 104 / 46 / 1 / 6 (correct — routes carry explicit agency_id) |
| `/api/where/stop-ids-for-agency/MTS` | 6,164 stops (all of them) |
| `/api/where/stop-ids-for-agency/{NCTD,SAN,UCSD}` | 0 (broken) |

Routes are partitioned correctly across agencies because `routes.txt` has
an explicit `agency_id` field. Stops are not, because `stops.txt` does not.

---

## 3. The two mechanisms OBA provides

OBA's federation-builder was designed for federated multi-agency
deployments from day one (Puget Sound — King County Metro + Sound Transit
+ Pierce + Community + Kitsap — was the original deployment). Two
complementary mechanisms work together:

1. **Per-agency `GtfsBundle` with `defaultAgencyId`.** Each input feed is
   loaded under its own agency identity. Stops in `mts.zip` become
   `AgencyAndId("MTS", …)`; stops in `ucsd.zip` become
   `AgencyAndId("UCSD", …)`. No upstream merging is required.

2. **`EntityReplacementStrategy`.** A declarative file mapping
   "absorb stop X into canonical stop Y." Used to handle shared physical
   stops between agencies (e.g., an MTS bus stop and a UCSD shuttle stop
   on the same curb). One stop entity survives; the other is dropped and
   all references to it transparently rewrite to the canonical.

Both are wired through Spring XML config, both run in the same load pass,
and both are battle-tested.

---

## 4. Mechanism 1 — Per-agency `GtfsBundle` + `defaultAgencyId`

### 4.1 The config object

`onebusaway-transit-data-federation-builder/src/main/java/org/onebusaway/transit_data_federation/bundle/model/GtfsBundle.java:35-108`
defines the schema for one input feed:

```java
public class GtfsBundle {
  private File path;                                   // local path
  private URL url;                                     // optional download URL
  private String defaultAgencyId;                      // the agency to stamp
  private Map<String,String> agencyIdMappings = ...;   // optional rewrites
}
```

Field-by-field:

| Field | Required | Effect |
|---|---|---|
| `path` | yes (or `url`) | Local `.zip` or directory holding GTFS files. |
| `url` | no | Download URL; ignored when `path` is set. |
| `defaultAgencyId` | recommended | Stamped onto every agency-less entity in the feed (most importantly, every stop). If unset, the external `GtfsReader` in `onebusaway-gtfs-modules` falls back to the first `agency_id` listed in `agency.txt`. (The fallback is implemented outside this repo; cross-check there if the behavior matters to you.) |
| `agencyIdMappings` | no | `Map<oldId, newId>` rewriting references inside the feed. Used when two feeds collide on agency id values (e.g., both ship `agency_id=1`). |

A `GtfsBundles` bean (note plural) holds a `List<GtfsBundle>` and is the
top-level construct the builder looks for.

### 4.2 How feeds get loaded

The driver is `GtfsReadingSupport.readGtfsIntoStore`
(`onebusaway-transit-data-federation-builder/src/main/java/org/onebusaway/transit_data_federation/bundle/tasks/GtfsReadingSupport.java:102-144`):

```java
GtfsBundles gtfsBundles = getGtfsBundles(context);

for (GtfsBundle gtfsBundle : gtfsBundles.getBundles()) {
  GtfsReader reader = new GtfsReader();
  reader.setOverwriteDuplicates(true);
  reader.setEntitySchemaFactory(factory);          // (omitted from snippet above
                                                   // in earlier drafts — present in real code)
  reader.setInputLocation(gtfsBundle.getPath());

  if (gtfsBundle.getDefaultAgencyId() != null)
    reader.setDefaultAgencyId(gtfsBundle.getDefaultAgencyId());

  for (Map.Entry<String,String> e : gtfsBundle.getAgencyIdMappings().entrySet())
    reader.addAgencyIdMapping(e.getKey(), e.getValue());

  multiReader.addGtfsReader(reader);
}
multiReader.run();
```

One `GtfsReader` per bundle, each with its own `defaultAgencyId`. A
single `GtfsMultiReaderImpl` then runs them all into a shared store.
`setOverwriteDuplicates(true)` is OBA's standard behavior — within a
single feed, later rows clobber earlier rows on id collision. With
per-agency `defaultAgencyId`s set, cross-feed id collisions don't happen
(ids are namespaced by agency), so this flag has no consolidation
implication. Don't worry about bundle ordering in the XML.

### 4.3 Why the load order matters

`GtfsMultiReaderImpl.run()`
(`onebusaway-transit-data-federation-builder/src/main/java/org/onebusaway/transit_data_federation/bundle/tasks/GtfsMultiReaderImpl.java:92-142`)
processes entity classes **class-major, then feed-minor**:

```java
// (Whole block lives inside try { … } catch (IOException ex) { ex.printStackTrace(); }
//  with StoreImpl store = new StoreImpl(_store); store.open(); … store.close();
//  framing around the loops — elided here for clarity.)
List<Class<?>> entityClasses = _readers.get(0).getEntityClasses();
for (Class<?> entityClass : entityClasses) {           // outer: type
  for (GtfsReader reader : _readers) {                  // inner: feed
    if (entityClass.equals(Agency.class))
      reader.setAgencies(agencies);                     // share Agency list

    reader.readEntities(entityClass);

    if (entityClass.equals(Agency.class))
      agencies = new ArrayList<Agency>(reader.getAgencies());

    store.flush();
  }
}
```

That outer loop is the whole reason consolidation works. The class order
itself comes from the external library (`_readers.get(0).getEntityClasses()`);
in `onebusaway-gtfs` **1.4.17** (the version pinned by this repo's
`pom.xml` `<gtfs-version>`) the relevant subsequence is
**Agency → … → Route → … → Stop → Trip → StopTime** — note that Routes
load *before* Stops. The load-bearing guarantee is: **every Stop from
every feed is in the store before any feed's StopTimes parse.** By the
time UCSD's `stop_times.txt` rows are parsing and asking "what Stop is
this row referring to?", every canonical stop from every feed is
guaranteed to already be in the store. The consolidation rewrites in §5
can therefore always resolve.

### 4.4 `agencyIdMappings`

`agencyIdMappings` is a per-feed rewrite table applied by `GtfsReader`
*before* the entity reaches the store. If two real-world feeds both
declare `agency_id=1`, mapping one to `MTS` and the other to `UCSD`
prevents them from clobbering each other.

For SDMTS this is not needed — MTS, NCTD, SAN, UCSD are already distinct
strings in the upstream feeds — but document it for the merge pipeline
because new agencies (or sloppy GTFS publishers) will eventually require
it.

---

## 5. Mechanism 2 — `EntityReplacementStrategy` (stop consolidation)

### 5.1 The interface

`onebusaway-transit-data-federation-builder/src/main/java/org/onebusaway/transit_data_federation/bundle/services/EntityReplacementStrategy.java:23-30`:

> "Generic interface to support swapping out one entity with another.
> Use primarily in GTFS loading to consolidate stops from GTFS feeds from
> separate agencies that have stops in common."

The interface is general (any entity type), but in practice it is used
almost exclusively for `org.onebusaway.gtfs.model.Stop`.

### 5.2 The mapping file format

Built by `EntityReplacementStrategyFactory`
(`onebusaway-transit-data-federation-builder/src/main/java/org/onebusaway/transit_data_federation/bundle/tasks/EntityReplacementStrategyFactory.java:43-106`;
the concrete `EntityReplacementStrategy` returned is `EntityReplacementStrategyImpl`,
in the same package). Format:

```text
# Comments are supported
MTS_60123  UCSD_gtsg-1
MTS_75096  NCTD_75096-alias  NCTD_75096-v2
MTS_15001  UCSD_genesee-utc
```

Rules:

- One **canonical** id per line, as the first whitespace-separated token.
- Subsequent tokens on the same line are **absorbed** ids — every reference
  to them is rewritten to the canonical.
- IDs use `AgencyAndId`'s string form: `<agencyId>_<entityId>`. The parser
  delegates to `AgencyAndIdLibrary.convertFromString` (line 98 of the factory).
- Tokens containing whitespace can be `"quoted"` (the factory checks for `"`
  and splits on quotes instead of whitespace — see line 90-93).
- Lines starting with `#`, `{{{`, or `}}}` are skipped (the `{{{`/`}}}` lines
  are an artifact of the original Trac-wiki–hosted Puget Sound consolidation
  file; harmless and worth keeping for backward compatibility with existing
  files in the wild).

### 5.3 How replacement actually works at load time

Wired in `GtfsReadingSupport.java:109-121` — picked up by Spring bean name
`entityReplacementStrategy`:

```java
if (!disableStopConsolidation && context.containsBean("entityReplacementStrategy")) {
  EntityReplacementStrategy strategy =
      (EntityReplacementStrategy) context.getBean("entityReplacementStrategy");
  multiReader.setEntityReplacementStrategy(strategy);
  // ... optional MultiCSVLogger / EntityReplacementLogger wiring follows
}
```

`GtfsMultiReaderImpl` wraps the real DAO in a `StoreImpl`
(`onebusaway-transit-data-federation-builder/src/main/java/org/onebusaway/transit_data_federation/bundle/tasks/GtfsMultiReaderImpl.java:152-195`)
that intercepts two operations:

```java
@Override
public void saveEntity(Object entity) {
  Class<? extends Object> entityType = entity.getClass();
  if (entity instanceof IdentityBean<?>
      && _entityReplacementStrategy.hasReplacementEntities(entityType)) {
    IdentityBean<?> bean = (IdentityBean<?>) entity;
    Serializable id = bean.getId();
    if (_entityReplacementStrategy.hasReplacementEntity(entityType, id)) {
      _rejectionStore.saveEntity(entity);   // shunt to side-store, never reach real DAO
      return;
    }
  }
  super.saveEntity(entity);
}

@Override
public <T> T getEntityForId(Class<T> type, Serializable id) {
  Serializable replacementId =
      _entityReplacementStrategy.getReplacementEntityId(type, id);
  if (replacementId != null) {
    T entity = super.getEntityForId(type, replacementId);
    if (entity != null) {
      _entityReplacementStrategy.logReplacement(type, id, replacementId,
          super.getEntityForId(type, id), entity);
      return entity;
    }
    _log.warn("error replacing entity: type=… fromId=… toId=… - replacement not found");
  }
  return super.getEntityForId(type, id);
}
```

Note the `IdentityBean<?>` guard in `saveEntity` — only entities that
implement `IdentityBean<?>` (which `Stop`, `Route`, `Trip`, `Agency` all do)
are candidates for replacement. Non-IdentityBean entities pass through
unchanged.

The semantics are subtle but important. Read these two methods together:

- **`saveEntity`** is invoked when a feed tries to store a `Stop` entity
  it just parsed. If that stop's id is *to be replaced*, the entity is
  diverted to a `_rejectionStore` (an in-memory side store — not the
  main DAO). The result: only one stop entity per physical location
  survives the build.
- **`getEntityForId`** is invoked when downstream parsing needs to look
  up a `Stop` — most importantly, while reading `stop_times.txt`. If the
  requested id is on the absorbed side of the mapping, the lookup
  transparently returns the canonical stop instead.

Combined: stop_times rows whose `stop_id` was absorbed get their
parent-pointer rewritten to the canonical stop. Their `Trip` (and
therefore their `Route`, and therefore their `Agency`) is unaffected.

### 5.4 Worked example — MTS bus + UCSD shuttle at Gilman Dr & Gilman Loop

Assume:

- `mts.zip` ships a stop with `stop_id=60123` ("Gilman Dr & Gilman Loop").
- `ucsd.zip` ships a stop with `stop_id=gtsg-1` at the same curb.
- Both are real, both have similar but not identical lat/lon (UCSD's might
  be 3m off the curb because their surveyor stood on the sidewalk).
- The MTS routes serving 60123 carry `agency_id=MTS` in `routes.txt`.
- The UCSD shuttle calling at gtsg-1 carries `agency_id=UCSD`.

Consolidation file entry:

```
MTS_60123  UCSD_gtsg-1
```

Build sequence:

1. `mts.zip` reads first (or in parallel with the others). MTS Agency is
   stored. MTS Stop 60123 is stored. Nothing unusual.
2. `ucsd.zip` reads. UCSD Agency is stored. UCSD Stop `gtsg-1` tries to
   save → `StoreImpl.saveEntity` sees it has a replacement → entity is
   shunted into `_rejectionStore`. **It never reaches the main store.**
3. Routes, Trips load normally. UCSD route still belongs to UCSD agency.
4. `stop_times.txt` rows from `ucsd.zip` parse. Each row referencing
   `gtsg-1` triggers `getEntityForId(Stop.class, "UCSD_gtsg-1")`, which
   sees the mapping and returns the `MTS_60123` Stop entity instead.
   The resulting `StopTime` is bolted onto `MTS_60123`.
5. Bundle-build downstream stages (block calculation, transfer patterns,
   stop-route indexing) operate on the consolidated graph and produce a
   single stop with arrivals from both agencies' routes.

Result via the API:

- `/api/where/arrivals-and-departures-for-stop/MTS_60123.json` returns
  both MTS bus arrivals and UCSD shuttle arrivals.
- `/api/where/stops-for-route/UCSD_<routeId>.json` includes `MTS_60123`
  as one of its stops, even though the stop "belongs" to MTS.
- `/api/where/route-ids-for-agency/UCSD.json` still returns 6 (unchanged).
- Map display shows one pin per physical location.

### 5.5 Where the canonical lat/lon comes from

The canonical stop entity is whichever one's `saveEntity` wins. With the
mapping `MTS_60123 UCSD_gtsg-1`, the canonical is `MTS_60123` and the
lat/lon used is MTS's. UCSD's surveyed coordinates are discarded along
with the rest of the absorbed entity.

This matters when the two feeds disagree on the stop's exact position.
Pick the canonical agency per shared stop based on whose lat/lon is more
trustworthy (usually whoever physically owns the curb).

---

## 6. Invoking the builder

### 6.1 The Main class's argument grammar

`FederatedTransitDataBundleCreatorMain.java:124-139` parses positional
arguments as follows:

- The **last** positional arg is the **output bundle directory**.
- Each preceding arg is either:
  - a path ending in `.zip` or pointing to a directory → constructed as
    an anonymous `GtfsBundle` with `path` set and **`defaultAgencyId`
    unset** (so falls back to first agency in `agency.txt`);
  - anything else → treated as a Spring context XML path, prefixed with
    `file:` and added to `contextPaths`.

Important corollary: **the Main class's positional-zip form does not let
you set `defaultAgencyId`.** To configure per-agency bundles you must use
the Spring XML form. The Main class then conditionally registers its own
`gtfs-bundles` bean only if at least one positional zip was supplied
(`FederatedTransitDataBundleCreatorMain.java:135-139`). Pass **only** XML
contexts plus the output dir, and your XML's `gtfs-bundles` bean wins.

A separate utility — `GtfsStopReplacementVerificationMain` — accepts a
`path:agencyId` syntax via `UtilityLibrary.getGtfsBundlesForArguments`
(`onebusaway-transit-data-federation-builder/src/main/java/org/onebusaway/transit_data_federation/bundle/utilities/UtilityLibrary.java:29-72`).
That syntax is **not supported by the main builder.**

A note on terminology: when this doc says the Main class creates
*anonymous* `GtfsBundle`s, it means `defaultAgencyId` is not set on
the bundle object — not that the Spring bean has no `id`. Spring bean
ids are unrelated. The XML in §6.2 declares all four `GtfsBundle`
beans without `id=` attributes (they're inline list elements), and
that's correct and idiomatic.

`GtfsReadingSupport.getGtfsBundles` contains what looks like a fallback
to a singular `gtfs-bundle` bean (no `s`)
(`onebusaway-transit-data-federation-builder/src/main/java/org/onebusaway/transit_data_federation/bundle/tasks/GtfsReadingSupport.java:153-168`),
but the fallback is unreachable in practice: the build context is a plain
Spring `GenericApplicationContext` (see
`ContainerLibrary.createContext`, called from
`FederatedTransitDataBundleCreator.build`), and `getBean("gtfs-bundles")`
on a missing bean **throws `NoSuchBeanDefinitionException`** rather than
returning `null` — the null-check on line 156 never sees a null. A config
without a `gtfs-bundles` bean fails the build with that exception.
Always define the plural `gtfs-bundles` bean.

### 6.2 Full Spring XML example for the SDMTS case

Save as `/oba/config/bundle-config.xml` (or similar) and pass it as a
positional argument to the builder. The two top-level beans the builder
looks for are `gtfs-bundles` (required) and `entityReplacementStrategy`
(optional — only if consolidation is desired).

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
                           http://www.springframework.org/schema/beans/spring-beans-3.0.xsd">

  <!-- ============================================================== -->
  <!-- Per-agency GTFS inputs                                          -->
  <!-- ============================================================== -->
  <bean id="gtfs-bundles"
        class="org.onebusaway.transit_data_federation.bundle.model.GtfsBundles">
    <property name="bundles">
      <list>
        <bean class="org.onebusaway.transit_data_federation.bundle.model.GtfsBundle">
          <property name="path"            value="/bundle/feeds/mts.zip"/>
          <property name="defaultAgencyId" value="MTS"/>
        </bean>
        <bean class="org.onebusaway.transit_data_federation.bundle.model.GtfsBundle">
          <property name="path"            value="/bundle/feeds/nctd.zip"/>
          <property name="defaultAgencyId" value="NCTD"/>
        </bean>
        <bean class="org.onebusaway.transit_data_federation.bundle.model.GtfsBundle">
          <property name="path"            value="/bundle/feeds/san.zip"/>
          <property name="defaultAgencyId" value="SAN"/>
        </bean>
        <bean class="org.onebusaway.transit_data_federation.bundle.model.GtfsBundle">
          <property name="path"            value="/bundle/feeds/ucsd.zip"/>
          <property name="defaultAgencyId" value="UCSD"/>
        </bean>
      </list>
    </property>
  </bean>

  <!-- ============================================================== -->
  <!-- Stop consolidation (optional — omit if no shared stops)         -->
  <!-- ============================================================== -->
  <bean id="entityReplacementStrategyFactory"
        class="org.onebusaway.transit_data_federation.bundle.tasks.EntityReplacementStrategyFactory">
    <property name="entityMappings">
      <map>
        <entry key="org.onebusaway.gtfs.model.Stop"
               value="/oba/config/sdmts-stop-consolidation.txt"/>
        <!-- Value can also be an https://… URL; IOLibrary.getPathAsInputStream
             handles both. Useful if the consolidation file lives in version
             control on GitHub and you want the build to fetch the latest. -->
      </map>
    </property>
  </bean>

  <bean id="entityReplacementStrategy"
        factory-bean="entityReplacementStrategyFactory"
        factory-method="create"/>

</beans>
```

### 6.2.1 Example with `agencyIdMappings`

If one of the upstream feeds ships a colliding `agency_id` (rare but
real), rewrite it inside the `GtfsBundle` for that feed:

```xml
<bean class="org.onebusaway.transit_data_federation.bundle.model.GtfsBundle">
  <property name="path"            value="/bundle/feeds/some-feed.zip"/>
  <property name="defaultAgencyId" value="SOMEAGENCY"/>
  <property name="agencyIdMappings">
    <map>
      <entry key="1" value="SOMEAGENCY"/>
      <!-- every reference to agency_id=1 inside this feed gets rewritten
           to SOMEAGENCY before the entity reaches the store -->
    </map>
  </property>
</bean>
```

The map keys are the agency ids as they appear in the upstream feed; the
values are the rewritten ids that downstream code (and other feeds) will
see.

### 6.2.2 Optional: replacement audit logging

To produce a CSV record of every consolidation that actually happened
during a build, wire in `MultiCSVLogger` and `entityReplacementLogger`.
`GtfsReadingSupport.java:112-120` reads these beans by id and attaches
them to the multi-reader. Add to the same XML:

```xml
<bean id="multiCSVLogger"
      class="org.onebusaway.transit_data_federation.bundle.tasks.MultiCSVLogger">
  <property name="basePath" value="/bundle/logs"/>
</bean>

<bean id="entityReplacementLogger"
      class="org.onebusaway.transit_data_federation.bundle.tasks.EntityReplacementLoggerImpl"/>
```

After the build, `/bundle/logs/` contains `gtfs_stop_replacements.csv`
— the logger is stop-specific (`EntityReplacementLoggerImpl.log()`
writes one row per replaced stop with original/replacement ids, names,
and coordinates), regardless of what other entity types appear in the
mapping — plus `MultiCSVLogger`'s own `summary.csv` of row counts.
Inspect it as a sanity check on consolidation completeness. (If
`basePath` is left unset, `MultiCSVLogger` falls back to
`java.io.tmpdir` and creates the directory itself —
`MultiCSVLogger.java:82-88`.)

### 6.3 Invocation

```bash
cd /bundle    # IMPORTANT — see note below
java -Xss4m -Xmx3g \
  -jar /oba/builder.jar \
  /oba/config/bundle-config.xml \
  /bundle
```

Two positional args:

- `/oba/config/bundle-config.xml` — the Spring config. Because it does
  not end in `.zip` and is not a directory, the Main class's parser
  (`FederatedTransitDataBundleCreatorMain.java:124-133`) treats it as a
  context path. Your XML's `gtfs-bundles` bean wins.
- `/bundle` — the output bundle directory (always the last positional
  arg).

The `cd /bundle` is non-obvious but required: the existing
`build_bundle.sh:51` carries the comment *"The JAR must be executed from
within the same directory as the bundle, or else some necessary files
are not generated."* The builder writes some scratch artifacts to the
current working directory, not just the output directory.

Do **not** pass any positional `.zip` arg. If you do, the Main class
registers its own in-memory `gtfs-bundles` bean definition containing
only the anonymous zip bundles
(`FederatedTransitDataBundleCreatorMain.java:135-139`) — and it **wins**:
`ContainerLibrary.createContext`
(`onebusaway-container/src/main/java/org/onebusaway/container/ContainerLibrary.java:104-108`)
registers programmatic bean definitions *after* loading the XML context
paths, so a same-named programmatic bean silently replaces your XML's
`gtfs-bundles` bean. Every `defaultAgencyId` you configured is dropped
and you're back to the flattened single-agency behavior of §2.

### 6.4 Complete replacement `build_bundle.sh`

The existing single-agency script lives at
`docker_app_server/bundle_builder/build_bundle.sh`. Below is a complete
replacement for the multi-agency case. **Replace the file**, don't
modify in place — the existing single-`GTFS_URL` mode and the new
multi-feed mode have different env-var contracts and tangling them is a
source of bugs.

```bash
#!/bin/bash
set -euo pipefail

# Per-agency feed URLs. These env vars are set in the Dockerfile or
# docker-compose.yml — one per agency. The agency id portion (MTS, NCTD,
# …) must match the defaultAgencyId values in bundle-config.xml exactly.
declare -A FEEDS=(
  [MTS]="$MTS_GTFS_URL"
  [NCTD]="$NCTD_GTFS_URL"
  [SAN]="$SAN_GTFS_URL"
  [UCSD]="$UCSD_GTFS_URL"
)

# Bail loudly if any feed URL is missing — partial builds are worse than
# no build.
for AGENCY in "${!FEEDS[@]}"; do
  if [ -z "${FEEDS[$AGENCY]:-}" ]; then
    echo "ERROR: ${AGENCY}_GTFS_URL is not set" >&2
    exit 1
  fi
done

mkdir -p /bundle/feeds /bundle/logs
cd /bundle

# Stage the builder jar out of the in-container Maven repository. The
# original build_bundle.sh does this too — /oba/builder.jar does not
# exist in the image until this cp runs (the jar is produced by building
# the source tree in-container via bin/make, not downloaded).
cp "/root/.m2/repository/org/onebusaway/onebusaway-transit-data-federation-builder/${OBA_VERSION}/onebusaway-transit-data-federation-builder-${OBA_VERSION}-withAllDependencies.jar" \
   /oba/builder.jar

for AGENCY in "${!FEEDS[@]}"; do
  URL="${FEEDS[$AGENCY]}"
  PRISTINE="/bundle/feeds/${AGENCY}_pristine.zip"
  TIDY_DIR="/bundle/feeds/${AGENCY}_tidy"
  FINAL="/bundle/feeds/$(echo "$AGENCY" | tr '[:upper:]' '[:lower:]').zip"

  echo "=== ${AGENCY}: downloading ==="
  wget -O "$PRISTINE" "$URL"

  echo "=== ${AGENCY}: tidying ==="
  rm -rf "$TIDY_DIR"
  mkdir -p "$TIDY_DIR"
  (cd "$TIDY_DIR" && gtfstidy -OscRCSmeD -o . "$PRISTINE")

  echo "=== ${AGENCY}: repacking ==="
  rm -f "$FINAL"
  (cd "$TIDY_DIR" && zip -q "$FINAL" *)
done

# Verify the consolidation file before building, so a stale canonical id
# fails the build loudly rather than silently dropping consolidations.
# COPY /oba/config/sdmts-stop-consolidation.txt and bundle-config.xml
# into the image at Dockerfile build time.
echo "=== verifying stop-consolidation file ==="
java -cp /oba/builder.jar \
  org.onebusaway.transit_data_federation.bundle.utilities.GtfsStopReplacementVerificationMain \
  /oba/config/sdmts-stop-consolidation.txt \
  /bundle/feeds/mts.zip:MTS \
  /bundle/feeds/nctd.zip:NCTD \
  /bundle/feeds/san.zip:SAN \
  /bundle/feeds/ucsd.zip:UCSD \
  | tee /bundle/logs/consolidation-verification.txt

# If verification output is non-empty, a canonical id is missing from
# the loaded feeds. Fail the build.
if [ -s /bundle/logs/consolidation-verification.txt ]; then
  echo "ERROR: stop-consolidation file references missing canonical ids" >&2
  cat /bundle/logs/consolidation-verification.txt >&2
  exit 1
fi

echo "=== building bundle ==="
cd /bundle
java -Xss4m -Xmx3g \
  -jar /oba/builder.jar \
  /oba/config/bundle-config.xml \
  /bundle
```

Things to notice:

- **Each `gtfstidy` run has its own scratch directory** (`_tidy/` per
  agency). Running `gtfstidy` four times into a shared `gtfs-out/` would
  produce corrupt outputs. The original script gets away with one
  scratch dir because it only runs `gtfstidy` once.
- **The verification utility runs before the build** and fails the
  build on any missing canonical id. This is the CI gate that catches
  consolidation rot before runtime.
- **`set -euo pipefail`** at the top — the original script omits this
  and so will silently continue past a failed `wget`. Don't replicate
  that bug.

### 6.5 Dockerfile contract

The new script depends on the consolidation file and the bundle config
XML being present in the container at fixed paths. Add to the
`Dockerfile` (or the docker-compose build context — whichever produces
the builder image):

```dockerfile
COPY config/bundle-config.xml             /oba/config/bundle-config.xml
COPY config/sdmts-stop-consolidation.txt  /oba/config/sdmts-stop-consolidation.txt
```

These files live in version control next to the Dockerfile (e.g.
`docker_app_server/bundle_builder/config/`). The consolidation file is
the audit-able artifact for which stops are merged together; treat it
as code, not data.

Set the four per-agency env vars in `docker-compose.yml` or wherever
the existing `GTFS_URL` is set:

```yaml
environment:
  - MTS_GTFS_URL=https://…/mts.zip
  - NCTD_GTFS_URL=https://…/nctd.zip
  - SAN_GTFS_URL=https://…/san.zip
  - UCSD_GTFS_URL=https://…/ucsd.zip
```

The single-feed `GTFS_URL` is no longer used in multi-agency mode.
Remove it from environment to avoid confusion.

---

## 7. Pipeline workflow for the merge system

### 7.1 Step 1 — Produce per-agency zips upstream

The current pipeline appears to merge four sources into one zip before
OBA sees it. For multi-agency builds, stop merging and instead emit one
zip per source. Naming convention recommended: `{agencyId}.zip`,
matching the `defaultAgencyId` you'll set in the bundle config.

If the upstream sources don't cleanly correspond 1:1 to OBA agencies
(e.g., NCTD ships a single GTFS that contains BREEZE bus + COASTER rail
+ SPRINTER rail under one `agency_id`), that's fine — they belong to one
OBA `defaultAgencyId` (`NCTD`) and the routes-vs-agency distinction is
handled by `routes.route_type` and `routes.route_short_name` downstream.

If they don't correspond at all (e.g., a single upstream feed contains
multiple agencies with their own `agency_id` rows), you have two options:

- Split it upstream into per-agency zips.
- Load it as a single `GtfsBundle` with `defaultAgencyId` matching the
  *primary* agency, knowing the other agencies in `agency.txt` will
  still be honored for their own routes. Stops in that single feed will
  all be stamped with the primary agency, though — same problem you're
  trying to escape. Prefer splitting.

### 7.2 Step 2 — Identify candidate stop consolidations (one-time setup, not per-build)

This is a **one-time, human-reviewed setup step**, not part of every
build. The pipeline can automate candidate detection, but the final
canonical-vs-absorbed decision per pair requires human judgment (per
§5.5 — the canonical's lat/lon wins, so the curb owner usually
should be canonical). Don't try to automate this end-to-end.

**Existing OBA primitive to leverage.** The federation-builder already
ships `StopConsolidationSuggestionsTask`
(`onebusaway-transit-data-federation-builder/src/main/java/org/onebusaway/transit_data_federation/bundle/tasks/StopConsolidationSuggestionsTask.java`).
It implements the proximity-based candidate detection logic OBA itself
uses: a hard-coded **20 m** threshold (`distanceThreshold` in `run()`),
great-circle distance via `SphericalGeometryLibrary.distance`,
cross-agency pairs only (each agency's stops are compared against the
stops of previously-seen agencies), and union–find grouping so chains of
nearby stops come out as one suggestion set. Two caveats before reusing
it directly: it runs as a *bundle task* against the already-loaded DAO
(not standalone against raw zips), and it hard-codes a fetch of the
Puget Sound consolidation wiki file (`CONSOLIDATED_URL`, line 45) to
suppress already-consolidated pairs — for SDMTS you'd have to modify
that. Read it as a reference for thresholds and pairing heuristics
even if you write your own script outside the builder pipeline.

**Algorithm if you write your own.** For each pair of agency feeds:

1. Load `stops.txt` from both feeds. Build a list of
   `(agency_id, stop_id, stop_name, lat, lon)` tuples.
2. For each cross-agency pair of stops, compute the great-circle
   distance via haversine.
3. Emit a candidate row if distance < 15m. Tune this radius after
   reviewing the first pass — a divided arterial can put physically
   distinct stops within 15m, and a sprawling transit center can put
   the *same* boarding area 30m apart on different feeds.
4. Optionally also gate on `stop_name` similarity (Levenshtein or token
   Jaccard) to filter out coincidental proximity.

**Output format.** Emit a TSV like:

```
canonical_agency  canonical_id  absorbed_agency  absorbed_id  distance_m  canonical_name  absorbed_name
MTS               60123         UCSD             gtsg-1       4.2         Gilman Dr & Gilman Loop  Gilman Drive
```

Sort by `distance_m` ascending so the most obvious matches surface
first. The canonical-vs-absorbed choice on each row is a default the
human will confirm or flip; pick by curb ownership using whatever
domain knowledge you have (for SDMTS: MTS owns off-campus curbs, UCSD
owns campus loop, NCTD owns coastal transit centers).

**Where the script lives.** This script is build-system infrastructure,
not OBA code. Put it in the `onebusawaycloud` merge-pipeline repo
alongside the bundle-build orchestration. It doesn't need to ship in
the OBA Docker image.

**Cadence.** Run it whenever any upstream feed undergoes a structural
change (new transit center opens, agency renumbers stops). For SDMTS
that's effectively quarterly. Between runs, the consolidation file is
static.

### 7.3 Step 3 — Author the consolidation file

Format per §5.2. Conventions to follow:

- **Pick the canonical agency by curb ownership** when known. For
  SDMTS-area shared stops, MTS owns most off-campus curbs; UCSD owns the
  campus loop. NCTD owns coastal transit centers like Oceanside TC.
- **One canonical id per line.** It's tempting to chain (`A B C` meaning
  "B and C both absorb into A") and the parser supports it
  (`EntityReplacementStrategyFactory.java:100-101`), but keep it
  one-per-line for readability and code-review-ability.
- **Add a comment header** with the consolidation file's provenance
  (date, source bundle versions it was derived from, contact email).
  Comments start with `#`.
- **Check it into version control** alongside the Dockerfile, not into
  the GTFS zip itself.

### 7.4 Step 4 — Run the verification utility

`GtfsStopReplacementVerificationMain`
(`onebusaway-transit-data-federation-builder/src/main/java/org/onebusaway/transit_data_federation/bundle/utilities/GtfsStopReplacementVerificationMain.java:46-95`)
loads only the `Agency` and `Stop` entity classes from each feed and
checks that every **canonical** id in the consolidation file (first
token on each line — see line 90) still exists.

Note: it does **not** check that the absorbed ids exist. Absorbed stops
are allowed to come and go in upstream feeds; they're only matched if
present. Canonicals disappearing is the failure mode this tool guards
against.

Invocation (uses its own `path:agencyId` syntax):

```bash
java -cp /oba/builder.jar \
  org.onebusaway.transit_data_federation.bundle.utilities.GtfsStopReplacementVerificationMain \
  /oba/config/sdmts-stop-consolidation.txt \
  /bundle/feeds/mts.zip:MTS \
  /bundle/feeds/nctd.zip:NCTD \
  /bundle/feeds/san.zip:SAN \
  /bundle/feeds/ucsd.zip:UCSD
```

It prints any canonical id that is missing from the loaded stops, one
per line, in the form `<id> <- <original-line-from-file>`. Empty output
means everything resolves. Run this as a CI gate — the build should not
proceed if any canonical id is missing.

Two argument-parsing details worth knowing
(`UtilityLibrary.getGtfsBundlesForArguments`):

- Any argument ending in `.xml` is loaded as a Spring context path and
  the `gtfs-bundles` bean found there is appended to the bundle list
  (`UtilityLibrary.java:38-39` and `60-69`). So instead of repeating
  `path:agencyId` pairs, you can pass the *same* `bundle-config.xml`
  used for the build — one source of truth for paths and agency ids.
  (The context is fully instantiated, so the XML's
  `entityReplacementStrategy` bean will also be created and will read
  the consolidation file; that's harmless here.)
- A bare path with **no** `:agencyId` suffix gets an auto-generated
  numeric default agency id (`99990`, `99991`, …
  `UtilityLibrary.java:34,50`) — which makes every canonical id in your
  file look "missing." Always pass the `:agencyId` suffix or the XML.

### 7.5 Step 5 — Build the bundle

Per §6.3. Output: a directory containing the binary bundle files
(`StopEntries.obj`, `BlockEntries.obj`, `TransferPatterns.obj`, etc.).
The federation-webapp reads this directory at runtime.

### 7.6 Step 6 — Smoke-test the resulting bundle

Once the federation-webapp is running against the new bundle, hit these
endpoints. Compare against numerical baselines you record before the
migration:

| Endpoint | Expected result |
|---|---|
| `/api/where/agencies-with-coverage.json` | All 4 agencies present |
| `/api/where/route-ids-for-agency/MTS.json` | 104 (matches §2 baseline) |
| `/api/where/route-ids-for-agency/NCTD.json` | 45-46 (matches §2 baseline) |
| `/api/where/route-ids-for-agency/SAN.json` | 1 (matches §2 baseline) |
| `/api/where/route-ids-for-agency/UCSD.json` | 6 (matches §2 baseline) |
| `/api/where/stop-ids-for-agency/MTS.json` | Several thousand stops |
| `/api/where/stop-ids-for-agency/NCTD.json` | Non-zero (was 0 before — this is the headline win) |
| `/api/where/stop-ids-for-agency/SAN.json` | Non-zero (was 0 before) |
| `/api/where/stop-ids-for-agency/UCSD.json` | Non-zero (was 0 before) |
| Total stop count across all four agencies | ≈ (sum of raw stops.txt rows across input zips) − (number of absorbed entries in consolidation file) |
| `/api/where/arrivals-and-departures-for-stop/<shared_stop>.json` | **Schedule entries from both consuming agencies' routes** |
| `/api/where/stops-for-route/<UCSD_route>.json` | Includes the canonical (MTS-prefixed) stop(s) where UCSD shares curb |
| Realtime arrivals on a consolidated stop | If RT was working pre-migration for the canonical's owning agency, it still works |

Numerical assertions to bake into CI:

- **Sum of `stops-for-agency` counts** = (sum of input `stops.txt` row
  counts) − (count of non-blank, non-comment, non-`{{{`/`}}}` lines in
  the consolidation file, multiplied by absorbed-tokens-per-line). A
  single mismatch means a consolidation silently failed to apply.
- **`route-ids-for-agency` per-agency counts unchanged** from §2 baseline.
- **At least one consolidated stop** returns arrivals from at least two
  distinct `agencyId` values in the `references.agencies` block.

---

## 8. Edge cases, gotchas, observability

### 8.1 No positional `:agencyId` for the main builder

Restating because it's the easiest mistake to make: the verification
tool's `path:agencyId` argument syntax is **not** wired into
`FederatedTransitDataBundleCreatorMain`. Use the XML-driven form for the
actual build. The Main class's parsing is at lines 124-139 of
`FederatedTransitDataBundleCreatorMain.java` — single-pass over args,
zips and dirs become anonymous bundles, everything else becomes a
context path. No `:` parsing.

### 8.2 What lives in the rejection store?

`StoreImpl.saveEntity` (`GtfsMultiReaderImpl.java:179-194`) diverts
absorbed entities into `_rejectionStore`, which is a separate
in-memory DAO created by the multi-reader. It is **not** queryable
through the normal API — its purpose is to give the optional
`EntityReplacementLogger` something to consult when generating the
replacement log CSV.

If you want a record of what was absorbed, wire in the optional
`MultiCSVLogger` and `entityReplacementLogger` beans
(`GtfsReadingSupport.java:112-120`). The `gtfs_stop_replacements.csv`
they produce (see §6.2.2) is the auditable artifact for each build.

### 8.3 Routes/trips with `agency_id` collisions across feeds

If two feeds both contain `agency_id=1` (rare but real), use
`agencyIdMappings` on at least one feed to rewrite the colliding id to
something distinct. The mapping applies to *every* reference to that
agency_id inside that feed (the agency row itself, every route's
`agency_id`, every trip's transitive agency, etc.). After mapping the
two feeds emit non-colliding entities.

### 8.4 Stops with the same id across feeds but at different physical locations

The reverse of the consolidation case. If both `mts.zip` and `ucsd.zip`
contain a stop with `stop_id=1001` but they're physically different
stops, per-agency `defaultAgencyId` already handles it cleanly — they
become `AgencyAndId("MTS", "1001")` and `AgencyAndId("UCSD", "1001")`,
which are distinct keys. No special config needed. This is precisely
why per-agency loading is the right primitive.

### 8.5 Shared stops where each agency has a slightly different lat/lon

The canonical stop's lat/lon wins (§5.5). Practical guidance: pick the
canonical by curb ownership, audit canonical lat/lons before publishing
the bundle. The verification utility doesn't check coordinates.

### 8.6 Adding a new agency later

The model scales: add another `<bean class="…GtfsBundle">` to the
`bundles` list and (if needed) entries to the consolidation file. No
schema migration, no code changes.

### 8.7 What if you keep one shared upstream "merged" zip after all?

Possible but lossy. With one merged zip, you must accept that all stops
get the merged feed's primary `agency.txt` row as their agency id.
You'd still get per-agency *routes* correctly, but per-agency *stops*
would still be flattened — i.e., you'd be back where you started. The
whole point of switching is to load the four agencies' stops under
their own agency ids; that requires four separate `GtfsReader`
instances, which requires four separate inputs.

### 8.8 Conflicting consolidation lines (two canonicals claiming one absorbed)

`EntityReplacementStrategyFactory.java:100-101` calls
`impl.addEntityReplacement(entityClass, ids.get(i), ids.get(0))` once
per absorbed token per line, with no collision detection. If two lines
both list `UCSD_gtsg-1` as absorbed:

```
MTS_60123   UCSD_gtsg-1
NCTD_456    UCSD_gtsg-1
```

…the second line's `addEntityReplacement` call silently overwrites the
first in the underlying map. The build does not warn. The behavior is
order-dependent on the file.

If your candidate-detection script (§7.2) can produce this case (it
can, if a UCSD stop is within 15m of *both* an MTS stop and an NCTD
stop), add a pre-flight check to your pipeline: every absorbed id
appears in `tokens[1..]` across the whole file exactly once.

### 8.9 Build-time: feed download failures

The replacement `build_bundle.sh` in §6.4 uses `set -euo pipefail`,
which aborts the whole build on any `wget` non-zero exit. Don't loosen
this. A partial bundle (3 of 4 agencies present) appears to succeed but
will silently drop entire agencies from production. Loud failure here
is correct.

If you need a degraded-mode build (e.g., one upstream feed is reliably
flaky), the right approach is an exponential-backoff retry around each
`wget`, *not* an `|| true`.

### 8.10 Build-time: missing canonical in consolidation file

The verification utility (§7.4) catches the case where a canonical id
listed in the consolidation file no longer exists in any loaded feed.
The §6.4 script gates the build on the utility's output being empty.

If you bypass the gate, the main build will still emit a `_log.warn`
for each unresolved replacement (see the `_log.warn(...)` line in the
`StoreImpl.getEntityForId` snippet in §5.3) — but those go to the
builder log, not to standard output, and are easy to miss. Keep the
gate.

### 8.11 Absorbed-id disappearance from upstream feed

The reverse: a stop on the absorbed side of a consolidation
(`UCSD_gtsg-1` in `MTS_60123 UCSD_gtsg-1`) disappears from
`ucsd.zip` because UCSD restructured their feed. The verification
utility does **not** flag this (it only checks `tokens[0]`).

What happens: the consolidation line is a silent no-op for that pair.
The build succeeds, but no UCSD shuttle data is attached to
`MTS_60123` anymore. The §7.6 "arrivals from both agencies" assertion
catches this at smoke-test time. The cleaner safeguard is an
additional CI check: parse the consolidation file, for each absorbed
id check that it appears in the corresponding agency's `stops.txt`,
warn (don't fail — these vanish legitimately too) if absent.

---

## Annex A — Sample stop-consolidation file

A starter file for SDMTS. Use as a template for the header and
comment style. Real entries to be sourced from the §7.2 candidate scan.

```text
# SDMTS multi-agency stop consolidation file
# ------------------------------------------
# Format: <canonical_agency_and_id>  <absorbed_agency_and_id>  [...]
# - First token on a line is the canonical (kept) stop entity.
# - All subsequent whitespace-separated tokens are absorbed into the canonical.
# - Lines starting with #, {{{, or }}} are ignored.
# - The canonical's lat/lon survives; the absorbed entities' lat/lons are discarded.
#
# Source bundles this file was derived from:
#   MTS  : <feed-version-id>  fetched <date>
#   NCTD : <feed-version-id>  fetched <date>
#   SAN  : <feed-version-id>  fetched <date>
#   UCSD : <feed-version-id>  fetched <date>
#
# Last reviewed: <date>
# Maintainer:    aaron@onebusaway.org

# --- UCSD <-> MTS shared curbs on Gilman Dr / La Jolla Village Dr ---
# MTS_60123  UCSD_gtsg-1            # Gilman Dr & Gilman Loop
# MTS_60125  UCSD_gtsg-3            # Gilman Dr & Russell Ln

# --- NCTD <-> MTS shared transit centers ---
# MTS_70114  NCTD_OSTC_bay1         # Oceanside Transit Center, bay 1
# MTS_70114  NCTD_OSTC_bay2         # Oceanside Transit Center, bay 2  (chained absorb)

# --- SAN <-> MTS shared stops (none expected — SAN owns only airport curbs) ---
```

The lines are commented out because the real ids belong to the
candidate-scan output, not to a doc template. Replace `#` after the
section headers with real entries once the scan completes.

For format reference at scale, the historical Puget Sound consolidation
file (the original deployment that drove this mechanism) was at
`https://raw.github.com/wiki/camsys/onebusaway-application-modules/PugetSoundStopConsolidation.md`
— that URL is what the commented-out hint in
`onebusaway-admin-webapp/src/main/resources/org/onebusaway/transit_data_federation/bundle/application-context-bundle-admin.xml`
points at. The wiki may have moved; use it for shape, not as a live
dependency.

---

## 9. Source reference index

Every behavior described in this document is grounded in one of the
files below. Use these as the starting points for any further
investigation:

| Subject | File:lines |
|---|---|
| Single-positional-arg main builder grammar | `onebusaway-transit-data-federation-builder/src/main/java/org/onebusaway/transit_data_federation/bundle/FederatedTransitDataBundleCreatorMain.java:124-139` |
| `GtfsBundle` config schema | `onebusaway-transit-data-federation-builder/src/main/java/org/onebusaway/transit_data_federation/bundle/model/GtfsBundle.java:35-108` |
| Per-feed `GtfsReader` setup loop | `onebusaway-transit-data-federation-builder/src/main/java/org/onebusaway/transit_data_federation/bundle/tasks/GtfsReadingSupport.java:102-144` |
| Optional `entityReplacementStrategy` bean pickup | `onebusaway-transit-data-federation-builder/src/main/java/org/onebusaway/transit_data_federation/bundle/tasks/GtfsReadingSupport.java:109-121` |
| Class-major, feed-minor load order | `onebusaway-transit-data-federation-builder/src/main/java/org/onebusaway/transit_data_federation/bundle/tasks/GtfsMultiReaderImpl.java:92-142` |
| `EntityReplacementStrategy` interface | `onebusaway-transit-data-federation-builder/src/main/java/org/onebusaway/transit_data_federation/bundle/services/EntityReplacementStrategy.java:23-74` |
| `StoreImpl` save+lookup interception | `onebusaway-transit-data-federation-builder/src/main/java/org/onebusaway/transit_data_federation/bundle/tasks/GtfsMultiReaderImpl.java:152-195` |
| Consolidation file parser | `onebusaway-transit-data-federation-builder/src/main/java/org/onebusaway/transit_data_federation/bundle/tasks/EntityReplacementStrategyFactory.java:43-105` |
| `path:agencyId` arg parser (verification tool only) | `onebusaway-transit-data-federation-builder/src/main/java/org/onebusaway/transit_data_federation/bundle/utilities/UtilityLibrary.java:29-72` |
| Verification utility entry point | `onebusaway-transit-data-federation-builder/src/main/java/org/onebusaway/transit_data_federation/bundle/utilities/GtfsStopReplacementVerificationMain.java:46-95` |
| Example commented-out wiring (admin webapp) | `onebusaway-admin-webapp/src/main/resources/org/onebusaway/transit_data_federation/bundle/application-context-bundle-admin.xml` |
| Singular `gtfs-bundle` fallback bean (unreachable — see §6.1) | `onebusaway-transit-data-federation-builder/src/main/java/org/onebusaway/transit_data_federation/bundle/tasks/GtfsReadingSupport.java:153-168` |
| Programmatic beans override XML beans (`createContext`) | `onebusaway-container/src/main/java/org/onebusaway/container/ContainerLibrary.java:87-110` |
| Candidate-detection reference impl | `onebusaway-transit-data-federation-builder/src/main/java/org/onebusaway/transit_data_federation/bundle/tasks/StopConsolidationSuggestionsTask.java` |
| `MultiCSVLogger` (`basePath` setter) | `onebusaway-transit-data-federation-builder/src/main/java/org/onebusaway/transit_data_federation/bundle/tasks/MultiCSVLogger.java:53` |
| `EntityReplacementLoggerImpl` | `onebusaway-transit-data-federation-builder/src/main/java/org/onebusaway/transit_data_federation/bundle/tasks/EntityReplacementLoggerImpl.java` |
| Current single-zip build invocation | `docker_app_server/bundle_builder/build_bundle.sh:30-57` |
| `cd /bundle` working-directory requirement (with rationale comment) | `docker_app_server/bundle_builder/build_bundle.sh:51-52` |
