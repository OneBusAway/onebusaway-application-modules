/**
 * Copyright (C) 2016 Cambridge Systematics, Inc.
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
package org.onebusaway.transit_data_federation.siri;

import java.io.StringWriter;
import java.io.Writer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.org.siri.siri_2.Siri;

/** 
 * Serializer for XSD-generated SIRI classes, creating XML in the format suitable
 * for Bus Time front-ends and third-party apps.
 * 
 * @author jmaki
 *
 */
public class SiriXmlSerializerV2 {

  private JAXBContext context = null;
  private static Logger _log = LoggerFactory.getLogger(SiriXmlSerializerV2.class);

  public SiriXmlSerializerV2() {
    try {
      context = JAXBContext.newInstance(
          Siri.class,
          SiriExtensionWrapper.class, 
          SiriDistanceExtension.class, 
          SiriUpcomingServiceExtension.class,
          SiriPolyLinesExtension.class);
    } catch(Exception e) {
      _log.error("Failed to Serialize Siri to XML", e);
    }
  }

  public String getXml(Siri siri) throws Exception {    
    Marshaller marshaller = context.createMarshaller();
    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);
    marshaller.setEventHandler(
        new ValidationEventHandler() {
            public boolean handleEvent(ValidationEvent event ) {
              _log.error(event.getMessage(), event.getLinkedException());
                throw new RuntimeException(event.getMessage(), event.getLinkedException());
            }
        }
    );
    
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);

    Writer output = new StringWriter();
    marshaller.marshal(siri, output);

    // FIXME: strip off ns6 namespaces on siri root namespace. super hack, please fix me!
    String outputAsString = output.toString();   

    /*outputAsString = outputAsString.replaceAll("<ns6:", "<");
    outputAsString = outputAsString.replaceAll("</ns6:", "</");
    outputAsString = outputAsString.replaceAll("xmlns:ns6", "xmlns");
*/

    String[] searchList = {
      "<siriExtensionWrapper>", 
      "</siriExtensionWrapper>",
      "<siriUpcomingServiceExtension>",
      "</siriUpcomingServiceExtension>",
      "<siriPolyLinesExtension>",
      "</siriPolyLinesExtension>"
  };
    
    String[] replacementList = {"","","","","",""};
    
    outputAsString = StringUtils.replaceEach(outputAsString, searchList, replacementList);

    return outputAsString;
  }
  /*
  public Siri fromXml(String xml) throws JAXBException {
    Unmarshaller u = context.createUnmarshaller();
    Siri siri = (Siri) u.unmarshal(new StringReader(xml));
    
    return siri;
  }
  */
}
