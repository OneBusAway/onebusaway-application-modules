package org.onebusaway.gtfs;

import org.onebusaway.container.ContainerLibrary;
import org.onebusaway.gtfs.impl.HibernateEntityStore;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.testing.ClearDatasetWork;
import org.onebusaway.testing.TestData;
import org.onebusaway.testing.WriteDatasetWork;

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
        "/org/onebusaway/gtfs/GtfsHibernateTestContext.xml","/data-sources.xml"
        );
    GenerateDatabaseDumps runner = new GenerateDatabaseDumps();
    SessionFactory sessionFactory = (SessionFactory) context.getBean("sessionFactory");
    runner.setSessionFactory(sessionFactory);
    runner.run();
  }

  private SessionFactory _sessionFactory;

  public void setSessionFactory(SessionFactory sessionFactory) {
    _sessionFactory = sessionFactory;
  }

  public void run() throws IOException {
    generateCaltrainDatabase();
    generateIslandAndPortDatabase();
  }

  public void generateCaltrainDatabase() throws IOException {
    generateDatabaseFromGtfs(TestData.getCaltrainGtfs());
    IDataSet data = writeData(new File(
        "src/test/resources/org/onebusaway/gtfs/CaltrainDatabase.xml.gz"),
        new File("src/test/resources/org/onebusaway/gtfs/CaltrainDatabase.dtd"));
    clearData(data);
  }

  public void generateIslandAndPortDatabase() throws IOException {
    generateDatabaseFromGtfs(TestData.getIslandGtfs());
    generateDatabaseFromGtfs(TestData.getPortGtfs());
    IDataSet data = writeData(new File(
        "src/test/resources/org/onebusaway/gtfs/IslandAndPortDatabase.xml.gz"),
        new File(
            "src/test/resources/org/onebusaway/gtfs/IslandAndPortDatabase.dtd"));
    clearData(data);
  }

  private void generateDatabaseFromGtfs(File path) throws IOException {

    HibernateEntityStore store = new HibernateEntityStore();
    store.setSessionFactory(_sessionFactory);

    GtfsReader reader = new GtfsReader();
    reader.setEntityStore(store);
    reader.setInputLocation(path);

    store.open();
    reader.run();
    store.close();
  }

  private IDataSet writeData(File xmlFile, File dtdFile) throws IOException {

    HibernateTemplate template = new HibernateTemplate(_sessionFactory);
    template.flush();

    WriteDatasetWork work = new WriteDatasetWork(xmlFile, dtdFile);

    Session session = _sessionFactory.openSession();
    session.doWork(work);
    session.close();

    return work.getDataSet();
  }

  private void clearData(IDataSet dataSet) {
    ClearDatasetWork work = new ClearDatasetWork(dataSet);

    Session session = _sessionFactory.openSession();
    session.doWork(work);
    session.close();
  }
}
