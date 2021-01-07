package linkedgraph;

import graph.Graph;

// file input/output
import java.io.File;
import java.util.Scanner;
import java.io.IOException;

// datastructures
import java.util.List;
// could probably replace all instances of arraylist with linked list
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;
import java.util.Queue;
import java.util.PriorityQueue;

/**
 * This object implements the Graph interface designed by Tyler. Additional
 * methods such as addEdge or load have been added on to this implementation to
 * assist the developer.
 *
 * @author aromualdo
 */
public class LinkedGraph implements Graph {

	// set this to false to hide hashId when printing.
	private final boolean SHOW_MEMORY = false;
        public static final int FAKE_EDGE_WEIGHT = 0;
	/**
	 * The list of all the vertices in the graph.
	 */
	private Node[] NODES;
	/**
	 * The list of all the edges in the decompressed graph.
	 */
	private ArrayList<Neighbors> DECOMPRESSED_MATRIX;
        /**
	 * The list of all the edges in the compressed graph.
	 */
	private ArrayList<Neighbors> COMPRESSED_MATRIX;
	/**
	 * The list of all the edges in the graph.
	 */
	private ArrayList<Neighbors> ORIGINAL_MATRIX;
	/**
	 * The current size of the graph, after all the merges.
	 */
	private int SIZE;
	/**
	 * The maximum size of the graph, or rather, the original size of the graph.
	 */
	private int MAX_SIZE;

	/**
	 * Private constructor that constructs the required data for an empty
	 * LinkedGraph, such as the size, adjacency list, default nodes.
	 *
	 * @param size Integer value of the size of the file
	 */
	private LinkedGraph(int size) {
		this.SIZE = size;
		this.MAX_SIZE = size;
		this.DECOMPRESSED_MATRIX = new ArrayList<Neighbors>(size);
		this.ORIGINAL_MATRIX = new ArrayList<Neighbors>(size);
		this.NODES = new Node[size];
		for (int i = 0; i < this.NODES.length; i++) {
			// populate nodes with default references
			this.NODES[i] = new Node(i);
			// populate adjacency list with empty lists
			this.DECOMPRESSED_MATRIX.add(new Neighbors());
			this.ORIGINAL_MATRIX.add(new Neighbors());
		}
	}

	/**
	 * Getter method for MAX SIZE
	 */
	public int getSize() {
		return this.MAX_SIZE;
	}

	/**
	 * Getter method for SIZE
	 */
	public int getCurrentSize() {
		return this.SIZE;
	}
        
	/**
	 * Manually add in edges, primarily for testing smaller data sets.
	 *
	 * @param from Integer value of the index of the first node
	 * @param to Integer value of the index of the second node
	 */
	public void addEdge(int from, int to, int weight) {
		int aFrom = this.NODES[from].getId();
		int aTo = this.NODES[to].getId();
		if (!this.ORIGINAL_MATRIX.get(aFrom).containsNode(aTo)) {
			this.DECOMPRESSED_MATRIX.get(aFrom).addNodeAndWeight(aTo, weight);
			this.ORIGINAL_MATRIX.get(aFrom).addNodeAndWeight(aTo, weight);
		}
		if (!this.ORIGINAL_MATRIX.get(aTo).containsNode(aFrom)) {
			this.DECOMPRESSED_MATRIX.get(aTo).addNodeAndWeight(aFrom, weight);
			this.ORIGINAL_MATRIX.get(aTo).addNodeAndWeight(aFrom, weight);
		}
	}

	/**
	 * Loads and returns a LinkedGraph with an existing list of edges.
	 *
	 * @param matrix the list of edges in 2D ArrayList form
	 * @return
	 */
	public static LinkedGraph load(ArrayList<Neighbors> m) {
		LinkedGraph other = new LinkedGraph(m.size());
		// populate the adjacency matrix/list
		for (int i = 0; i < other.MAX_SIZE; i++) {
			other.DECOMPRESSED_MATRIX.set(i, new Neighbors(m.get(i)));
			other.ORIGINAL_MATRIX.set(i, new Neighbors(m.get(i)));
		}
		return other;
	}

	/**
	 * Loads and returns a LinkedGraph with an existing data set. The expected
	 * format of the file is the first line being the integer number of vertices
	 * and each subsequent line contains an edge
	 * <br/>
	 * The below example creates a triangle.<br/>
	 * Example:     <br/>
	 * 3       <br/>
	 * 0 1   <br/>
	 * 0 2   <br/>
	 * 1 2   <br/>
	 *
	 * @param filename String name and location of the file
	 * @return
	 */
	public static LinkedGraph load(String filename) {
		File f = new File(filename);
		Scanner s = null;
		try {
			s = new Scanner(f);
		} catch (IOException e) {
			System.out.println("Graph not loaded properly: " + e.getMessage());
			return null;
		}
		int SIZE = s.nextInt();
		// set default data
		LinkedGraph other = new LinkedGraph(SIZE);
		// populate the adjacency matrix/list
		while (s.hasNext()) {
			int from = s.nextInt();
			int to = s.nextInt();
                        int weight = s.nextInt();
			other.addEdge(from, to, weight);
		}
		return other;
	}

	public Node get(int index) {
		return this.NODES[this.NODES[index].getId()];
	}

	public boolean sameCluster(int from, int to) {
		int slave = this.NODES[from].getId();
		int master = this.NODES[to].getId();
                
		return slave == master;
	}

	/*
    From here on, the javadocs will be inherited from the Graph class that this
    code implements .
	 */
	public void merge(int from, int to) {
            
		int slave = this.NODES[from].getId();
		int master = this.NODES[to].getId();
//                System.out.println("mergeing " + slave + " and " + master);
                                
		if (master == slave) {
			System.out.println(from + " to " + to);
			return;
		}

		this.NODES[slave].setReference(this.NODES[master]);
               
                //connect slave neighbors (-master) to master
                Neighbors slaveNeighbors = new Neighbors(this.DECOMPRESSED_MATRIX.get(slave));
                for(int node: this.NODES[slave].getMergeNodes()) {
                    slaveNeighbors.removeNode(node);
                }
                for(int node: this.NODES[master].getMergeNodes()) {
                    slaveNeighbors.removeNode(node);
                }
                slaveNeighbors.removeNode(master);
                slaveNeighbors.removeNode(slave);
                for(NodeEdge edge: slaveNeighbors.getNeighbors()) {
                    //average out the edge weights
                    double numOfEdges = 2+this.NODES[slave].getMergeNodes().size() + this.NODES[master].getMergeNodes().size();
                    double totalWeight = (edge.getWeight()*(1+this.NODES[slave].getMergeNodes().size())) + (this.DECOMPRESSED_MATRIX.get(master).weightOf(edge.getNode())*(1+this.NODES[master].getMergeNodes().size()));
                    double newEdgeWeight = totalWeight/numOfEdges;
                    
                    //update weight between edge and slave
                    this.DECOMPRESSED_MATRIX.get(slave).setWightOfNode(edge.getNode(), newEdgeWeight);
                    this.DECOMPRESSED_MATRIX.get(edge.getNode()).setWightOfNode(slave, newEdgeWeight);
                    
                    //update or add weight between edge and master
                    if(this.DECOMPRESSED_MATRIX.get(master).containsNode(edge.getNode())) {
                        this.DECOMPRESSED_MATRIX.get(master).setWightOfNode(edge.getNode(), newEdgeWeight);
                        this.DECOMPRESSED_MATRIX.get(edge.getNode()).setWightOfNode(master, newEdgeWeight);
                    } else {
                        this.DECOMPRESSED_MATRIX.get(master).addNodeAndWeight(edge.getNode(), newEdgeWeight);
                        this.DECOMPRESSED_MATRIX.get(edge.getNode()).addNodeAndWeight(master, newEdgeWeight);
                    }
                    
                    //update or add weight between all the merged nodes in slave and edge
                    for(int node: this.NODES[slave].getMergeNodes()) {
                        if(node != edge.getNode()) {
                            if(this.DECOMPRESSED_MATRIX.get(edge.getNode()).containsNode(node)) {
                                this.DECOMPRESSED_MATRIX.get(edge.getNode()).setWightOfNode(node, newEdgeWeight);
                                this.DECOMPRESSED_MATRIX.get(node).setWightOfNode(edge.getNode(), newEdgeWeight);
                            } else {
                                this.DECOMPRESSED_MATRIX.get(edge.getNode()).addNodeAndWeight(node, newEdgeWeight);
                                this.DECOMPRESSED_MATRIX.get(node).addNodeAndWeight(edge.getNode(), newEdgeWeight);
                            }
                        }
                    }
                    
                    //update or add weight between all the merged nodes in master and edge
                    for(int node: this.NODES[master].getMergeNodes()) {
                        if(node != edge.getNode()) {
                            if(this.DECOMPRESSED_MATRIX.get(edge.getNode()).containsNode(node)) {
                                this.DECOMPRESSED_MATRIX.get(edge.getNode()).setWightOfNode(node, newEdgeWeight);
                                this.DECOMPRESSED_MATRIX.get(node).setWightOfNode(edge.getNode(), newEdgeWeight);
                            } else {
                                this.DECOMPRESSED_MATRIX.get(edge.getNode()).addNodeAndWeight(node, newEdgeWeight);
                                this.DECOMPRESSED_MATRIX.get(node).addNodeAndWeight(edge.getNode(), newEdgeWeight);
                            }
                        }
                    }
                }
                
                //connect master neighbors (-slave&merged nodes) to slave
                Neighbors masterNeighbors = new Neighbors(this.DECOMPRESSED_MATRIX.get(master));
                for(int node: this.NODES[master].getMergeNodes()) {
                    masterNeighbors.removeNode(node);
                }
                for(int node: this.NODES[slave].getMergeNodes()) {
                    masterNeighbors.removeNode(node);
                }
                masterNeighbors.removeNode(slave);
                masterNeighbors.removeNode(master);
                for(NodeEdge edge: masterNeighbors.getNeighbors()) {
                    double numOfEdges = 2+this.NODES[slave].getMergeNodes().size() + this.NODES[master].getMergeNodes().size();
                    double totalWeight = (edge.getWeight()*(1+this.NODES[master].getMergeNodes().size())) + (this.DECOMPRESSED_MATRIX.get(slave).weightOf(edge.getNode())*(1+this.NODES[slave].getMergeNodes().size()));
                    double newEdgeWeight = totalWeight/numOfEdges;
                    
                    //update weight between edge and master
                    this.DECOMPRESSED_MATRIX.get(master).setWightOfNode(edge.getNode(), newEdgeWeight);
                    this.DECOMPRESSED_MATRIX.get(edge.getNode()).setWightOfNode(master, newEdgeWeight);
                    
                    //update or add weight between edge and slave
                    if(this.DECOMPRESSED_MATRIX.get(slave).containsNode(edge.getNode())) {
                        this.DECOMPRESSED_MATRIX.get(slave).setWightOfNode(edge.getNode(), newEdgeWeight);
                        this.DECOMPRESSED_MATRIX.get(edge.getNode()).setWightOfNode(slave, newEdgeWeight);
                    } else {
                        this.DECOMPRESSED_MATRIX.get(slave).addNodeAndWeight(edge.getNode(), newEdgeWeight);
                        this.DECOMPRESSED_MATRIX.get(edge.getNode()).addNodeAndWeight(slave, newEdgeWeight);
                    }
                    
                    //update or add weight between all the merged nodes in master and edge
                    for(int node: this.NODES[master].getMergeNodes()) {
                        if(node != edge.getNode()) {
                            if(this.DECOMPRESSED_MATRIX.get(edge.getNode()).containsNode(node)) {
                                this.DECOMPRESSED_MATRIX.get(edge.getNode()).setWightOfNode(node, newEdgeWeight);
                                this.DECOMPRESSED_MATRIX.get(node).setWightOfNode(edge.getNode(), newEdgeWeight);
                            } else {
                                this.DECOMPRESSED_MATRIX.get(edge.getNode()).addNodeAndWeight(node, newEdgeWeight);
                                this.DECOMPRESSED_MATRIX.get(node).addNodeAndWeight(edge.getNode(), newEdgeWeight);
                            }
                        }
                    }
                    
                    //update or add weight between all the merged nodes in slave and edge
                    for(int node: this.NODES[slave].getMergeNodes()) {
                        if(node != edge.getNode()) {
                            if(this.DECOMPRESSED_MATRIX.get(edge.getNode()).containsNode(node)) {
                                this.DECOMPRESSED_MATRIX.get(edge.getNode()).setWightOfNode(node, newEdgeWeight);
                                this.DECOMPRESSED_MATRIX.get(node).setWightOfNode(edge.getNode(), newEdgeWeight);
                            } else {
                                this.DECOMPRESSED_MATRIX.get(edge.getNode()).addNodeAndWeight(node, newEdgeWeight);
                                this.DECOMPRESSED_MATRIX.get(node).addNodeAndWeight(edge.getNode(), newEdgeWeight);
                            }
                        }
                    }         
                }
                
                //add/update an edge with new weight between slave and master
                double numOfEdges = 1; //edge between slave and master
                double totalWeight = this.DECOMPRESSED_MATRIX.get(slave).weightOf(master); //weight between slave and master
                for(int mNodes: this.NODES[master].getMergeNodes()) { //edges between slave and master merges
                    totalWeight = totalWeight + this.DECOMPRESSED_MATRIX.get(slave).weightOf(mNodes);
                    totalWeight = totalWeight + this.DECOMPRESSED_MATRIX.get(master).weightOf(mNodes);
                    numOfEdges += 2;
                    for(int sNodes: this.NODES[slave].getMergeNodes()) {
                        totalWeight = totalWeight + this.DECOMPRESSED_MATRIX.get(mNodes).weightOf(sNodes);
                        numOfEdges++;
                    }
                }
                for(int sNodes: this.NODES[slave].getMergeNodes()) { //edges between master and slave merges
                    totalWeight = totalWeight + this.DECOMPRESSED_MATRIX.get(master).weightOf(sNodes);
                    totalWeight = totalWeight + this.DECOMPRESSED_MATRIX.get(slave).weightOf(sNodes);
                    numOfEdges += 2;
                }
                double newEdgeWeight = totalWeight/numOfEdges;

                //update or add weight between slave and master
                if(this.DECOMPRESSED_MATRIX.get(master).containsNode(slave)) {
                    this.DECOMPRESSED_MATRIX.get(master).setWightOfNode(slave, newEdgeWeight);
                    this.DECOMPRESSED_MATRIX.get(slave).setWightOfNode(master, newEdgeWeight);
                } else {
                    this.DECOMPRESSED_MATRIX.get(master).addNodeAndWeight(slave, newEdgeWeight);
                    this.DECOMPRESSED_MATRIX.get(slave).addNodeAndWeight(master, newEdgeWeight);
                }
                
                //for all slave merge nodes
                for(int sNode: this.NODES[slave].getMergeNodes()) {
                    //update or add weight between slave merge and master
                    if(this.DECOMPRESSED_MATRIX.get(master).containsNode(sNode)) {
                        this.DECOMPRESSED_MATRIX.get(master).setWightOfNode(sNode, newEdgeWeight);
                        this.DECOMPRESSED_MATRIX.get(sNode).setWightOfNode(master, newEdgeWeight);
                    } else {
                        this.DECOMPRESSED_MATRIX.get(master).addNodeAndWeight(sNode, newEdgeWeight);
                        this.DECOMPRESSED_MATRIX.get(sNode).addNodeAndWeight(master, newEdgeWeight);
                    }
                    //update or add weight between slave merge and slave
                    if(this.DECOMPRESSED_MATRIX.get(slave).containsNode(sNode)) {
                        this.DECOMPRESSED_MATRIX.get(slave).setWightOfNode(sNode, newEdgeWeight);
                        this.DECOMPRESSED_MATRIX.get(sNode).setWightOfNode(slave, newEdgeWeight);
                    } else {
                        this.DECOMPRESSED_MATRIX.get(slave).addNodeAndWeight(sNode, newEdgeWeight);
                        this.DECOMPRESSED_MATRIX.get(sNode).addNodeAndWeight(slave, newEdgeWeight);
                    }
                    //for all master merges
                    for(int mNode: this.NODES[master].getMergeNodes()) {
                        //update or add weight
                        if(this.DECOMPRESSED_MATRIX.get(sNode).containsNode(mNode)) {
                            this.DECOMPRESSED_MATRIX.get(sNode).setWightOfNode(mNode, newEdgeWeight);
                            this.DECOMPRESSED_MATRIX.get(mNode).setWightOfNode(sNode, newEdgeWeight);
                        } else {
                            this.DECOMPRESSED_MATRIX.get(sNode).addNodeAndWeight(mNode, newEdgeWeight);
                            this.DECOMPRESSED_MATRIX.get(mNode).addNodeAndWeight(sNode, newEdgeWeight);
                        }
                    }
                }
                //for all master merge
                for(int mNode: this.NODES[master].getMergeNodes()) {
                    //update or add weight between slave and master merge
                    if(this.DECOMPRESSED_MATRIX.get(slave).containsNode(mNode)) {
                        this.DECOMPRESSED_MATRIX.get(slave).setWightOfNode(mNode, newEdgeWeight);
                        this.DECOMPRESSED_MATRIX.get(mNode).setWightOfNode(slave, newEdgeWeight);
                    } else {
                        this.DECOMPRESSED_MATRIX.get(slave).addNodeAndWeight(mNode, newEdgeWeight);
                        this.DECOMPRESSED_MATRIX.get(mNode).addNodeAndWeight(slave, newEdgeWeight);
                    }
                    //update or add weight between master and master merge
                    if(this.DECOMPRESSED_MATRIX.get(master).containsNode(mNode)) {
                        this.DECOMPRESSED_MATRIX.get(master).setWightOfNode(mNode, newEdgeWeight);
                        this.DECOMPRESSED_MATRIX.get(mNode).setWightOfNode(master, newEdgeWeight);
                    } else {
                        this.DECOMPRESSED_MATRIX.get(master).addNodeAndWeight(mNode, newEdgeWeight);
                        this.DECOMPRESSED_MATRIX.get(mNode).addNodeAndWeight(master, newEdgeWeight);
                    }
                }

                //add the slave as a merged node of master
		this.NODES[master].absorb(slave);
                for(int n: this.NODES[slave].getMergeNodes()) {
                    this.NODES[master].absorb(n);
                }
                
		// updates size
		this.SIZE--;
//                System.out.println("done mergeing " + slave + " and " + master);
	}
        
        public double getFitness(String method) {
            double fitness = -1;
            if(method.equalsIgnoreCase("AbsoluteTotalWeightDiff")) {
                double originalWeight = 0;
                for(Neighbors n: this.ORIGINAL_MATRIX) {
                    for(NodeEdge e: n.getNeighbors()) {
                        originalWeight += e.getWeight();
                    }
                }
                
                double decompressedWeight = 0;
                for(Neighbors n: this.DECOMPRESSED_MATRIX) {
                    for(NodeEdge e: n.getNeighbors()) {
                        decompressedWeight += e.getWeight();
                    }
                }

                fitness = Math.abs(decompressedWeight - originalWeight);
                if(fitness > 0.05) {
                    System.out.println("fitness is: " + fitness);
                }
            } else if (method.equalsIgnoreCase("SumAbsWeightDiff")) {
                double absWeight = 0;
                for(int n = 0; n < this.ORIGINAL_MATRIX.size(); n++) {
                    for(int e = 0; e <this.ORIGINAL_MATRIX.size(); e++) {
                        absWeight += Math.abs(this.DECOMPRESSED_MATRIX.get(n).weightOf(e) - this.ORIGINAL_MATRIX.get(n).weightOf(e));
                    }
                }
                fitness = absWeight;
            } else if(method.equalsIgnoreCase("SumSqrWeightDiff")) {
                double sqrWeight = 0;
                for(int n = 0; n < this.ORIGINAL_MATRIX.size(); n++) {
                    for(int e = 0; e <this.ORIGINAL_MATRIX.size(); e++) {
                        sqrWeight += Math.pow(this.DECOMPRESSED_MATRIX.get(n).weightOf(e) - this.ORIGINAL_MATRIX.get(n).weightOf(e), 2);
                    }
                }
                fitness = sqrWeight;
            } else {
                fitness = -1;
            }
            
            return fitness;
            
	}
        
        public void initializeCompressedGraph() {
            this.COMPRESSED_MATRIX = new ArrayList<>(this.MAX_SIZE);
            for(int n = 0; n < this.ORIGINAL_MATRIX.size(); n++) {
                this.COMPRESSED_MATRIX.add(new Neighbors(this.ORIGINAL_MATRIX.get(n)));
            }
        }//initializeCompressedGraph
        
        public void compressedMerge(int from, int to) {
            
            int slave = this.NODES[from].getId();
            int master = this.NODES[to].getId();

            if (master == slave) {
                    System.out.println(from + " to " + to);
                    return;
            }

            this.NODES[slave].setReference(this.NODES[master]);
            
            //connect slave neighbors (-master) to master
            Neighbors slaveNeighbors = new Neighbors(this.COMPRESSED_MATRIX.get(slave));
            slaveNeighbors.removeNode(master);
            slaveNeighbors.removeNode(slave);
            for(NodeEdge edge: slaveNeighbors.getNeighbors()) {
                //average out the edge weights
                double numOfEdges = 2+this.NODES[slave].getMergeNodes().size() + this.NODES[master].getMergeNodes().size();
                double totalWeight = (edge.getWeight()*(1+this.NODES[slave].getMergeNodes().size())) + (this.COMPRESSED_MATRIX.get(master).weightOf(edge.getNode())*(1+this.NODES[master].getMergeNodes().size()));
                double newEdgeWeight = totalWeight/numOfEdges;

                //update or add weight between edge and master
                if(this.COMPRESSED_MATRIX.get(master).containsNode(edge.getNode())) {
                    this.COMPRESSED_MATRIX.get(master).setWightOfNode(edge.getNode(), newEdgeWeight);
                    this.COMPRESSED_MATRIX.get(edge.getNode()).setWightOfNode(master, newEdgeWeight);
                } else {
                    this.COMPRESSED_MATRIX.get(master).addNodeAndWeight(edge.getNode(), newEdgeWeight);
                    this.COMPRESSED_MATRIX.get(edge.getNode()).addNodeAndWeight(master, newEdgeWeight);
                }
                
                //remove edge from slave
                this.COMPRESSED_MATRIX.get(slave).removeNode(edge.getNode());
                this.COMPRESSED_MATRIX.get(edge.getNode()).removeNode(slave);
            }
            
            //connect master neighbors (-slave) to master
            Neighbors masterNeighbors = new Neighbors(this.COMPRESSED_MATRIX.get(master));
            masterNeighbors.removeNode(master);
            masterNeighbors.removeNode(slave);
            masterNeighbors.removeAllNodes(slaveNeighbors);
            for(NodeEdge edge: masterNeighbors.getNeighbors()) {
                //average out the edge weights
                double numOfEdges = 2+this.NODES[slave].getMergeNodes().size() + this.NODES[master].getMergeNodes().size();
                double totalWeight = (edge.getWeight()*(1+this.NODES[master].getMergeNodes().size())) + (this.COMPRESSED_MATRIX.get(slave).weightOf(edge.getNode())*(1+this.NODES[slave].getMergeNodes().size()));
                double newEdgeWeight = totalWeight/numOfEdges;

                //update or add weight between edge and master
                if(this.COMPRESSED_MATRIX.get(master).containsNode(edge.getNode())) {
                    this.COMPRESSED_MATRIX.get(master).setWightOfNode(edge.getNode(), newEdgeWeight);
                    this.COMPRESSED_MATRIX.get(edge.getNode()).setWightOfNode(master, newEdgeWeight);
                } else {
                    this.COMPRESSED_MATRIX.get(master).addNodeAndWeight(edge.getNode(), newEdgeWeight);
                    this.COMPRESSED_MATRIX.get(edge.getNode()).addNodeAndWeight(master, newEdgeWeight);
                }
                
                //remove edge from slave
//                this.COMPRESSED_MATRIX.get(slave).removeNode(edge.getNode());
//                this.COMPRESSED_MATRIX.get(edge.getNode()).removeNode(slave);
            }

            //add an edge with new weight between master and itself
            double numOfEdges = 1; //edge between slave and master
            double totalWeight = this.COMPRESSED_MATRIX.get(slave).weightOf(master); //weight between slave and master
            for(int mNodes: this.NODES[master].getMergeNodes()) { //edges between slave and master merges
                totalWeight = totalWeight + this.COMPRESSED_MATRIX.get(slave).weightOf(master);
                totalWeight = totalWeight + this.COMPRESSED_MATRIX.get(master).weightOf(master);
                numOfEdges += 2;
                for(int sNodes: this.NODES[slave].getMergeNodes()) {
                    totalWeight = totalWeight + this.COMPRESSED_MATRIX.get(master).weightOf(slave);
                    numOfEdges++;
                }
            }
            for(int sNodes: this.NODES[slave].getMergeNodes()) { //edges between master and slave merges
                totalWeight = totalWeight + this.COMPRESSED_MATRIX.get(master).weightOf(slave);
                totalWeight = totalWeight + this.COMPRESSED_MATRIX.get(slave).weightOf(slave);
                numOfEdges += 2;
            }
            double newEdgeWeight = totalWeight/numOfEdges;

            if(this.COMPRESSED_MATRIX.get(master).containsNode(master)) {
                this.COMPRESSED_MATRIX.get(master).setWightOfNode(master, newEdgeWeight);
            } else {
                this.COMPRESSED_MATRIX.get(master).addNodeAndWeight(master, newEdgeWeight);
            }
            
            //remove edge between master and slave
            this.COMPRESSED_MATRIX.get(slave).removeNode(master);
            this.COMPRESSED_MATRIX.get(master).removeNode(slave);
            
            //remove edge between slave and itself (needed if slave was previously a master)
            this.COMPRESSED_MATRIX.get(slave).removeNode(slave);

            //add the slave as a merged node of master
            this.NODES[master].absorb(slave);
            for(int n: this.NODES[slave].getMergeNodes()) {
                this.NODES[master].absorb(n);
            }

            // updates size
            this.SIZE--;
	} //compressedMerge
        
        public ArrayList<Neighbors> getCompressedGraph() {
            return this.COMPRESSED_MATRIX;
        } //getCompressedGraph

	public int printFakeLinks() {
//		int total = 0;
//		for (Node node : this.NODES) {
//			total += node.getFakeEdges().size();
//			System.out.println(node.ID + ": " + node.getFakeEdges());
//		}
//		return total / 2;
            System.err.println("method 'printFakeLinks' disabled.");
		return -1;
	}

	public int fakeLinks(int from, int to) {
		System.err.println("method 'fakeLinks' disabled.");
		return -1;
		/*
		int aFrom = this.NODES[from].getId();
		int aTo = this.NODES[to].getId();
		if (aFrom == aTo) {
			return -1;
		}

		// links from previous merges
		int linksFrom = this.NODES[aFrom].getLinks();
		int linksTo = this.NODES[aTo].getLinks();
		int mergedCount = 0;
		List<Integer> mergedFrom = this.NODES[aFrom].getMergeNodes();
		for (int node : mergedFrom) {
			if (!this.ORIGINAL_MATRIX.get(aTo).contains(node)) {
				mergedCount++;
			}
		}
		List<Integer> mergedTo = this.NODES[aTo].getMergeNodes();
		for (int node : mergedTo) {
			if (!this.ORIGINAL_MATRIX.get(aFrom).contains(node)) {
				mergedCount++;
			}
		}
		int inheritedLinks = linksFrom + linksTo + mergedCount;

		Set<Integer> fakes = new HashSet<Integer>(this.MATRIX.get(aFrom));
		fakes.remove(aFrom);
		Set<Integer> fakeCompares = new HashSet<Integer>(this.MATRIX.get(aTo));
		fakeCompares.remove(aTo);
		for (Integer t : fakeCompares) {
			if (fakes.contains(t)) {
				fakes.remove(t);
			} else {
				fakes.add(t);
			}
		}
		// return size+1 if they weren't initially connected
		int rawLinks = fakes.size() + 1;
		// return size-2 they're initially connected
		if (this.MATRIX.get(aTo).contains(aFrom) || this.MATRIX.get(aFrom).contains(aTo)) {
			if (fakes.size() < 2) {
				System.out.println("CRAP");
			}
			rawLinks = fakes.size() - 2;
		}
		return rawLinks + inheritedLinks;
		 */
	}

	public LinkedGraph deepCopy() {
		// create default object
		LinkedGraph other = LinkedGraph.load(this.ORIGINAL_MATRIX);
		// update size with current size
		other.SIZE = this.SIZE;
		// update reference of each node
		for (int i = 0; i < this.MAX_SIZE; i++) {
			int index = this.NODES[i].getId();
			other.NODES[i].setReference(other.NODES[index]);
		}
		return other;
	}

	public void print() {
		for (int i = 0; i < this.MAX_SIZE; i++) {
			// only print out nodes that haven't been merged into an other node
			if (this.NODES[i].getId() == i) {
				ArrayList<String> neighbors = new ArrayList<String>();
				// adding to arraylist to make use of String.join
				for (NodeEdge iNeighbor : this.DECOMPRESSED_MATRIX.get(i).getNeighbors()) {
					neighbors.add(String.valueOf(iNeighbor));
				}
				String vertice = "{(" + i;
				if (this.SHOW_MEMORY) {
					vertice += "," + Integer.toHexString(this.NODES[i].hashCode());
				}
				if (this.NODES[i].getMergeNodes().size() > 0) {
					vertice += ": " + this.NODES[i].getMergeNodes();
				}
				vertice += ") -> " + String.join(",", neighbors) + "}";
				System.out.println(vertice);
			}
		}
	}

//	public String toString() {
//		// addings each vertice to arraylist to make use of String.join
//		ArrayList<String> returnValue = new ArrayList<String>();
//		for (int i = 0; i < this.MAX_SIZE; i++) {
//			// only print out nodes that haven't been merged into an other node
//			if (this.NODES[i].getId() == i) {
//				ArrayList<String> neighbors = new ArrayList<String>();
//				// adding to arraylist to make use of String.join
//				for (Integer iNeighbor : this.MATRIX.get(i)) {
//					neighbors.add(String.valueOf(iNeighbor));
//				}
//				ArrayList<Integer> cluster = new ArrayList<>(this.NODES[i].getMergeNodes());
//				cluster.add(i);
//				Collections.sort(cluster);
//				String vertice = "{" + cluster;
//				if (this.SHOW_MEMORY) {
//					vertice += ":" + Integer.toHexString(this.NODES[i].hashCode());
//				}
//				vertice += " -> " + String.join(",", neighbors) + "}";
//				returnValue.add(vertice);
//			}
//		}
//		return "{" + String.join(",", returnValue) + "}";
//	}

//	public String toGraphViz() {
//		// need particular format for this
//		return null;
//	}

	public List<Integer> bfs(int root, int depth) {
		int rootValue = this.NODES[root].getId();
		//System.out.println("Root: "+rootValue);
		Set<Integer> explored = new HashSet<>();
		Queue<WrappedNode> toExplore = new LinkedList<>();
		// initialize root as what needs to be explored
		explored.add(rootValue);
		toExplore.add(new WrappedNode(rootValue, 0));
		// explore while there are items to explore
		while (!toExplore.isEmpty()) {
			WrappedNode at = toExplore.remove();
			int atIndex = this.NODES[at.index].getId();
			double atDistance = at.distance;
			if (atDistance < depth) {
				List<NodeEdge> neighbors = this.DECOMPRESSED_MATRIX.get(atIndex).getNeighbors();
				//System.out.println("Found "+neighbors.size()+" neighbors for "+atIndex+" current distance "+atDistance);

				// explore neighboring nodes
				for (NodeEdge index : neighbors) {
					int iValue = this.NODES[index.getNode()].getId();
					//if not previously visited, queue up the item
					if (!explored.contains(iValue)) {
						//System.out.println("Checking "+atIndex+"'s neighor "+iValue+" "+(atDistance+1)+" away");
						toExplore.add(new WrappedNode(iValue, atDistance + 1));
						explored.add(iValue);
					}
				}
			}
			//System.out.println("Done checking "+atIndex);
		}
		List<Integer> returnValue = new ArrayList<>(explored);
		returnValue.remove(returnValue.indexOf(rootValue));
		//System.out.println("retvalu"+returnValue);
		return returnValue;
	}

	public double distance(int from, int to) {
		int aFrom = this.NODES[from].getId();
		int aTo = this.NODES[to].getId();

		if (aFrom == aTo) {
			return 0;
		}

		double shortest = Double.MAX_VALUE;
		// keeping a track of previously visited to avoid infinite loops
		ArrayList<Integer> explored = new ArrayList<Integer>();
		PriorityQueue<WrappedNode> toExplore = new PriorityQueue<WrappedNode>();
		// initialize from Node to be the first node to check
		toExplore.add(new WrappedNode(aFrom, 0));
		while (!toExplore.isEmpty()) {
			WrappedNode current = toExplore.remove();
			int currentIndex = this.NODES[current.index].getId();
			double currentDistance = current.distance;
			// if we're where we want to be, return the distance
			List<NodeEdge> neigh = this.DECOMPRESSED_MATRIX.get(currentIndex).getNeighbors();
			//System.out.println("Found "+neigh.size()+" neighbors for "+currentIndex+" current distance "+currentDistance+".");
			// explore neighboring nodes
			for (NodeEdge index : neigh) {
				int iValue = this.NODES[index.getNode()].getId();
				if (iValue == aTo && (current.distance + 1) < shortest) {
					shortest = current.distance + 1;
				}
				// if not previously visited, queue up the item
				if (!explored.contains(iValue)) {
					explored.add(iValue);
					toExplore.add(new WrappedNode(iValue, current.distance + 1));
				}
			}

		}
		// if unable to be found, return -1
		return shortest == Double.MAX_VALUE ? -1 : shortest;
	}
}
