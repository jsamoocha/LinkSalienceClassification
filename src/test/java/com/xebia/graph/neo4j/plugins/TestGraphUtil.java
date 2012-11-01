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
