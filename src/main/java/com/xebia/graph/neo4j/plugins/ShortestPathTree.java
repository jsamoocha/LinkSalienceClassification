package com.xebia.graph.neo4j.plugins;

import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.neo4j.graphdb.Node;

public class ShortestPathTree {
	private Stack<Node> endNodes;
	private Map<Node, List<Node>> predecessors;
	
	public ShortestPathTree(Stack<Node> endNodes, Map<Node, List<Node>> predecessors) {
		this.endNodes = endNodes;
		this.predecessors = predecessors;
	}

	public boolean hasMoreEndNodes() {
	  return !endNodes.isEmpty();
  }

	public Node nextEndNode() {
	  return endNodes.pop();
  }

	public List<Node> getPredecessorNodesFor(Node currentSptEndNode) {
	  return predecessors.get(currentSptEndNode);
  }

}
