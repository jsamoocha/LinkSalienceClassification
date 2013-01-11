LinkSalienceClassification
==========================

Neo4j plugin implementing the [approach described by Grady et. al. (2012)](http://rocs.northwestern.edu/index_assets/grady_2012.pdf) 
for computing link salience in networks.

Installation Instructions
--------------------

* Run `mvn assembly:assembly` to generate the plugin `LinkSalience-[VERSION]-jar-with-dependencies.jar`,
* Copy above jar to [NEO4J_HOME]/plugins,
* (Re-)Start Neo4j server,
* Run `curl -v http://[NEO4J_HOST]:7474/db/data/ext` and make sure LinkSaliencePlugin is mentioned in the list of extensions;

Usage (and limits)
------------------

Perform `curl -v http://[NEO4J_HOST]:7474/db/data/ext/LinkSaliencePlugin` to expose the methods of the REST API. There are 3 flavors:

* `computeLinkSalience` - computes link salience for each relationship in the graph, using the algorithm as described in the 
[supplements to the Grady paper](http://www.nature.com/ncomms/journal/v3/n5/extref/ncomms1847-s1.pdf), 
* `computeLinkSalienceForQueryResult` - computes link salience as above, but on the subset of nodes returned from the provided Cypher query;
* `computeLinkSalienceWithDijkstra` - computes link salience for each relationship in the graph, using the shortest path algorithm built into Neo4j,

Example usages:

* `curl -X POST http://[NEO4J_HOST]:7474/db/data/ext/LinkSaliencePlugin/graphdb/computeLinkSalience \  
	-H "content-type: application/json" -d '{"weightProperty":"w", "directed":"true"}'`
* `curl -X POST http://[NEO4J_HOST]:7474/db/data/ext/LinkSaliencePlugin/graphdb/computeLinkSalience \  
	-H "content-type: application/json" \  
	-d '{"weightProperty":"w", "directed":"true""query":"start n = node(*) where n.surplus! > 0"}'`

The algorithm (at least the version based on the Grady paper, see issues below) has reasonable performance on graphs of limited size 
(few thousand nodes maximum) - salience should be computed within the order of magnitude of seconds/minutes on a single node with modern 
hardware. For larger graphs, some kind of batch parallel implementation should be created, e.g. for the Giraph platform.

Known Issues
------------

The computation using the Neo4j-provided Dijkstra algorithm is significantly slower than the approach based on the Shortest Path Tree (SPT) algorithm proposed by Grady - 
running Dijkstra on n * (n - 1) nodes is less efficient than the "direct" SPT algorithm.

In addition, the computed salience based on the Dijkstra version of the algorithm leads to (slightly) different results on non-trivial graphs, we haven't been able
to find out the reason for that yet.