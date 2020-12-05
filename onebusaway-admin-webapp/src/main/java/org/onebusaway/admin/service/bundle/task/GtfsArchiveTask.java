/**
 * Copyright (C) 2015 Cambridge Systematics, Inc.
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
package org.onebusaway.admin.service.bundle.task;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.jdbc.Work;
import org.onebusaway.admin.model.BundleBuildRequest;
import org.onebusaway.admin.model.BundleBuildResponse;
import org.onebusaway.admin.model.BundleRequestResponse;
import org.onebusaway.admin.service.bundle.task.model.GtfsBundleInfo;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs.services.HibernateGtfsFactory;
import org.onebusaway.transit_data_federation.bundle.model.GtfsBundle;
import org.onebusaway.transit_data_federation.bundle.model.GtfsBundles;
import org.onebusaway.util.SystemTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

public class GtfsArchiveTask implements  Runnable {
  private static Logger _log = LoggerFactory.getLogger(GtfsArchiveTask.class);
  protected ApplicationContext _applicationContext;
  
  private BundleRequestResponse requestResponse;
  private static String[] TMP_TABLES = {
    "tmp_gtfs_calendar_dates",
    "tmp_gtfs_calendars",
    "tmp_gtfs_block",
    "tmp_gtfs_fare_rules",
    "tmp_gtfs_fare_attributes",
    "tmp_gtfs_feed_info",
    "tmp_gtfs_frequencies",
    "tmp_gtfs_pathways",
    "tmp_gtfs_shape_points",
    "tmp_gtfs_stop_times",
    "tmp_gtfs_transfers",
    "tmp_gtfs_stops",
    "tmp_gtfs_trips",
    "tmp_gtfs_routes",
    "tmp_gtfs_agencies"
  };

  private static Map<String, String> PRIMARY_KEY_MAP = new HashMap<String, String>();
  static {
    PRIMARY_KEY_MAP.put("gtfs_agencies", "id");
    PRIMARY_KEY_MAP.put("gtfs_block", "gid");
    PRIMARY_KEY_MAP.put("gtfs_calendar_dates", "gid");
    PRIMARY_KEY_MAP.put("gtfs_calendars", "gid");
    PRIMARY_KEY_MAP.put("gtfs_fare_attributes", "agencyId,id");
    PRIMARY_KEY_MAP.put("gtfs_fare_rules", "gid");
    PRIMARY_KEY_MAP.put("gtfs_feed_info", "gid");
    PRIMARY_KEY_MAP.put("gtfs_frequencies", "gid");
    PRIMARY_KEY_MAP.put("gtfs_pathways", "agencyId,id");
    PRIMARY_KEY_MAP.put("gtfs_routes", "agencyId,id");
    PRIMARY_KEY_MAP.put("gtfs_shape_points", "gid");
    PRIMARY_KEY_MAP.put("gtfs_stop_times", "gid");
    PRIMARY_KEY_MAP.put("gtfs_stops", "agencyId,id");
    PRIMARY_KEY_MAP.put("gtfs_transfers", "gid");
    PRIMARY_KEY_MAP.put("gtfs_trips", "agencyId,id");
  }
  
  @Autowired
  public void setApplicationContext(ApplicationContext applicationContext) {
    _applicationContext = applicationContext;
  }
  
  @Autowired
  public void setBundleRequestResponse(BundleRequestResponse requestResponse) {
    this.requestResponse = requestResponse;
  }
  
  @Override
  public void run() {
    if (!requestResponse.getRequest().getArchiveFlag()) {
      _log.info("archive flag not set, exiting");
      return;
    }
    
    long start = SystemTime.currentTimeMillis();
    _log.info("archiving gtfs");
    
    Configuration config = getConfiguration();
    if (config == null) {
      _log.error("missing configuration, GTFS will not be archived");
      return;
    }
    SessionFactory sessionFactory = config.buildSessionFactory();
    Session session = sessionFactory.openSession();
    Transaction transaction = session.beginTransaction();
    HibernateGtfsFactory factory = new HibernateGtfsFactory(sessionFactory);

    GtfsBundles gtfsBundles = getGtfsBundles(_applicationContext);
    Integer gtfsBundleInfoId = createMetaData(session, requestResponse);
    
    for (GtfsBundle gtfsBundle : gtfsBundles.getBundles()) {
    
      GtfsReader reader = new GtfsReader();
      try {
        
        cleanTempTables(session);
        reader.setInputLocation(gtfsBundle.getPath());
        GtfsMutableRelationalDao dao = factory.getDao();
        reader.setEntityStore(dao);
        _log.info("running for gtfs=" + gtfsBundle.getPath());
        reader.run();
        reader.close();
        
        archiveData(session, gtfsBundleInfoId);
      } catch (IOException e) {
        _log.error("gtfs archive failure:", e);
      }
    }
    cleanTempTables(session);
    transaction.commit();
    session.flush();
    session.close();

    long stop = SystemTime.currentTimeMillis();
    _log.info("archiving gtfs complete in " + (stop-start)/1000 + "s");
    
  }

  private Integer createMetaData(Session session,
      BundleRequestResponse requestResponse) {
    GtfsBundleInfo info = new GtfsBundleInfo();
    BundleBuildRequest request = requestResponse.getRequest();
    BundleBuildResponse response = requestResponse.getResponse();
    info.setBundleId(response.getBundleId());
    info.setName(request.getBundleName());
    info.setDirectory(request.getBundleDirectory());
    info.setStartDate(request.getBundleStartDate().toDate());
    info.setEndDate(request.getBundleEndDate().toDate());
    info.setTimestamp(new Date()); 
    return (Integer)session.save(info);
  }

  private void archiveData(Session session, Integer gtfsBundleInfoId) {
    for (String table : TMP_TABLES) {
      String newTable = table.replaceAll("tmp_", "");
      
      boolean exists = checkTableExists(session, newTable);
      if (!exists) {
        String sql = "create table " + newTable + " like " + table;
        logUpdate(session, sql);

        sql = "alter table " + newTable + " add gtfs_bundle_info_id int(11)";
        logUpdate(session, sql);
        
        sql = "alter table " + newTable + " drop primary key, add primary key(gtfs_bundle_info_id, " + PRIMARY_KEY_MAP.get(newTable) + ")";
        logUpdate(session, sql);
      }
      
      copyData(session, newTable, table, gtfsBundleInfoId);
      
    }
    
  }

  private void copyData(Session session, String newTable, String table, Integer gtfsBundleInfoId) {
    ArrayList<String> columnNames = findColumnNames(session, table);
    if (columnNames.isEmpty()) {
      _log.info("empty table " + table + ", skipping");
      return;
    }

    _log.info("finding col names for table " + table + "=" + columnNames);

    
    String sql = "insert ignore into " + newTable + "(gtfs_bundle_info_id , " + formatColumnNames(columnNames) + ")";
    sql += " select '" + gtfsBundleInfoId + "', " + formatColumnNames(columnNames) + " from " + table;
    logUpdate(session, sql);
  }

  private String formatColumnNames(ArrayList<String> columnNames) {
    StringBuffer sb = new StringBuffer();
    for (String s : columnNames) {
      sb.append(s);
      sb.append(", ");
    }
    if (sb.length() > 2)
      return sb.substring(0, sb.length()-2);
    return sb.toString();
  }

  private ArrayList<String> findColumnNames(Session session, String table) {
    ColumnNameWork work = new ColumnNameWork(table);
    session.doWork(work);
    return work.getColumnNames();
  }

  private boolean checkTableExists(Session session, final String newTable) {
    TableExistsWork work = new TableExistsWork(newTable);
    session.doWork(work);
    return work.exists();
  }

  private void cleanTempTables(Session session) {
    SQLQuery check = session.createSQLQuery("set foreign_key_checks = 0");
    check.executeUpdate();
    
    for (String table : TMP_TABLES) {
      logUpdate(session, "truncate table " + table);
    }
    
    check = session.createSQLQuery("set foreign_key_checks = 1");
    check.executeUpdate();

  }
  
  private int logUpdate(Session session, String sql) {
    long start = SystemTime.currentTimeMillis();
    SQLQuery query = session.createSQLQuery(sql);
    _log.debug("query:  " + sql);
    int rc = -1;
    try {
      rc = query.executeUpdate();
    } catch (Throwable t) {
      _log.error("Exception from sql=" + sql);
      _log.error("Exception:", t);
      throw t;
    }
    long stop = SystemTime.currentTimeMillis();
    _log.info("result: " + rc + " in " + (stop-start)/1000 + "s for query: " + sql);
    return rc;
  }

  private Configuration getConfiguration() {
    try {
      
      Context initialContext = new InitialContext();
      Context environmentContext = (Context) initialContext.lookup("java:comp/env");
      DataSource ds = (DataSource) environmentContext.lookup("jdbc/archiveDB");
      if (ds == null) { 
        _log.error("unable to locate expected datasource jdbc/archiveDB");
        return null;
      }
      Configuration config = new Configuration();
      config.setProperty("hibernate.connection.datasource", "java:comp/env/jdbc/archiveDB");
      config.setProperty("hibernate.connection.pool_size", "1");
      config.setProperty("hibernate.cache.provider_class",
          "org.hibernate.cache.internal.NoCachingRegionFactory");
      config.setProperty("hibernate.hbm2ddl.auto", "update");
      config.addResource("org/onebusaway/gtfs/model/GtfsArchiveMapping.hibernate.xml");
      config.addResource("org/onebusaway/gtfs/impl/HibernateGtfsRelationalDaoImpl.hibernate.xml");

      //performance tuning
      config.setProperty("hibernate.jdbc.batch_size", "4000");
      config.setProperty("hibernate.jdbc.fetch_size", "64");
      config.setProperty("hibernate.order_inserts", "true");
      config.setProperty("hibernate.order_updates", "true");
      config.setProperty("hibernate.cache.use_second_level_cache", "false");
      config.setProperty("hibernate.generate_statistics", "false");
      config.setProperty("hibernate.cache.use_query_cache", "false");

      return config;
    } catch (Throwable t) {
      _log.error("configuration exception:", t);
      return null;
    }
  }

  protected GtfsBundles getGtfsBundles(ApplicationContext context) {

    GtfsBundles bundles = (GtfsBundles) context.getBean("gtfs-bundles");
    if (bundles != null)
      return bundles;

    GtfsBundle bundle = (GtfsBundle) context.getBean("gtfs-bundle");
    if (bundle != null) {
      bundles = new GtfsBundles();
      bundles.getBundles().add(bundle);
      return bundles;
    }

    throw new IllegalStateException(
        "must define either \"gtfs-bundles\" or \"gtfs-bundle\" in config");
  }
  
  // TDOO replace with generic JDBC approach that is not MySQL specific
  private static class TableExistsWork implements Work {
    
    private String newTable = null;
    private boolean exists = false;
    public TableExistsWork(String newTable) {
      this.newTable = newTable;
    }
    
    public boolean exists() {
      return exists;
    }
    
    @Override
    public void execute(Connection conn) throws SQLException {
      PreparedStatement ps = null;
      try {
        long start = SystemTime.currentTimeMillis();
        String sql = "show tables like '" + newTable + "'";
        ps = conn.prepareStatement(sql);
        ResultSet query = ps.executeQuery();
        exists = query.first();
        long stop = SystemTime.currentTimeMillis();
        _log.info("TableExists(" + newTable + ") took " + (stop-start)/1000 + "s");
      } finally {
        ps.close();
      }

    }
  }
  
  private static class ColumnNameWork implements Work {
    private String newTable = null;
    private ArrayList<String> columnNames = new ArrayList<String>();
    
    public ColumnNameWork(String table) {
      newTable = table;
    }
    
    public ArrayList<String> getColumnNames() {
      return columnNames;
    }
    
    @Override
    public void execute(Connection conn) throws SQLException {
      PreparedStatement ps = null;
      try {
        long start = SystemTime.currentTimeMillis();
        String sql = "Select * from " + newTable + " limit 1";
        ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        ResultSetMetaData rsmd = rs.getMetaData();
        for (int i = 1; i <= rsmd.getColumnCount(); i++) {
          columnNames.add(rsmd.getColumnName(i));
        }
        long stop = SystemTime.currentTimeMillis();
        _log.info("columnNames(" + newTable + ") took " + (stop-start)/1000 + "s");
      } finally {
        ps.close();
      }
    }
  }
  
}
