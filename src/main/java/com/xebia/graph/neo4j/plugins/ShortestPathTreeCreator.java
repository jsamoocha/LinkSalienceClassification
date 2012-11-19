package com.xebia.graph.neo4j.plugins;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Stack;
import java.util.logging.Logger;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ShortestPathTreeCreator {
  private static final Logger LOG = Logger.getLogger(ShortestPathTreeCreator.class.getName());
  private Map<Node, List<Node>> predecessors = Maps.newHashMap();
  private PriorityQueue<Node> queue = new PriorityQueue<Node>(13, new SptNodeComparator());
  private Stack<Node> stack = new Stack<Node>();
  private Map<Node, Double> sptNodeDistancesToRootNode = Maps.newHashMap();
  private String weightPropertyName;
  private List<Long> nodeIdsToProcess = null;

  private class SptNodeComparator implements Comparator<Node> {

    public int compare(Node n1, Node n2) {
      return Double.compare(sptNodeDistancesToRootNode.get(n1), sptNodeDistancesToRootNode.get(n2));
    }

  }

  public ShortestPathTreeCreator(String weightPropertyName) {
    this.weightPropertyName = weightPropertyName;
  }

  public ShortestPathTreeCreator(String weightPropertyName, List<Long> nodesInSubGraph) {
    this.weightPropertyName = weightPropertyName;
    this.nodeIdsToProcess = nodesInSubGraph;
  }

  public ShortestPathTree createShortestPathTree(Node rootNode) {
    initNodes(rootNode);

    while (!queue.isEmpty()) {
      Node minimumDistanceNode = queue.poll();

      stack.push(minimumDistanceNode);

      for (Relationship edge : getEdgesConnectedTo(minimumDistanceNode)) {
        Node potentialShortestPathNode = edge.getOtherNode(minimumDistanceNode);

        if (nodeIdsToProcess == null || nodeIdsToProcess.contains(potentialShortestPathNode.getId())) {

          double connectionDistance = 1.0 / getWeightPropertyAsDoubleFor(edge);
          double minimumDistance = getDistance(minimumDistanceNode);
          double potentialShortestPathNodeDistance = getDistance(potentialShortestPathNode);

          if (potentialShortestPathNodeDistance > minimumDistance + connectionDistance) {
            sptNodeDistancesToRootNode.put(potentialShortestPathNode, minimumDistance + connectionDistance);

            if (queue.contains(potentialShortestPathNode)) {
              queue.remove(potentialShortestPathNode);
            }

            queue.add(potentialShortestPathNode);
            predecessors.put(potentialShortestPathNode, new ArrayList<Node>());
          }

          if (getDistance(potentialShortestPathNode) != Double.POSITIVE_INFINITY
                  && getDistance(potentialShortestPathNode) == minimumDistance + connectionDistance) {
            predecessors.get(potentialShortestPathNode).add(minimumDistanceNode);
          }
        }
      }
    }

    return new ShortestPathTree(stack, predecessors);
  }

  private double getWeightPropertyAsDoubleFor(Relationship edge) {
    Object weightValue = edge.getProperty(weightPropertyName);

    if (weightValue instanceof Double) {
      return (Double) weightValue;
    } else if (weightValue instanceof Integer) {
      return (double) ((Integer) weightValue).intValue();
    } else if (weightValue instanceof Long) {
      return (double) ((Long) weightValue).longValue();
    } else if (weightValue instanceof Float) {
      return ((Float) weightValue).doubleValue();
    } else if (weightValue instanceof String) {
      try {
        return Double.parseDouble(weightPropertyName);
      } catch (NumberFormatException e) {
        LOG.warning("Failed to parse weight property for edge [" + edge.getId() + "]: " + e.getMessage());
        return 0.0;
      }
    } else
      return 0.0;
  }

  protected Iterable<Relationship> getEdgesConnectedTo(Node node) {
    return node.getRelationships();
  }

  private double getDistance(Node node) {
    Double distance = sptNodeDistancesToRootNode.get(node);
    return (distance == null) ? Double.POSITIVE_INFINITY : distance.doubleValue();
  }

  void initNodes(Node rootNode) {
    sptNodeDistancesToRootNode.clear();
    predecessors.clear();

    sptNodeDistancesToRootNode.put(rootNode, 0.0);
    predecessors.put(rootNode, Lists.<Node> newArrayList());
    queue.add(rootNode);
  }

}
