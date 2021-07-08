/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package linkedgraph;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author Emilia
 */
public class FindFitness {
    
    private static String INPUTDIR = "C:\\Users\\Emilia\\Documents\\Master\\Conference Papers\\Weighted Graph Compression with NSGA-II\\test";
    private static String OUTPUTDIR = "C:\\Users\\Emilia\\Documents\\Master\\Conference Papers\\Weighted Graph Compression with NSGA-II\\testOut\\";
    private static String OUTPUT_FILE = "SumAbsWeightDiff Graph Compression Results - nsga-ii";
    private BufferedWriter RESULT_OUTPUT;
    private BufferedWriter DECOMPRESSED_GRAPH_OUTPUT;
    
    private int GRAPH_SIZE;
    private LinkedGraph GRAPH;
    
    private String FITNESS_METHOD;
    
    public FindFitness() {
        //
        try {
                FileWriter fw = new FileWriter(OUTPUTDIR + OUTPUT_FILE + ".txt");
                this.RESULT_OUTPUT = new BufferedWriter(fw);
        } catch (Exception e) {
                System.out.println("Error creating write file: " + e.getMessage());
        }
        
        //Read solutions and write fitnesses to output file
        File dir = new File(INPUTDIR);
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                String filename = child.getName();
                System.out.println("File: " + filename);
                
                //get the source file name
                String graph = "";
                //month
                if (filename.split("_")[0].equals("04")) {
                    graph = graph + "Apr";
                } else if(filename.split("_")[0].equals("06")) {
                    graph = graph + "June";
                } else if(filename.split("_")[0].equals("07")) {
                    graph = graph + "July";
                }
                //day
                if (filename.split("_")[1].equals("28")) {
                    graph = graph + "28";
                } else if(filename.split("_")[1].equals("02")) {
                    graph = graph + "2";
                } else if(filename.split("_")[1].equals("07")) {
                    graph = graph + "7";
                } else if(filename.split("_")[1].equals("15")) {
                    graph = graph + "15";
                }
                
                //type
                if(!graph.equals("")) {
                    if (filename.split("_")[2].equalsIgnoreCase("adj")) {
                        graph = graph + "_adjusted";
                    }
                }
                
                if (graph.equals("")) {
                    graph = filename.split("_")[0];
                    if (filename.split("_")[1].equalsIgnoreCase("adjusted") || filename.split("_")[1].equalsIgnoreCase("adj")) {
                        graph = graph+"_adjusted";
                    }
                }
                
                //get the fitness method
                this.FITNESS_METHOD = "SumAbsWeightDiff"; //"Sum" + filename.split("_Sum")[1].split("_")[0];
                
                //create the linked graph object
                LinkedGraph g = LinkedGraph.load(graph+".txt");
                this.GRAPH_SIZE = g.getSize();
                this.GRAPH = g;
                
                //read in solution
                Scanner s = null;
		try {
                    s = new Scanner(child);
                    while(s.hasNext()) {
                        String[] line = s.next().split(",");
                        if (line.length > 1) {
                            for(int n = 1; n < line.length; n++) {
                                int nodeOne = Integer.parseInt(line[n]);
                                int nodeTwo = Integer.parseInt(line[0]);
                                this.GRAPH.merge(nodeTwo, nodeOne);
//                                System.out.println("merge node " + nodeOne + " with node " + nodeTwo);
                            }
                        }
                    }
		} catch (IOException e) {
			System.out.println("Solution not loaded properly: " + e.getMessage());
		}
                
//                printDecompressedGraph(g, graph+"_DecompressedGraph");
                
                //Calculate fitness of the compressed graph
                try {
                    double fitness = this.GRAPH.getFitness(this.FITNESS_METHOD);
                    if(this.FITNESS_METHOD == "SumSqrWeightDiff") fitness = Math.sqrt(fitness);
                    this.RESULT_OUTPUT.write(filename.replace(".csv", "") + " = " + fitness + "\n");
                    System.out.println(filename.replace(".csv", "") + " = " + fitness + "\n");
                } catch (IOException ex) {
                    Logger.getLogger(FindFitness.class.getName()).log(Level.SEVERE, null, ex);
                }
                   
            }
        } else {
            System.out.println("Error. No File.");
        }
        
        //close result file
        try {
            this.RESULT_OUTPUT.close();
        } catch (IOException ex) {
            Logger.getLogger(FindFitness.class.getName()).log(Level.SEVERE, null, ex);
        }
    } //FindFitness
    
    private void printDecompressedGraph(LinkedGraph graph, String filename) {
            //print graph info
            try {
                    FileWriter fw = new FileWriter(OUTPUTDIR + filename + ".txt");
                    this.DECOMPRESSED_GRAPH_OUTPUT = new BufferedWriter(fw);
            } catch (Exception e) {
                    System.out.println("Error creating write file: " + e.getMessage());
            }
            try {
                    ArrayList<Neighbors> decompressedGraph = graph.getDecompressedGraph();
                    this.DECOMPRESSED_GRAPH_OUTPUT.write(this.GRAPH_SIZE + "\n");
                    for(int n = 0; n < decompressedGraph.size(); n++) {
                        for(NodeEdge e: decompressedGraph.get(n).getNeighbors()) {
                            this.DECOMPRESSED_GRAPH_OUTPUT.write(n + "\t" + e.getNode() + "\t" + e.getWeight() + "\n");
                        }
                    }
//                    this.DECOMPRESSED_GRAPH_OUTPUT.newLine();
                    this.DECOMPRESSED_GRAPH_OUTPUT.close();
            } catch (Exception e) {
                    System.out.println("Unable to write to file: " + e.getMessage());
            }
        } //printDecompressedGraph
    
    public static void main(String[] args) { FindFitness f = new FindFitness(); };
    
}
