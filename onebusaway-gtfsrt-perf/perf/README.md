# Constrained, production-faithful GTFS-RT poll reproduction

Reproduces the GTFS-RT poll/matching path under the same shape as the production
container in `agency-dockerfiles/test-organization-1/Dockerfile` (multi-agency
Puget Sound bundle, live King County Metro feed, production `[%F:%L]` log config,
1 vCPU / 2 GB, `America/Los_Angeles`). See the write-up and results in
`docs/perf/2026-07-10-gtfsrt-matching-findings.md` (Addendum).

## What's here

- `logconfigs/log4j2-prod.xml` — prod-faithful: `Root=info` + the `[%F:%L]`
  location converter (mirrors `agency-dockerfiles/config/tdf-log4j2.xml`).
- `logconfigs/log4j2-cheap.xml` — same, but the layout drops `[%F:%L]`.
- `logconfigs/log4j2-quiet.xml` — the three flooding loggers muted to `OFF`.
- `run-constrained.sh` — runs `GtfsRtRefreshBenchmark` inside
  `docker run --cpus=1 --memory=2g` with one of the configs. Mounts `~/.m2`, the
  repo, and the experiment dir at their identical host paths so a host-generated
  classpath resolves verbatim, and sets `TZ=America/Los_Angeles`.

## Reproduce

```bash
# 0. one-time: build the module + capture its runtime classpath
mvn -q -pl onebusaway-gtfsrt-perf package -DskipTests
mvn -q -pl onebusaway-gtfsrt-perf dependency:build-classpath -Dmdep.outputFile=/tmp/perf-cp.txt

# 1. download the 5 rule-set-19 build-56 feeds into <agencyId>/<feed>.zip layout
#    (agency id = parent dir name; the convention builder reads it)
EXP=/tmp/gtfsrt-perf-exp
base=https://gtfs-bundles-development.onebusawaycloud.com/rule-set-19-feeds
mkdir -p "$EXP"/input/{1,40,20,97,96} "$EXP"/rt
curl -sS -o "$EXP/input/1/metro.zip"          "$base/metro.zip"
curl -sS -o "$EXP/input/40/sound_transit.zip" "$base/sound_transit.zip"
curl -sS -o "$EXP/input/20/kitsap.zip"        "$base/kitsap.zip"
curl -sS -o "$EXP/input/97/everett.zip"       "$base/everett.zip"
curl -sS -o "$EXP/input/96/monorail.zip"      "$base/monorail.zip"

# 2. download the live KCM RT feeds (staleness vs build-56 is what makes the flood)
for f in tripupdates vehiclepositions alerts; do
  curl -sS -o "$EXP/rt/$f.pb" "https://s3.amazonaws.com/kcm-alerts-realtime-prod/$f.pb"
done

# 3. build the multi-agency bundle (convention builder refuses a pre-existing out dir)
CP="onebusaway-gtfsrt-perf/target/classes:$(cat /tmp/perf-cp.txt)"
rm -rf "$EXP/out"
java -cp "$CP" -Xmx4g org.onebusaway.perf.gtfsrt.BuildKcmBundle "$EXP/input" "$EXP/out" pugetsound

# 4. run the constrained A/B/C matrix
export CP_FILE=/tmp/perf-cp.txt
for cfg in prod cheap quiet; do
  ./onebusaway-gtfsrt-perf/perf/run-constrained.sh "$cfg" "$EXP/out" "$EXP/rt" 8 30
done
```

`BASELINE …` lines report mean/p50/p95 ms per refresh. Override the container
shape with `CPUS=`, `MEM=`, `XMX=`, `TZ_ENV=`, `IMAGE=` env vars.
