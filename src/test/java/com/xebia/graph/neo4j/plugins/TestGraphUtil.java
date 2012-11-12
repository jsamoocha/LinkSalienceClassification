package com.xebia.graph.neo4j.plugins;

import java.util.List;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;

import com.google.common.collect.Lists;

public class TestGraphUtil {
	private enum RelTypes implements RelationshipType {
		X
	}
	
	public static List<Node> createTestNodes(GraphDatabaseService graphDb) {
		Transaction tx = graphDb.beginTx();
		Node n1 = null, n2 = null, n3 = null;

		try {
			n1 = graphDb.createNode();
			n1.setProperty("name", "n1");
			n2 = graphDb.createNode();
			n2.setProperty("name", "n2");
			n3 = graphDb.createNode();
			n3.setProperty("name", "n3");

			tx.success();
		} catch (Exception e) {
			tx.failure();
		} finally {
			tx.finish();
		}

		return Lists.newArrayList(n1, n2, n3);
	}
	
	public static List<Relationship> createTestEdgesLinearlyConnecting(List<Node> nodes, GraphDatabaseService graphDb) {
		Transaction tx = graphDb.beginTx();
		List<Relationship> testEdges = Lists.newArrayList();
		
		try {
			for (int i = 0; i < nodes.size() - 1; i ++) {
				Relationship edge = nodes.get(i).createRelationshipTo(nodes.get(i + 1), RelTypes.X);
				//edge.setProperty("absoluteSalience", 0);
				testEdges.add(edge);
				
				tx.success();
			}
		} catch (Exception e) {
			tx.failure();
		} finally {
			tx.finish();
		}
		
		return testEdges;
	}
	
	public static List<Relationship> createTestEdgesMakingTriangleGraphWithUnbalancedWeight(Node n1, Node n2, Node n3,
			GraphDatabaseService graphDb) {
		Transaction tx = graphDb.beginTx();
		List<Relationship> testEdges = Lists.newArrayList();
		
		try {
			Relationship e1 = n1.createRelationshipTo(n2, RelTypes.X);
			e1.setProperty("weight", 3.0);
			
			Relationship e2 = n1.createRelationshipTo(n3, RelTypes.X);
			e2.setProperty("weight", 3.0);
			
			Relationship e3 = n2.createRelationshipTo(n3, RelTypes.X);
			e3.setProperty("weight", 1.0);
			
			testEdges.add(e1);
			testEdges.add(e2);
			testEdges.add(e3);
			
			tx.success();
		} catch (Exception e) {
			tx.failure();
		} finally {
			tx.finish();
		}
		
		return testEdges;
	}

	public static List<Node> createFourTestNodes(GraphDatabaseService graphDb) {
		Transaction tx = graphDb.beginTx();
		Node n1 = null, n2 = null, n3 = null, n4 = null;

		try {
			n1 = graphDb.createNode();
			n1.setProperty("name", "n1");
			n2 = graphDb.createNode();
			n2.setProperty("name", "n2");
			n3 = graphDb.createNode();
			n3.setProperty("name", "n3");
			n4 = graphDb.createNode();
			n4.setProperty("name", "n4");

			tx.success();
		} catch (Exception e) {
			tx.failure();
		} finally {
			tx.finish();
		}

		return Lists.newArrayList(n1, n2, n3, n4);
	}
	
	public static List<Relationship> createTestEdgesMakingSquareGraphWithUnbalancedWeight(Node n1, Node n2, Node n3, Node n4,
			GraphDatabaseService graphDb) {
		Transaction tx = graphDb.beginTx();
		List<Relationship> testEdges = Lists.newArrayList();
		
		try {
			Relationship e1 = n1.createRelationshipTo(n2, RelTypes.X);
			e1.setProperty("weight", 3.0);
			
			Relationship e2 = n1.createRelationshipTo(n3, RelTypes.X);
			e2.setProperty("weight", 3.0);
			
			Relationship e3 = n2.createRelationshipTo(n3, RelTypes.X);
			e3.setProperty("weight", 3.0);
			
			Relationship e4 = n2.createRelationshipTo(n4, RelTypes.X);
			e4.setProperty("weight", 2.0);
			
			Relationship e5 = n3.createRelationshipTo(n4, RelTypes.X);
			e5.setProperty("weight", 0.5);
			
			testEdges.add(e1);
			testEdges.add(e2);
			testEdges.add(e3);
			testEdges.add(e4);
			testEdges.add(e5);
			
			tx.success();
		} catch (Exception e) {
			tx.failure();
		} finally {
			tx.finish();
		}
		
		return testEdges;
	}
}
