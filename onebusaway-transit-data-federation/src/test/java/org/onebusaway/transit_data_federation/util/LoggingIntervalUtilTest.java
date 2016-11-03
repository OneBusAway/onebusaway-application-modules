package org.onebusaway.transit_data_federation.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class LoggingIntervalUtilTest {

	@Test
	public void test() {
		LoggingIntervalUtil l = new LoggingIntervalUtil();
		
		assertEquals(Integer.valueOf(l.getAppropriateLoggingInterval(16777216)), Integer.valueOf(1000000));
		assertEquals(Integer.valueOf(l.getAppropriateLoggingInterval(10)), Integer.valueOf(1));
	}

}
