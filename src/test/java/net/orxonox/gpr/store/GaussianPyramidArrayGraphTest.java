package net.orxonox.gpr.store;

import static net.orxonox.gpr.store.ArrayDijkstra.HEIGHT;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class GaussianPyramidArrayGraphTest {
	@Test
	public void shouldSampleDown4x4to2x2() {
		// given
		Double[][][] level0 = new Double[4][4][4];
		level0[0][0][HEIGHT] = 100.;
		level0[0][1][HEIGHT] = 100.;
		level0[0][2][HEIGHT] = 100.;
		level0[0][3][HEIGHT] = 100.;
		level0[1][0][HEIGHT] = 100.;
		level0[1][1][HEIGHT] = 100.;
		level0[1][2][HEIGHT] = 100.;
		level0[1][3][HEIGHT] = 100.;
		level0[2][0][HEIGHT] = 100.;
		level0[2][1][HEIGHT] = 100.;
		level0[2][2][HEIGHT] = 100.;
		level0[2][3][HEIGHT] = 100.;
		level0[3][0][HEIGHT] = 100.;
		level0[3][1][HEIGHT] = 100.;
		level0[3][2][HEIGHT] = 100.;
		level0[3][3][HEIGHT] = 100.;

		System.out.println(toGraphString(level0));

		// when
		GaussianPyramidArrayGraph gaussianPyramid = new GaussianPyramidArrayGraph(
				level0, 2);

		// then
		assertThat(gaussianPyramid.getGraphAtLevel(1), is(notNullValue()));

		System.out.println(toGraphString(gaussianPyramid.getGraphAtLevel(1)));
	}

	private String toGraphString(Double[][][] graph) {
		StringBuilder sb = new StringBuilder();
		int cellWidth = 7;

		for (int y = 0; y < graph[0].length; y++) {
			for (int x = 0; x < graph.length; x++) {
				sb.append("|");
				String height = graph[x][y][HEIGHT].toString();
				int spaces = cellWidth - height.length();
				for (int i = 0; i < spaces; i++) {
					sb.append(" ");
				}
				sb.append(height);
			}
			sb.append("|\n");
		}

		return sb.toString();
	}
}
