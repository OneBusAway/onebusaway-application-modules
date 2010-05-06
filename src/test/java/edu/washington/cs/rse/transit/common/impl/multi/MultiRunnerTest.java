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
package edu.washington.cs.rse.transit.common.impl.multi;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

public class MultiRunnerTest extends TestCase {

    public void testSimple01() throws InterruptedException {

        Set<Integer> ids = new HashSet<Integer>();
        for (int i = 0; i < 10; i++)
            ids.add(i);

        final Set<Integer> received = new HashSet<Integer>();

        MultiOperation<Integer> op = new MultiOperation<Integer>() {
            public void evaluate(MultiContext<Integer> context, Integer entry) {
                received.add(entry);
            }
        };

        MultiRunner<Integer> r1 = MultiRunner.create(2, op, ids);
        r1.start();
        r1.waitForCompletion();
        assertEquals(ids, received);
    }

    public void testSimple02() throws InterruptedException {

        Set<Integer> ids = new HashSet<Integer>();
        for (int i = 0; i < 10; i++)
            ids.add(i);

        final Set<Integer> receivedA = new HashSet<Integer>();
        final Set<Integer> receivedB = new HashSet<Integer>();

        MultiOperation<Integer> op = new MultiOperation<Integer>() {
            public void evaluate(MultiContext<Integer> context, Integer entry) {
                if (receivedA.add(entry))
                    context.add(entry);
                else
                    receivedB.add(entry);
            }
        };

        MultiRunner<Integer> r1 = MultiRunner.create(2, op, ids);
        r1.start();
        r1.waitForCompletion();
        assertEquals(ids, receivedA);
        assertEquals(ids, receivedB);
    }

    public void testSimple03() throws InterruptedException {

        Set<Integer> ids = new HashSet<Integer>();
        for (int i = 0; i < 10; i++)
            ids.add(i);

        final Set<Integer> receivedA = new HashSet<Integer>();
        final Set<Integer> receivedB = new HashSet<Integer>();

        MultiOperation<Integer> op = new MultiOperation<Integer>() {
            public void evaluate(MultiContext<Integer> context, Integer entry) {
                if (receivedA.add(entry)) {
                    context.add(entry, (long) (5000 * Math.random()));
                } else {
                    receivedB.add(entry);
                }
            }
        };

        MultiRunner<Integer> r1 = MultiRunner.create(1, op, ids);
        r1.start();
        r1.waitForCompletion();
        assertEquals(ids, receivedA);
        assertEquals(ids, receivedB);
    }
}
