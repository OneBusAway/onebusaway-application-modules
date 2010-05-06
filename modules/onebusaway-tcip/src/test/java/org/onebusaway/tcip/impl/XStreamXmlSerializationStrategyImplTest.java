package org.onebusaway.tcip.impl;

import org.onebusaway.tcip.model.CPTStoppointIden;
import org.onebusaway.tcip.model.CPTSubscriptionHeader;
import org.onebusaway.tcip.model.CPTSubscriptionType;
import org.onebusaway.tcip.model.PiStopPointETASub;
import org.onebusaway.tcip.services.XmlSerializationStrategy;

import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class XStreamXmlSerializationStrategyImplTest {
  
  private XmlSerializationStrategy _strategy = new XStreamXmlSerializationStrategyImpl();

  @Test
  public void test() throws Exception {

    PiStopPointETASub message = new PiStopPointETASub();
    message.setCreated(new Date());
    message.setSchVersion("TCIP 3.0.3");
    message.setSourceapp("blah");
    message.setSourceip("blah");
    message.setNoNameSpaceSchemaLocation("TCIP_Final_3_0_3.xsd");
    message.setSourceport(8080);

    CPTSubscriptionHeader header = new CPTSubscriptionHeader();
    header.setRequestedType(CPTSubscriptionType.TYPE_QUERY);
    header.setExpirationDate(new Date());
    header.setExpirationTime(new Date());
    message.setSubscriptionInfo(header);

    CPTStoppointIden id = new CPTStoppointIden();
    id.setAgencyId("1");
    id.setStoppointId("75403");
    message.setStoppoints(new ArrayList<CPTStoppointIden>(Arrays.asList(id)));

    String asXml = _strategy.toXml(message);

    System.out.println(asXml);

    PrintWriter writer = new PrintWriter(new FileWriter(new File(
        "/Users/bdferris/oba/trunk/data/apta/TCIP/schemas/test.xml")));
    writer.println(asXml);
    writer.close();
  }
}
