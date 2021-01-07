/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package linkedgraph;

import java.util.ArrayList;
import java.util.Set;

/**
 *
 * @author Emilia
 */
public class Neighbors {
    private ArrayList<NodeEdge> neighbors;
    
    public Neighbors() {
        neighbors = new ArrayList<>();
    } //default constructor
    
    public Neighbors(Neighbors n) {
//        neighbors = n.getNeighbors();
        this();
        addAllNodeEdge(n.getNeighbors());
    } //constructor
    
    public Neighbors(Set<NodeEdge> n) {
//        neighbors = new ArrayList<>(n);
        this();
        addAllNodeEdge(n);
    } //constructor
    
    public ArrayList<NodeEdge> getNeighbors() {
        return neighbors;
    } //getNodes
    
    public void addNodeAndWeight(int n, double w) {
        if(!containsNode(n)) neighbors.add(new NodeEdge(n, w));
    } //addNodeAndWeight
    
    public void addAllNodeEdge(ArrayList<NodeEdge> nodes) {
        for(NodeEdge node: nodes) {
            addNodeEdge(node);
        }
    } //addAllNodeEdge
    
    public void addAllNodeEdge(Set<NodeEdge> nodes) {
        for(NodeEdge node: nodes) {
            addNodeEdge(node);
        }
    } //addAllNodeEdge
    
    public void addNodeEdge(NodeEdge n) {
        if(!containsNode(n.getNode())) {
            addNodeAndWeight(n.getNode(), n.getWeight());
        }
    } //addNeighbor()
    
    public void removeAllNodes(Neighbors n) {
        for(NodeEdge neighbor: n.getNeighbors()) {
            removeNode(neighbor.getNode());
        }
    } //removeAllNodes
    
    public void removeNode(int n) {
        while(containsNode(n)) {
            neighbors.remove(indexOf(n));
        }
    } //removeNeighbor
    
    public double weightOf(int n) {
        double weight = 0;
        for(NodeEdge neighbor: neighbors) {
            if(neighbor.getNode() == n) {
                weight = neighbor.getWeight();
            }
        }
        return weight;
    } //weightOf
    
    public void setWightOfNode(int n, double w) {
        for(NodeEdge neighbor: neighbors) {
            if(neighbor.getNode() == n) {
                neighbor.setWeight(w);
            }
        }
    } //setWeightOfNode
    
    public boolean containsNode(int n) {
        boolean contains = false;
        for(NodeEdge neighbor: neighbors) {
            if(neighbor.getNode() == n) contains = true;
        }
        return contains;
    } //containsNode
    
    private int indexOf(int node) {
        int index = -1;
        for(NodeEdge neighbor: neighbors) {
            if(neighbor.getNode() == node) {
                index = neighbors.indexOf(neighbor);
            }
        }
        return index;
    } //indexOf
        
} //Neighbors
