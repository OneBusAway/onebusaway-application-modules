/**
 * Copyright (C) 2017 Cambridge Systematics, Inc.
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
package org.onebusaway.rss.impl;

import com.rometools.rome.feed.module.Module;
import com.rometools.rome.io.ModuleGenerator;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.rss.model.AffectsClauseRssBean;
import org.onebusaway.rss.model.IServiceAlert;
import org.onebusaway.rss.model.TimeRangeRssBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Translate IServiceAlert to RSS via ROME plugin.
 */
public class ServiceAlertRssModuleGenerator  implements ModuleGenerator {

    private static Logger _log = LoggerFactory.getLogger(ServiceAlertRssModuleGenerator.class);

    private static final Namespace SERVICE_ALERT_NS = Namespace.getNamespace("oba",
            IServiceAlert.URI);

    @Override
    public String getNamespaceUri() {
        return IServiceAlert.URI;
    }

    private static final Set NAMESPACES;

    static {
        Set nss = new HashSet();
        nss.add(SERVICE_ALERT_NS);
        NAMESPACES = Collections.unmodifiableSet(nss);
    }

    @Override
    public Set<Namespace> getNamespaces() {
        return NAMESPACES;
    }

    @Override
    public void generate(Module module, Element element) {
        // this is not necessary, it is done to avoid the namespace definition in every item.
        Element root = element;
        while (root.getParent()!=null && root.getParent() instanceof Element) {
            root = (Element) element.getParent();
        }
        root.addNamespaceDeclaration(SERVICE_ALERT_NS);
        Element sas = null;

        Element channel = getElement(root,"channel");
        if (channel == null) {
            throw new RuntimeException("missing channel from element=" + root);
        }
        sas = getElement(channel, "ServiceAlerts");
        if (sas == null) {
            sas = generateSimpleElement("ServiceAlerts", "");
            sas.setName("ServiceAlerts");
            channel.addContent(sas);
        }

        if (module instanceof IServiceAlert) {
            IServiceAlert isa = (IServiceAlert) module;
            if (isa.getId() != null) {
                Element sa = generateSimpleElement("ServiceAlert", "");
                sa.setName("ServiceAlert");
                sa.addContent(generateSimpleElement("id", isa.getId()));
                sa.addContent(generateSimpleElement("summary", isa.getSummary()));
                sa.addContent(generateSimpleElement("description", isa.getDescription()));
                sa.addContent(generateSimpleElement("reason", isa.getReason()));
                sa.addContent(generateSimpleElement("severity", isa.getSeverity()));
                if (isa.getPublicationWindows() != null && !isa.getPublicationWindows().isEmpty()) {
                    Element publicationWindows = generateSimpleElement("PublicationWindows", "");
                    publicationWindows.setName("PublicationWindows");
                    sa.addContent(publicationWindows);
                    for (TimeRangeRssBean trb : isa.getPublicationWindows()) {
                        Element timeRange = generateSimpleElement("TimeRange", "");
                        timeRange.addContent(generateSimpleElement("from", ""+trb.getFrom()));
                        timeRange.addContent(generateSimpleElement("to", ""+trb.getTo()));
                        publicationWindows.addContent(timeRange);
                    }
                }
                if (isa.getAffectsClauses() != null && !isa.getAffectsClauses().isEmpty()) {
                    Element affectsClauses = generateSimpleElement("AffectsClauses", "");
                    affectsClauses.setName("AffectsClauses");
                    sa.addContent(affectsClauses);
                    for (AffectsClauseRssBean acrb : isa.getAffectsClauses()) {
                        Element affect = generateSimpleElement("Affect", "");
                        if (acrb.getAgencyId() != null
                                && acrb.getRouteId() == null
                                && acrb.getStopId() == null
                                && acrb.getTripId() == null)
                            affect.addContent(generateAgencyElement("agencyId", acrb.getAgencyId()));
                        if (acrb.getRouteId() != null)
                            affect.addContent(generateAgencyElement("routeId", acrb.getRouteId()));
                        if (acrb.getStopId() != null)
                            affect.addContent(generateAgencyElement("stopId", acrb.getStopId()));
                        if (acrb.getTripId() != null)
                            affect.addContent(generateAgencyElement("tripId", acrb.getTripId()));
                        affectsClauses.addContent(affect);
                    }
                }

                sas.addContent(sa);

            }
        } else {
            _log.error("unknown type=" + module);
        }

    }

    private Element getElement(Element root, String name) {
        if (root == null || name.equals(root.getName())) {
            return root;
        }
        List<Element> elements = root.getChildren();
        for (Element e : elements) {
            // recurse
            Element found = getElement(e, name);
            if (found != null)
                return found;
        }
        return null;
    }

    protected Element generateSimpleElement(String name, String value)  {
        Element element = new Element(name, SERVICE_ALERT_NS);
        element.addContent(value);
        return element;
    }

    protected Element generateAgencyElement(String key, String agencyId)  {
        Element agency = new Element("Agency", SERVICE_ALERT_NS);
        agency.setName("Agency");
        try {
            AgencyAndId agencyAndId = AgencyAndId.convertFromString(agencyId);
            agency.addContent(generateSimpleElement("agency", agencyAndId.getAgencyId()));
            agency.addContent(generateSimpleElement(key, agencyAndId.getId()));
            return agency;
        } catch (Exception e) {
            agency.addContent(generateSimpleElement(key, agencyId));
            return agency;
        }
    }

}
