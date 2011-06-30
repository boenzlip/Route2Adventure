package net.orxonox.gpr.store;

import static java.lang.Double.POSITIVE_INFINITY;
import static java.lang.Math.abs;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.builder.CompareToBuilder;

public class ArrayDijkstra {
	static final int HEIGHT = 0;
	static final int DISTANCE = 1;
	static final int P_X = 2;
	static final int P_Y = 3;
	static final int X = 0;
	static final int Y = 1;
	private static final double DELTA_IN_M = 100.;
	private static final double VERTICAL_FACTOR = 13.0;

	private final Double[][][] graph;

	public ArrayDijkstra(final Double[][][] graph) {
		this.graph = graph;
	}

	/**
	 * @param graph
	 *            [x][y] - [0]: height, [1]: distance, [2]: previous x, [3]:
	 *            previous y
	 * @param start
	 * @param end
	 * @return a {@link List} from goal to start representing the shortest path.
	 *         <code>null</code> if start or goal lies out of the graph's
	 *         bounds.
	 */
	public List<int[]> shortestPath(final int[] start, final int[] goal) {
		if (!inBounds(start[X], start[Y]) || !inBounds(goal[X], goal[Y])) {
			return null;
		}

		// init the Q sorted by distance
		SortedSet<Node> q = initQ();

		// set start's distance to 0.
		graph[start[X]][start[Y]][DISTANCE] = 0.;

		while (!q.isEmpty()) {
			Node current = q.first();
			q.remove(current);

			// unreachable?
			if (current.getDistance() == POSITIVE_INFINITY) {
				break;
			}

			// end early if goal was found
			if (current.x == goal[X] && current.y == goal[Y]) {
				return current.toList();
			}

			for (Node n : current.expand()) {
				Double d = current.getDistance() + current.distanceTo(n);

				if (d < n.getDistance()) {
					q.remove(n);

					n.setDistance(d);

					n.setPrevious(current.x, current.y);

					q.add(n);
				}
			}
		}

		return null;
	}

	private SortedSet<Node> initQ() {
		SortedSet<Node> q = new TreeSet<Node>();
		for (int x = 0; x < graph.length; x++) {
			for (int y = 0; y < graph[x].length; y++) {
				graph[x][y][DISTANCE] = POSITIVE_INFINITY;
				q.add(new Node(x, y));
			}
		}

		return q;
	}

	/**
	 * Simple {@link Node} that is backed by the given graph
	 * 
	 */
	private class Node implements Comparable<Node> {
		final int x, y;

		public Double getDistance() {
			return graph[x][y][DISTANCE];
		}

		public void setDistance(Double distance) {
			graph[x][y][DISTANCE] = distance;
		}

		public Double getHeight() {
			return graph[x][y][HEIGHT];
		}

		public void setPrevious(int px, int py) {
			graph[x][y][P_X] = (double) px;
			graph[x][y][P_Y] = (double) py;
		}

		public int[] getPrevious() {
			if (graph[x][y][P_X] != null && graph[x][y][P_Y] != null) {
				return new int[] { graph[x][y][P_X].intValue(),
						graph[x][y][P_Y].intValue() };
			}

			return null;
		}

		public Node(int x, int y) {
			this.x = x;
			this.y = y;
		}

		public Double distanceTo(Node that) {
			// TODO introduce distance function
			double delta = euclideanDistance(this, that);
			return abs(this.getHeight() - that.getHeight()) * VERTICAL_FACTOR
					+ delta;
		}

		public List<Node> expand() {
			List<Node> result = new ArrayList<Node>();

			createAndAdd(result, x, y + 1);
			createAndAdd(result, x, y - 1);
			createAndAdd(result, x + 1, y);
			createAndAdd(result, x + 1, y + 1);
			createAndAdd(result, x + 1, y - 1);
			createAndAdd(result, x - 1, y);
			createAndAdd(result, x - 1, y + 1);
			createAndAdd(result, x - 1, y - 1);

			return result;
		}

		private void createAndAdd(List<Node> result, int x, int y) {
			Node node = createNode(x, y);
			if (node != null) {
				result.add(node);
			}
		}

		public int compareTo(Node that) {
			CompareToBuilder cp = new CompareToBuilder();
			cp.append(this.getDistance(), that.getDistance());
			cp.append(this.x, that.x);
			cp.append(this.y, that.y);

			return cp.toComparison();
		}

		/**
		 * @return a {@link List} of all previous x,y coordinates where the
		 *         first element is the goal
		 */
		public List<int[]> toList() {
			List<int[]> result = new ArrayList<int[]>();
			int[] current = new int[] { x, y };
			result.add(current);

			while (current != null) {
				if (graph[current[X]][current[Y]][P_X] != null
						&& graph[current[X]][current[Y]][P_Y] != null) {
					int px = graph[current[X]][current[Y]][P_X].intValue();
					int py = graph[current[X]][current[Y]][P_Y].intValue();

					current = new int[] { px, py };
					result.add(current);
				} else {
					current = null;
				}
			}

			return result;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("[x=");
			sb.append(this.x);
			sb.append(", y=");
			sb.append(this.y);
			sb.append(", h=");
			sb.append(this.getHeight());
			sb.append(", d=");
			sb.append(this.getDistance());
			sb.append("] previous: ");
			sb.append("[");
			sb.append(this.getPrevious());
			sb.append("]");

			return sb.toString();
		}
	}

	/**
	 * @param a
	 * @param b
	 * @return the distance between two {@link Node}s
	 */
	private static double euclideanDistance(final Node a, final Node b) {
		return sqrt(pow(a.x - b.x, 2.) + pow(a.y - b.y, 2.)) * DELTA_IN_M;
	}

	/**
	 * @param x
	 * @param y
	 * @return a new {@link Node} if not out of the array graph's bounds, null
	 *         otherwise.
	 */
	private Node createNode(int x, int y) {
		if (inBounds(x, y)) {
			return new Node(x, y);
		} else {
			return null;
		}
	}

	private boolean inBounds(int x, int y) {
		return (x >= 0 && y >= 0 && x < graph.length && y < graph[x].length);
	}
}
