package ch.ethz.ruediste.roofline.test;

import static junit.framework.Assert.assertEquals;

import org.junit.Test;

import ch.ethz.ruediste.roofline.measurementDriver.dom.Histogram;

public class StatisticsTest {
	@Test
	public void testHistogram() {
		Histogram hist = new Histogram();
		hist.addValue(1);
		hist.addValue(2);
		hist.addValue(3);
		hist.addValue(4);

		int[] counts = hist.getCounts(4);

		assertEquals(1, counts[0]);
		assertEquals(1, counts[1]);
		assertEquals(1, counts[2]);
		assertEquals(1, counts[3]);
	}
}
