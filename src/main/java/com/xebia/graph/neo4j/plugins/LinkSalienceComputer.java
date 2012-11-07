package com.xebia.graph.neo4j.plugins;

import java.util.List;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.tooling.GlobalGraphOperations;

import com.google.common.collect.Lists;

public class LinkSalienceComputer {
	private ShortestPathTreeCreator sptCreator;
	private List<Node> nodes;
	private List<Relationship> edges;
	private GraphDatabaseService graphDb;

	public LinkSalienceComputer(GraphDatabaseService graphDb) {
		this.graphDb = graphDb;
	  this.nodes = readAllNodesFrom(graphDb);
	  this.edges = readAllEdgesFrom(graphDb);
  }
	
	public List<Relationship> computeLinkSalience() {
		sptCreator = new ShortestPathTreeCreator(graphDb);
		
		for (Node currentNode: nodes) {
			ShortestPathTree spt = sptCreator.createShortestPathTree(currentNode);
			
			while (spt.hasMoreEndNodes()) {
				Node currentSptEndNode = spt.nextEndNode();
				
				for (Node predecessor: spt.getPredecessorNodesFor(currentSptEndNode)) {
					increaseAbsoluteSalienceForEdgeBetween(predecessor, currentSptEndNode);
				}
			}
		}
		
		for (Relationship edge: edges) {
			setPropertyFor(edge, "salience", (double) (Integer) edge.getProperty("absoluteSalience") / ((double) nodes.size() - 1));
		}
		
		return edges;
	}
	
	Relationship increaseAbsoluteSalienceForEdgeBetween(Node fromNode, Node toNode) {
		
		for (Relationship edge: fromNode.getRelationships()) {
			if (edge.getOtherNode(fromNode).equals(toNode)) {
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
	
	List<Node> readAllNodesFrom(GraphDatabaseService graphDb) {
		List<Node> nodes = Lists.newArrayList();
		
		for (Node node: GlobalGraphOperations.at(graphDb).getAllNodes()) {
			nodes.add(node);
		}
		
		return nodes;
	}
	
	List<Relationship> readAllEdgesFrom(GraphDatabaseService graphDb) {
		List<Relationship> edges = Lists.newArrayList();
		
		for (Relationship edge: GlobalGraphOperations.at(graphDb).getAllRelationships()) {
			edges.add(edge);
		}
		
		return edges;
	}
}
