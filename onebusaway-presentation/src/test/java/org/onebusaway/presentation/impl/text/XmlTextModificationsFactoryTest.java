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
