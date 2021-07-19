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
    
    private static String INPUTDIR = "C:\\Users\\Emilia\\Documents\\Master\\COSC 5F90 (MSc Thesis)\\AdjustedRandIndex\\Input\\Converted";
    private static String OUTPUTDIR = "C:\\Users\\Emilia\\Documents\\Master\\COSC 5F90 (MSc Thesis)\\AdjustedRandIndex\\Output\\MergedNodes\\";
    private static String OUTPUT_FILE = "FitnessChromsOfFirstPaper";
    private BufferedWriter RESULT_OUTPUT;
    private BufferedWriter DECOMPRESSED_GRAPH_OUTPUT;
    private BufferedWriter MERGED_NODES_OUTPUT;
    
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
                String graph = filename.split("_")[0];
                
                //type
                if(filename.split("_")[1].equals("adj")) {
                    graph = graph + "_adjusted";
                }
                
                System.out.println("Graph = " + graph);
                
                //get the fitness method
                if(filename.contains("SumAbs")) {
                    this.FITNESS_METHOD = "SumAbsWeightDiff";
                    System.out.println("Fitness Function: SumAbsWeightDiff");
                } else if (filename.contains("SumSqr")) {
                    this.FITNESS_METHOD = "SumSqrWeightDiff";
                    System.out.println("Fitness Function: SumSqrWeightDiff");
                } else if (filename.contains("none")) {
                    this.FITNESS_METHOD = "error";
                    System.out.println("No valid fitness function");
                }
                
                
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
                        System.out.println("s = " + line[0] + " and " + line[1]);
                        if (line.length > 1) {
                            for(int n = 1; n < line.length; n++) {
                                int nodeOneOffset = Integer.parseInt(line[n]);
                                int nodeTwo = Integer.parseInt(line[0].replaceAll("\\p{C}", ""));
                                int nodeOne = (nodeTwo + nodeOneOffset) % this.GRAPH_SIZE;
                                this.GRAPH.merge(nodeTwo, nodeOne);
//                                System.out.println("merge node " + nodeOne + " with node " + nodeTwo);
                            }
                        }
                    }
		} catch (IOException e) {
			System.out.println("Solution not loaded properly: " + e.getMessage());
		}
                
//                printDecompressedGraph(g, graph+"_DecompressedGraph");
                printMergedNodes(g, filename.replace(".csv", ""));
                
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
    
    //print the merged nodes of each node
        private void printMergedNodes(LinkedGraph best, String filename) {
            //print graph info
            try {
                    FileWriter fw = new FileWriter(OUTPUTDIR + filename + "_MergedNodes.csv");
                    this.MERGED_NODES_OUTPUT = new BufferedWriter(fw);
            } catch (Exception e) {
                    System.out.println("Error creating write file: " + e.getMessage());
            }
            try {
                    for (int n = 0; n < best.getNodes().length; n++) {
                        String community = n + ",";
                        if (!best.getNodes()[n].getMergeNodes().isEmpty()) {
                            for(Integer m: best.getNodes()[n].getMergeNodes()) {
                                community = community + m + ",";
                            }
                        }
                        this.MERGED_NODES_OUTPUT.write(community);
                        this.MERGED_NODES_OUTPUT.newLine();
                    }
                    this.MERGED_NODES_OUTPUT.close();
                    System.out.println("Wrote merged nodes file.");
            } catch (Exception e) {
                    System.out.println("Unable to write to file: " + e.getMessage());
            }
            
            
        } //printMergedNodes
    
    public static void main(String[] args) { FindFitness f = new FindFitness(); };
    
}
