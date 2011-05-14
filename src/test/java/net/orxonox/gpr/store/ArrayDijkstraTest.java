package net.orxonox.gpr.store;

import static java.lang.Math.random;
import static net.orxonox.gpr.store.ArrayDijkstra.HEIGHT;
import static net.orxonox.gpr.store.ArrayDijkstra.X;
import static net.orxonox.gpr.store.ArrayDijkstra.Y;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

public class ArrayDijkstraTest {
	@Test
	public void shouldFindSimpleShortestPath() {
		// given
		// x - -
		// - x -
		// - x -
		Double[][][] graph = new Double[3][3][4];

		graph[0][0][HEIGHT] = 0.;
		graph[0][1][HEIGHT] = 1000.;
		graph[0][2][HEIGHT] = 1000.;
		graph[1][0][HEIGHT] = 1000.;
		graph[1][1][HEIGHT] = 100.;
		graph[1][2][HEIGHT] = 200.;
		graph[2][0][HEIGHT] = 1000.;
		graph[2][1][HEIGHT] = 1000.;
		graph[2][2][HEIGHT] = 1000.;

		ArrayDijkstra arrayDijkstra = new ArrayDijkstra(graph);

		// when
		List<int[]> shortestPath = arrayDijkstra.shortestPath(
				new int[] { 0, 0 }, new int[] { 1, 2 });

		// then
		int[] node12 = shortestPath.get(0);
		assertTrue(node12[X] == 1 && node12[Y] == 2);

		int[] node11 = shortestPath.get(1);
		assertTrue(node11[X] == 1 && node11[Y] == 1);

		int[] node00 = shortestPath.get(2);
		assertTrue(node00[X] == 0 && node00[Y] == 0);
	}

	@Test
	public void shouldFindDiagonalRouteOnEvenHeightGraph() {
		// given
		int dimension = 500;
		Double[][][] graph = new Double[dimension][dimension][4];

		for (int x = 0; x < graph.length; x++) {
			for (int y = 0; y < graph[x].length; y++) {
				graph[x][y][HEIGHT] = 100.;
			}
		}

		ArrayDijkstra arrayDijkstra = new ArrayDijkstra(graph);

		// when
		List<int[]> shortestPath = arrayDijkstra.shortestPath(
				new int[] { 0, 0 }, new int[] { dimension - 1, dimension - 1 });

		// then
		assertFalse(shortestPath.isEmpty());

		Iterator<int[]> it = shortestPath.iterator();
		for (int xy = dimension - 1; xy >= 0; xy--) {
			int[] current = it.next();
			assertTrue(current[X] == xy && current[Y] == xy);
		}
	}

	@Test
	@Ignore
	public void shouldFindRouteOnRandomHeightGraph() {
		// given
		int dimension = 5000;
		Double[][][] graph = new Double[dimension][dimension][4];

		for (int x = 0; x < graph.length; x++) {
			for (int y = 0; y < graph[x].length; y++) {
				graph[x][y][HEIGHT] = random() * 1000.;
			}
		}

		ArrayDijkstra arrayDijkstra = new ArrayDijkstra(graph);

		// when
		List<int[]> shortestPath = arrayDijkstra.shortestPath(
				new int[] { 0, 0 }, new int[] { dimension - 1, dimension - 1 });

		// then
		assertFalse(shortestPath.isEmpty());
	}
}
