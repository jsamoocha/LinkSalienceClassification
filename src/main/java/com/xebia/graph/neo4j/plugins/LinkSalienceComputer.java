package com.xebia.graph.neo4j.plugins;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.neo4j.graphalgo.CommonEvaluators;
import org.neo4j.graphalgo.CostEvaluator;
import org.neo4j.graphalgo.EstimateEvaluator;
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
import org.neo4j.graphdb.traversal.Evaluator;
import org.neo4j.kernel.StandardExpander;
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

		for (Node currentNode : nodes) {
			ShortestPathTree spt = sptCreator.createShortestPathTree(currentNode);

			while (spt.hasMoreEndNodes()) {
				Node currentSptEndNode = spt.nextEndNode();

				for (Node predecessor : spt.getPredecessorNodesFor(currentSptEndNode)) {
					increaseAbsoluteSalienceForEdgeBetween(predecessor, currentSptEndNode);
				}
			}
		}

		return computeSalience();
	}

	public List<Relationship> computeLinkSalienceWithDijkstra() {
		CostEvaluator<Double> costEvaluator = new WeightCostEvaluator();
		PathFinder<WeightedPath> pathPathFinder = GraphAlgoFactory.dijkstra((PathExpander<?>) StandardExpander.DEFAULT, costEvaluator);

		for (Node currentNode : nodes) {
			Set<Relationship> edgesInPaths = new HashSet<Relationship>();
			for (Node otherNode : nodes) {
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

		return computeSalience();
	}

	private List<Relationship> computeSalience() {
		for (Relationship edge : edges) {
			setPropertyFor(edge, "salience", (double) (Integer) edge.getProperty("absoluteSalience", 0)
					/ ((double) nodes.size() - 1));
			removePropertyFor(edge, "absoluteSalience");
		}
		return edges;
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

		@Override
		public Double getCost(Relationship relationship, Direction direction) {
	        Object costProp = relationship.getProperty("weight");
	        if(costProp instanceof Double)
	            return 1.0 / (Double)costProp;
	        if(costProp instanceof Integer)
	            return 1.0 / Double.valueOf(((Integer)costProp).intValue());
	        else
	            return 1.0 / Double.valueOf(Double.parseDouble(costProp.toString()));
	    }

	}
	
}
