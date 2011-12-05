/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2011 Google, Inc.
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
package org.onebusaway.transit_data_federation.impl.realtime.siri;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.onebusaway.transit_data_federation.services.service_alerts.ServiceAlerts.Affects;
import org.onebusaway.transit_data_federation.services.service_alerts.ServiceAlerts.ServiceAlert;
import org.onebusaway.transit_data_federation.services.service_alerts.ServiceAlerts.ServiceAlert.Builder;

import uk.org.siri.siri.AffectedVehicleJourneyStructure;
import uk.org.siri.siri.AffectsScopeStructure;
import uk.org.siri.siri.AffectsScopeStructure.Networks;
import uk.org.siri.siri.AffectsScopeStructure.Networks.AffectedNetwork;
import uk.org.siri.siri.AffectsScopeStructure.VehicleJourneys;
import uk.org.siri.siri.DirectionRefStructure;
import uk.org.siri.siri.LineRefStructure;
import uk.org.siri.siri.PtSituationElementStructure;

public class SiriServiceTest extends SiriService {

  @Test
  public void testHandleAffects() {
    PtSituationElementStructure p = new PtSituationElementStructure();
    
    AffectsScopeStructure ass = new AffectsScopeStructure();
    p.setAffects(ass );
    VehicleJourneys vj = new VehicleJourneys();
    ass.setVehicleJourneys(vj );
    List<AffectedVehicleJourneyStructure> list = vj.getAffectedVehicleJourney();
    AffectedVehicleJourneyStructure avjs = new AffectedVehicleJourneyStructure();
    list.add(avjs );
    LineRefStructure lrs = new LineRefStructure();
    avjs.setLineRef(lrs );
    lrs.setValue("MTA NYCT_B63");
    DirectionRefStructure drs = new DirectionRefStructure();
    avjs.setDirectionRef(drs );
    drs.setValue("N");
    
    Builder sa = ServiceAlert.newBuilder();
    handleAffects(p, sa);
    List<Affects> affectsList = sa.getAffectsList();
    assertTrue( affectsList != null);
    assertEquals(1, affectsList.size());
  }

}
