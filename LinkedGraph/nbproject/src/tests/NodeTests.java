/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import linkedgraph.Node;
/**
 *
 * @author ar14rk
 */
public class NodeTests {
    private static void printNodes(Node[] nodes){
        for(Node n: nodes){
            System.out.println(n);
        }
    }
    public static void main(String[] args){
        Node[] node = new Node[10];
        for(int i = 0;i<node.length;i++){
            node[i] = new Node(i);
        }
        System.out.println("Original");
        printNodes(node);
        System.out.println("Set: n1->n2");
        node[1].setReference(node[2]);
        System.out.println("Expected: n1->n2");
        printNodes(node);
        System.out.println("Set: n2->n1");
        node[2].setReference(node[1]);
        System.out.println("Expected: n1->n2");
        printNodes(node);
        System.out.println("Set: n2->n3");
        node[2].setReference(node[3]);
        System.out.println("Expected: n1->n2->3, n2->3");
        printNodes(node);
        System.out.println("Set: n3->n4");
        node[3].setReference(node[4]);
        System.out.println("Expected: n1->n2->n3->n4, n2->n3->n4, n3->n4");
        printNodes(node);
        System.out.println("Set: n4->n2");
        node[4].setReference(node[2]);
        System.out.println("Expected: n1->n2->n3->n4, n2->n3->n4, n3->n4");
        printNodes(node);
    }
}
