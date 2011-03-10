package org.onebusaway.testing;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.hibernate.jdbc.Work;

import java.sql.Connection;
import java.sql.SQLException;

public class ClearDatasetWork implements Work {

  private IDataSet _dataSet;

  public ClearDatasetWork(IDataSet dataSet) {
    _dataSet = dataSet;
  }
  
  public void execute(Connection jdbcConnection) throws SQLException {
    try {
      IDatabaseConnection connection = new DatabaseConnection(jdbcConnection);
      DatabaseOperation.DELETE_ALL.execute(connection, _dataSet);
    } catch (Exception ex) {
      throw new IllegalStateException(ex);
    }
  }
}
