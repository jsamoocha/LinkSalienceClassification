package com.xebia.graph.neo4j.plugins;

import java.util.List;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.server.plugins.Description;
import org.neo4j.server.plugins.Name;
import org.neo4j.server.plugins.PluginTarget;
import org.neo4j.server.plugins.ServerPlugin;
import org.neo4j.server.plugins.Source;
import org.neo4j.tooling.GlobalGraphOperations;

import com.google.common.collect.Lists;

@Description("Computes link salience of each edge in a (sub-) graph")
public class LinkSaliencePlugin extends ServerPlugin {
	
	@Name("computeLinkSalience")
	@PluginTarget(GraphDatabaseService.class)
	public void computeLinkSalience(@Source GraphDatabaseService graphDb) {
		List<Node> nodes = readAllNodesFrom(graphDb);
		List<Relationship> edges = readAllEdgesFrom(graphDb);
		LinkSalienceComputer salienceComputer = new LinkSalienceComputer(nodes, edges);
		
	}
	
	List<Node> readAllNodesFrom(GraphDatabaseService graphDb) {
		List<Node> nodes = Lists.newArrayList();
		
		for (Node node: GlobalGraphOperations.at(graphDb).getAllNodes()) {
			nodes.add(node);
		}
		
		return nodes;
	}
	
	List<Relationship> readAllEdgesFrom(GraphDatabaseService graphDb) {
		List<Relationship> edges = Lists.newArrayList();
		
		for (Relationship edge: GlobalGraphOperations.at(graphDb).getAllRelationships()) {
			edges.add(edge);
		}
		
		return edges;
	}
}
