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

abstract class GeographyFieldSetter extends StifFieldSetter<GeographyRecord> {
}

public class GeographyRecordFactory extends StifRecordFactory<GeographyRecord> {
  static class FieldDef extends StifFieldDefinition<GeographyRecord> {
    public FieldDef(int length, String name,
        StifFieldSetter<GeographyRecord> setter) {
      super(length, name, setter);
    }
  };

  @SuppressWarnings("rawtypes")
  private static StifFieldDefinition[] fields;

  static {
    fields = new StifFieldDefinition[] {
        new FieldDef(3 - 1, "record type", null),
        new FieldDef(7 - 3, "identifier", new GeographyFieldSetter() {
          public void setField(GeographyRecord record) {
            record.setIdentifier(getStringData());
          }
        }), new FieldDef(29 - 7, "location description", null),
        new FieldDef(51 - 29, "intersection description", null),
        new FieldDef(63 - 51, "geocode", null),
        new FieldDef(65 - 63, "borough code", null),
        new FieldDef(66 - 65, "empty", null),
        new FieldDef(74 - 66, "timepoint identifier", null),
        new FieldDef(75 - 74, "empty", null),
        new FieldDef(77 - 75, "location type", null),
        new FieldDef(78 - 77, "empty", null),
        new FieldDef(88 - 78, "latitude", new GeographyFieldSetter() {
          public void setField(GeographyRecord record) {
            record.setLatitude(getDecimalFixedPoint(2));
          }
        }), new FieldDef(98 - 88, "latitude", new GeographyFieldSetter() {
          public void setField(GeographyRecord record) {
            record.setLongitude(getDecimalFixedPoint(2));
          }
        }), new FieldDef(99 - 98, "empty", null),
        new FieldDef(105 - 99, "box id", new GeographyFieldSetter() {
          public void setField(GeographyRecord record) {
            record.setBoxID(getStringData());
          }
        }),

    };
  }

  @Override
  public GeographyRecord createEmptyRecord() {
    return new GeographyRecord();
  }

  @SuppressWarnings("unchecked")
  @Override
  public StifFieldDefinition<GeographyRecord>[] getFields() {
    return fields;
  }

}
