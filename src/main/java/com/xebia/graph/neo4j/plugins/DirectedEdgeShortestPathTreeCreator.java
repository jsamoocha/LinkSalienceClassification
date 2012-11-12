package com.xebia.graph.neo4j.plugins;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

public class DirectedEdgeShortestPathTreeCreator extends ShortestPathTreeCreator {

  public DirectedEdgeShortestPathTreeCreator(String weightPropertyName) {
    super(weightPropertyName);
  }
  
  @Override
  protected Iterable<Relationship> getEdgesConnectedTo(Node node) {
    return node.getRelationships(Direction.OUTGOING);
  }

}
