package org.onebusaway.transit_data_federation_webapp.siri;

import java.io.Reader;
import java.io.Writer;

import org.onebusaway.siri.core.SiriClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SiriController extends SiriClient {

  @Autowired
  public void start() {
    super.start();
  }

  @Autowired
  public void stop() {
    super.stop();
  }

  @RequestMapping(value = "/siri.action")
  public void siri(Reader reader, Writer writer) {
    this.handleRawRequest(reader, writer);
  }
}
