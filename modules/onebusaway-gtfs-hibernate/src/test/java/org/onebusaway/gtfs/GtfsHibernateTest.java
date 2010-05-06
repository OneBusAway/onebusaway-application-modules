package org.onebusaway.gtfs;


import org.onebusaway.testing.DbUnitTestExecutionListener;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/org/onebusaway/gtfs/GtfsHibernateTestContext.xml","/data-sources.xml"})
@TestExecutionListeners( {
    DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class})
@Transactional
public abstract class GtfsHibernateTest {
  
  public static DateFormat _dateFormat = new SimpleDateFormat("yyyy-MM-dd");

  public static DateFormat _timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

  protected ApplicationContext _context;


  public static final Date date(String spec) {
    try {
      return _dateFormat.parse(spec);
    } catch (ParseException ex) {
      throw new IllegalStateException(ex);
    }
  }

  public static final Date dateAndTime(String spec) {
    try {
      return _timeFormat.parse(spec);
    } catch (ParseException ex) {
      throw new IllegalStateException(ex);
    }
  }
  
  @Autowired
  public  void setApplicationContext(ApplicationContext context) {
    _context = context;
  }

  /****
   * Protected Methods
   ****/

  protected <T> T autowire(T bean) {
    _context.getAutowireCapableBeanFactory().autowireBean(bean);
    return bean;
  }

  protected void deleteFile(File file) {
    if (!file.exists())
      return;
    if (file.isDirectory()) {
      File[] children = file.listFiles();
      if (children != null) {
        for (File child : children)
          deleteFile(child);
      }
    }
    file.delete();
  }
}
