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

import org.onebusaway.presentation.services.text.TextModification;

import org.apache.commons.digester.Digester;
import org.springframework.core.io.Resource;
import org.xml.sax.SAXException;

import java.io.IOException;

public class XmlTextModificationsFactory {
  private Resource _resource;

  public void setResource(Resource resource) {
    _resource = resource;
  }

  public TextModification create() throws IOException, SAXException {

    TextModifications modifications = new TextModifications();

    Digester digester = new Digester();

    digester.addObjectCreate("text-modifications/replacement",
        ReplacementTextModification.class);
    digester.addSetProperties("text-modifications/replacement");
    digester.addSetNext("text-modifications/replacement", "addModification");

    digester.push(modifications);
    digester.parse(_resource.getInputStream());

    return modifications;
  }
}
