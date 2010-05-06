package org.onebusaway.testing;

import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.ext.mysql.MySqlDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.jdbc.Work;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.zip.GZIPInputStream;

public class DbUnitTestExecutionListener implements TestExecutionListener {

  private boolean _prepared = false;
  
  public void prepareTestInstance(TestContext testContext) throws Exception {

    if( _prepared )
      return;
    
    _prepared = true;
    
    ApplicationContext context = testContext.getApplicationContext();
    Class<?> targetClass = testContext.getTestClass();
    
    loadDataFromAnnotation(context, targetClass);
  }

  public static void loadDataFromAnnotation(ApplicationContext context, Class<?> targetClass) throws IOException {
    
    DbUnitTestConfiguration config = targetClass.getAnnotation(DbUnitTestConfiguration.class);

    if (config == null)
      return;

    String location = config.location();
    if (location == null || location.length() == 0)
      return;
    
    final Resource resource = context.getResource(location);

    SessionFactory sessionFactory = (SessionFactory) context.getBean("sessionFactory");
    Session session = sessionFactory.openSession();
    session.doWork(new Work() {
      public void execute(Connection connection) throws SQLException {
        try {
          InputStream in = resource.getInputStream();
          if( resource.getFilename().endsWith(".gz"))
            in = new GZIPInputStream(in);
          IDatabaseConnection dc = new DatabaseConnection(connection);
          
          DatabaseConfig config = dc.getConfig();
          config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY,
              new MySqlDataTypeFactory());
          
          IDataSet dataSet = new FlatXmlDataSet(in);
          System.out.println("in");
          DatabaseOperation.CLEAN_INSERT.execute(dc, dataSet);
          System.out.println("out");
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
    });
    session.close();
  }

  public void afterTestMethod(TestContext testContext) throws Exception {

  }

  public void beforeTestMethod(TestContext testContext) throws Exception {

  }
}
