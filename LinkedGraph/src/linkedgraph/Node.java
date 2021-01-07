package linkedgraph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedList;
import java.util.Set;

/**
 * Node class that holds a reference to it's actual value
 *
 * @author ar14rk
 */
public class Node {
        
        public static final double FAKE_EDGE_WEIGHT = 0.0;
    
	private Node REFERENCE;
	public final int ID;
//	private Set<NodeEdge> FAKE_EDGES;
	private Set<Integer> MERGED_NODES;

	/**
	 * @param id the index of value of the node
	 */
	public Node(int id) {
		this.ID = id;
		this.REFERENCE = this;
		this.MERGED_NODES = new HashSet<>();
//		this.FAKE_EDGES = new HashSet<>();
	}

	/**
	 * This method will only copy the object's value, not the reference.
	 *
	 * @param other the node object to copy
	 */
	public Node(Node other) {
		this(other.ID);
	}

	public void absorb(int node) {
		this.MERGED_NODES.add(node);
	}

	public void absorb(Set<Integer> nodes) {
		this.MERGED_NODES.addAll(nodes);
	}

	public Set<Integer> getMergeNodes() {
		return new HashSet<>(this.MERGED_NODES);
	}

//	public void addFakeEdge(int node) {
//		if(!containsNode(node)) this.FAKE_EDGES.add(new NodeEdge(node, FAKE_EDGE_WEIGHT));
//	}
        
//        public void addFakeEdge(int node, double weight) {
//		if(!containsNode(node)) this.FAKE_EDGES.add(new NodeEdge(node, weight));
//	}

//	public void addFakeEdges(ArrayList<NodeEdge> nodes) {
////		this.FAKE_EDGES.addAll(nodes);
//                for(NodeEdge node: nodes) {
//                    addFakeEdge(node.getNode(), node.getWeight());
//                }
//	}
        
//        private boolean containsNode(int n) {
//            boolean contains = false;
//            for(NodeEdge node: this.FAKE_EDGES) {
//                if(node.getNode() == n) contains = true;
//            }
//            return contains;
//        } //containsNode

//	public Set<NodeEdge> getFakeEdges() {
//		return new HashSet<>(this.FAKE_EDGES);
//	}

	/**
	 * Sets node reference to itself
	 */
	public void cleanReference() {
		this.setReference(this);
	}

	/**
	 * This method hops through all the references and returns the very latest
	 * reference in the list
	 *
	 * @return
	 */
	public Node getReference() {
		// This method is used primarily to avoid loops
		if (this.REFERENCE == this) {
			return this;
		}
		return this.REFERENCE.getReference();
	}

	/**
	 * This sets this node's reference to be last reference in the list of
	 * references
	 *
	 * @param other
	 */
	public void setReference(Node other) {
		this.REFERENCE = other.getReference();
	}

	/**
	 * Returns the value of the end of the list of references
	 *
	 * @return
	 */
	public int getId() {
		if (this.REFERENCE == this) {
			return this.ID;
		}
		return this.REFERENCE.getId();
	}

	@Override
	public String toString() {
		if (this.REFERENCE == this) {
			return "(" + this.ID + ":" + Integer.toHexString(this.hashCode()) + ")";
		} else {
			return "("
					+ this.ID + ":"
					+ Integer.toHexString(this.hashCode()) + "->"
					+ this.REFERENCE.toString()
					+ ")";
		}
	}
}
