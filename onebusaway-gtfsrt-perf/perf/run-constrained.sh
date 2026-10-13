#!/usr/bin/env bash
#
# Copyright (C) 2026 Open Transit Software Foundation
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#         http://www.apache.org/licenses/LICENSE-2.0
#
# Runs GtfsRtRefreshBenchmark inside a CPU/memory-constrained container that
# mirrors the production box from agency-dockerfiles/test-organization-1
# (1 vCPU / 2 GB), with one of the perf/logconfigs/ log4j2 configs active.
#
# The container mounts ~/.m2, the repo, and the experiment dir at their
# IDENTICAL host paths, so the host-generated classpath file resolves verbatim.
#
# Usage: run-constrained.sh <prod|cheap|quiet> <bundleRoot> <rtDir> [warmup] [measure]
set -euo pipefail

CFG="${1:?config: prod|cheap|quiet}"
BUNDLE_ROOT="${2:?bundleRoot}"
RT_DIR="${3:?rtDir}"
WARMUP="${4:-8}"
MEASURE="${5:-40}"

REPO="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd -P)"
M2="${HOME}/.m2"
CP_FILE="${CP_FILE:-/tmp/perf-cp.txt}"
IMAGE="${IMAGE:-eclipse-temurin:11-jdk}"
CPUS="${CPUS:-1}"
MEM="${MEM:-2g}"
XMX="${XMX:-1200m}"
# The production image (its Dockerfile's TZ env, in the separate
# agency-dockerfiles deploy repo) runs in America/Los_Angeles. GTFS service-date
# math (BlockFinder) is timezone-sensitive;
# the stock temurin image is UTC, which mis-resolves service dates and NPEs.
TZ_ENV="${TZ_ENV:-America/Los_Angeles}"
LOGCFG="${REPO}/onebusaway-gtfsrt-perf/perf/logconfigs/log4j2-${CFG}.xml"

[ -f "$LOGCFG" ] || { echo "no such log config: $LOGCFG" >&2; exit 2; }
[ -f "$CP_FILE" ] || { echo "missing classpath file $CP_FILE (run: mvn -pl onebusaway-gtfsrt-perf dependency:build-classpath -Dmdep.outputFile=$CP_FILE)" >&2; exit 2; }

# EXTRA_CP is prepended so freshly-compiled classes (e.g. a module's target/classes)
# win over the same artifact's jar in the resolved classpath -- lets a rebuilt
# module be measured without a slow `mvn install` (which also triggers gpg signing).
CP="${EXTRA_CP:+${EXTRA_CP}:}${REPO}/onebusaway-gtfsrt-perf/target/classes:$(cat "$CP_FILE")"
# The experiment dir lives under /private/tmp on macOS; mount it too.
EXP_MNT="$(cd "$BUNDLE_ROOT/.." && pwd -P)"

echo ">>> config=${CFG} cpus=${CPUS} mem=${MEM} xmx=${XMX} warmup=${WARMUP} measure=${MEASURE}"
docker run --rm \
  --cpus="${CPUS}" --memory="${MEM}" \
  -e TZ="${TZ_ENV}" \
  -v "${M2}:${M2}:ro" \
  -v "${REPO}:${REPO}:ro" \
  -v "${EXP_MNT}:${EXP_MNT}" \
  -w "${REPO}" \
  "${IMAGE}" \
  java -cp "${CP}" \
    -Xmx"${XMX}" \
    -Dperf.bundleRoot="${BUNDLE_ROOT}" \
    -Dperf.rtDir="${RT_DIR}" \
    -Dperf.agencyIds=1 \
    -Dperf.warmup="${WARMUP}" \
    -Dperf.measure="${MEASURE}" \
    -Dlog4j2.configurationFile="${LOGCFG}" \
    org.onebusaway.perf.gtfsrt.GtfsRtRefreshBenchmark
