package ga;

import display.GraphDisplay;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.LinkedList;
import java.util.Random;

import graph.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import linkedgraph.*;


/*
Parameters can be separated by space, tab, colon, and/or equals.

Example params file:

outPrefix angelo_is_cool
population = 106
generations : 10000
tournament : 1
crossover   0.23
mutation 1.0
chromesome 10
runs 10
source ecoli.txt

 */
/**
 *
 * @author ar14rk
 */
public class GAImplementation {

	private long SEED;
    private String PREFIX;
	private String OUTPUT_FILENAME;
	private String SOURCE_FILENAME;
	private int GENERATION_SPAN;
	private int POPULATION_SIZE;
	private float COMPRESSION_RATE;
	private int TOURNAMENT_SIZE;
	private float CROSSOVER_RATE;
	private float MUTATION_RATE;
	private float ELITISM_RATE;
	private int ELITE_COUNT;
	private int CHROMESOME_SIZE;
	private int DISTANCE_LIMIT;
	private int GRAPH_SIZE;
	private int RUN_SPAN;
    private String FITNESS_METHOD;
    private String COMPRESSION_CALC;
	private Random RANDOM;
	private boolean VALID;
	private BufferedWriter OUTPUT;
    private BufferedWriter COMPRESSED_GRAPH_OUTPUT;

	private final String DEFAULT_OUTPUT = "";
	private final float DEFAULT_RATE = -Float.MAX_VALUE;
	private final int DEFAULT_SIZE = Integer.MIN_VALUE;
    private final String DEFAULT_FITNESS = "AbsoluteTotalWeightDiff";
    private final String DEFAULT_COMPRESSION_CALC = "run";

	private Graph ORIGINAL_GRAPH;
	private int[][][] POPULATION;
	private double[] POPULATION_FITNESS;

	private static final String IN_DIRECTORY = "data/in/";
	private static final String OUT_DIRECTORY = "data/out/";

	private List<List<Integer>> ORIGINAL_NEIGHBORHOODS;
	private Map<String, Double> CACHED_CHROMESOME_FITNESS;

	/**
	 * builds GA based on fileLocation file
	 *
	 * @param timeSuffix
	 * @param fileLocation
	 */
	public GAImplementation(long timeSuffix, String fileLocation) {
		this.SEED = timeSuffix;
		this.RANDOM = new Random(this.SEED);
		if (!buildData(IN_DIRECTORY + fileLocation)) {
			return;
		}
		// compression suffix
		this.OUTPUT_FILENAME += "_cmp" + (int) (this.COMPRESSION_RATE * 100);
		// distance suffix
		this.OUTPUT_FILENAME += "_dst" + this.DISTANCE_LIMIT;
		// mutation suffix
		this.OUTPUT_FILENAME += "_mut" + (int) (this.MUTATION_RATE * 100);
		// crossover suffix
		this.OUTPUT_FILENAME += "_xvr" + (int) (this.CROSSOVER_RATE * 100);
		// run span
		this.OUTPUT_FILENAME += "_run" + this.RUN_SPAN;
		// generation span
		this.OUTPUT_FILENAME += "_gen" + this.GENERATION_SPAN;
		// seed suffix
		this.OUTPUT_FILENAME += "_" + this.FITNESS_METHOD + ".csv";
	}

	/**
	 * Runs the Genetic Algorithm
	 */
	public void run() {
		if (this.VALID) {
			try {
				FileWriter fw = new FileWriter(OUT_DIRECTORY + this.OUTPUT_FILENAME);
				this.OUTPUT = new BufferedWriter(fw);
			} catch (Exception e) {
				System.out.println("Error creating write file: " + e.getMessage());
			}
			try {
				// CSV Columns
				this.OUTPUT.write("Source,"
						+ "Seed,"
						+ "Graph Size,"
						+ "Population Size,"
						+ "Compression Rate,"
						+ "Chromesome Size,"
						+ "Elitism Rate,"
						+ "Elite Size,"
						+ "Tournament Size,"
						+ "Mutation Rate,"
						+ "Crossover Rate,"
						+ "Maximum Distance,"
						+ "Run Span,"
						+ "Run,"
						+ "Generation Span,"
						+ "Generation,"
						+ "Time to Complete,"
                                                + "Fitness Method, "
						+ "Global Best Fitness,"
						+ "Global Average Fitness,"
						+ "Global Worst Fitness,"
						+ "Run Best Fitness,"
						+ "Run Average Fitness,"
						+ "Run Worst Fitness,"
						+ "Generation Best Fitness,"
						+ "Generation Average Fitness,"
						+ "Generation Worst Fitness,"
						+ "Global Best Chromesome,"
						+ "Run Best Chromesome,"
						+ "Generation Best Chromesome,"
                                                + "Generation Worst Chromosome"
				);
				this.OUTPUT.newLine();
				this.OUTPUT.flush();
			} catch (Exception e) {
				System.out.println("Unable to write to file: " + e.getMessage());
			}
			this.ORIGINAL_NEIGHBORHOODS = new LinkedList<>();
			this.CACHED_CHROMESOME_FITNESS = new HashMap<>();

			for (int i = 0; i < this.GRAPH_SIZE; i++) {
				this.ORIGINAL_NEIGHBORHOODS.add(new LinkedList<Integer>());
			}
			// initialize global settings
			double globalWorstFitness = Double.MIN_VALUE;
			double globalBestFitness = Double.MAX_VALUE;
			int[][] globalBest = new int[CHROMESOME_SIZE][2];
			int[][] globalWorst = new int[CHROMESOME_SIZE][2];
			long globalSum = 0;
			this.POPULATION_FITNESS = new double[this.POPULATION_SIZE];
			// Each Run
			for (int run = 1; run <= this.RUN_SPAN; run++) {
				initPopulation();

				// Grab initial population fitness
				for (int i = 0; i < this.POPULATION_SIZE; i++) {
//                                        System.out.println("Evaluating chromosome " + i);
					this.POPULATION_FITNESS[i] = this.Evaluate(this.POPULATION[i]);
//                                        System.out.println("Done evaluating chromosome " + i);
				}

				double runWorstFitness = Double.MIN_VALUE;
				double runBestFitness = Double.MAX_VALUE;
				int[][] runBest = new int[CHROMESOME_SIZE][2];
				int[][] runWorst = new int[CHROMESOME_SIZE][2];
				long runSum = 0;

				// Each Generation
				for (int gen = 1; gen <= this.GENERATION_SPAN; gen++) {
					long startTime = System.currentTimeMillis();
					double genBestFitness = Double.MAX_VALUE;
					int[][] genBest = new int[CHROMESOME_SIZE][2];
					double genWorstFitness = Double.MIN_VALUE;
					int[][] genWorst = new int[CHROMESOME_SIZE][2];
					long genSum = 0;
					System.out.println("Thread " + Thread.currentThread().getId() + " Run " + run + " Generation " + gen);

					// Elitism
					int[][][] generation = this.getElitePopulation();
					for (int c = this.ELITE_COUNT; c < this.POPULATION_SIZE; c += 2) {
						// Get Parents via tournament selection
						int[][] parent1 = this.TournamentSelection();
						int[][] parent2 = this.TournamentSelection();
						// Apply Crossover
						if (this.RANDOM.nextDouble() < this.CROSSOVER_RATE) {
							this.Crossover(parent1, parent2);
						}
						// Apply Mutation
						if (this.RANDOM.nextDouble() < this.MUTATION_RATE) {
							this.Mutate(parent1);
						}
						if (this.RANDOM.nextDouble() < this.MUTATION_RATE) {
							this.Mutate(parent2);
						}
						// Add to generation
						if (c + 1 == this.POPULATION_SIZE) {
							int[][] randomParent = this.RANDOM.nextBoolean() ? parent1 : parent2;
							generation[c] = randomParent;
						} else {
							generation[c] = parent1;
							generation[c + 1] = parent2;
						}
					}
					// Collect fitnesses
					for (int i = 0; i < this.POPULATION_SIZE; i++) {
//                                                System.out.println("Evaluating chromosome " + i);
						double fitness = this.Evaluate(generation[i]);
						this.POPULATION_FITNESS[i] = fitness;
//                                                System.out.println("Done evaluating chromosome " + i);
						// Collect Generation, Run, Global statistics
						if (fitness < genBestFitness) {
							genBest = Copy(generation[i]);
							genBestFitness = fitness;
							if (genBestFitness < runBestFitness) {
								runBest = Copy(genBest);
								runBestFitness = genBestFitness;
								if (runBestFitness < globalBestFitness) {
									globalBest = Copy(runBest);
									globalBestFitness = runBestFitness;
								}
							}
						}
						if (fitness > genWorstFitness) {
							genWorst = Copy(generation[i]);
							genWorstFitness = fitness;
							if (genWorstFitness > runWorstFitness) {
								runWorst = Copy(genWorst);
								runWorstFitness = genWorstFitness;
								if (runWorstFitness > globalWorstFitness) {
									globalWorst = Copy(runWorst);
									globalWorstFitness = runWorstFitness;
								}
							}
						}
						globalSum += fitness;
						genSum += fitness;
						runSum += fitness;

						this.POPULATION[i] = generation[i];
					}
                                        
                                        //calculate global average, run average and generation average
                                        double globalAverageFitness = (globalSum / (this.POPULATION_SIZE * ((this.GENERATION_SPAN * (run - 1)) + gen)));
                                        double runAverageFitness = (runSum / (this.POPULATION_SIZE * gen));
                                        double genAverageFitness = (genSum / this.POPULATION_SIZE);
                                        
                                        double printedGlobalBestFitness = globalBestFitness;
                                        double printedGlobalAverageFitness = globalAverageFitness; //inaccurate because of the sqrt
                                        double printedGlobalWorstFitness = globalWorstFitness;
                                        double printedRunBestFitness = runBestFitness;
                                        double printedRunAverageFitness = runAverageFitness; //inaccurate because of the sqrt
                                        double printedRunWorstFitness = runWorstFitness;
                                        double printedGenBestFitness = genBestFitness;
                                        double printedGenAverageFitness = genAverageFitness; //inaccurate because of the sqrt
                                        double printedGenWorstFitness = genWorstFitness;
                                        
                                        //add the squareroot at the end if SumSqrWeightDiff is selected as the fitness method
                                        if(this.FITNESS_METHOD.equalsIgnoreCase("SumSqrWeightDiff")) {
                                            printedGlobalBestFitness = Math.sqrt(globalBestFitness);
                                            printedGlobalAverageFitness = Math.sqrt(globalAverageFitness); //inaccurate because of the sqrt
                                            printedGlobalWorstFitness = Math.sqrt(globalWorstFitness);
                                            printedRunBestFitness = Math.sqrt(runBestFitness);
                                            printedRunAverageFitness = Math.sqrt(runAverageFitness); //inaccurate because of the sqrt
                                            printedRunWorstFitness = Math.sqrt(runWorstFitness);
                                            printedGenBestFitness = Math.sqrt(genBestFitness);
                                            printedGenAverageFitness = Math.sqrt(genAverageFitness); //inaccurate because of the sqrt
                                            printedGenWorstFitness = Math.sqrt(genWorstFitness);
                                        }
					// Output the results
					try {
						this.OUTPUT.write(this.SOURCE_FILENAME + ","
								+ this.SEED + ","
								+ this.GRAPH_SIZE + ","
								+ this.POPULATION_SIZE + ","
								+ String.format("%.5f%%", this.CHROMESOME_SIZE / Double.valueOf(this.GRAPH_SIZE)) + ","
								+ this.CHROMESOME_SIZE + ","
								+ String.format("%.5f%%", this.ELITE_COUNT / Double.valueOf(this.GRAPH_SIZE)) + ","
								+ this.ELITE_COUNT + ","
								+ this.TOURNAMENT_SIZE + ","
								+ String.format("%.5f%%", this.MUTATION_RATE) + ","
								+ String.format("%.5f%%", this.CROSSOVER_RATE) + ","
								+ this.DISTANCE_LIMIT + ","
								+ this.RUN_SPAN + ","
								+ run + ","
								+ this.GENERATION_SPAN + ","
								+ gen + ","
								+ (System.currentTimeMillis() - startTime) + ","
                                                                + this.FITNESS_METHOD + ","
								+ printedGlobalBestFitness + ","
								+ printedGlobalAverageFitness + ","
								+ printedGlobalWorstFitness + ","
								+ printedRunBestFitness + ","
								+ printedRunAverageFitness + ","
								+ printedRunWorstFitness + ","
								+ printedGenBestFitness + ","
								+ printedGenAverageFitness + ","
								+ printedGenWorstFitness + ","
								+ "\"" + GAImplementation.buildChromesomeString(globalBest) + "\","
								+ "\"" + GAImplementation.buildChromesomeString(runBest) + "\","
								+ "\"" + GAImplementation.buildChromesomeString(genBest) + "\","
								+ "\"" + GAImplementation.buildChromesomeString(genWorst) + "\","
						);
						this.OUTPUT.newLine();
						this.OUTPUT.flush();
					} catch (Exception e) {
						System.out.println("Unable to write to file: " + e.getMessage());
					}
//					System.out.println("Global Sum: " + globalSum);
//					System.out.println("Global total: " + (this.POPULATION_SIZE * ((this.GENERATION_SPAN * (run - 1)) + gen)));
//					System.out.println("Run Sum: " + runSum);
//					System.out.println("Run total: " + (this.POPULATION_SIZE * gen));
//					System.out.println("Generation Sum: " + genSum);
//					System.out.println("Generation total: " + this.POPULATION_SIZE);
                                        
                                        //print best chrom from the run
                                        if(this.COMPRESSION_CALC.equalsIgnoreCase("gen")) {
                                            String genBestFileName = this.PREFIX;
                                            genBestFileName += "_dst" + this.DISTANCE_LIMIT; // distance suffix
                                            genBestFileName += "_cmp" + (int) (this.COMPRESSION_RATE * 100); // compression suffix
                                            genBestFileName += "_mut" + (int) (this.MUTATION_RATE * 100); // mutation suffix
                                            genBestFileName += "_xvr" + (int) (this.CROSSOVER_RATE * 100); // crossover suffix
                                            genBestFileName += "_" + this.FITNESS_METHOD ;
                                            genBestFileName += "_Run"+run+"_"+"gen"+gen;
                                            printCompressedGraph(genBest, genBestFileName);
                                        }
				}
                                
                                //print best chrom from the run
                                if(this.COMPRESSION_CALC.equalsIgnoreCase("run")) {
                                    String runBestFileName = this.PREFIX;
                                    runBestFileName += "_dst" + this.DISTANCE_LIMIT; // distance suffix
                                    runBestFileName += "_cmp" + (int) (this.COMPRESSION_RATE * 100); // compression suffix
                                    runBestFileName += "_mut" + (int) (this.MUTATION_RATE * 100); // mutation suffix
                                    runBestFileName += "_xvr" + (int) (this.CROSSOVER_RATE * 100); // crossover suffix
                                    runBestFileName += "_" + this.FITNESS_METHOD ;
                                    runBestFileName += "_Run"+run;
                                    printCompressedGraph(runBest, runBestFileName);
                                }
			}

			try {
				this.OUTPUT.close();
			} catch (Exception e) {
				System.out.println("Unable to close file: " + e.getMessage());
			}
		}

	}

	/**
	 * returns chromesome at specified index
	 *
	 * @param index
	 * @return
	 */
	public int[][] getChromesome(int index) {
		if (index >= this.POPULATION_SIZE) {
			return null;
		}
		return Copy(this.POPULATION[index]);
	}

	/**
	 * prints all the chromesomes
	 */
	public void print() {
		for (int i = 0; i < this.POPULATION_SIZE; i++) {
			System.out.print("Chromesome " + i + " ");
			println(this.POPULATION[i]);
		}
	}

	/**
	 * prints specific chromesomes
	 *
	 * @param chromesome
	 */
	public static void print(int[][] chromesome) {
		System.out.print(GAImplementation.buildChromesomeString(chromesome));
	}

	/**
	 * prints specific chromesomes with a newline at the end
	 *
	 * @param chromesome
	 */
	public static void println(int[][] chromesome) {
		print(chromesome);
		System.out.println();
	}

	/*
		initializes the population with random data
	 */
	private void initPopulation() {
		if (!VALID) {
			return;
		}
		this.POPULATION = new int[this.POPULATION_SIZE][this.CHROMESOME_SIZE][2];
		for (int c = 0; c < this.POPULATION_SIZE; c++) {
			for (int g = 0; g < this.CHROMESOME_SIZE; g++) {
				this.MutateGene(this.POPULATION[c][g]);
			}
		}
	}

	/**
	 * deep copy chromesome
	 *
	 * @param pairs
	 * @return
	 */
	public static int[][] Copy(int[][] pairs) {
		int[][] returnPairs = new int[pairs.length][2];
		for (int i = 0; i < pairs.length; i++) {
			returnPairs[i][0] = pairs[i][0]; // root
			returnPairs[i][1] = pairs[i][1]; // offset
		}
		return returnPairs;
	}

	/**
	 * Gets a preset number of individual chromesomes, return the best
	 * chromesome
	 *
	 * @return chromesome
	 */
	public int[][] TournamentSelection() {
		double best = Double.MAX_VALUE;
		int[][] winner = new int[this.CHROMESOME_SIZE][2];
		for (int i = 0; i < this.TOURNAMENT_SIZE; i++) {
			int randomIndex = this.RANDOM.nextInt(this.POPULATION_SIZE);
			int[][] participant = this.getChromesome(randomIndex);
			double fitness = this.EvaluatePrevious(randomIndex);
			if (fitness < best) {
				best = fitness;
				winner = participant;
			}
		}
		return winner;
		//this.RANDOM.nextInt(TOURNAMENT_SIZE)
	}

	/**
	 * cross over the two given chromesomes
	 *
	 * @param pair1
	 * @param pair2
	 */
	public void Crossover(int[][] pair1, int[][] pair2) {
		if (!VALID) {
			return;
		}
		int start = this.RANDOM.nextInt(CHROMESOME_SIZE);
		int end = this.RANDOM.nextInt(CHROMESOME_SIZE - start) + start;
		for (int i = 0; i < pair1.length; i++) {
			if (i >= start && i <= end) {
				int tempRoot = pair1[i][0];
				pair1[i][0] = pair2[i][0];
				pair2[i][0] = tempRoot;
				int tempOffset = pair1[i][1];
				pair1[i][1] = pair2[i][1];
				pair2[i][1] = tempOffset;
			}
		}
	}

	/*
		This method can only be done from the original graph, not constantly changing ones such as during eval
	 */
	private void MutateGene(int[] gene) {
		int randomRoot = this.RANDOM.nextInt(this.GRAPH_SIZE);
		gene[0] = randomRoot;
                int randomNeighbor;
                if(this.DISTANCE_LIMIT == -1) {
                    randomNeighbor = this.RANDOM.nextInt(this.GRAPH_SIZE);
                } else {
                    if (this.ORIGINAL_NEIGHBORHOODS.get(randomRoot).size() < 1) {
                            this.ORIGINAL_NEIGHBORHOODS.get(randomRoot).addAll(this.ORIGINAL_GRAPH.bfs(randomRoot, this.DISTANCE_LIMIT));
                    }
                    randomNeighbor = this.ORIGINAL_NEIGHBORHOODS.get(randomRoot).get(this.RANDOM.nextInt(this.ORIGINAL_NEIGHBORHOODS.get(randomRoot).size()));
                }
                int randomOffset = Math.floorMod(randomNeighbor - randomRoot, this.GRAPH_SIZE);
		gene[1] = randomOffset;
	}

	/**
	 * mutate the given chromesome
	 *
	 * @param pairs
	 */
	public void Mutate(int[][] pairs) {
		if (!VALID) {
			return;
		}
		int randomIndex = this.RANDOM.nextInt(pairs.length);
		this.MutateGene(pairs[randomIndex]);
	}

	private boolean duplicateGene(int[][] chromesome, int[] gene) {
		boolean found = false;
		for (int i = 0; i < chromesome.length; i++) {
			if (chromesome[i][0] == gene[0] && chromesome[i][1] == gene[1]) {
				if(found){
					return true;
				} else{
					found = true;
				}
			}
		}
		return false;
	}

	/**
	 * Evaluates a single gene within a chromesome
	 *
	 * @param graph
	 * @param gene
	 * @return geneFitness
	 */
	public void EvaluateGene(LinkedGraph graph, int[][] chromesome, int[] gene) {
		int from = gene[0];
		int to = (gene[0] + gene[1]) % this.GRAPH_SIZE;
		int[] tempGene = new int[]{gene[0], gene[1]};
                
                if(this.DISTANCE_LIMIT == -1) {
                    while(duplicateGene(chromesome, tempGene) || graph.sameCluster(from, to)) {
                        to = this.RANDOM.nextInt(this.GRAPH_SIZE);
                        tempGene[0] = from;
                        tempGene[1] = Math.floorMod(to - from, this.GRAPH_SIZE);
                    }
                } else {
                    if (duplicateGene(chromesome, tempGene) || graph.sameCluster(from, to)) {
                            List<Integer> possibleNeighbors = graph.bfs(from, this.DISTANCE_LIMIT);
                            for (Integer neighbor : possibleNeighbors) {
                                    to = neighbor;
                                    tempGene[0] = from;
                                    tempGene[1] = Math.floorMod(to - from, this.GRAPH_SIZE);
                                    if (!graph.sameCluster(from, to) && !duplicateGene(chromesome, tempGene)) {
                                            break;
                                    }
                            }

                            while (duplicateGene(chromesome, tempGene) || graph.sameCluster(from, to)) {
                                    from = this.RANDOM.nextInt(this.GRAPH_SIZE);
                                    possibleNeighbors = graph.bfs(from, this.DISTANCE_LIMIT);
                                    if (possibleNeighbors.size() < 1) {
                                            System.out.println("No neighbors");
                                            continue;
                                    }
                                    to = possibleNeighbors.get(this.RANDOM.nextInt(possibleNeighbors.size()));

                                    tempGene[0] = from;
                                    tempGene[1] = Math.floorMod(to - from, this.GRAPH_SIZE);
                            }
                    }
                }
		gene[0] = from;
		gene[1] = Math.floorMod(to - from, this.GRAPH_SIZE);
//                System.out.println("from: " + from + "\tto: " + to);
//                System.out.println("gene[0]: " + gene[0] + "\tgene[1]: " + gene[1]);
                graph.merge(from, to);
	}

	/**
	 * fitness function. updates chromesome if chromesome has some invalid
	 * genes.
	 *
	 * @param chromesome
	 * @return
	 */
	public double Evaluate(int[][] chromesome) {
		String chromesomeString = buildChromesomeString(chromesome);
		if (this.CACHED_CHROMESOME_FITNESS.containsKey(chromesomeString)) {
			return this.CACHED_CHROMESOME_FITNESS.get(chromesomeString);
		}

		LinkedGraph current = (LinkedGraph) this.ORIGINAL_GRAPH.deepCopy();
		// iterate through each gene
		for (int i = 0; i < chromesome.length; i++) {
			this.EvaluateGene((LinkedGraph) current, chromesome, chromesome[i]);
		}

		double fitness = current.getFitness(this.FITNESS_METHOD);
		String currentChromesomeString = buildChromesomeString(chromesome);
		this.CACHED_CHROMESOME_FITNESS.put(currentChromesomeString, fitness);

		return fitness;
	}

	/**
	 * returns the fitness from the previous generation
	 *
	 * @param chromesome
	 * @return
	 */
	public double EvaluatePrevious(int chromesome) {
		// wrapper function primarily for sanity
		return this.POPULATION_FITNESS[chromesome];
	}

	/**
	 * returns a preset number of elites from the previous generation
	 *
	 * @return newPopulation
	 */
	public int[][][] getElitePopulation() {
		int[][][] newPop = new int[this.POPULATION_SIZE][this.CHROMESOME_SIZE][2];
		PriorityQueue<WrappedNode> fitness = new PriorityQueue<WrappedNode>();
		for (int i = 0; i < this.POPULATION_SIZE; i++) {
			fitness.add(new WrappedNode(i, EvaluatePrevious(i)));
		}
		for (int e = 0; e < this.ELITE_COUNT; e++) {
			//System.out.println(fitness.size());
			WrappedNode elite = fitness.remove();
			newPop[e] = this.getChromesome(elite.index);
		}
		return newPop;
	}

	// DEFAULTS AND DATA VALIDATION
	/*
	Apply default values to the parameters
	 */
	private void buildDefaults() {
		this.OUTPUT_FILENAME = DEFAULT_OUTPUT;
		this.SOURCE_FILENAME = DEFAULT_OUTPUT;
                this.PREFIX = DEFAULT_OUTPUT;
		this.GENERATION_SPAN = DEFAULT_SIZE;
		this.POPULATION_SIZE = DEFAULT_SIZE;
		this.TOURNAMENT_SIZE = DEFAULT_SIZE;
		this.CHROMESOME_SIZE = DEFAULT_SIZE;
		this.DISTANCE_LIMIT = DEFAULT_SIZE;
		this.ELITE_COUNT = DEFAULT_SIZE;
		this.GRAPH_SIZE = DEFAULT_SIZE;
		this.COMPRESSION_RATE = DEFAULT_RATE;
		this.CROSSOVER_RATE = DEFAULT_RATE;
		this.MUTATION_RATE = DEFAULT_RATE;
		this.ELITISM_RATE = DEFAULT_RATE;
		this.RUN_SPAN = 1;
                this.FITNESS_METHOD = DEFAULT_FITNESS;
                this.COMPRESSION_CALC = DEFAULT_COMPRESSION_CALC;
	}

	// validates if properly built
	private boolean isProperlyBuilt() {
		this.VALID = true;
		if (this.OUTPUT_FILENAME.equals(DEFAULT_OUTPUT)) {
			System.out.println("Output filename not specified"
					+ ", use parameter: outPrefix");
			this.VALID = false;
		}
		if (this.SOURCE_FILENAME.equals(DEFAULT_OUTPUT)) {
			System.out.println("Source filename not specified"
					+ ", use parameter: source");
			this.VALID = false;
		}
		if (this.COMPRESSION_RATE < 1.0 && this.COMPRESSION_RATE > 0.0) {
			this.CHROMESOME_SIZE = (int) (this.COMPRESSION_RATE * this.GRAPH_SIZE);
		} else {
			if (this.CHROMESOME_SIZE < 1) {
				System.out.println("Compression rate invalid"
						+ ", use parameter: compression [0.0,1.0]");
				this.VALID = false;
			} else {
				System.out.println("Compression rate invalid"
						+ ", defaulting to parameter: chromesome "
						+ this.CHROMESOME_SIZE
				);
			}
		}
		if (this.POPULATION_SIZE < 1) {
			System.out.println("Population size invalid"
					+ ", use parameter: population [1,infinity)");
		}
		if (this.TOURNAMENT_SIZE < 1) {
			System.out.println("Tournament size invalid"
					+ ", use parameter: tournament [1,population_size]");
			this.VALID = false;
		} else if (this.TOURNAMENT_SIZE > this.POPULATION_SIZE) {
			this.TOURNAMENT_SIZE = this.POPULATION_SIZE;
		}

		if (this.GENERATION_SPAN < 1) {
			System.out.println("Generation size invalid"
					+ ", use parameter: generations [1,infinity)");
			this.VALID = false;
		}
		if (this.DISTANCE_LIMIT < 1 ) {
                    if(this.DISTANCE_LIMIT == -1) {
                        System.out.println("Distance limit is -1. Any node can merge with any node.");
                    }
			System.out.println("Distance limit invalid"
					+ ", use parameter: maxDistance [1,infinity)");
			this.VALID = false;
		}
		if (this.TOURNAMENT_SIZE < 1) {
			System.out.println("Tournament size invalid"
					+ ", use parameter: tournament [1,infinity)");
			this.VALID = false;
		}
		if (this.CROSSOVER_RATE > 1.0 || this.CROSSOVER_RATE < 0.0) {
			System.out.println("Crossover rate invalid"
					+ ", use parameter: crossover [0.0,1.0]");
			this.VALID = false;
		}
		if (this.MUTATION_RATE > 1.0 || this.MUTATION_RATE < 0.0) {
			System.out.println("Mutation rate invalid"
					+ ", use parameter: mutation [0.0,1.0]");
			this.VALID = false;
		}
		if (this.ELITISM_RATE < 1.0 && this.ELITISM_RATE > 0.0) {
			System.out.println(this.ELITISM_RATE);
			this.ELITE_COUNT = (int) (this.ELITISM_RATE * this.POPULATION_SIZE);
			if (this.ELITE_COUNT < 1 || this.ELITE_COUNT >= this.POPULATION_SIZE) {
				System.out.println("Invalid number of elites: " + this.ELITE_COUNT);
				this.VALID = false;
			}
		} else {
			if (this.ELITE_COUNT < 1) {
				System.out.println("Elitism rate invalid"
						+ ", use parameter: elitism [0.0,1.0]");
				this.VALID = false;
			} else {
				System.out.println("Elitism rate invalid"
						+ ", defaulting to parameter: elites "
						+ this.ELITE_COUNT
				);
				if (this.ELITE_COUNT < 1 || this.ELITE_COUNT >= this.POPULATION_SIZE) {
					System.out.println("Invalid number of elites: " + this.ELITE_COUNT);
					this.VALID = false;
				}
			}
		}
		if (this.RUN_SPAN < 0) {
			System.out.println("Run size invalid"
					+ ", use parameter: runs [1,infinity]");
			this.VALID = false;
		}
                if (!this.FITNESS_METHOD.equalsIgnoreCase("AbsoluteTotalWeightDiff") && !this.FITNESS_METHOD.equalsIgnoreCase("SumAbsWeightDiff") && !this.FITNESS_METHOD.equalsIgnoreCase("SumSqrWeightDiff")) {
                    System.out.println("Fitness method is invalid"
			+ ", defaulting to method: "
                        + this.DEFAULT_FITNESS
                    );
                }
                if (!this.COMPRESSION_CALC.equalsIgnoreCase("gen") && !this.COMPRESSION_CALC.equalsIgnoreCase("run")) {
                    System.out.println("Compression calc is invalid"
			+ ", defaulting to: "
                        + this.DEFAULT_COMPRESSION_CALC
                    );
                }
		return this.VALID;
	}

	/*
		retrieves data for the GA from the given data file
	 */
	private boolean buildData(String filename) {
		buildDefaults();
		try {
			List<String> lines = Files.readAllLines(Paths.get(filename), Charset.defaultCharset());
			for (String line : lines) {
				String[] data = line.split("[\\s\\t:=]+");
				//System.out.println("["+String.join(",",data)+"]");
				if (data.length != 2) {
					continue;
				}
				switch (data[0].trim()) {
					case "compression":
						this.COMPRESSION_RATE = Float.parseFloat(data[1].trim());
						break;
					case "generations":
						this.GENERATION_SPAN = Integer.parseInt(data[1].trim());
						break;
					case "tournament":
						this.TOURNAMENT_SIZE = Integer.parseInt(data[1].trim());
						break;
					case "crossover":
						this.CROSSOVER_RATE = Float.parseFloat(data[1].trim());
						break;
					case "mutation":
						this.MUTATION_RATE = Float.parseFloat(data[1].trim());
						break;
					case "elitism":
						this.ELITISM_RATE = Float.parseFloat(data[1].trim());
						break;
					case "elites":
						this.ELITE_COUNT = Integer.parseInt(data[1].trim());
						break;
					case "chromesome":
						this.CHROMESOME_SIZE = Integer.parseInt(data[1].trim());
						break;
					case "population":
						this.POPULATION_SIZE = Integer.parseInt(data[1].trim());
						break;
					case "outPrefix":
						this.OUTPUT_FILENAME = data[1].trim();
                                                this.PREFIX = data[1].trim();
						break;
					case "maxDistance":
						this.DISTANCE_LIMIT = Integer.parseInt(data[1].trim());
						break;
					case "runs":
						this.RUN_SPAN = Integer.parseInt(data[1].trim());
						break;
					case "source":
						this.SOURCE_FILENAME = data[1].trim();
						LinkedGraph g = LinkedGraph.load(this.SOURCE_FILENAME);
						this.GRAPH_SIZE = g.getSize();
						this.ORIGINAL_GRAPH = g;
						break;
                                        case "fitness":
                                                this.FITNESS_METHOD = data[1].trim();
                                                break;
                                        case "printCompression":
                                                this.COMPRESSION_CALC = data[1].trim();
                                                break;
					default:
						break;
				}
			}
		} catch (IOException e) {
			System.out.println("Error reading from file: " + e.getMessage());
			isProperlyBuilt();
			buildDefaults();
			return false;
		} catch (NumberFormatException e) {
			System.out.println("Error converting number: " + e.getMessage());
			isProperlyBuilt();
			buildDefaults();
			return false;
		}
		return isProperlyBuilt();
	}
        
        //build and print the compressed graph
        private void printCompressedGraph(int[][] chromesome, String filename) {
            
            //build compressed graph
            LinkedGraph current = (LinkedGraph) this.ORIGINAL_GRAPH.deepCopy();
            current.initializeCompressedGraph();
            // iterate through each gene
            for (int i = 0; i < chromesome.length; i++) {
                int tempFrom = chromesome[i][0];
                int tempTo = Math.floorMod(chromesome[i][1] + chromesome[i][0], this.GRAPH_SIZE);
//                System.out.println("tempFrom: " + tempFrom + "\ttempTo: " + tempTo);
                current.compressedMerge(tempFrom, tempTo);
            }
            
            ArrayList<Neighbors> compressedGraph = current.getCompressedGraph();
            
            //print graph info
            try {
                    FileWriter fw = new FileWriter(OUT_DIRECTORY + filename + ".txt");
                    this.COMPRESSED_GRAPH_OUTPUT = new BufferedWriter(fw);
            } catch (Exception e) {
                    System.out.println("Error creating write file: " + e.getMessage());
            }
            try {
                    this.COMPRESSED_GRAPH_OUTPUT.write(this.GRAPH_SIZE + "\n");
                    for(int n = 0; n < compressedGraph.size(); n++) {
                        for(NodeEdge e: compressedGraph.get(n).getNeighbors()) {
                            this.COMPRESSED_GRAPH_OUTPUT.write(n + "\t" + e.getNode() + "\t" + e.getWeight() + "\n");
                        }
                    }
//                    this.COMPRESSED_GRAPH_OUTPUT.newLine();
                    this.COMPRESSED_GRAPH_OUTPUT.flush();
            } catch (Exception e) {
                    System.out.println("Unable to write to file: " + e.getMessage());
            }
            
        } //buildCompressedGraph

	/**
	 * returns string representation of chromesome
	 *
	 * @param chromesome
	 * @return
	 */
	public static String buildChromesomeString(int[][] chromesome) {
		String output = "[";
		for (int i = 0; i < chromesome.length; i++) {
			if (i > 0) {
				output += ",";
			}
			output += "(";
			for (int j = 0; j < chromesome[i].length; j++) {
				if (j > 0) {
					output += ",";
				}
				output += chromesome[i][j];
			}
			output += ")";
		}
		output += "]";
		return output;
	}

	public static LinkedGraph BuildChromesome(LinkedGraph testGraph, String chromesome) {
		LinkedGraph graph = testGraph.deepCopy();
		chromesome = chromesome.replaceAll("\\]", "");
		chromesome = chromesome.replaceAll("\\[", "");
		String[] chromesomes = chromesome.split("\\),\\(");
//		int sum = 0;

		for (String c : chromesomes) {
			c = c.replaceAll("\\(", "");
			c = c.replaceAll("\\)", "");
			String[] gene = c.split(",");
			int from = Integer.valueOf(gene[0]);
			int to = (Integer.valueOf(gene[1]) + from) % graph.getSize();
			graph.merge(from, to);
		}

		return graph;
	}

	/**
	 * shows step by step fitness evaluation of chromesome
	 *
	 * @param testGraph
	 * @param chromesome
	 */
	public static void ViewChromesome(LinkedGraph testGraph, String chromesome) {
		LinkedGraph graph = BuildChromesome(testGraph, chromesome);
		System.out.println("Should be " + graph.getFitness("fitnessMethod") + " fitness");
		PrintChromesome(graph);

	}
        
        public String getFitnessMethod() {
            return this.FITNESS_METHOD;
        }

	public static void PrintChromesome(LinkedGraph g) {
		g.print();
//		GraphDisplay.displayLinkedGraph(g);
	}
}
