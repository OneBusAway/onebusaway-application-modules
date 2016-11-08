package org.onebusaway.transit_data_federation.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class LoggingIntervalUtilTest {

	@Test
	public void test() {
				
		assertEquals(LoggingIntervalUtil.getAppropriateLoggingInterval(16777216), 1000000);
		assertEquals(LoggingIntervalUtil.getAppropriateLoggingInterval(10), 1);
		assertEquals(LoggingIntervalUtil.getAppropriateLoggingInterval(-10), 1);
		assertEquals(LoggingIntervalUtil.getAppropriateLoggingInterval(Integer.MIN_VALUE), 1);
		assertTrue(LoggingIntervalUtil.getAppropriateLoggingInterval(Integer.MAX_VALUE) > 1000000);
	}

}
