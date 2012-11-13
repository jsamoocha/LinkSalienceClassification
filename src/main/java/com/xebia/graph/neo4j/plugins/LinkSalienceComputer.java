package com.xebia.graph.neo4j.plugins;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphalgo.CostEvaluator;
import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphalgo.WeightedPath;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PathExpander;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.StandardExpander;
import org.neo4j.tooling.GlobalGraphOperations;

public class LinkSalienceComputer {
	private ShortestPathTreeCreator sptCreator;
	private GraphDatabaseService graphDb;
	private int[] absoluteSalienceForEdges = new int[INCREMENT];
	private static final int INCREMENT = 1000;

	public LinkSalienceComputer(GraphDatabaseService graphDb) {
		this.graphDb = graphDb;
	}

	public void computeLinkSalience(String weightProperty) {
		sptCreator = new ShortestPathTreeCreator(weightProperty);

		long numberOfNodesProcessed = 0;
		for (Node currentNode : GlobalGraphOperations.at(graphDb).getAllNodes()) {
			numberOfNodesProcessed++;
			ShortestPathTree spt = sptCreator.createShortestPathTree(currentNode);

			while (spt.hasMoreEndNodes()) {
				Node currentSptEndNode = spt.nextEndNode();

				for (Node predecessor : spt.getPredecessorNodesFor(currentSptEndNode)) {
					increaseAbsoluteSalienceForEdgeBetween(predecessor, currentSptEndNode);
				}
			}
		}

		computeSalience(numberOfNodesProcessed - 1);
	}

	public void computeLinkSalienceWithDijkstra(String weightProperty) {
		CostEvaluator<Double> costEvaluator = new WeightCostEvaluator(weightProperty);
		PathFinder<WeightedPath> pathPathFinder = GraphAlgoFactory.dijkstra((PathExpander<?>) StandardExpander.DEFAULT,
				costEvaluator);

		long numberOfNodesProcessed = 0;
		for (Node currentNode : GlobalGraphOperations.at(graphDb).getAllNodes()) {
			numberOfNodesProcessed++;
			Set<Relationship> edgesInPaths = new HashSet<Relationship>();
			for (Node otherNode : GlobalGraphOperations.at(graphDb).getAllNodes()) {
				Path path = pathPathFinder.findSinglePath(currentNode, otherNode);
				if (path != null) {
					for (Relationship edge : path.relationships()) {
						edgesInPaths.add(edge);
					}
				}
			}
			for (Relationship edge : edgesInPaths) {
				increaseAbsoluteSalienceFor(edge);
			}
		}

		computeSalience(numberOfNodesProcessed - 1);
	}

	private void computeSalience(double nodeSize) {
		Transaction tx = graphDb.beginTx();

		try {
			for (int i = 0; i < absoluteSalienceForEdges.length; i++) {
				if (absoluteSalienceForEdges[i] > 0) {
					Relationship edge = graphDb.getRelationshipById(i);

					edge.setProperty("salience", (double) absoluteSalienceForEdges[i] / ((double) nodeSize));
				}
			}
			tx.success();
		} catch (Exception e) {
			tx.failure();
		} finally {
			tx.finish();
		}
	}

	Relationship increaseAbsoluteSalienceForEdgeBetween(Node fromNode, Node toNode) {

		for (Relationship edge : fromNode.getRelationships()) {
			if (edge.getOtherNode(fromNode).equals(toNode)) {
				increaseAbsoluteSalienceFor(edge);
				return edge;
			}
		}

		return null;
	}

	private void increaseAbsoluteSalienceFor(Relationship edge) {
		while (edge.getId() > absoluteSalienceForEdges.length) {
			int[] tmp = new int[absoluteSalienceForEdges.length + INCREMENT];
			System.arraycopy(absoluteSalienceForEdges, 0, tmp, 0, absoluteSalienceForEdges.length);
			absoluteSalienceForEdges = tmp;
		}

		absoluteSalienceForEdges[(int) edge.getId()]++;
	}

	class WeightCostEvaluator implements CostEvaluator<Double> {
		private String weightProperty;

		public WeightCostEvaluator(String weightProperty) {
			this.weightProperty = weightProperty;
		}

		@Override
		public Double getCost(Relationship relationship, Direction direction) {
			Object costProp = relationship.getProperty(weightProperty);
			if (costProp instanceof Double)
				return 1.0 / (Double) costProp;
			if (costProp instanceof Integer)
				return 1.0 / Double.valueOf(((Integer) costProp).intValue());
			else
				return 1.0 / Double.valueOf(Double.parseDouble(costProp.toString()));
		}

	}

	public void computeLinkSalienceForQueryResult(String query, String weightProperty) {
		ExecutionEngine engine = new ExecutionEngine(graphDb);
		ExecutionResult executionResult = engine.execute(query);
		List<String> columns = executionResult.columns();
		Iterator<Node> nodes = executionResult.columnAs(columns.get(0));

		List<Long> nodesInSubGraph = new ArrayList<Long>();
		while (nodes.hasNext()) {
			nodesInSubGraph.add(nodes.next().getId());
		}	
		
		sptCreator = new ShortestPathTreeCreator(weightProperty, nodesInSubGraph);

		executionResult = engine.execute(query);
		columns = executionResult.columns();
		nodes = executionResult.columnAs(columns.get(0));
		long numberOfNodesProcessed = 0;
		while (nodes.hasNext()) {

			Node currentNode = nodes.next();
			if (currentNode.getId() != 0) {
				numberOfNodesProcessed++;
				ShortestPathTree spt = sptCreator.createShortestPathTree(currentNode);
	
				while (spt.hasMoreEndNodes()) {
					Node currentSptEndNode = spt.nextEndNode();
	
					for (Node predecessor : spt.getPredecessorNodesFor(currentSptEndNode)) {
						increaseAbsoluteSalienceForEdgeBetween(predecessor, currentSptEndNode);
					}
				}
			}
		}

		computeSalience(numberOfNodesProcessed);

	}

}
