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
