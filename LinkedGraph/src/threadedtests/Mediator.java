/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package threadedtests;

import ga.GAImplementation;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Angelo Romualdo <angelo.romualdo at brocku.ca>
 */
public class Mediator {

	private List<GAImplementation> GAS;

	public Mediator(GAImplementation[] gas) {
		GAS = new LinkedList<>();
		GAS.addAll(Arrays.asList(gas));
	}
	public Mediator(List<GAImplementation> gas) {
		GAS = new LinkedList<>();
		GAS.addAll(gas);
	}

	public synchronized GAImplementation getGA() {
		if (!GAS.isEmpty()) {
			return GAS.remove(0);
		}
		return null;
	}
}
