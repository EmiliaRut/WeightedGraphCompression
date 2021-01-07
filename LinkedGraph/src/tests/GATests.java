/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import ga.GAImplementation;
import java.util.ArrayList;

/**
 *
 * @author aromualdo
 */
public class GATests extends Thread {

	GAImplementation[] GAs;

	public GATests(GAImplementation[] gas) {
		this.GAs = gas;
	}

	public void run() {
		System.out.println("Thread " + Thread.currentThread().getId() + " BEGIN");
		for (int ga = 0; ga < this.GAs.length; ga++) {
			System.out.println("Thread " + Thread.currentThread().getId() + " GA " + (ga + 1) + " BEGIN");
			this.GAs[ga].run();
			System.out.println("Thread " + Thread.currentThread().getId() + " GA " + (ga + 1) + " COMPLETE");
		}
	}

	public static void main(String... args) {
		/*GAImplementation test = new GAImplementation(System.nanoTime(),"figeys16.dat");
		test.run();*/
	}
}
