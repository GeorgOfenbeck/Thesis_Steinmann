package ch.ethz.ruediste.roofline.measurementDriver.measurementControllers;

import static ch.ethz.ruediste.roofline.measurementDriver.util.IterableUtils.addAll;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.exec.ExecuteException;

import ch.ethz.ruediste.roofline.measurementDriver.baseClasses.IMeasurementController;
import ch.ethz.ruediste.roofline.measurementDriver.dom.entities.QuantityCalculator.QuantityCalculator;
import ch.ethz.ruediste.roofline.measurementDriver.dom.entities.plot.DistributionPlot;
import ch.ethz.ruediste.roofline.measurementDriver.dom.quantities.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.*;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.QuantityMeasuringService.MemoryTransferBorder;
import ch.ethz.ruediste.roofline.measurementDriver.dom.services.QuantityMeasuringService.QuantityMap;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.*;
import ch.ethz.ruediste.roofline.sharedEntities.kernels.MMMKernel.MMMAlgorithm;

import com.google.inject.Inject;

public class MMMOpIntensMeasurementController implements IMeasurementController {

	public String getName() {
		return "mmmOp";
	}

	public String getDescription() {
		return "generates a plot showing the operational intensity of the triple loop and blocked";
	}

	@Inject
	public QuantityMeasuringService quantityMeasuringService;

	@Inject
	public PlotService plotService;

	public void measure(String outputName) throws IOException {
		DistributionPlot plot = new DistributionPlot();
		plot.setOutputName(outputName).setxLabel("Matrix Size")
				.setxUnit("Doubles").setyLabel("Operational Intensity")
				.setyUnit("Operations/Byte").setLog().setBoxWidth(0.03);

		DistributionPlot plotMin = new DistributionPlot();
		plotMin.setOutputName(outputName + "Min").setxLabel("Matrix Size")
				.setxUnit("Doubles").setyLabel("Operational Intensity")
				.setyUnit("Operations/Byte").setLog().setBoxWidth(0.03);

		DistributionPlot tlbMissPlot = new DistributionPlot();
		tlbMissPlot.setOutputName(outputName + "Tlb").setxLabel("Matrix Size")
				.setxUnit("Doubles").setyLabel("TLB Misses")
				.setyUnit("1").setLog().setBoxWidth(0.03);

		addTriplePoints(plot, tlbMissPlot);
		addBlockedPoints(plot);
		plotService.plot(plot);
		plotService.plot(tlbMissPlot);
	}

	/**
	 * @param plot
	 * @throws ExecuteException
	 * @throws IOException
	 */
	protected void addBlockedPoints(DistributionPlot plot)
			throws ExecuteException,
			IOException {
		ArrayList<Integer> sizes = new ArrayList<Integer>();
		addAll(sizes, 100, 150, 200, 250, 300, 350, 400, 500, 600, 700
				, 1000
				, 1400
		//, 1500
		//, 2500
		);

		for (int size : sizes) {
			System.out.println(size);
			MMMKernel kernel = new MMMKernel();
			kernel.setOptimization("-O3");
			kernel.setAlgorithm(MMMAlgorithm.MMMAlgorithm_Blocked_Restrict);
			kernel.setMatrixSize(size);
			kernel.setNb(50);
			kernel.setNu(2);
			kernel.setMu(2);
			kernel.setKu(2);

			QuantityCalculator<TLBMisses> tlbCalc = quantityMeasuringService
					.getTLBMissesCalculator();

			QuantityCalculator<TransferredBytes> tbCalc = quantityMeasuringService
					.getTransferredBytesCalculator(MemoryTransferBorder.LlcRamLines);

			QuantityMap result = quantityMeasuringService.measureQuantities(
					kernel, size <= 400 ? 10 : 1, tlbCalc, tbCalc);

			double opCount = 2 * Math.pow(size, 3);

			plot.addValue(kernel.getLabel(), size, opCount
					/ result.min(tbCalc).getValue());

			plot.addValue("Blocked Theoretical", size,
					thOpIntBlocked(size, 0));

			/*plot.addValue("Blocked Theoretical wit TLB", size,
					thOpIntBlocked(size, result.min(tlbCalc).getValue()));
			System.out.printf("tlb: %e\n", result.min(tlbCalc).getValue());*/

			/*plot.addValue(kernel.getLabel(), size, result.min(tlbCalc)
					.getValue()
					/ (Math.pow(size / 50., 3)
					* 3.
					* Math.min(50. * size * 8. / (1024 * 4), 50)));*/
		}

		String thName = "Blocked Theoretical";
		plot.addValue(thName, 5000, thOpIntBlocked(5000, 0));
		plot.addValue(thName, 5500, thOpIntBlocked(5500, 0));
		plot.addValue(thName, 7000, thOpIntBlocked(7000, 0));

	}

	private double thOpIntBlocked(double n, double tlbMisses) {
		double k = 2 * 1024 * 1024;
		double nb = 50;
		double ops = 2 * Math.pow(n, 3);
		double tlbOverhead = tlbMisses * 64;

		if (n < Math.sqrt(k / 8)) {
			return ops
					/ (tlbOverhead + 3 * n * n * 8 + Math.max(
							n * n * 8 - Math.max(k - n * n * 8, 0),
							0));
		}
		if (n < k / (nb * 8)) {
			return ops / (tlbOverhead + (3 * n * n + n * n * n / nb) * 8);
		}
		return ops / (tlbOverhead + (2 * n * n + 2 * n * n * n / nb) * 8);
	}

	private double thOpIntTriple(double n, double tlbMisses) {
		double k = 2 * 1024 * 1024;
		double nb = 50;
		double ops = 2 * Math.pow(n, 3);
		double tlbOverhead = tlbMisses * 64;

		if (n < Math.sqrt(k / 8)) {
			return ops
					/ (tlbOverhead + 3 * n * n * 8 + Math.max(
							n * n * 8 - Math.max(k - n * n * 8, 0),
							0));
		}
		if (n < k / (nb * 8)) {
			return ops / (tlbOverhead + (3 * n * n + n * n * n) * 8);
		}
		return ops / (tlbOverhead + (2 * n * n + 2 * n * n * n / nb) * 8);
	}

	double thTlbMissesTriple(double size) {
		double pagesPerColumn = Math.min(size * size * 8 / (4 * 1024), size);
		if (pagesPerColumn < 128) {
			return 0;
		}
		return Math.pow(size, 2) * pagesPerColumn;
	}

	/**
	 * @param plot
	 * @param tlbMissPlot
	 */
	protected void addTriplePoints(DistributionPlot plot,
			DistributionPlot tlbMissPlot) {
		ArrayList<Integer> sizes = new ArrayList<Integer>();
		addAll(sizes, 20, 50, 70, 75, 100, 105, 150, 200, 225, 250, 275, 280,
				285, 290,
				300,
				325,
				350, 400,
				500, 520, 700,
				1000);

		for (int size : sizes) {
			MMMKernel kernel = new MMMKernel();
			kernel.setOptimization("-O3");
			kernel.setAlgorithm(MMMAlgorithm.MMMAlgorithm_TripleLoop);
			kernel.setMatrixSize(size);

			QuantityCalculator<TransferredBytes> tbCalc = quantityMeasuringService
					.getTransferredBytesCalculator(MemoryTransferBorder.LlcRamLines);

			QuantityCalculator<TLBMisses> tlbCalc = quantityMeasuringService
					.getTLBMissesCalculator();

			QuantityMap result = quantityMeasuringService.measureQuantities(
					kernel, size < 400 ? 10 : 1,
					tbCalc, tlbCalc);

			tlbMissPlot.addValues(kernel.getLabel(), size,
					result.getStatistics(tlbCalc));
			tlbMissPlot.addValue("Theoretical", size, thTlbMissesTriple(size));

			System.out.printf("Triple: %d TLBMisses: %e\n", size,
					result.min(tlbCalc)
							.getValue());
			double opCount = 2 * Math.pow(size, 3);
			plot.addValue(kernel.getLabel(), size, opCount
					/ result.min(tbCalc).getValue());

			plot.addValue("Triple Theoretical", size, thOpIntTriple(size, 0));

		}
	}

}
