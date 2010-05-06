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

DROP TABLE IF EXISTS phone_location_bookmarks;
CREATE TABLE phone_location_bookmarks (
 id INTEGER NOT NULL AUTO_INCREMENT,
 userId VARCHAR(40) NOT NULL,
 sequence INTEGER,
 locationType INTEGER,
 locationId INTEGER,
 locationLabel VARCHAR(120),
 PRIMARY KEY(id));
CREATE INDEX phone_location_bookmarks_id ON phone_location_bookmarks (id); 
CREATE INDEX phone_location_bookmarks_userId ON phone_location_bookmarks (userId); 