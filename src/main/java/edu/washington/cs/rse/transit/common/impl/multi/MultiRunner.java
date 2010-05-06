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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MultiRunner<T> {

    private static Logger _log = Logger.getLogger(MultiRunner.class.getName());

    public static <T> MultiRunner<T> create(int workers, MultiOperation<T> operation, Iterable<T> elements) {
        return new MultiRunner<T>(workers, operation, elements);
    }

    private int _workers;

    private int _workersWaitingForNext = 0;

    private int _entryIndex = 0;

    private MultiOperation<T> _operation;

    public LinkedList<T> _active = new LinkedList<T>();

    private SortedSet<TimedEntry> _inactive = new TreeSet<TimedEntry>();

    private boolean _exit = false;

    private List<Thread> _threads = new ArrayList<Thread>();

    public MultiRunner(int workers, MultiOperation<T> operation, Iterable<T> elements) {
        _workers = workers;
        _operation = operation;
        for (T element : elements)
            _active.add(element);
    }

    public synchronized void start() {

        _exit = false;

        for (int i = 0; i < _workers; i++) {
            Thread thread = new Thread(new Worker());
            _threads.add(thread);
            thread.start();
        }
    }

    public synchronized void doExit() {
        _exit = true;
        notifyAll();
    }

    public void waitForExit() {
        doExit();
        waitForCompletion();
    }

    public void waitForCompletion() {
        for (Thread t : _threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                return;
            }
        }
        _threads.clear();
    }

    /***************************************************************************
     * 
     **************************************************************************/

    private synchronized boolean wantsExit() {
        return _exit;
    }

    private synchronized T getNext() {
        _workersWaitingForNext++;
        T next = getNextInner();
        _workersWaitingForNext--;
        return next;
    }

    private T getNextInner() {

        while (true) {

            long now = System.currentTimeMillis();
            long t = updateInactiveEntries(now);

            if (!_active.isEmpty())
                return _active.removeFirst();

            if (_active.isEmpty() && _inactive.isEmpty() && _workersWaitingForNext == _workers) {
                System.err.println("No more elements: " + _operation);
                doExit();
                return null;
            }

            try {
                wait(t);
                if (_exit)
                    return null;
            } catch (InterruptedException ex) {
                return null;
            }
        }
    }

    private synchronized void addInternal(T element, long delay) {

        if (element == null) {
            _log.warning("null element from op=" + _operation);
            return;
        }

        if (delay <= 0) {
            _active.add(element);
            notifyAll();
        } else {
            long t = System.currentTimeMillis() + delay;
            _inactive.add(new TimedEntry(element, t, _entryIndex++));
            notifyAll();
        }
    }

    private long updateInactiveEntries(long now) {

        while (!_inactive.isEmpty()) {
            TimedEntry entry = _inactive.first();
            if (entry.getTime() > now)
                return (entry.getTime() - now);
            _inactive.remove(entry);
            _active.add(entry.getElement());
        }

        return 0;
    }

    /***************************************************************************
     * 
     **************************************************************************/

    private class Worker implements Runnable {

        public void run() {
            while (!wantsExit()) {

                T element = getNext();

                if (element == null)
                    return;

                try {
                    ContextImpl context = new ContextImpl();
                    _operation.evaluate(context, element);
                } catch (Exception ex) {
                    _log.log(Level.WARNING, "error executing task for element=" + element, ex);
                }
            }
        }
    }

    private class ContextImpl implements MultiContext<T> {

        public boolean wantsExit() {
            return MultiRunner.this.wantsExit();
        }

        public void add(T element) {
            addInternal(element, 0);
        }

        public void add(T element, long delay) {
            addInternal(element, delay);
        }
    }

    private class TimedEntry implements Comparable<TimedEntry> {

        private T _element;

        private long _time;

        private int _index;

        public TimedEntry(T element, long time, int index) {
            _element = element;
            _time = time;
            _index = index;
        }

        public T getElement() {
            return _element;
        }

        public long getTime() {
            return _time;
        }

        public int compareTo(TimedEntry o) {
            long a = _time;
            long b = o._time;
            if (a == b)
                return _index == o._index ? 0 : (_index < o._index ? -1 : 1);
            return a == b ? 0 : (a < b ? -1 : 1);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || !(obj.getClass().equals(this.getClass())))
                return false;
            TimedEntry en = (TimedEntry) obj;
            return _index == en._index && _time == en._time;
        }

        @Override
        public int hashCode() {
            return (int) (_index * 13 + _time);
        }
    }
}
