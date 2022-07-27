/**
 * Copyright (C) 2022 Cambridge Systematics, Inc.
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
package org.onebusaway.transit_data_federation.bundle.utilities;

import org.onebusaway.transit_data_federation.bundle.model.BundleFile;
import org.onebusaway.transit_data_federation.bundle.model.SourceFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * Some bundle building convenience methods shared between
 * builder and admin console.
 */
public class BundleUtilties {

    private static Logger _log = LoggerFactory.getLogger(BundleUtilties.class);

    public List<BundleFile> getBundleFilesWithSumsForDirectory(File baseDir, File dir, File rootDir) throws IllegalArgumentException {
        List<BundleFile> files = new ArrayList<BundleFile>();

        if (!dir.isDirectory()) {
            throw new IllegalArgumentException(dir.getPath() + " is not a directory");
        } else {
            for (String filePath : dir.list()) {
                File listEntry = new File(dir, filePath);
                String listEntryFilename = null;
                try {
                    listEntryFilename = listEntry.getCanonicalPath();
                } catch (Exception e) {
                    // bury
                }
                // prevent lock files from insertion into json, they change
                if (listEntry.isFile()
                        && listEntryFilename != null
                        && !listEntryFilename.endsWith(".lck")) {
                    BundleFile file = new BundleFile();

                    String relPathToBase = null;
                    if (rootDir == null) {
                        _log.debug("baseDir=" + baseDir + "; listEntry=" + listEntry);
                        relPathToBase = baseDir.toURI().relativize(listEntry.toURI()).getPath();
                        file.setFilename(relPathToBase);
                    } else {
                        relPathToBase = rootDir.toURI().relativize(listEntry.toURI()).getPath();
                        file.setFilename(relPathToBase);
                    }

                    String sum = getMd5ForFile(listEntry);
                    file.setMd5(sum);

                    files.add(file);
                    _log.debug("file:" + listEntry + " has Md5=" + sum);
                } else if (listEntry.isDirectory()) {
                    files.addAll(getBundleFilesWithSumsForDirectory(baseDir, listEntry, rootDir));
                }
            }
        }

        return files;
    }

    public List<SourceFile> getSourceFilesWithSumsForDirectory(File baseDir, File dir, File rootDir) throws IllegalArgumentException {
        List<SourceFile> files = new ArrayList<SourceFile>();
        if (!dir.isDirectory()) {
            throw new IllegalArgumentException(dir.getPath() + " is not a directory");
        } else {
            for (String filePath : dir.list()) {
                File listEntry = new File(dir, filePath);
                String listEntryFilename = null;
                try {
                    listEntryFilename = listEntry.getCanonicalPath();
                } catch (Exception e) {
                    // bury
                }
                // prevent lock files from insertion into json, they change
                if (listEntry.isFile()
                        && listEntryFilename != null
                        && !listEntryFilename.endsWith(".lck")) {
                    SourceFile file = new SourceFile();
                    file.setCreatedDate(getCreatedDate(listEntry));

                    String relPathToBase = null;
                    if (rootDir == null) {
                        relPathToBase = baseDir.toURI().relativize(listEntry.toURI()).getPath();
                        file.setUri(relPathToBase);
                        file.setFilename(relPathToBase);
                    } else {
                        relPathToBase = rootDir.toURI().relativize(listEntry.toURI()).getPath();
                        file.setUri(relPathToBase);
                        file.setFilename(relPathToBase);
                    }
                    String sum = getMd5ForFile(listEntry);
                    file.setMd5(sum);

                    files.add(file);
                    _log.debug("file:" + listEntry + " has Md5=" + sum + " and createdDate=" + file.getCreatedDate());
                } else if (listEntry.isDirectory()) {
                    files.addAll(getSourceFilesWithSumsForDirectory(baseDir, listEntry, rootDir));
                }
            }
        }

        return files;
    }

    private Date getCreatedDate(File file) {
        if (!file.exists()) {
            _log.error("file " + file + File.separator + file + " does not exist: cannot guess date!");
        }
        return new Date(file.lastModified());
    }

    public String getMd5ForFile(File file) {
        String sum;
        try {
            sum = Md5Checksum.getMD5Checksum(file.getPath());
        } catch (Exception e) {
            sum = "Error generating md5 for " + file.getPath();
        }

        return sum;
    }

}
