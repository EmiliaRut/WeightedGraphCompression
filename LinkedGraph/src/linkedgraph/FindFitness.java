/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package linkedgraph;

import ga.GAImplementation;
import static ga.GAImplementation.buildChromesomeString;
import java.io.BufferedWriter;
import java.util.ArrayList;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author Emilia
 */
public class FindFitness {
    
    private static String INPUTDIR = "C:\\Users\\Emilia\\Documents\\Master\\Conference Papers\\Graph Compression and Community Detection\\communities\\louvaincommunities";
    private static String OUTPUTDIR = "C:\\Users\\Emilia\\Documents\\Master\\Conference Papers\\Graph Compression and Community Detection\\communities\\";
    private static String OUTPUT_FILE = "CommunityFitnessResults - louvaincommunities - SumSqrWeightDiff";
    private BufferedWriter RESULT_OUTPUT;
    
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
                if (filename.split("_")[2].equals("adj")) {
                    graph = graph + "_adjusted";
                }
                
                if (graph.equals("")) {
                    graph = filename.split("_")[0];
                    if (filename.split("_")[1].equalsIgnoreCase("adjusted")) {
                        graph = graph+"_adjusted";
                    }
                }
                
                //get the fitness method
                this.FITNESS_METHOD = "SumSqrWeightDiff"; //"Sum" + filename.split("_Sum")[1].split("_")[0];
                
                //create the linked graph object
                LinkedGraph g = LinkedGraph.load(graph+".txt");
                this.GRAPH_SIZE = g.getSize();
                this.GRAPH = g;
                
                //create the compressed linked graph object
//                this.CURRENT_GRAPH = (LinkedGraph) this.ORIGINAL_GRAPH.deepCopy();
                
                //read in solution
                Scanner s = null;
		try {
                    s = new Scanner(child);
                    while(s.hasNext()) {
                        String[] line = s.next().split(",");
                        if (line.length > 1) {
                            for(int n = 1; n < line.length; n++) {
                                int nodeOne = Integer.parseInt(line[0]);
                                int nodeTwo = Integer.parseInt(line[n]);
//                                nodeTwo = Math.floorMod(nodeTwo - nodeOne, this.GRAPH_SIZE);
                                this.GRAPH.merge(nodeTwo, nodeOne);
                            }
                        }
                    }
		} catch (IOException e) {
			System.out.println("Solution not loaded properly: " + e.getMessage());
		}
                
                //Calculate fitness of the compressed graph
                try {
                    double fitness = this.GRAPH.getFitness(this.FITNESS_METHOD);
                    this.RESULT_OUTPUT.write(filename.replace(".csv", "") + " = " + fitness + "\n");
//                    System.out.println("Fitness: " + fitness);
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
    
    public static void main(String[] args) { FindFitness f = new FindFitness(); };
    
}
