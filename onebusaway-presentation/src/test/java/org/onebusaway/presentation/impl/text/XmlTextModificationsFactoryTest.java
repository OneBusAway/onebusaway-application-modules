/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.presentation.impl.text;

import static org.junit.Assert.assertEquals;

import org.onebusaway.presentation.services.text.TextModification;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.xml.sax.SAXException;

import java.io.IOException;

public class XmlTextModificationsFactoryTest {

  @Test
  public void go() throws IOException, SAXException {
    ClassPathResource resource = new ClassPathResource("org/onebusaway/presentation/impl/text/text-modifications.xml");

    XmlTextModificationsFactory factory = new XmlTextModificationsFactory();
    factory.setResource(resource);

    TextModification modification = factory.create();

    String result = modification.modify("they left the dog in the car");
    assertEquals("they right the cat in the car", result);
    
    result = modification.modify("the value is ( 1 )");
    assertEquals("the value is left paren #1 right paren", result);
  }
}
