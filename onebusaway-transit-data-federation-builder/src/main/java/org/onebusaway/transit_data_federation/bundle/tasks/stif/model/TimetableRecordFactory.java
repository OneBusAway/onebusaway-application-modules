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

abstract class TimetableFieldSetter extends StifFieldSetter<TimetableRecord> {}
public class TimetableRecordFactory extends StifRecordFactory<TimetableRecord> {
	static class FieldDef extends StifFieldDefinition <TimetableRecord>{
		public FieldDef(int length, String name,
				StifFieldSetter<TimetableRecord> setter) {
			super(length, name, setter);
		}};

	@SuppressWarnings("rawtypes")
	private static StifFieldDefinition[] fields = {
		new FieldDef(3-1, "record type", null),
		new FieldDef(7-3, "depot code", null),
		new FieldDef(9-7, "borough code", null),
		new FieldDef(15-9, "route identifier", new TimetableFieldSetter() {
			public void setField(TimetableRecord record) {record.setRouteIdentifier(getStringData());}}),
		new FieldDef(17-15, "service code", new TimetableFieldSetter() {
			public void setField(TimetableRecord record) {record.setServiceCode(ServiceCode.getServiceCodeForId(getStringData()));}}),
		new FieldDef(41-17, "depot description", null),
		new FieldDef(65-41, "route description", null),
		new FieldDef(73-65, "schedule number", null),
		new FieldDef(77-73, "version number", null),
		new FieldDef(78-77, "empty", null),
		new FieldDef(79-78, "stif type code", null),
		new FieldDef(80-79, "empty", null),
    new FieldDef(82 - 80, "organization", new TimetableFieldSetter() {
        public void setField(TimetableRecord record) {
          String org = getStringData();
          if ("TA".equals(org) || "OA".equals(org)) {
            record.setAgencyId("MTA NYCT");
          } else if ("MB".equals(org) || "BC".equals(org)) {
            record.setAgencyId("MTABC");
          } else {
            record.setAgencyId(org);
          }
        }
      }), 
    new FieldDef(84 - 82, "empty", null),
		new FieldDef(92-84, "generation date", null),
		new FieldDef(93-92, "empty", null),
		new FieldDef(95-93, "additional depot code 1", null),
		new FieldDef(96-95, "empty", null),
		new FieldDef(98-96, "additional depot code 2", null),
		new FieldDef(99-98, "empty", null),
		new FieldDef(101-99, "additional depot code 3", null),
		new FieldDef(102-101, "empty", null),
		new FieldDef(104-102, "additional depot code 4", null),
		new FieldDef(105-104, "empty", null),
		new FieldDef(111-105, "additional schedule number 1", null),
		new FieldDef(112-111, "empty", null),
		new FieldDef(118-112, "additional schedule number 2", null),
		new FieldDef(119-118, "empty", null),
		new FieldDef(125-119, "additional schedule number 3", null),
		new FieldDef(126-125, "empty", null),
		new FieldDef(132-126, "additional schedule number 4", null),
		new FieldDef(146-132, "(fields skipped)", null),
		new FieldDef(154-146, "holiday code", new TimetableFieldSetter() {
      public void setField(TimetableRecord record) {
        if (record.getServiceCode() == null || !record.getServiceCode().isHoliday()) {
          String data = getStringData();
          if (data != null && data.length() > 0) {
            record.setServiceCode(ServiceCode.getServiceCodeForId(data));
          }
        }
      }
      })
	};

	@Override
	public TimetableRecord createEmptyRecord() {
		return new TimetableRecord();
	}

	@SuppressWarnings("unchecked")
	@Override
	public StifFieldDefinition<TimetableRecord>[] getFields() {
		return fields;
	}

}
