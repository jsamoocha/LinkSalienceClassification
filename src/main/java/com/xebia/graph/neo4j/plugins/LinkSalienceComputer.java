package com.xebia.graph.neo4j.plugins;

import java.util.List;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

import com.xebia.graph.salience.ShortestPathTree;

public class LinkSalienceComputer {
	private ShortestPathTreeCreator sptCreator;
	private List<Node> nodes;
	private List<Relationship> edges;

	public LinkSalienceComputer(List<Node> nodes, List<Relationship> edges) {
	  this.nodes = nodes;
	  this.edges = edges;
  }
	
	public List<Relationship> computeLinkSalience() {
		for (Node currentNode: nodes) {
			ShortestPathTree spt = sptCreator.createShortestPathTree(currentNode, nodes, edges);
			
			while (spt.hasMoreEndNodes()) {
				Node currentSptEndNode = spt.nextEndNode();
				
				for (Node predecessor: spt.getPredecessorNodesFor(currentSptEndNode)) {
					increaseAbsoluteSalienceForEdgeBetween(predecessor, currentSptEndNode);
				}
			}
		}
		
		for (Relationship edge: edges) {
			setPropertyFor(edge, "globalSalience", (Double) edge.getProperty("absoluteSalience") / (double) nodes.size());
		}
		
		return edges;
	}
	
	Relationship increaseAbsoluteSalienceForEdgeBetween(Node fromNode, Node toNode) {
		
		for (Relationship edge: fromNode.getRelationships()) {
			if (edge.getEndNode().equals(toNode)) {
				return setPropertyFor(edge, "absoluteSalience", (Integer) edge.getProperty("absoluteSalience") + 1);
			}
		}
		
		return null;
	}
	
	Relationship setPropertyFor(Relationship edge, String name, Object value) {
		Transaction tx = edge.getGraphDatabase().beginTx();
		
		try {
			edge.setProperty(name, value);
			tx.success();
		} catch (Exception e) {
			tx.failure();
		} finally {
			tx.finish();
		}
		
		return edge;
	}
}
