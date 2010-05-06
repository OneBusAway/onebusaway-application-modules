/*
 * Copyright 2008 Brian Ferris
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.washington.cs.rse.transit.common;

import edu.washington.cs.rse.transit.MetroKCApplicationContext;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.xml.FlatDtdDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlWriter;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.datasource.DataSourceUtils;

import java.io.FileOutputStream;
import java.sql.Connection;

import javax.sql.DataSource;

public class MetroKCDAOTestDataExtractor {

  private static final boolean INCLUDE_DTD = false;

  public static void main(String[] args) throws Exception {

    ApplicationContext context = MetroKCApplicationContext.getApplicationContext();
    DataSource source = (DataSource) context.getBean("dataSource");
    Connection con = DataSourceUtils.doGetConnection(source);
    DatabaseConnection connection = new DatabaseConnection(con);

    // partial database export
    QueryDataSet partialDataSet = new QueryDataSet(connection);
    partialDataSet.addTable("transit_change_dates");

    partialDataSet.addTable(
        "transit_ordered_pattern_stops",
        "SELECt * FROM transit_ordered_pattern_stops WHERE (route_id = 100174 AND stop_id = 10500 AND schedulePatternId = 2) OR (route_id = 100254 AND stop_id = 25150 AND schedulePatternId = 58)");
    partialDataSet.addTable("transit_routes",
        "select * from transit_routes where id IN (100174,100254)");
    partialDataSet.addTable("transit_service_patterns",
        "SELECT * FROM transit_service_patterns where id IN (11030002,20065058)");
    partialDataSet.addTable("transit_stop_locations",
        "SELECT * FROM transit_stop_locations WHERE id IN (25840,25150,10500,10030)");
    partialDataSet.addTable("transit_street_names",
        "SELECT * FROM transit_street_names WHERE id IN (1688,5636)");
    partialDataSet.addTable("transit_trans_links",
        "SELECT * FROM transit_trans_links where id IN (5561,38637,69707)");

    if (INCLUDE_DTD) {
      FlatXmlWriter datasetWriter = new FlatXmlWriter(
          new FileOutputStream(
              "src/test/java/edu/washington/cs/rse/transit/common/MetroKCDAOTest.xml"));
      datasetWriter.setDocType("src/test/java/edu/washington/cs/rse/transit/common/MetroKCDAOTest.dtd");
      datasetWriter.write(partialDataSet);

      // write DTD file
      FlatDtdDataSet.write(
          connection.createDataSet(),
          new FileOutputStream(
              "src/test/java/edu/washington/cs/rse/transit/common/MetroKCDAOTest.dtd"));
    } else {
      FlatXmlDataSet.write(
          partialDataSet,
          new FileOutputStream(
              "src/test/java/edu/washington/cs/rse/transit/common/MetroKCDAOTest.xml"));
    }

    DataSourceUtils.releaseConnection(con, source);
  }
}
