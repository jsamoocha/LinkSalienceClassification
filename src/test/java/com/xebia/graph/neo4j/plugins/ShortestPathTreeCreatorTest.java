package com.xebia.graph.neo4j.plugins;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.test.TestGraphDatabaseFactory;


public class ShortestPathTreeCreatorTest {
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
	
	@Test
	public void testCreateShortestPathTree_unbalancedTriangleGraph_sptReturned() {
		List<Node> nodes = TestGraphUtil.createTestNodes(graphDb);
		List<Relationship> edges = TestGraphUtil.createTestEdgesMakingTriangleGraphWithUnbalancedWeight(
				nodes.get(0), nodes.get(1), nodes.get(2), graphDb);
		
		ShortestPathTreeCreator sptCreator = new ShortestPathTreeCreator("weight");
		ShortestPathTree spt = sptCreator.createShortestPathTree(nodes.get(0));
		
		assertEquals(nodes.get(2), spt.nextEndNode());
		assertEquals(1, spt.getPredecessorNodesFor(nodes.get(2)).size());
		assertEquals(nodes.get(1), spt.getPredecessorNodesFor(nodes.get(2)).get(0));
		
		assertEquals(nodes.get(1), spt.nextEndNode());
		assertEquals(1, spt.getPredecessorNodesFor(nodes.get(1)).size());
		assertEquals(nodes.get(0), spt.getPredecessorNodesFor(nodes.get(1)).get(0));
		
		assertEquals(nodes.get(0), spt.nextEndNode());
		assertEquals(0, spt.getPredecessorNodesFor(nodes.get(0)).size());
		
		ShortestPathTree spt1 = sptCreator.createShortestPathTree(nodes.get(1));
		assertEquals(nodes.get(0), spt1.nextEndNode());
		assertEquals(1, spt1.getPredecessorNodesFor(nodes.get(0)).size());
		assertEquals(nodes.get(1), spt1.getPredecessorNodesFor(nodes.get(0)).get(0));
		
		assertEquals(nodes.get(2), spt1.nextEndNode());
		assertEquals(1, spt1.getPredecessorNodesFor(nodes.get(2)).size());
		assertEquals(nodes.get(1), spt1.getPredecessorNodesFor(nodes.get(2)).get(0));
		
		assertEquals(nodes.get(1), spt1.nextEndNode());
		assertEquals(0, spt1.getPredecessorNodesFor(nodes.get(1)).size());
		
		ShortestPathTree spt2 = sptCreator.createShortestPathTree(nodes.get(2));
		assertEquals(nodes.get(0), spt2.nextEndNode());
		assertEquals(1, spt2.getPredecessorNodesFor(nodes.get(0)).size());
		assertEquals(nodes.get(1), spt2.getPredecessorNodesFor(nodes.get(0)).get(0));
		
		assertEquals(nodes.get(1), spt2.nextEndNode());
		assertEquals(1, spt2.getPredecessorNodesFor(nodes.get(1)).size());
		assertEquals(nodes.get(2), spt2.getPredecessorNodesFor(nodes.get(1)).get(0));
		
		assertEquals(nodes.get(2), spt2.nextEndNode());
		assertEquals(0, spt2.getPredecessorNodesFor(nodes.get(2)).size());
	}
}
