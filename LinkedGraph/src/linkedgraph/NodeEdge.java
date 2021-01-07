/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package linkedgraph;

/**
 *
 * @author Emilia
 */
public class NodeEdge {
    private int node;
    private double weight;
    
    public NodeEdge(int n, double w) {
        node = n;
        weight = w;
    } //constructor
    
    public int getNode() {
        return node;
    } //getNode
    
    public double getWeight() {
        return weight;
    } //getWeight
    
    public void setWeight(double w) {
        weight = w;
    } //setWeight
    
} //NodeEdge
