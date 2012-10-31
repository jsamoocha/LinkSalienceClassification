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
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.test.TestGraphDatabaseFactory;

import com.google.common.collect.Lists;

public class LinkSalienceComputerTest {
	private enum RelTypes implements RelationshipType {
		X
	}

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
		List<Node> testNodes = createTestNodes();
		List<Relationship> testEdges = createTestEdgesLinearlyConnecting(testNodes);
		
		// test graph is (n1)--[e1]--(n2)--[e2]--(n3)
		Iterator<Relationship> n1Relationships = testNodes.get(0).getRelationships().iterator();
		
		assertEquals(testEdges.get(0), n1Relationships.next());
		assertFalse(n1Relationships.hasNext());
	}
	
	@Test
	public void testSetPropertyFor_propertySetAndEdgeReturned() {
		List<Node> testNodes = createTestNodes();
		List<Relationship> testEdges = createTestEdgesLinearlyConnecting(testNodes);
		
		salienceComputer = new LinkSalienceComputer(testNodes, testEdges);
		Relationship updatedEdge = salienceComputer.setPropertyFor(testEdges.get(0), "testProp", "foo");
		Relationship persistedEdge = graphDb.getRelationshipById(testEdges.get(0).getId());
		
		assertEquals("foo", updatedEdge.getProperty("testProp"));
		assertEquals("foo", persistedEdge.getProperty("testProp"));
	}

	@Test
	public void testIncreaseAbsoluteSalienceForEdgeBetween_unconnectedNodes_nullReturnedAndNoEdgesUpdated() {
		List<Node> testNodes = createTestNodes();
		List<Relationship> testEdges = createTestEdgesLinearlyConnecting(testNodes);
		
		salienceComputer = new LinkSalienceComputer(testNodes, testEdges);
		Relationship updatedEdge = salienceComputer.increaseAbsoluteSalienceForEdgeBetween(testNodes.get(0), testNodes.get(2));

		assertEquals(null, updatedEdge);
		assertEquals(0, graphDb.getRelationshipById(testEdges.get(0).getId()).getProperty("absoluteSalience"));
		assertEquals(0, graphDb.getRelationshipById(testEdges.get(1).getId()).getProperty("absoluteSalience"));
	}

	@Test
	public void testIncreaseAbsoluteSalienceForEdgeBetween_linkedNodes_updatedEdgeReturned() {
		List<Node> testNodes = createTestNodes();
		List<Relationship> testEdges = createTestEdgesLinearlyConnecting(testNodes);
		
		salienceComputer = new LinkSalienceComputer(testNodes, testEdges);
		Relationship updatedEdge = salienceComputer.increaseAbsoluteSalienceForEdgeBetween(testNodes.get(1), testNodes.get(2));

		assertEquals(testEdges.get(1), updatedEdge);
		assertEquals(0, graphDb.getRelationshipById(testEdges.get(0).getId()).getProperty("absoluteSalience"));
		assertEquals(1, graphDb.getRelationshipById(testEdges.get(1).getId()).getProperty("absoluteSalience"));
	}

	private List<Node> createTestNodes() {
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

	private List<Relationship> createTestEdgesLinearlyConnecting(List<Node> nodes) {
		Transaction tx = graphDb.beginTx();
		List<Relationship> testEdges = Lists.newArrayList();
		
		try {
			for (int i = 0; i < nodes.size() - 1; i ++) {
				Relationship edge = nodes.get(i).createRelationshipTo(nodes.get(i + 1), RelTypes.X);
				edge.setProperty("absoluteSalience", 0);
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
}
