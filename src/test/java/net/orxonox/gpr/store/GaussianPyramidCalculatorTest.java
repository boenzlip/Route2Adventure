package net.orxonox.gpr.store;

import static net.orxonox.gpr.store.ArrayDijkstra.HEIGHT;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Ignore;
import org.junit.Test;

public class GaussianPyramidCalculatorTest {
	@Test
	public void shouldSampleDown4x4to1x1() {
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

		GaussianPyramidCalculator calculator = new GaussianPyramidCalculator();

		// when
		Double[][][] result = calculator.downSample(level0, 2);

		// then
		assertThat(result, is(notNullValue()));
		assertThat(result.length, is(1));
		assertThat(result[0].length, is(1));
		assertThat(result[0][0][HEIGHT], is(closeTo(100., 0.1)));
	}

	@Test
	public void shouldSampleDown512x512to32x32() {
		// given
		Double[][][] level0 = new Double[512][512][4];
		for (int x = 0; x < level0.length; x++) {
			for (int y = 0; y < level0[0].length; y++) {
				level0[x][y][HEIGHT] = 100.;
			}
		}

		GaussianPyramidCalculator calculator = new GaussianPyramidCalculator();

		// when
		Double[][][] result = calculator.downSample(level0, 4);

		// then
		assertThat(result, is(notNullValue()));
		assertThat(result.length, is(32));
		assertThat(result[0].length, is(32));
	}

	@Test
	@Ignore
	public void shouldSampleDown5000x5000to625x625() {
		// given
		Double[][][] level0 = new Double[5000][5000][4];
		for (int x = 0; x < level0.length; x++) {
			for (int y = 0; y < level0[0].length; y++) {
				level0[x][y][HEIGHT] = 100.;
			}
		}

		GaussianPyramidCalculator calculator = new GaussianPyramidCalculator();

		// when
		Double[][][] result = calculator.downSample(level0, 3);

		// then
		assertThat(result, is(notNullValue()));
		assertThat(result.length, is(625));
		assertThat(result[0].length, is(625));
	}
}
