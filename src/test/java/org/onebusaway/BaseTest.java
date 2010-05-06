package org.onebusaway;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration(locations = {
    "data-sources-test.xml",
    "/org/onebusaway/gtdf/application-context.xml",
    "/org/onebusaway/where/application-context.xml",
    "/org/onebusaway/tripplanner/application-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
// @TransactionConfiguration
// @Transactional
public class BaseTest {

  public static ApplicationContext getContext() {

    GenericApplicationContext ctx = new GenericApplicationContext();
    XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(ctx);

    ContextConfiguration config = BaseTest.class.getAnnotation(ContextConfiguration.class);
    if (config != null) {
      for (String location : config.locations()) {
        xmlReader.loadBeanDefinitions(new ClassPathResource(location));
      }
    }
    ctx.refresh();
    ctx.registerShutdownHook();

    return ctx;
  }

}
