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
package edu.washington.cs.rse.transit;

import edu.washington.cs.rse.transit.MetroKCApplicationContext;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.datasource.DataSourceUtils;

import java.io.FileOutputStream;
import java.sql.Connection;

import javax.sql.DataSource;

public class MetroKCDAODataExtractor {
  public static void main(String[] args) throws Exception {

    ApplicationContext context = MetroKCApplicationContext.getApplicationContext();
    DataSource source = (DataSource) context.getBean("dataSource");
    Connection con = DataSourceUtils.doGetConnection(source);
    DatabaseConnection connection = new DatabaseConnection(con);

    // partial database export
    QueryDataSet partialDataSet = new QueryDataSet(connection);
    partialDataSet.addTable("transit_stop_locations",
        "SELECT * FROM transit_stop_locations WHERE id IN (10020, 10030, 10500, 10510)");
    FlatXmlDataSet.write(partialDataSet, new FileOutputStream(
        "partial-dataset.xml"));

    DataSourceUtils.releaseConnection(con, source);
  }
}
