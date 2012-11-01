package com.xebia.graph.neo4j.plugins;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Iterator;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.test.TestGraphDatabaseFactory;

public class LinkSalienceComputerTest {
	private GraphDatabaseService graphDb;
	private LinkSalienceComputer salienceComputer;

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
	public void testSetPropertyFor_propertySetAndEdgeReturned() {
		List<Node> testNodes = TestGraphUtil.createTestNodes(graphDb);
		List<Relationship> testEdges = TestGraphUtil.createTestEdgesLinearlyConnecting(testNodes, graphDb);
		
		salienceComputer = new LinkSalienceComputer(testNodes, testEdges);
		Relationship updatedEdge = salienceComputer.setPropertyFor(testEdges.get(0), "testProp", "foo");
		Relationship persistedEdge = graphDb.getRelationshipById(testEdges.get(0).getId());
		
		assertEquals("foo", updatedEdge.getProperty("testProp"));
		assertEquals("foo", persistedEdge.getProperty("testProp"));
	}

	@Test
	public void testIncreaseAbsoluteSalienceForEdgeBetween_unconnectedNodes_nullReturnedAndNoEdgesUpdated() {
		List<Node> testNodes = TestGraphUtil.createTestNodes(graphDb);
		List<Relationship> testEdges = TestGraphUtil.createTestEdgesLinearlyConnecting(testNodes, graphDb);
		
		salienceComputer = new LinkSalienceComputer(testNodes, testEdges);
		Relationship updatedEdge = salienceComputer.increaseAbsoluteSalienceForEdgeBetween(testNodes.get(0), testNodes.get(2));

		assertEquals(null, updatedEdge);
		assertEquals(0, graphDb.getRelationshipById(testEdges.get(0).getId()).getProperty("absoluteSalience"));
		assertEquals(0, graphDb.getRelationshipById(testEdges.get(1).getId()).getProperty("absoluteSalience"));
	}

	@Test
	public void testIncreaseAbsoluteSalienceForEdgeBetween_linkedNodes_updatedEdgeReturned() {
		List<Node> testNodes = TestGraphUtil.createTestNodes(graphDb);
		List<Relationship> testEdges = TestGraphUtil.createTestEdgesLinearlyConnecting(testNodes, graphDb);
		
		salienceComputer = new LinkSalienceComputer(testNodes, testEdges);
		Relationship updatedEdge = salienceComputer.increaseAbsoluteSalienceForEdgeBetween(testNodes.get(1), testNodes.get(2));

		assertEquals(testEdges.get(1), updatedEdge);
		assertEquals(0, graphDb.getRelationshipById(testEdges.get(0).getId()).getProperty("absoluteSalience"));
		assertEquals(1, graphDb.getRelationshipById(testEdges.get(1).getId()).getProperty("absoluteSalience"));
	}
}