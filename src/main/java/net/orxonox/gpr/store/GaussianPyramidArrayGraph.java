package net.orxonox.gpr.store;

import java.util.ArrayList;
import java.util.List;

public class GaussianPyramidArrayGraph {
	private static final int DEFAULT_NUMBER_OF_LEVELS = 3;
	private GaussianPyramidCalculator calculator = new GaussianPyramidCalculator();

	private List<Double[][][]> pyramid = new ArrayList<Double[][][]>();

	/**
	 * @param graph
	 *            the real graph full resolution
	 */
	public GaussianPyramidArrayGraph(final Double[][][] graph) {
		this(graph, DEFAULT_NUMBER_OF_LEVELS);
	}

	/**
	 * @param graph
	 * @param numberOfLevels
	 *            of the Gaussian pyramid where the provided graph is included
	 */
	public GaussianPyramidArrayGraph(final Double[][][] graph,
			int numberOfLevels) {
		this.calculateLevels(graph, numberOfLevels);
	}

	public Double[][][] getGraphAtLevel(final int level) {
		return pyramid.get(level);
	}

	private void calculateLevels(final Double[][][] level0, int numberOfLevels) {
		pyramid.add(level0);

		Double[][][] currentLevel = level0;

		for (int l = 1; l < numberOfLevels; l++) {
			currentLevel = calculator.downSample(currentLevel);
			pyramid.add(currentLevel);
		}
	}
}
