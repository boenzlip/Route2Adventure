package net.orxonox.gpr.store;

import static java.lang.Math.abs;
import static net.orxonox.gpr.store.ArrayDijkstra.HEIGHT;

public class GaussianPyramidCalculator {
	private static final double[][] KERNEL = new double[][] {
			{ 1, 1, 1, 1, 1 }, { 1, 4, 4, 4, 1 }, { 1, 4, 6, 4, 1 },
			{ 1, 4, 4, 4, 1 }, { 1, 1, 1, 1, 1 } };

	/**
	 * @param graph
	 * @return the down-sampled graph of the given graph (half the resolution)
	 */
	public Double[][][] downSample(final Double[][][] graph) {
		return downSample(graph, 1);
	}

	/**
	 * @param graph
	 * @param n
	 * @return the n times down-sampled graph
	 */
	public Double[][][] downSample(final Double[][][] graph, int n) {
		// 0 times down-sampled is the same graph
		Double[][][] downSampled = graph;

		for (int times = 1; times <= n; times++) {
			downSampled = new Double[downSampled.length / 2][downSampled[0].length / 2][downSampled[0][0].length];

			for (int x = 0; x < downSampled.length; x++) {
				for (int y = 0; y < downSampled.length; y++) {
					downSampled[x][y][HEIGHT] = gaussian(graph, x, y);
				}
			}
		}

		return downSampled;

	}

	private Double gaussian(final Double[][][] l_1, final int x, final int y) {
		double g = 0.;
		for (int m = -2; m <= 2; m++) {
			for (int n = -2; n <= 2; n++) {

				// mirror at axis borders
				int i = abs(2 * x + m);
				int j = abs(2 * y + n);

				// mirror at length borders
				if (i > l_1.length - 1) {
					i = 2 * l_1.length - 1 - i;
				}
				if (j > l_1[0].length - 1) {
					j = 2 * l_1[0].length - 1 - j;
				}

				g += w(m, n) * l_1[i][j][HEIGHT];
			}
		}

		return g;
	}

	private double w(int m, int n) {
		// using 5-tap kernel
		return 1 / 54. * (KERNEL[m + 2][n + 2]);
	}
}
