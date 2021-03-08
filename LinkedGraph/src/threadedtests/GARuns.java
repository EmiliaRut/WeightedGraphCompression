/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package threadedtests;

import ga.GAImplementation;
import java.util.ArrayList;

/**
 *
 * @author Angelo Romualdo <angelo.romualdo at brocku.ca>
 */
public class GARuns {

	public static final int MAX_THREADS = 1;
	public static final int FIRST_TEST = 2;
	public static final int LAST_TEST = 2;
	public static final String[] TESTS = new String[]{
//                "June2_?.dat"
//                "Apr28_?.dat"
//                "July7_?.dat"
                "July15_?.dat"
	};
	
	
	public static void main(String... args) {

		long SEED = System.nanoTime();
//		SEED = 107651916186943L;
		GAThread[] threads = new GAThread[MAX_THREADS];
		ArrayList<GAImplementation> threadData = new ArrayList<>();
		for (int i = FIRST_TEST; i <= LAST_TEST; i++) {
			for(int j=0;j<TESTS.length;j++){
				String filename = TESTS[j].replace("?", String.valueOf(i));
				System.out.println(filename);
				threadData.add(new GAImplementation(SEED, filename));
			}
		}
		
		Mediator shared = new Mediator(threadData);
		for (int i = 0; i < MAX_THREADS; i++) {
			(new GAThread(shared)).start();
		}
	}

}
