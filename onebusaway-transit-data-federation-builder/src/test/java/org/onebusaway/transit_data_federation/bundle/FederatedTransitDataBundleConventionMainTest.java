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
package org.onebusaway.transit_data_federation.bundle;

import junit.framework.TestCase;
import org.junit.Ignore;
import org.onebusaway.transit_data_federation.impl.bundle.S3BundleStoreImpl;
import org.onebusaway.transit_data_federation.model.bundle.BundleItem;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

public class FederatedTransitDataBundleConventionMainTest extends TestCase {



    @Ignore
    public void testRun() throws Exception {
        if (true) return;
        FederatedTransitDataBundleConventionMain main = new FederatedTransitDataBundleConventionMain();
        String bundleOutputDir =
        System.getProperty("java.io.tmpdir") + File.separator
                + "bundle" + System.currentTimeMillis();
        String bundleName = "v"+System.currentTimeMillis();
        String[] args = {"classpath:multi_dir_test", bundleOutputDir, bundleName};
        main.run(args);
        // now create index file for this bundle
        String indexFile = System.getProperty("java.io.tmpdir") + File.separator
                + "test_bundle_index_file.json";
        String bundleFile = bundleOutputDir + File.separator + bundleName + File.separator
                + bundleName + ".tar.gz";
        FileWriter fw = new FileWriter(indexFile);
        fw.write("{\"latest\":\"" + bundleFile + "\"}");
        fw.close();
        System.out.println("index file written to " + indexFile);
        File testBundleFile = new File(bundleFile);

        assertTrue(testBundleFile.exists());
        assertTrue(testBundleFile.isFile());

        // now attempt to load the bundle from the indexfile
        testS3BundleStoreImpl(indexFile, bundleName);
        // cleanup after ourselves
        // comment this out if you want to use these bundle for testing.
        testBundleFile.delete();
        new File(indexFile).delete();
    }

    private void testS3BundleStoreImpl(String indexFile, String bundleName) throws Exception {
        String bundleRootPath = "/tmp/bundleTest" + System.currentTimeMillis();
        System.out.println("loading '" + indexFile + "' to store at " + bundleRootPath);
        S3BundleStoreImpl impl = new S3BundleStoreImpl(bundleRootPath, indexFile);
        List<BundleItem> bundles = impl.getBundles();
        assertNotNull(bundles);
        assertEquals(1, bundles.size());
        BundleItem item = bundles.get(0);
        assertEquals(bundleName, item.getName());
    }
}