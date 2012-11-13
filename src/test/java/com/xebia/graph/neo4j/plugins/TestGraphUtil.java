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
//			e1.setProperty("absoluteSalience", 0);
			e1.setProperty("weight", 3.0);
			
			Relationship e2 = n1.createRelationshipTo(n3, RelTypes.X);
//			e2.setProperty("absoluteSalience", 0);
			e2.setProperty("weight", 3.0);
			
			Relationship e3 = n2.createRelationshipTo(n3, RelTypes.X);
//			e3.setProperty("absoluteSalience", 0);
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
	
	public static List<Node> createCypherTestNodes(GraphDatabaseService graphDb) {
		Transaction tx = graphDb.beginTx();
		Node n1 = null, n2 = null, n3 = null, n4 = null, n5 = null, n6 = null, n7 = null, n8 = null, n9 = null, n10 = null;

		try {
			n1 = graphDb.createNode();
			n1.setProperty("name", "n1");
			n2 = graphDb.createNode();
			n2.setProperty("name", "n2");
			n3 = graphDb.createNode();
			n3.setProperty("name", "n3");
			n4 = graphDb.createNode();
			n4.setProperty("name", "n4");
			n5 = graphDb.createNode();
			n5.setProperty("name", "n5");
			n6 = graphDb.createNode();
			n6.setProperty("name", "n6");
			n7 = graphDb.createNode();
			n7.setProperty("name", "n7");
			n8 = graphDb.createNode();
			n8.setProperty("name", "n8");
			n9 = graphDb.createNode();
			n9.setProperty("name", "n9");
			n10 = graphDb.createNode();
			n10.setProperty("name", "n10");

			tx.success();
		} catch (Exception e) {
			tx.failure();
		} finally {
			tx.finish();
		}

		return Lists.newArrayList(n1, n2, n3, n4, n5, n6, n7, n8, n9, n10);
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

	public static List<Relationship> createTestEdgesMakingBrancheGraphWithUnbalancedWeight(Node n1, Node n2, Node n3, Node n4,
			Node n5, GraphDatabaseService graphDb) {
		Transaction tx = graphDb.beginTx();
		List<Relationship> testEdges = Lists.newArrayList();
		
		try {
			Relationship e1 = n1.createRelationshipTo(n2, RelTypes.X);
			e1.setProperty("weight", 3.0);
			
			Relationship e2 = n1.createRelationshipTo(n3, RelTypes.X);
			e2.setProperty("weight", 1.0);
			
			Relationship e3 = n2.createRelationshipTo(n4, RelTypes.X);
			e3.setProperty("weight", 2.0);
			
			Relationship e4 = n3.createRelationshipTo(n4, RelTypes.X);
			e4.setProperty("weight", 3.0);
			
			Relationship e5 = n4.createRelationshipTo(n5, RelTypes.X);
			e5.setProperty("weight", 3.0);
			
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

	public static List<Relationship> createTestEdgesCypherGraph(Node n1, Node n2, Node n3, Node n4,
			Node n5, Node n6, Node n7, Node n8, Node n9, Node n10, GraphDatabaseService graphDb) {
		Transaction tx = graphDb.beginTx();
		List<Relationship> testEdges = Lists.newArrayList();
		
		try {
			// first same as triangle
			Relationship e1 = n1.createRelationshipTo(n2, RelTypes.X);
			e1.setProperty("weight", 3.0);

			Relationship e2 = n1.createRelationshipTo(n3, RelTypes.X);
			e2.setProperty("weight", 3.0);
			
			Relationship e3 = n2.createRelationshipTo(n3, RelTypes.X);
			e3.setProperty("weight", 1.0);
			
			testEdges.add(e1);
			testEdges.add(e2);
			testEdges.add(e3);
			
			// second same as square
			Relationship e4 = n7.createRelationshipTo(n8, RelTypes.X);
			e4.setProperty("weight", 3.0);
			
			Relationship e5 = n7.createRelationshipTo(n9, RelTypes.X);
			e5.setProperty("weight", 3.0);
			
			Relationship e6 = n8.createRelationshipTo(n9, RelTypes.X);
			e6.setProperty("weight", 3.0);
			
			Relationship e7 = n8.createRelationshipTo(n10, RelTypes.X);
			e7.setProperty("weight", 2.0);
			
			Relationship e8 = n9.createRelationshipTo(n10, RelTypes.X);
			e8.setProperty("weight", 0.5);
			
			testEdges.add(e4);
			testEdges.add(e5);
			testEdges.add(e6);
			testEdges.add(e7);
			testEdges.add(e8);
			
			// last connect these two subgraphs
			Relationship e9 = n1.createRelationshipTo(n4, RelTypes.X);
			e9.setProperty("weight", 0.5);
			
			Relationship e10 = n4.createRelationshipTo(n7, RelTypes.X);
			e10.setProperty("weight", 0.5);
			
			Relationship e11 = n2.createRelationshipTo(n5, RelTypes.X);
			e11.setProperty("weight", 0.5);
			
			Relationship e12 = n5.createRelationshipTo(n9, RelTypes.X);
			e12.setProperty("weight", 0.5);
			
			Relationship e13 = n3.createRelationshipTo(n6, RelTypes.X);
			e13.setProperty("weight", 0.5);
			
			Relationship e14 = n6.createRelationshipTo(n8, RelTypes.X);
			e14.setProperty("weight", 0.5);
			
			testEdges.add(e9);
			testEdges.add(e10);
			testEdges.add(e11);
			testEdges.add(e12);
			testEdges.add(e13);
			testEdges.add(e14);
			
			tx.success();
		} catch (Exception e) {
			tx.failure();
		} finally {
			tx.finish();
		}
		
		return testEdges;
	}
}
