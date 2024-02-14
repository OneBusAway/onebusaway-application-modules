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
package org.onebusaway.transit_data_federation.impl.bundle;

import com.amazonaws.util.IOUtils;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.onebusaway.cloud.api.ExternalServices;
import org.onebusaway.cloud.api.ExternalServicesBridgeFactory;
import org.onebusaway.cloud.api.InputStreamConsumer;
import org.onebusaway.cloud.aws.CredentialContainer;
import org.onebusaway.transit_data_federation.model.bundle.BundleItem;
import org.onebusaway.transit_data_federation.services.bundle.BundleStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * Host Bundles off S3 instead of admin/tdm server.
 *
 * Impl: has this convention:  Host an index file at remoteSourceURI that looks like:
 * {"latest":"s3://camsys-mta-otp-graph/v2dev/bundle/v.1658939424000.tar.gz"}
 * The value of latest should point at a bundle in its tar.gz format as built by
 * FederatedTransitDataBundleMain -convention
 */
public class S3BundleStoreImpl extends AbstractBundleStoreImpl implements BundleStoreService {

    private static Logger _log = LoggerFactory.getLogger(S3BundleStoreImpl.class);

    private String latestBundleURI = null;
    private String remoteIndexURI = null;
    private boolean initialized = false;

    private final Map<Date, S3BundleItem> _bundleFolderByDate = new HashedMap();
    private final Map<Date, S3BundleItem> _bundleFileByDate = new HashedMap();

    public void setLatestBundleURI(String s) {
        latestBundleURI = s;
    }
    public S3BundleStoreImpl(String bundleRootPath, String remoteSourceURI) {
        super(bundleRootPath);
        this.remoteIndexURI = remoteSourceURI;
    }

    @Override
    public List<BundleItem> getBundles() throws Exception {
        if (!initialized) {
            loadInitial();
            initialized = true;
        }
        // for testing we support a file:/// or / syntax
        if (isFile(remoteIndexURI)) {
            String indexFile = remoteIndexURI.replaceFirst("file:", "");
            _log.info("looking for index file on disk at " + indexFile);
           String latest = parseConfigFromStream(new FileInputStream(indexFile));
            if (latest != null)
                setLatestBundleURI(latest);
        } else {
            ExternalServices es = new ExternalServicesBridgeFactory().getExternalServices();
            String s3IndexFile = remoteIndexURI;
            es.getFileAsStream(s3IndexFile, new InputStreamConsumer() {
                @Override
                public void accept(InputStream inputStream) throws IOException {
                    String latest = parseConfigFromStream(inputStream);
                    if (latest != null)
                        setLatestBundleURI(latest);
                }
            }, getProfile(), getRegion());
        }
        if (this.latestBundleURI == null)
            throw new RuntimeException("no index file found at " + remoteIndexURI);

        String bundleRoot = this._bundleRootPath;
        String[] bundleNameParts = latestBundleURI.split("/");
        final String bundleTarName = bundleNameParts[bundleNameParts.length-1];
        final String bundleName = bundleTarName.replace(".tar.gz", "");
        S3BundleItem fileItem = new S3BundleItem(bundleTarName, bundleRoot);
        S3BundleItem folderItem = new S3BundleItem(bundleName, bundleRoot);

        if (bundleExists(bundleName)) {
            _log.info("bundle {} exists on disk, skipping download", bundleName);
            prune();
            // TODO: could verify checksums here
        } else {
            // download and extract bundle
            if (latestBundleURI != null) {
                if (isFile(latestBundleURI)) {
                    String bundleFile = latestBundleURI.replaceFirst("file:", "");
                    _log.info("loading bundle file on disk at " + bundleFile);
                    extractBundleFromInput(new FileInputStream(bundleFile), bundleRoot, bundleName);
                    this._bundleFileByDate.put(new Date(), fileItem);
                    this._bundleFolderByDate.put(new Date(), folderItem);
                } else {
                    ExternalServices es = new ExternalServicesBridgeFactory().getExternalServices();
                    es.getFileAsStream(latestBundleURI, new InputStreamConsumer() {
                        @Override
                        public void accept(InputStream inputStream) throws IOException {
                            FileOutputStream fileStream = extractBundleFromInput(inputStream, bundleRoot, bundleName);
                        }
                    }, getProfile(), getRegion());

                    this._bundleFileByDate.put(new Date(), fileItem);
                    this._bundleFolderByDate.put(new Date(), folderItem);

                }
            }
        }

        File possibleBundle = new File(bundleRoot, bundleName);
        if (!possibleBundle.exists())
            throw new RuntimeException("expected dir to exist: " + possibleBundle);
        File dataDir = new File(possibleBundle + File.separator + "data");
        if (dataDir.exists() && dataDir.isDirectory()) {
            _log.info("found unpacked bundle with data directory, re-organizing to be flat");
            try {
                moveBundleContents(dataDir, possibleBundle);
            } catch (Throwable t) {
                _log.error("organizing bundle failed:", t);
            }
        } else {
            _log.info("possibleBundle looks unpacked at {}, about to check contents", dataDir);
        }
        File calendarServiceObjectFile = new File(possibleBundle, CALENDAR_DATA);
        if (!calendarServiceObjectFile.exists())
            throw new RuntimeException("expected file " + calendarServiceObjectFile);
        File metadataFile = new File(possibleBundle, METADATA);
        if (!metadataFile.exists()) {
            throw new RuntimeException("expected file " + metadataFile);
        }
        _log.info("bundle looking hopeful at {}", possibleBundle);

        // TODO validate checksums here

        List<BundleItem> bundleItems = new ArrayList<>();
        BundleItem validLocalBundle = createBundleItem(calendarServiceObjectFile, metadataFile, bundleName);
        bundleItems.add(validLocalBundle);
        _log.info("created bundleItems for local bundle {}", possibleBundle);
        return bundleItems;
    }

    private void loadInitial() {
        // list all directories at bundleRoot
        String[] bundleDirectories = new File(this._bundleRootPath).list();
        for (String bundleDirectory : bundleDirectories) {
            File possibleDirectory = new File(this._bundleRootPath, bundleDirectory);
            if (possibleDirectory.isDirectory()) {
                _bundleFolderByDate.put(parseDateFromFolderName(bundleDirectory), new S3BundleItem(bundleDirectory, this._bundleRootPath));
            } else if (possibleDirectory.isFile() && bundleDirectory.endsWith(".tar.gz")) {
                _bundleFileByDate.put(parseDateFromFileName(bundleDirectory), new S3BundleItem(bundleDirectory, this._bundleRootPath));
            } else {
                _log.error("unexpected sub directory {}", bundleDirectory);
            }
        }


    }

    private Date parseDateFromFileName(String bundleFile) {
        if (bundleFile.startsWith("v.")) {
            try {
                String millisStr = bundleFile.substring("v.".length(), bundleFile.length() - ".tar.gz".length());
                long millis = Long.parseLong(millisStr);
                return new Date(millis);
            } catch (Exception any) {
                _log.error("unexpected bundle file {}", bundleFile);
            }
        }
        return new Date();
    }

    private Date parseDateFromFolderName(String bundleDirectory) {
        if (bundleDirectory.startsWith("v.")) {
            try {
                // we have a date in millis
                String millisStr = bundleDirectory.substring("v.".length(), bundleDirectory.length());
                long millis = Long.parseLong(millisStr);
                return new Date(millis);
            } catch (Exception any) {
                _log.error("unexpected bundle directory {}", bundleDirectory);
                return new Date();
            }
        }
        return new Date();
    }

    private boolean isFile(String remoteIndexURI) {
        if (remoteIndexURI == null) return false;
        return remoteIndexURI.startsWith("/")
                || remoteIndexURI.startsWith("file:///");
    }

    private FileOutputStream extractBundleFromInput(InputStream inputStream, String bundleRoot, String bundleName) throws IOException {
        String bundleLocation = bundleRoot + File.separator + bundleName;

        new File(bundleLocation).mkdirs();
        File bundleFile = new File(bundleLocation + ".tar.gz");
        bundleFile.createNewFile();
        _log.info("copying bundle to {}", bundleFile);
        FileOutputStream fileStream = new FileOutputStream(bundleFile);
        IOUtils.copy(inputStream, fileStream);
        _log.info("new bundle at {}", bundleLocation);
        Process exec = Runtime.getRuntime().exec("tar zxvf " + bundleFile + " -C " + bundleRoot);
        try {
            int i = exec.waitFor();
        } catch (InterruptedException e) {
            return null;
        }

        return fileStream;
    }

    private String parseConfigFromStream(InputStream inputStream) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        if (inputStream == null) {
            _log.error("no content for index {}", remoteIndexURI);
            return null;
        }
        JsonParser parser = mapper.createParser(inputStream);
        TreeNode treeNode = parser.readValueAsTree();
        TreeNode latestNode = treeNode.get("latest");
        if (latestNode == null || !latestNode.isValueNode()) {
            _log.error("unexpected format for index {}, {}", remoteIndexURI, parser.getValueAsString());
            return null;
        }
        String latest = latestNode.toString();
        if (latest != null && latest.indexOf("\"") != -1)
            latest = latest.replace("\"", "");

        if (StringUtils.isBlank(latest)) {
            _log.error("no value retrieved for key latest at {}", remoteIndexURI);
            return null;
        }
        _log.info("found latest bundle at {}", latest);
        return latest;
    }

    private boolean bundleExists(String bundleName) {
        File bundleCheck = new File(_bundleRootPath + File.separator + bundleName);
        _log.info("checking on bundle {}", bundleCheck.toString());
        return bundleCheck.exists() && bundleCheck.isDirectory();
    }

    private void moveBundleContents(File srcDir, File destDir) throws IOException {
        _log.info("examining srcDir = {}", srcDir);
        boolean found = false;
        for (String file : srcDir.list()) {
            found = true;
            String dataFileName = srcDir + File.separator + file;
            File dataFile = new File(dataFileName);
            if (dataFile.exists() && dataFile.isFile()) {
                File destFile = new File(destDir + File.separator + file);
                _log.info("copying data bundle item {} to {}", dataFile, destFile);
                Files.move(
                        Paths.get(dataFile.getPath()),
                        Paths.get(destFile.getPath()),
                        StandardCopyOption.REPLACE_EXISTING);
            } else {
                _log.info("skipping copy of {}", dataFile);
            }
        }
        if (!found)
            throw new RuntimeException("no files found for " + srcDir);
    }

    private String getRegion() {
        String specifiedRegion =System.getProperty("oba.cloud.region");
        if (specifiedRegion != null)
            return specifiedRegion;
        return CredentialContainer.DEFAULT_REGION;
    }

    private String getProfile() {
        String specifiedProfile = System.getProperty("oba.cloud.profile");
        if (specifiedProfile != null)
            return specifiedProfile;
        return CredentialContainer.DEFAULT_PROFILE;
    }

    @Override
    public boolean isLegacyBundle() {
        return false;
    }

    public void prune() {
        if (_bundleFolderByDate.size() > 3) {
            List<Date> ages = new ArrayList<>(_bundleFolderByDate.keySet());
            Collections.sort(ages);
            for (int i = 0; i<3; i++) {
                ages.remove(ages.size()-1); // keep the last 3
            }
            for (Date age : ages) {
              S3BundleItem s3BundleItem = _bundleFolderByDate.get(age);
              _log.info("deleting bundle directory {} dated {}", s3BundleItem.bundleName, age);
              deleteFolder(new File(s3BundleItem.bundleRoot, s3BundleItem.bundleName));
              _bundleFolderByDate.remove(age);
            }
        }
        if (_bundleFileByDate.size() > 3) {
            List<Date> ages = new ArrayList<>(_bundleFileByDate.keySet());
            Collections.sort(ages);
            for (int i = 0; i<3; i++) {
                ages.remove(ages.size() - 1); // keep the last 3
            }
            for (Date age : ages) {
              S3BundleItem s3BundleItem = _bundleFileByDate.get(age);
              _log.info("deleting bundle file {} dated {}", s3BundleItem.bundleName, age);
              deleteFile(new File(s3BundleItem.bundleRoot, s3BundleItem.bundleName));
              _bundleFileByDate.remove(age);
            }
        }
    }

    private void deleteFile(File file) {
        try {
            file.delete();
        } catch (Exception any) {
            // bury
        }
    }

    private void deleteFolder(File dir) {
        try {
            FileUtils.deleteDirectory(dir);
        } catch (Exception any) {
            // bury
        }
    }

    public static class S3BundleItem {

        private final String bundleName;
        private final String bundleRoot;
        public S3BundleItem(String bundleName, String bundleRoot) {
            this.bundleName = bundleName;
            this.bundleRoot = bundleRoot;
        }
    }
}
