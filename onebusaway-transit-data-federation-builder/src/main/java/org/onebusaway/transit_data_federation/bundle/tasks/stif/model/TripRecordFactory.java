/**
 * Copyright (C) 2011 Metropolitan Transportation Authority
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
package org.onebusaway.transit_data_federation.bundle.tasks.stif.model;

abstract class TripFieldSetter extends StifFieldSetter<TripRecord> {
}

public class TripRecordFactory extends StifRecordFactory<TripRecord> {
  static class FieldDef extends StifFieldDefinition<TripRecord> {
    public FieldDef(int length, String name, StifFieldSetter<TripRecord> setter) {
      super(length, name, setter);
    }
  };

  @SuppressWarnings("rawtypes")
  private static StifFieldDefinition[] fields = {
      new FieldDef(3 - 1, "record type", null),
      new FieldDef(7 - 3, "origin location", new TripFieldSetter() {
        public void setField(TripRecord record) {
          record.setOriginLocation(getStringData());
        }
      }),
      new FieldDef(15 - 7, "origin time", new TripFieldSetter() {
        public void setField(TripRecord record) {
          record.setOriginTime(getTimeFromCentiminutesSafe());
        }
      }),
      new FieldDef(17 - 15, "direction", null),
      new FieldDef(19 - 17, "trip type", new TripFieldSetter() {
        public void setField(TripRecord record) {
          record.setTripType(getInteger());
        }
      }),
      new FieldDef(23 - 19, "destination location", new TripFieldSetter() {
        public void setField(TripRecord record) {
          record.setDestinationLocation(getStringData());
        }
      }),
      new FieldDef(31 - 23, "destination time", new TripFieldSetter() {
        public void setField(TripRecord record) {
          record.setDestinationTime(getTimeFromCentiminutesSafe());
        }
      }),
      new FieldDef(35 - 31, "pick code", null),
      new FieldDef(41 - 35, "primary run number", new TripFieldSetter() {
          public void setField(TripRecord record) {
              record.setRunNumber(getStringData());
            }
          }),
      new FieldDef(53 - 41, "path code", null),
      new FieldDef(59 - 53, "primary run route", new TripFieldSetter() {
        public void setField(TripRecord record) {
          record.setRunRoute(getStringDataUppercased());
        }
      }),
      new FieldDef(65 - 59, "relief run number", new TripFieldSetter() {
          public void setField(TripRecord record) {
              record.setReliefRunNumber(getStringData());
            }
          }),
      new FieldDef(71 - 65, "relief run route", new TripFieldSetter() {
        public void setField(TripRecord record) {
          record.setReliefRunRoute(getStringDataUppercased());
        }
      }),
      new FieldDef(79 - 71, "relief time", new TripFieldSetter() {
        public void setField(TripRecord record) {
          record.setReliefTime(getTimeFromCentiminutesSafe());
        }
      }),
      new FieldDef(83 - 79, "relief location", null),
      new FieldDef(84 - 83, "bus type code", null),
      new FieldDef(88 - 84, "sign code", new TripFieldSetter() {
        public void setField(TripRecord record) {
          record.setSignCode(getStringData());
        }
      }),
      new FieldDef(89 - 88, "empty", null),
      new FieldDef(90 - 89, "first trip in sequence",  new TripFieldSetter() {
        public void setField(TripRecord record) {
          record.setFirstTripInSequence(getBoolean());
        }
      }),
      new FieldDef(91 - 90, "empty", null),
      new FieldDef(92 - 91, "last trip in sequence",  new TripFieldSetter() {
        public void setField(TripRecord record) {
          record.setLastTripInSequence(getBoolean());
        }
      }),
      new FieldDef(93 - 92, "primary relief status", null),
      new FieldDef(99 - 93, "next operator run number", null),
      new FieldDef(100 - 99, "empty", null),
      new FieldDef(106 - 100, "next operator route", null),
      new FieldDef(107 - 106, "empty", null),
      new FieldDef(112 - 107, "trip mileage", null),
      new FieldDef(113 - 112, "empty", null),
      new FieldDef(115 - 113, "depot code",  new TripFieldSetter() {
        public void setField(TripRecord record) {
          record.setDepotCode(getStringData());
        }
      }),
      new FieldDef(116 - 115, "empty", null),
      new FieldDef(126 - 116, "block number", new TripFieldSetter() {
        public void setField(TripRecord record) {
          record.setBlockNumber(getStringData());
        }
      }),
      new FieldDef(127 - 126, "empty", null),
      new FieldDef(133 - 127, "next trip operator run number", new TripFieldSetter() {
        public void setField(TripRecord record) {
          record.setNextTripOperatorRunNumber(getStringData());
        }
      }),
      new FieldDef(134 - 133, "empty", null),
      new FieldDef(140 - 134, "next trip operator route", new TripFieldSetter() {
        public void setField(TripRecord record) {
          record.setNextTripOperatorRunRoute(getStringDataUppercased());
        }
      }),
      new FieldDef(141 - 140, "empty", null),
      new FieldDef(149 - 141, "next trip origin time", null),
      new FieldDef(150 - 149, "empty", null),
      new FieldDef(154 - 150, "recovery time after this trip", new TripFieldSetter() {
        public void setField(TripRecord record) {
          record.setRecoveryTime(getIntegerSafe());
        }
      }),
      new FieldDef(155 - 154, "empty", null),
      new FieldDef(161 - 155, "sign code route for this trip",
          new TripFieldSetter() {
            public void setField(TripRecord record) {
              record.setSignCodeRoute(getStringData());
            }
          }), new FieldDef(162 - 161, "empty", null),
      new FieldDef(168 - 162, "previous trip operator run number", new TripFieldSetter() {
        public void setField(TripRecord record) {
          record.setPreviousRunNumber(getStringData());
        }
      }),
      new FieldDef(169 - 168, "empty", null),
      new FieldDef(175 - 169, "previous trip operator route", new TripFieldSetter() {
        public void setField(TripRecord record) {
          record.setPreviousRunRoute(getStringDataUppercased());
        }
      }),
      new FieldDef(176 - 175, "empty", null),
      new FieldDef(183 - 176, "previous trip origin time", null),
      
      new FieldDef(212 - 183, "(many fields skipped)", null),
      new FieldDef(214 - 212, "next trip operator depot", new TripFieldSetter() {
        public void setField(TripRecord record) {
          record.setNextTripOperatorDepotCode(getStringData());
        }
      }),
      new FieldDef(218 - 214, "(many fields skipped)", null),
      new FieldDef(257 - 218, "gtfs trip ID", new TripFieldSetter() {
        public void setField(TripRecord record) {
          record.setGtfsTripId(getStringData());
        }
      }),
  };

  @Override
  public TripRecord createEmptyRecord() {
    return new TripRecord();
  }

  @SuppressWarnings("unchecked")
  @Override
  public StifFieldDefinition<TripRecord>[] getFields() {
    return fields;
  }

}
