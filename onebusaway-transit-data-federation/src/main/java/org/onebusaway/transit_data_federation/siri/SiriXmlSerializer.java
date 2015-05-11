/**
 * Copyright (C) 2010 OpenPlans
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

import uk.org.siri.siri.Siri;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.parsers.DocumentBuilderFactory;

/** 
 * Serializer for XSD-generated SIRI classes, creating XML in the format suitable
 * for Bus Time front-ends and third-party apps.
 * 
 * @author jmaki
 *
 */
public class SiriXmlSerializer {

  private JAXBContext context = null;
  
  public SiriXmlSerializer() {
    try {
      context = JAXBContext.newInstance(uk.org.siri.siri.Siri.class, SiriExtensionWrapper.class, SiriDistanceExtension.class);
    } catch(Exception e) {
      // discard
    }
  }
  
  public String getXml(Siri siri) throws Exception {    
    Marshaller marshaller = context.createMarshaller();
    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);
    marshaller.setEventHandler(
        new ValidationEventHandler() {
            public boolean handleEvent(ValidationEvent event ) {
                throw new RuntimeException(event.getMessage(), event.getLinkedException());
            }
        }
    );
    
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);

    Writer output = new StringWriter();
    marshaller.marshal(siri, output);

    // FIXME: strip off ns5 namespaces on siri root namespace. super hack, please fix me!
    String outputAsString = output.toString();    
    outputAsString = outputAsString.replaceAll("<ns5:", "<");
    outputAsString = outputAsString.replaceAll("</ns5:", "</");
    outputAsString = outputAsString.replaceAll("xmlns:ns5", "xmlns");

    outputAsString = outputAsString.replaceAll("<siriExtensionWrapper>", "");
    outputAsString = outputAsString.replaceAll("</siriExtensionWrapper>", "");

    return outputAsString;
  }
  
  public Siri fromXml(String xml) throws JAXBException {
    Unmarshaller u = context.createUnmarshaller();
    Siri siri = (Siri) u.unmarshal(new StringReader(xml));
    
    return siri;
  }

}