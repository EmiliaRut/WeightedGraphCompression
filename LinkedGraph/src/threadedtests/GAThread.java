/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package threadedtests;

import ga.GAImplementation;

/**
 *
 * @author Angelo Romualdo <angelo.romualdo at brocku.ca>
 */
public class GAThread extends Thread{

	private Mediator MEDIATOR;
	public GAThread(Mediator m) {
		this.MEDIATOR = m;
	}

	public void run() {
		System.out.println("Thread " + Thread.currentThread().getId() + " BEGIN");
		GAImplementation current = this.MEDIATOR.getGA();
		while(current != null){
			System.out.println("Thread " + Thread.currentThread().getId() + " GA BEGIN");
			current.run();
			System.out.println("Thread " + Thread.currentThread().getId() + " GA COMPLETE");
			current = this.MEDIATOR.getGA();
		}
		System.out.println("Thread " + Thread.currentThread().getId() + " COMPLETE");
	}
	
}
