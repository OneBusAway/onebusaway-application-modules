/**
 * Copyright (C) 2026 Open Transit Software Foundation
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
package org.onebusaway.users.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.DelegatingDataSource;

/**
 * Counts connection borrows. In production every borrow from the pool can
 * cost a database round trip (validation on borrow, autocommit toggling), so
 * a borrow is the thing to avoid on hot, cacheable paths.
 */
public class ConnectionCountingDataSource extends DelegatingDataSource {

  private final AtomicInteger _connections = new AtomicInteger();

  public ConnectionCountingDataSource(DataSource target) {
    super(target);
  }

  @Override
  public Connection getConnection() throws SQLException {
    _connections.incrementAndGet();
    return super.getConnection();
  }

  public int getConnectionCount() {
    return _connections.get();
  }

  public void reset() {
    _connections.set(0);
  }
}
