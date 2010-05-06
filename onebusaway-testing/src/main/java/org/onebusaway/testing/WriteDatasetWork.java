/**
 * 
 */
package org.onebusaway.testing;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.DatabaseSequenceFilter;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.FilteredDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.filter.ITableFilter;
import org.dbunit.dataset.xml.FlatDtdDataSet;
import org.dbunit.dataset.xml.FlatXmlWriter;
import org.hibernate.jdbc.Work;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class WriteDatasetWork implements Work {

  private File _xmlFile;

  private File _dtdFile;

  private IDataSet _dataSet;

  private boolean _dtdIsRelativeToWorkingDirectory = true;

  public WriteDatasetWork(File xmlFile, File dtdFile) {
    _xmlFile = xmlFile;
    _dtdFile = dtdFile;
  }

  public IDataSet getDataSet() {
    return _dataSet;
  }

  public void setDtdIsRelativeToWorkingDirectory(
      boolean dtdIsRelativeToWorkingDirectory) {
    _dtdIsRelativeToWorkingDirectory = dtdIsRelativeToWorkingDirectory;
  }

  public void execute(Connection jdbcConnection) throws SQLException {
    try {
      IDatabaseConnection connection = new DatabaseConnection(jdbcConnection);
      ITableFilter filter = new DatabaseSequenceFilter(connection);
      _dataSet = new FilteredDataSet(filter, connection.createDataSet());

      OutputStream xmlOut = getFileOutputStream(_xmlFile);
      FlatXmlWriter datasetWriter = new FlatXmlWriter(
          xmlOut);
      datasetWriter.setIncludeEmptyTable(true);
      datasetWriter.setDocType(_dtdFile.getAbsolutePath());
      datasetWriter.write(_dataSet);
      xmlOut.close();

      FlatDtdDataSet.write(_dataSet, new FileOutputStream(_dtdFile));

      if (_dtdIsRelativeToWorkingDirectory) {

        File tmp = File.createTempFile(WriteDatasetWork.class.getName(), ".tmp");
        tmp.deleteOnExit();

        copyFile(_xmlFile, tmp);

        // Fixup and compress the output file
        String cwd = System.getProperty("user.dir") + "/";
        
        BufferedReader reader = new BufferedReader(new FileReader(tmp));
        String line = null;

        xmlOut = getFileOutputStream(_xmlFile);
        PrintWriter out = new PrintWriter(new OutputStreamWriter(
            xmlOut));

        while ((line = reader.readLine()) != null) {
          line = line.replaceAll(cwd, "");
          out.println(line);
        }

        reader.close();
        out.close();

        tmp.delete();
      }

    } catch (Exception ex) {
      throw new IllegalStateException(ex);
    }
  }

  private void copyFile(File src, File dst) throws IOException {
    InputStream in = getFileInputStream(src);
    OutputStream out = getFileOutputStream(dst);
    byte[] buffer = new byte[1024];

    while (true) {
      int rc = in.read(buffer);
      if (rc == -1)
        break;
      out.write(buffer, 0, rc);
    }

    in.close();
    out.close();
  }

  private InputStream getFileInputStream(File file) throws IOException {
    InputStream in = new FileInputStream(file);
    if (file.getName().endsWith(".gz"))
      in = new GZIPInputStream(in);
    return in;
  }

  private OutputStream getFileOutputStream(File file) throws IOException {
    OutputStream out = new FileOutputStream(file);
    if (file.getName().endsWith(".gz"))
      out = new GZIPOutputStream(out);
    return out;
  }

}