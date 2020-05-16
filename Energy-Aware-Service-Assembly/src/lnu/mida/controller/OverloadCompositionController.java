/*
 * Copyright (c) 2012, 2014 Moreno Marzolla
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 2 as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */

package lnu.mida.controller;

import lnu.mida.entity.GeneralNode;
import lnu.mida.protocol.OverloadApplication;
import lnu.mida.protocol.OverloadComponentAssembly;
import peersim.config.*;
import peersim.core.*;

public class OverloadCompositionController implements Control {

	// ///////////////////////////////////////////////////////////////////////
	// Constants
	// ///////////////////////////////////////////////////////////////////////

	/**
	 * The component assambly protocol.
	 */
	private static final String COMP_PROT = "comp_prot";

	/**
	 * The application protocol.
	 */
	private static final String APPL_PROT = "appl_prot";

	// ///////////////////////////////////////////////////////////////////////
	// Fields
	// ///////////////////////////////////////////////////////////////////////

	/**
	 * The name of this observer in the configuration file. Initialized by the
	 * constructor parameter.
	 */
	private final String name;

	private final int component_assembly_pid;
	private final int application_pid;

	public static int failed_connections;
	public static int number_of_experiences;
	public static double total_experiences_value;

	public static double variance;
	public static double stdDev;

	// ///////////////////////////////////////////////////////////////////////
	// Constructor
	// ///////////////////////////////////////////////////////////////////////
	/**
	 * Standard constructor that reads the configuration parameters. Invoked by
	 * the simulation engine.
	 * 
	 * @param name
	 *            the configuration prefix for this class.
	 */
	public OverloadCompositionController(String name) {
		this.name = name;
		component_assembly_pid = Configuration.getPid(name + "." + COMP_PROT);
		application_pid = Configuration.getPid(name + "." + APPL_PROT);
	}

	// ///////////////////////////////////////////////////////////////////////
	// Methods
	// ///////////////////////////////////////////////////////////////////////

	@Override
	public boolean execute() {
		
		// non calcolo al round 0 (nessun bind)
		if (CommonState.getIntTime() == 0)
			return false;

		// get from every node the real quality experienced
		int notResolved = 0;

		for (int i = 0; i < Network.size(); i++) {

			if (!Network.get(i).isUp()) {
				continue;
			}

			GeneralNode node = (GeneralNode) Network.get(i);
			OverloadComponentAssembly ca = (OverloadComponentAssembly) node.getProtocol(component_assembly_pid);
			OverloadApplication appl = (OverloadApplication) node.getProtocol(application_pid);
	

			double experiencedCU = 1;

			if (!ca.isFullyResolved()) {
				experiencedCU = 0;
				notResolved++;
			} else {

				OverloadComponentAssembly[] listDepObj = ca.getDependencies_obj();
				boolean[] listDep = ca.getDependencies();

				for (int j = 0; j < listDep.length; j++) {
					
					boolean dep = listDep[j];
					if (dep == true) {

						OverloadComponentAssembly depObj = listDepObj[j];
						
						// should not happen
						if(ca.getType()==depObj.getType()) {
							System.err.println("Cannot have dependency on same type: OverloadComponentAssembly");
							System.exit(0);
						}

						// updates lambda
 						//if(depObj.getLambda_t()==0)
						depObj.updateLambdaTot();

						double experienced_utility = depObj.getRealUtility(ca);

						appl.addQoSHistoryExperience(depObj, experienced_utility, depObj.getDeclaredUtility());
						
						
						GeneralNode depNode = GeneralNode.getNode(depObj.getId());
						
						// added individual energy lambda
						appl.addEnergyHistoryExperience(depObj, depNode.getI_comp_lambda()+depNode.getI_comm_lambda());

						experiencedCU = experiencedCU * experienced_utility; // Experienced Compound Utility (multiplication of all dependencies)				
					}
				}
			}

			ca.setExperiencedCU(experiencedCU);

		}
		
		// set new declared utilities
//		for (int i = 0; i < Network.size(); i++) {
//
//			if (!Network.get(i).isUp()) {
//				continue;
//			}
//			GeneralNode node = (GeneralNode) Network.get(i);
//			OverloadComponentAssembly ca = (OverloadComponentAssembly) node.getProtocol(component_assembly_pid);
//			ca.setDeclared_utility(ca.getUtilityFromLambda());
//		}
	

		return false;
	}

	public static double getVariance() {
		return variance;
	}

	public static double getStdDev() {
		return stdDev;
	}

}
