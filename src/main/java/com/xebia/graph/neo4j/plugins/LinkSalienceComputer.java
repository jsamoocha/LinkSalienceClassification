package com.xebia.graph.neo4j.plugins;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

import com.google.common.collect.Lists;

public class LinkSalienceComputer {
	private ShortestPathTreeCreator sptCreator;
	private GraphDatabaseService graphDb;

	public LinkSalienceComputer(GraphDatabaseService graphDb) {
		this.graphDb = graphDb;
	}
	
	public void computeLinkSalience(String weightProperty) {
		sptCreator = new ShortestPathTreeCreator(weightProperty);
		
		long numberOfNodesProcessed = 0;
		for (Node currentNode: GlobalGraphOperations.at(graphDb).getAllNodes()) {
			numberOfNodesProcessed++;
			ShortestPathTree spt = sptCreator.createShortestPathTree(currentNode);

			while (spt.hasMoreEndNodes()) {
				Node currentSptEndNode = spt.nextEndNode();

				for (Node predecessor : spt.getPredecessorNodesFor(currentSptEndNode)) {
					increaseAbsoluteSalienceForEdgeBetween(predecessor, currentSptEndNode);
				}
			}
		}

		computeSalience(numberOfNodesProcessed);
	}

	public void computeLinkSalienceWithDijkstra(String weightProperty) {
		CostEvaluator<Double> costEvaluator = new WeightCostEvaluator(weightProperty);
		PathFinder<WeightedPath> pathPathFinder = GraphAlgoFactory.dijkstra((PathExpander<?>) StandardExpander.DEFAULT, costEvaluator);

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
				setPropertyFor(edge, "absoluteSalience", (Integer) edge.getProperty("absoluteSalience", 0) + 1);
			}
		}

		computeSalience(numberOfNodesProcessed);
	}

	private void computeSalience(double nodeSize) {
		for (Relationship edge : GlobalGraphOperations.at(graphDb).getAllRelationships()) {
			setPropertyFor(edge, "salience", (double) (Integer) edge.getProperty("absoluteSalience", 0)
					/ ((double) nodeSize - 1));
			removePropertyFor(edge, "absoluteSalience");
		}
	}

	Relationship increaseAbsoluteSalienceForEdgeBetween(Node fromNode, Node toNode) {

		for (Relationship edge : fromNode.getRelationships()) {
			if (edge.getOtherNode(fromNode).equals(toNode)) {
				return setPropertyFor(edge, "absoluteSalience", (Integer) edge.getProperty("absoluteSalience", 0) + 1);
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

	Relationship removePropertyFor(Relationship edge, String name) {
		Transaction tx = edge.getGraphDatabase().beginTx();

		try {
			edge.removeProperty(name);
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

		for (Node node : GlobalGraphOperations.at(graphDb).getAllNodes()) {
			nodes.add(node);
		}

		return nodes;
	}

	List<Relationship> readAllEdgesFrom(GraphDatabaseService graphDb) {
		List<Relationship> edges = Lists.newArrayList();

		for (Relationship edge : GlobalGraphOperations.at(graphDb).getAllRelationships()) {
			edges.add(edge);
		}

		return edges;
	}
	
	class WeightCostEvaluator implements CostEvaluator<Double> {
		private String weightProperty;
		
		public WeightCostEvaluator(String weightProperty) {
			this.weightProperty = weightProperty;
		}
		
		@Override
		public Double getCost(Relationship relationship, Direction direction) {
	        Object costProp = relationship.getProperty(weightProperty);
	        if(costProp instanceof Double)
	            return 1.0 / (Double)costProp;
	        if(costProp instanceof Integer)
	            return 1.0 / Double.valueOf(((Integer)costProp).intValue());
	        else
	            return 1.0 / Double.valueOf(Double.parseDouble(costProp.toString()));
	    }

	}
	
}
