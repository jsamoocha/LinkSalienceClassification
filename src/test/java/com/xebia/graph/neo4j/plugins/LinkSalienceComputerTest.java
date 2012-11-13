package com.xebia.graph.neo4j.plugins;

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.test.TestGraphDatabaseFactory;
import org.neo4j.tooling.GlobalGraphOperations;

public class LinkSalienceComputerTest {
	private GraphDatabaseService graphDb;

	@Before
	public void setupDatabase() {
		graphDb = new TestGraphDatabaseFactory().newImpermanentDatabaseBuilder()
		    .newGraphDatabase();
	}

	@After
	public void shutDownDatabase() {
		graphDb.shutdown();
	}
	
	@Test // learning test case
	public void testGetRelationships_relationshipsForNodeReturned() {
		List<Node> testNodes = TestGraphUtil.createTestNodes(graphDb);
		List<Relationship> testEdges = TestGraphUtil.createTestEdgesLinearlyConnecting(testNodes, graphDb);
		
		// test graph is (n1)--[e1]--(n2)--[e2]--(n3)
		Iterator<Relationship> n1Relationships = testNodes.get(0).getRelationships().iterator();
		
		assertEquals(testEdges.get(0), n1Relationships.next());
		assertFalse(n1Relationships.hasNext());
	}
	
	
	@Test
	public void testComputeLinkSalience_simpleTriangleGraph_salienceReturned() {
		List<Node> nodes = TestGraphUtil.createTestNodes(graphDb);
		List<Relationship> edges = TestGraphUtil.createTestEdgesMakingTriangleGraphWithUnbalancedWeight(
				nodes.get(0), nodes.get(1), nodes.get(2), graphDb);
		
		LinkSalienceComputer worker = new LinkSalienceComputer(graphDb);
		worker.computeLinkSalience("weight");
		
		for (Relationship edge: GlobalGraphOperations.at(graphDb).getAllRelationships()) {
			if (edge.getStartNode().equals(nodes.get(0)) && edge.getEndNode().equals(nodes.get(1))) {
				assertEquals(1.0, edge.getProperty("salience", 0.0));
			} else if (edge.getStartNode().equals(nodes.get(0)) && edge.getEndNode().equals(nodes.get(2))) {
				assertEquals(1.0, edge.getProperty("salience", 0.0));
			} else if (edge.getStartNode().equals(nodes.get(1)) && edge.getEndNode().equals(nodes.get(2))) {
				assertEquals(0.0, edge.getProperty("salience", 0.0));
			} else {
				fail();
			}
		}
	}

	@Test
	public void testComputeLinkSalienceWithDijkstra_simpleTriangleGraph_salienceReturned() {
		List<Node> nodes = TestGraphUtil.createTestNodes(graphDb);
		List<Relationship> edges = TestGraphUtil.createTestEdgesMakingTriangleGraphWithUnbalancedWeight(
				nodes.get(0), nodes.get(1), nodes.get(2), graphDb);
		
		LinkSalienceComputer worker = new LinkSalienceComputer(graphDb);
		worker.computeLinkSalienceWithDijkstra("weight");
		
		for (Relationship edge: GlobalGraphOperations.at(graphDb).getAllRelationships()) {
			if (edge.getStartNode().equals(nodes.get(0)) && edge.getEndNode().equals(nodes.get(1))) {
				assertEquals(1.0, edge.getProperty("salience", 0.0));
			} else if (edge.getStartNode().equals(nodes.get(0)) && edge.getEndNode().equals(nodes.get(2))) {
				assertEquals(1.0, edge.getProperty("salience", 0.0));
			} else if (edge.getStartNode().equals(nodes.get(1)) && edge.getEndNode().equals(nodes.get(2))) {
				assertEquals(0.0, edge.getProperty("salience", 0.0));
			} else {
				fail();
			}
		}
	}

	@Test
	public void testComputeLinkSalienceWithCypher_simpleTriangleGraph_salienceReturned() {
		List<Node> nodes = TestGraphUtil.createTestNodes(graphDb);
		List<Relationship> edges = TestGraphUtil.createTestEdgesMakingTriangleGraphWithUnbalancedWeight(
				nodes.get(0), nodes.get(1), nodes.get(2), graphDb);
		
		LinkSalienceComputer worker = new LinkSalienceComputer(graphDb);
		worker.computeLinkSalienceForQueryResult("START n=node(*) RETURN n;", "weight");
		
		for (Relationship edge: GlobalGraphOperations.at(graphDb).getAllRelationships()) {
			if (edge.getStartNode().equals(nodes.get(0)) && edge.getEndNode().equals(nodes.get(1))) {
				assertEquals(1.0, edge.getProperty("salience", 0.0));
			} else if (edge.getStartNode().equals(nodes.get(0)) && edge.getEndNode().equals(nodes.get(2))) {
				assertEquals(1.0, edge.getProperty("salience", 0.0));
			} else if (edge.getStartNode().equals(nodes.get(1)) && edge.getEndNode().equals(nodes.get(2))) {
				assertEquals(0.0, edge.getProperty("salience", 0.0));
			} else {
				fail();
			}
		}
	}
	
	@Test
	public void testComputeLinkSalience_simpleSquareGraph_salienceReturned() {
		List<Node> nodes = TestGraphUtil.createFourTestNodes(graphDb);
		List<Relationship> edges = TestGraphUtil.createTestEdgesMakingSquareGraphWithUnbalancedWeight(
				nodes.get(0), nodes.get(1), nodes.get(2), nodes.get(3), graphDb);
		
		LinkSalienceComputer worker = new LinkSalienceComputer(graphDb);
		worker.computeLinkSalience("weight");
		
		for (Relationship edge: GlobalGraphOperations.at(graphDb).getAllRelationships()) {
			if (edge.getStartNode().equals(nodes.get(0)) && edge.getEndNode().equals(nodes.get(1))) {
				assertEquals(0.75, edge.getProperty("salience", 0.0));
			} else if (edge.getStartNode().equals(nodes.get(0)) && edge.getEndNode().equals(nodes.get(2))) {
				assertEquals(0.5, edge.getProperty("salience", 0.0));
			} else if (edge.getStartNode().equals(nodes.get(1)) && edge.getEndNode().equals(nodes.get(2))) {
				assertEquals(0.75, edge.getProperty("salience", 0.0));
			} else if (edge.getStartNode().equals(nodes.get(1)) && edge.getEndNode().equals(nodes.get(3))) {
				assertEquals(1.0, edge.getProperty("salience", 0.0));
			} else if (edge.getStartNode().equals(nodes.get(2)) && edge.getEndNode().equals(nodes.get(3))) {
				assertEquals(0.0, edge.getProperty("salience", 0.0));
			} else {
				fail();
			}
		}
	}

	@Test
	public void testComputeLinkSalienceWithDijkstra_simpleSquareGraph_salienceReturned() {
		List<Node> nodes = TestGraphUtil.createFourTestNodes(graphDb);
		List<Relationship> edges = TestGraphUtil.createTestEdgesMakingSquareGraphWithUnbalancedWeight(
				nodes.get(0), nodes.get(1), nodes.get(2), nodes.get(3), graphDb);
		
		LinkSalienceComputer worker = new LinkSalienceComputer(graphDb);
		worker.computeLinkSalienceWithDijkstra("weight");
		
		for (Relationship edge: GlobalGraphOperations.at(graphDb).getAllRelationships()) {
			if (edge.getStartNode().equals(nodes.get(0)) && edge.getEndNode().equals(nodes.get(1))) {
				assertEquals(0.75, edge.getProperty("salience", 0.0));
			} else if (edge.getStartNode().equals(nodes.get(0)) && edge.getEndNode().equals(nodes.get(2))) {
				assertEquals(0.5, edge.getProperty("salience", 0.0));
			} else if (edge.getStartNode().equals(nodes.get(1)) && edge.getEndNode().equals(nodes.get(2))) {
				assertEquals(0.75, edge.getProperty("salience", 0.0));
			} else if (edge.getStartNode().equals(nodes.get(1)) && edge.getEndNode().equals(nodes.get(3))) {
				assertEquals(1.0, edge.getProperty("salience", 0.0));
			} else if (edge.getStartNode().equals(nodes.get(2)) && edge.getEndNode().equals(nodes.get(3))) {
				assertEquals(0.0, edge.getProperty("salience", 0.0));
			} else {
				fail();
			}
		}
	}

	@Test
	public void testComputeLinkSalienceWithCypher_simpleSquareGraph_salienceReturned() {
		List<Node> nodes = TestGraphUtil.createFourTestNodes(graphDb);
		List<Relationship> edges = TestGraphUtil.createTestEdgesMakingSquareGraphWithUnbalancedWeight(
				nodes.get(0), nodes.get(1), nodes.get(2), nodes.get(3), graphDb);
		
		LinkSalienceComputer worker = new LinkSalienceComputer(graphDb);
		worker.computeLinkSalienceForQueryResult("START n=node(*) RETURN n;", "weight");
		
		for (Relationship edge: GlobalGraphOperations.at(graphDb).getAllRelationships()) {
			if (edge.getStartNode().equals(nodes.get(0)) && edge.getEndNode().equals(nodes.get(1))) {
				assertEquals(0.75, edge.getProperty("salience", 0.0));
			} else if (edge.getStartNode().equals(nodes.get(0)) && edge.getEndNode().equals(nodes.get(2))) {
				assertEquals(0.5, edge.getProperty("salience", 0.0));
			} else if (edge.getStartNode().equals(nodes.get(1)) && edge.getEndNode().equals(nodes.get(2))) {
				assertEquals(0.75, edge.getProperty("salience", 0.0));
			} else if (edge.getStartNode().equals(nodes.get(1)) && edge.getEndNode().equals(nodes.get(3))) {
				assertEquals(1.0, edge.getProperty("salience", 0.0));
			} else if (edge.getStartNode().equals(nodes.get(2)) && edge.getEndNode().equals(nodes.get(3))) {
				assertEquals(0.0, edge.getProperty("salience", 0.0));
			} else {
				fail();
			}
		}
	}
	
	@Test
	public void testComputeLinkSalienceWithCypher_selectSimpleTriangleGraph_salienceReturned() {
		List<Node> nodes = TestGraphUtil.createCypherTestNodes(graphDb);
		List<Relationship> edges = TestGraphUtil.createTestEdgesCypherGraph(
				nodes.get(0), nodes.get(1), nodes.get(2), nodes.get(3), nodes.get(4), nodes.get(5), nodes.get(6), nodes.get(7), nodes.get(8), nodes.get(9), graphDb);
		
		LinkSalienceComputer worker = new LinkSalienceComputer(graphDb);
		worker.computeLinkSalienceForQueryResult("START n=node(*) WHERE n.name! IN ['n1','n2','n3'] RETURN n;", "weight");

		for (Relationship edge: GlobalGraphOperations.at(graphDb).getAllRelationships()) {
			if (edge.getStartNode().equals(nodes.get(0)) && edge.getEndNode().equals(nodes.get(1))) {
				assertEquals(1.0, edge.getProperty("salience", 0.0));
			} else if (edge.getStartNode().equals(nodes.get(0)) && edge.getEndNode().equals(nodes.get(2))) {
				assertEquals(1.0, edge.getProperty("salience", 0.0));
			} else if (edge.getStartNode().equals(nodes.get(1)) && edge.getEndNode().equals(nodes.get(2))) {
				assertEquals(0.0, edge.getProperty("salience", 0.0));
			} else {
				assertEquals(0.0, edge.getProperty("salience", 0.0));
			}
		}
	}

	@Test
	public void testComputeLinkSalienceWithCypher_selectSimpleSquareGraph_salienceReturned() {
		List<Node> nodes = TestGraphUtil.createCypherTestNodes(graphDb);
		List<Relationship> edges = TestGraphUtil.createTestEdgesCypherGraph(
				nodes.get(0), nodes.get(1), nodes.get(2), nodes.get(3), nodes.get(4), nodes.get(5), nodes.get(6), nodes.get(7), nodes.get(8), nodes.get(9), graphDb);
		
		LinkSalienceComputer worker = new LinkSalienceComputer(graphDb);
		worker.computeLinkSalienceForQueryResult("START n=node(*) WHERE n.name! IN ['n7','n8','n9','n10'] RETURN n;", "weight");
		
		for (Relationship edge: GlobalGraphOperations.at(graphDb).getAllRelationships()) {
			if (edge.getStartNode().equals(nodes.get(6)) && edge.getEndNode().equals(nodes.get(7))) {
				assertEquals(0.75, edge.getProperty("salience", 0.0));
			} else if (edge.getStartNode().equals(nodes.get(6)) && edge.getEndNode().equals(nodes.get(8))) {
				assertEquals(0.5, edge.getProperty("salience", 0.0));
			} else if (edge.getStartNode().equals(nodes.get(7)) && edge.getEndNode().equals(nodes.get(8))) {
				assertEquals(0.75, edge.getProperty("salience", 0.0));
			} else if (edge.getStartNode().equals(nodes.get(7)) && edge.getEndNode().equals(nodes.get(9))) {
				assertEquals(1.0, edge.getProperty("salience", 0.0));
			} else if (edge.getStartNode().equals(nodes.get(8)) && edge.getEndNode().equals(nodes.get(9))) {
				assertEquals(0.0, edge.getProperty("salience", 0.0));
			} else {
				assertEquals(0.0, edge.getProperty("salience", 0.0));
			}
		}
	}
	
}
