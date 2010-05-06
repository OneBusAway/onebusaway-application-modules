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

DELIMITER |
DROP FUNCTION IF EXISTS RADIAL_MOD|
CREATE FUNCTION RADIAL_MOD (a DOUBLE, b DOUBLE)
  RETURNS DOUBLE
  DETERMINISTIC
  BEGIN
    DECLARE m DOUBLE;
    SET m = IF(a*2>b,b-a,a);
    RETURN m;
END|
DELIMITER ;

CREATE INDEX fromTimepointSequence ON transit_stop_timepoint_interpolations (fromTimepointSequence);
CREATE INDEX toTimepointSequence ON transit_stop_timepoint_interpolations (toTimepointSequence);
CREATE INDEX stopTimePosition ON transit_stop_times (stopTimePosition);
CREATE INDEX scheduleTypeIndex ON transit_trips (scheduleType);
CREATE INDEX serviceTypeIndex ON transit_service_patterns (serviceType);
CREATE INDEX patternTimepointPosition ON transit_pattern_timepoints (patternTimepointPosition);