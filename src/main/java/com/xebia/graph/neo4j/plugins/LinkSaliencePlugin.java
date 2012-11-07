package com.xebia.graph.neo4j.plugins;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.server.plugins.Description;
import org.neo4j.server.plugins.Name;
import org.neo4j.server.plugins.PluginTarget;
import org.neo4j.server.plugins.ServerPlugin;
import org.neo4j.server.plugins.Source;

@Description("Computes link salience of each edge in a (sub-) graph")
public class LinkSaliencePlugin extends ServerPlugin {
	
	@Name("computeLinkSalience")
	@PluginTarget(GraphDatabaseService.class)
	public void computeLinkSalience(@Source GraphDatabaseService graphDb) {
		LinkSalienceComputer salienceComputer = new LinkSalienceComputer(graphDb);
		salienceComputer.computeLinkSalience();
	}
}
