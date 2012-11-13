package com.xebia.graph.neo4j.plugins;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Stack;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ShortestPathTreeCreator {
	private Map<Node, List<Node>> predecessors = Maps.newHashMap();
	private PriorityQueue<Node> queue = new PriorityQueue<Node>(13, new SptNodeComparator());
	private Stack<Node> stack = new Stack<Node>();
	private Map<Node, Double> sptNodeDistancesToRootNode = Maps.newHashMap();
	private String weightPropertyName;
	private List<Long> nodeIdsToProcess = null;

	private class SptNodeComparator implements Comparator<Node> {

		public int compare(Node n1, Node n2) {
			return Double.compare(sptNodeDistancesToRootNode.get(n1), sptNodeDistancesToRootNode.get(n2));
		}

	}

	public ShortestPathTreeCreator(String weightPropertyName) {
		this.weightPropertyName = weightPropertyName;
	}

	public ShortestPathTreeCreator(String weightPropertyName, List<Long> nodesInSubGraph) {
		this.weightPropertyName = weightPropertyName;
		this.nodeIdsToProcess = nodesInSubGraph;
	}
	
	public ShortestPathTree createShortestPathTree(Node rootNode) {
		initNodes(rootNode);

		while (!queue.isEmpty()) {
			Node minimumDistanceNode = queue.poll();
			if (nodeIdsToProcess == null || nodeIdsToProcess.contains(minimumDistanceNode.getId())) {
				stack.push(minimumDistanceNode);

				for (Relationship edge : minimumDistanceNode.getRelationships()) {
					Node potentialShortestPathNode = edge.getOtherNode(minimumDistanceNode);
					double connectionDistance = 1.0 / (Double) edge.getProperty(weightPropertyName);
					double minimumDistance = getDistance(minimumDistanceNode);
					double potentialShortestPathNodeDistance = getDistance(potentialShortestPathNode);

					if (potentialShortestPathNodeDistance > minimumDistance + connectionDistance) {
						sptNodeDistancesToRootNode.put(potentialShortestPathNode, minimumDistance + connectionDistance);

						if (queue.contains(potentialShortestPathNode)) {
							queue.remove(potentialShortestPathNode);
						}

						queue.add(potentialShortestPathNode);
						predecessors.put(potentialShortestPathNode, new ArrayList<Node>());
					}

					if (sptNodeDistancesToRootNode.get(potentialShortestPathNode) == minimumDistance
							+ connectionDistance) {
						predecessors.get(potentialShortestPathNode).add(minimumDistanceNode);
					}
				}
			}
		}

		return new ShortestPathTree(stack, predecessors);
	}

	private double getDistance(Node node) {
		Double distance = sptNodeDistancesToRootNode.get(node);
		return (distance == null) ? Double.POSITIVE_INFINITY : distance.doubleValue();
	}

	void initNodes(Node rootNode) {
		sptNodeDistancesToRootNode.clear();
		predecessors.clear();

		sptNodeDistancesToRootNode.put(rootNode, 0.0);
		predecessors.put(rootNode, Lists.<Node> newArrayList());
		queue.add(rootNode);
	}

}
