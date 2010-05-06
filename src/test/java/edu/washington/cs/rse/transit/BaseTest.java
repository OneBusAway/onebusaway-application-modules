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

import com.carbonfive.testutils.spring.dbunit.DataSetTestExecutionListener;

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;

@ContextConfiguration(locations = {
    "applicationContext-test.xml", "/applicationContext-server.xml",
    "data-sources-test.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
@TransactionConfiguration
@TestExecutionListeners( {
    MyTestHandler.class, DataSetTestExecutionListener.class})
public class BaseTest extends AbstractTransactionalJUnit4SpringContextTests {

}
