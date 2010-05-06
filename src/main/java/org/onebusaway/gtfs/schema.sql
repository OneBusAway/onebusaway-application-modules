-- Copyright 2008 Brian Ferris
--
-- Licensed under the Apache License, Version 2.0 (the "License"); you may not
-- use this file except in compliance with the License. You may obtain a copy of
-- the License at
--
-- http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
-- WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
-- License for the specific language governing permissions and limitations under
-- the License.

CREATE INDEX gtfs_trips_serviceId ON gtfs_trips (serviceId);
CREATE INDEX gtfs_trips_directionId ON gtfs_trips (directionId);
CREATE INDEX gtfs_trips_blockId ON gtfs_trips (blockId);
CREATE INDEX gtfs_shape_points_distTraveled ON gtfs_shape_points (distTraveled);
CREATE INDEX gtfs_shape_points_shapeId ON gtfs_shape_points (shapeId);
CREATE SPATIAL INDEX gtfs_stops_location ON gtfs_stops (location);
