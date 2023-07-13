/**
 * Copyright (C) 2023 Cambridge Systematics, Inc.
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
package org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.transit.realtime.GtfsRealtime;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

/**
 * Just enough support for JSON-based GTFS-RT for integration test support.
 * Not production ready.
 */
public class GtfsRtBuilder {
  private ObjectMapper _mapper = new ObjectMapper();
  public GtfsRealtime.FeedMessage readJson(URL url) throws URISyntaxException, IOException {
    GtfsRealtime.FeedMessage.Builder feedMessage = GtfsRealtime.FeedMessage.newBuilder();
    Path jsonPath = Paths.get(url.toURI());
    BufferedReader jsonReader = Files.newBufferedReader(jsonPath);
    JsonParser parser = _mapper.createParser(jsonReader);
    TreeNode treeNode = parser.readValueAsTree();

    feedMessage.setHeader(buildHeader(treeNode.get("header")));
    ArrayNode list = (ArrayNode)treeNode.get("entity");
    Iterator<JsonNode> entities = list.elements();
    while (entities.hasNext()) {
      ObjectNode node = (ObjectNode) entities.next();
      if (node.has("trip_update")) {
        GtfsRealtime.FeedEntity.Builder entity = buildEntity(node);
        if (entity != null) {
          feedMessage.addEntity(entity);
        }

      } else {
        throw new UnsupportedOperationException("missing support for this node=" + node);
      }
    }
    return feedMessage.build();
  }

  private GtfsRealtime.FeedEntity.Builder buildEntity(ObjectNode node) {
    
    GtfsRealtime.FeedEntity.Builder entity = GtfsRealtime.FeedEntity.newBuilder();
    entity.setId(node.get("id").asText());
    if (node.has("trip_update")) {
      entity.setTripUpdate(buildTripUpdate(node.get("trip_update")));
    }
    return entity;
  }

  private GtfsRealtime.TripUpdate.Builder buildTripUpdate(JsonNode node) {
    GtfsRealtime.TripUpdate.Builder tu = GtfsRealtime.TripUpdate.newBuilder();
    if (node.has("trip")) {
      tu.setTrip(buildTripDescriptor(node.get("trip")));
    }
    if (node.has("vehicle")) {
      tu.setVehicle(buildVehicleDescriptor(node.get("vehicle")));
    }
    if (node.has("stop_time_update")) {
      Iterator<JsonNode> updates = node.get("stop_time_update").elements();
      while (updates.hasNext()) {

        tu.addStopTimeUpdate(buildStopTimeUpdate(updates.next()));
      }
    }
    return tu;
  }

  private GtfsRealtime.TripUpdate.StopTimeUpdate.Builder buildStopTimeUpdate(JsonNode node) {
    GtfsRealtime.TripUpdate.StopTimeUpdate.Builder stu = GtfsRealtime.TripUpdate.StopTimeUpdate.newBuilder();
    if (node.has("stop_sequence"))
      stu.setStopSequence(node.get("stop_sequence").asInt());
    if (node.has("stop_id"))
      stu.setStopId(node.get("stop_id").asText());
    if (node.has("arrival"))
      stu.setArrival(buildAD(node.get("arrival")));
    if (node.has("departure"))
      stu.setDeparture(buildAD(node.get("departure")));

    return stu;
  }

  private GtfsRealtime.TripUpdate.StopTimeEvent.Builder buildAD(JsonNode node) {
    GtfsRealtime.TripUpdate.StopTimeEvent.Builder event = GtfsRealtime.TripUpdate.StopTimeEvent.newBuilder();
    if (node.has("delay"))
      event.setDelay(node.get("delay").asInt());
    if (node.has("time"))
      event.setTime(node.get("time").asInt());
    return event;
  }

  private GtfsRealtime.VehicleDescriptor.Builder buildVehicleDescriptor(JsonNode node) {
    GtfsRealtime.VehicleDescriptor.Builder v = GtfsRealtime.VehicleDescriptor.newBuilder();
    if (node.has("id"))
      v.setId(node.get("id").asText());
    if (node.has("label"))
      v.setLabel(node.get("label").asText());
    return v;
  }

  private GtfsRealtime.TripDescriptor.Builder buildTripDescriptor(JsonNode node) {
    GtfsRealtime.TripDescriptor.Builder td = GtfsRealtime.TripDescriptor.newBuilder();
    if (node.has("trip_id"))
      td.setTripId(node.get("trip_id").asText());
    if (node.has("route_id"))
      td.setRouteId(node.get("route_id").asText());
    if (node.has("direction_id"))
      td.setDirectionId(node.get("direction_id").asInt());
    if (node.has("start_time"))
      td.setStartTime(node.get("start_time").asText());
    if (node.has("start_date"))
      td.setStartTime(node.get("start_date").asText());
    td.setScheduleRelationship(GtfsRealtime.TripDescriptor.ScheduleRelationship.valueOf(node.get("schedule_relationship").asText()));
    return td;
  }

  private GtfsRealtime.FeedHeader buildHeader(TreeNode json) {
    GtfsRealtime.FeedHeader.Builder header = GtfsRealtime.FeedHeader.newBuilder();
    TextNode node = (TextNode) json.get("gtfs_realtime_version");
    header.setGtfsRealtimeVersion(node.asText());
    IntNode inode = (IntNode) json.get("timestamp");
    header.setTimestamp(inode.asLong());
    return header.build();
  }
}
