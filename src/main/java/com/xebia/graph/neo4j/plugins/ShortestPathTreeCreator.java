package com.xebia.graph.neo4j.plugins;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Stack;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.tooling.GlobalGraphOperations;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ShortestPathTreeCreator {
	private GraphDatabaseService graphDb;
	private Map<Node, List<Node>> predecessors = Maps.newHashMap();
	private PriorityQueue<Node> queue = new PriorityQueue<Node>(13, new SptNodeComparator());
	private Stack<Node> stack = new Stack<Node>();
	private Map<Node, Double> sptNodeDistancesToRootNode = Maps.newHashMap();
	private String weightPropertyName;
	
	private class SptNodeComparator implements Comparator<Node> {

		public int compare(Node n1, Node n2) {
	    return Double.compare(sptNodeDistancesToRootNode.get(n1), sptNodeDistancesToRootNode.get(n2));
    }
		
	}
	
	public ShortestPathTreeCreator(GraphDatabaseService graphDb, String weightPropertyName) {
		this.graphDb = graphDb;
		this.weightPropertyName = weightPropertyName;
	}

	public ShortestPathTree createShortestPathTree(Node rootNode) {
		initNodes(rootNode);
		
		while (!queue.isEmpty()) {
			Node minimumDistanceNode = queue.poll();
			stack.push(minimumDistanceNode);
			
			for (Node potentialShortestPathNode: getNodesConnectedTo(minimumDistanceNode)) {
				double connectionDistance = edgeDistance(minimumDistanceNode, potentialShortestPathNode);
				double minimumDistance = sptNodeDistancesToRootNode.get(minimumDistanceNode);
				double potentialShortestPathNodeDistance = sptNodeDistancesToRootNode.get(potentialShortestPathNode); 
				
				if (potentialShortestPathNodeDistance > minimumDistance + connectionDistance) {
					sptNodeDistancesToRootNode.put(potentialShortestPathNode, minimumDistance + connectionDistance);
					
					if (queue.contains(potentialShortestPathNode)) {
						queue.remove(potentialShortestPathNode);
					}
					
					queue.add(potentialShortestPathNode);
					predecessors.put(potentialShortestPathNode, new ArrayList<Node>());
				}
				
				if (sptNodeDistancesToRootNode.get(potentialShortestPathNode) == minimumDistance + connectionDistance) {
					predecessors.get(potentialShortestPathNode).add(minimumDistanceNode);
				}
			}
		}
		
	  return new ShortestPathTree(stack, predecessors);
  }
	
	void initNodes(Node rootNode) {
		for (Node node: GlobalGraphOperations.at(graphDb).getAllNodes()) {
			if (!node.equals(rootNode)) {
				sptNodeDistancesToRootNode.put(node, Double.POSITIVE_INFINITY);
				predecessors.put(node, Lists.<Node>newArrayList());
			}
		}
		
		sptNodeDistancesToRootNode.put(rootNode, 0.0);
		predecessors.put(rootNode, Lists.<Node>newArrayList());
		queue.add(rootNode);
	}
	
	List<Node> getNodesConnectedTo(Node n) {
		List<Node> connectedNodes = Lists.newArrayList();
		
		for (Relationship edge: n.getRelationships()) {
			connectedNodes.add(edge.getOtherNode(n));
		}
		
		return connectedNodes;
	}
	
	
	double edgeDistance(Node n1, Node n2) {
		
		for (Relationship edge: n1.getRelationships()) {
			if (edge.getOtherNode(n1).equals(n2)) {
				return 1.0 / (Double) edge.getProperty(weightPropertyName);
			}
		}
		
		return Double.POSITIVE_INFINITY;
	}

}
