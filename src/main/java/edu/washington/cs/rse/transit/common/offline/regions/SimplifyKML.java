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
package edu.washington.cs.rse.transit.common.offline.regions;

import edu.washington.cs.rse.transit.MetroKCApplicationContext;
import edu.washington.cs.rse.transit.common.impl.RegionsDAO;
import edu.washington.cs.rse.transit.common.model.aggregate.Region;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SimplifyKML {

  @Autowired
  private RegionsDAO _dao;

  private File _inputDir;

  private File _outputDir;

  public void setInputDir(File inputDir) {
    System.out.println("input=" + inputDir);
    _inputDir = inputDir;
  }

  public void setOutputDir(File outputDir) {
    System.out.println("output=" + outputDir);
    _outputDir = outputDir;
  }

  public static void main(String[] args) throws Exception {
    ApplicationContext context = MetroKCApplicationContext.getApplicationContext();
    SimplifyKML m = (SimplifyKML) context.getBean("simplifyKml");
    m.run();
  }

  public void run() throws Exception {
    List<File> inputFiles = getInputFiles();
    for (File inputFile : inputFiles) {
      File outputFile = getOutputFile(inputFile);
      System.out.println(inputFile);
      System.out.println("  " + outputFile);
      File parent = outputFile.getParentFile();
      if (!parent.exists())
        parent.mkdirs();

      List<Region> regions = new ArrayList<Region>();
      _dao.readRegionsFromKML(inputFile, regions);
      List<Region> simplified = _dao.simplifyRegions(regions);
      _dao.writeRegionsAsKML(simplified, outputFile);
    }
  }

  private List<File> getInputFiles() {
    List<File> files = new ArrayList<File>();
    getInputFiles(_inputDir, files);
    return files;
  }

  private void getInputFiles(File file, List<File> files) {
    if (file.isDirectory()) {
      File[] children = file.listFiles();
      if (children != null) {
        for (File child : children)
          getInputFiles(child, files);
      }
    } else if (file.getName().endsWith(".kml"))
      files.add(file);
  }

  private File getOutputFile(File inputFile) {
    String path = inputFile.getAbsolutePath();
    path = path.replaceFirst(_inputDir.getAbsolutePath(),
        _outputDir.getAbsolutePath());
    return new File(path);
  }
}
