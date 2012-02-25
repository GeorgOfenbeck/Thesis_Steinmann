package ch.ethz.ruediste.roofline.test;

import static junit.framework.Assert.assertEquals;

import org.junit.Test;

import ch.ethz.ruediste.roofline.measurementDriver.dom.Histogram;

public class StatisticsTest {
	@Test
	public void testHistogram() {
		Histogram hist = new Histogram();
		hist.apply(1.);
		hist.apply(2.);
		hist.apply(3.);
		hist.apply(4.);

		int[] counts = hist.getCounts(4);

		assertEquals(1, counts[0]);
		assertEquals(1, counts[1]);
		assertEquals(1, counts[2]);
		assertEquals(1, counts[3]);
	}
}
