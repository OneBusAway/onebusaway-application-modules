/*
 * Copyright 2008 Brian Ferris
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.washington.cs.rse.transit.web.oba.iphone.client;

import edu.washington.cs.rse.transit.web.oba.common.client.AbstractApplication;
import edu.washington.cs.rse.transit.web.oba.iphone.client.pages.CustomStopByNumberPage;
import edu.washington.cs.rse.transit.web.oba.iphone.client.pages.ExceptionPage;
import edu.washington.cs.rse.transit.web.oba.iphone.client.pages.IndexPage;
import edu.washington.cs.rse.transit.web.oba.iphone.client.pages.StopIdentificationPage;
import edu.washington.cs.rse.transit.web.oba.iphone.client.pages.StopsByNumberPage;
import edu.washington.cs.rse.transit.web.oba.iphone.client.pages.StopsByRoutePage;
import edu.washington.cs.rse.transit.web.oba.iphone.client.pages.StopsPage;

public class OneBusAwayIPhoneApplication extends AbstractApplication {

  public OneBusAwayIPhoneApplication() {
    addPage("index", new IndexPage());
    addPage("stops", new StopsPage());
    addPage("stopsByNumber", new StopsByNumberPage());
    addPage("stopIdentification", new StopIdentificationPage());
    addPage("stopsByRoute", new StopsByRoutePage());
    addPage("stop", new CustomStopByNumberPage());

    setExceptionPage(new ExceptionPage());
  }
}
