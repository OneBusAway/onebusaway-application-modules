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
package edu.washington.cs.rse.transit.web.oba.standard.client;

import com.google.gwt.libideas.client.StyleInjector;

import edu.washington.cs.rse.transit.web.oba.common.client.AbstractApplication;
import edu.washington.cs.rse.transit.web.oba.standard.client.pages.CustomStopByNumberPage;
import edu.washington.cs.rse.transit.web.oba.standard.client.pages.ExceptionPage;
import edu.washington.cs.rse.transit.web.oba.standard.client.pages.IndexPage;
import edu.washington.cs.rse.transit.web.oba.standard.client.pages.RoutesPage;
import edu.washington.cs.rse.transit.web.oba.standard.client.pages.ServicePatternsPage;
import edu.washington.cs.rse.transit.web.oba.standard.client.resources.OneBusAwayStandardResources;

public class OneBusAwayStandardApplication extends AbstractApplication {

    public OneBusAwayStandardApplication() {

        addPage("index", new IndexPage());
        addPage("stop", new CustomStopByNumberPage());
        addPage("routes", new RoutesPage());
        addPage("service_patterns", new ServicePatternsPage());
        setExceptionPage(new ExceptionPage());

        StyleInjector.injectStylesheet(OneBusAwayStandardResources.INSTANCE.getCSS().getText());
    }
}
