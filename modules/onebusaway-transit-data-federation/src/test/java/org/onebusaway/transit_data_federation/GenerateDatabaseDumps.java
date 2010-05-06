package org.onebusaway.transit_data_federation;

import org.onebusaway.container.ContainerLibrary;
import org.onebusaway.testing.ClearDatasetWork;
import org.onebusaway.testing.TestData;
import org.onebusaway.testing.WriteDatasetWork;
import org.onebusaway.transit_data_federation.impl.offline.GtfsReaderTask;
import org.onebusaway.transit_data_federation.services.RunnableWithOutputPath;

import org.dbunit.dataset.IDataSet;
import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.springframework.context.ApplicationContext;
import org.springframework.orm.hibernate3.HibernateTemplate;

import java.io.File;
import java.io.IOException;

public class GenerateDatabaseDumps {

  public static void main(String[] args) throws IOException {
    ApplicationContext context = ContainerLibrary.createContext(
        "/data-sources-test.xml",
        "/org/onebusaway/transit_data_federation/TransitDataFederationBaseTestContext.xml");
    GenerateDatabaseDumps runner = new GenerateDatabaseDumps();
    runner.setApplicationContext(context);
    SessionFactory sessionFactory = (SessionFactory) context.getBean("sessionFactory");
    runner.setSessionFactory(sessionFactory);
    runner.run();
  }

  private ApplicationContext _context;

  private SessionFactory _sessionFactory;

  public void setApplicationContext(ApplicationContext context) {
    _context = context;
  }

  public void setSessionFactory(SessionFactory sessionFactory) {
    _sessionFactory = sessionFactory;
  }

  public void run() throws IOException {
    generateIslandAndPortDatabaseWithRouteCollections();
    generateCaltrainDatabaseWithRouteCollections();
  }

  public void generateCaltrainDatabaseWithRouteCollections() throws IOException {
    generateDatabaseFromGtfs(TestData.getCaltrainGtfs());
    generateExtendedData("Caltrain");
    writeData(new File("CaltrainDatabase.xml"), new File(
        "CaltrainDatabase.dtd"));
  }

  public void generateIslandAndPortDatabaseWithRouteCollections()
      throws IOException {
    generateDatabaseFromGtfs(TestData.getIslandGtfs());
    generateDatabaseFromGtfs(TestData.getPortGtfs());
    writeData(new File("IslandAndPortDatabase.xml"), new File(
        "IslandAndPortDatabase.dtd"));
    
    generateDatabaseFromGtfs(TestData.getIslandGtfs());
    generateDatabaseFromGtfs(TestData.getPortGtfs());
    generateExtendedData("IslandAndPort");
    writeData(new File("IslandAndPortDatabaseExtended.xml"), new File(
        "IslandAndPortDatabaseExtended.dtd"));
  }

  private void generateDatabaseFromGtfs(File path) throws IOException {
    GtfsReaderTask reader = new GtfsReaderTask();
    reader.setSessionFactory(_sessionFactory);
    reader.setInputLocation(path);
    reader.run();
  }

  private void generateExtendedData(String indexPrefix) {

    Runnable generateRouteCollectionsTask = (Runnable) _context.getBean("generateRouteCollectionsTask");
    generateRouteCollectionsTask.run();

    RunnableWithOutputPath generateRouteCollectionsSearchIndex = (RunnableWithOutputPath) _context.getBean("generateRouteCollectionSearchIndexTask");
    generateRouteCollectionsSearchIndex.setOutputPath(new File(indexPrefix
        + "-RouteCollectionSearchIndex"));
    generateRouteCollectionsSearchIndex.run();

    RunnableWithOutputPath generateStopSearchIndex = (RunnableWithOutputPath) _context.getBean("generateStopSearchIndexTask");
    generateStopSearchIndex.setOutputPath(new File(indexPrefix
        + "-StopSearchIndex"));
    generateStopSearchIndex.run();
  }

  private void writeData(File xmlFile, File dtdFile) {

    HibernateTemplate template = new HibernateTemplate(_sessionFactory);
    template.flush();

    WriteDatasetWork work = new WriteDatasetWork(xmlFile, dtdFile);

    Session session = _sessionFactory.openSession();
    session.doWork(work);
    session.close();

    IDataSet dataSet = work.getDataSet();

    // Clear the database
    session = _sessionFactory.openSession();
    session.doWork(new ClearDatasetWork(dataSet));
    session.close();
  }

}
