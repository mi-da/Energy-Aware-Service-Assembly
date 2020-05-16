package lnu.mida.controller.init;

import peersim.config.*;
import peersim.core.*;

import lnu.mida.protocol.OverloadComponentAssembly;

/**
 * I am an initializer which sets the dependencies for each node. I must be
 * activated <strong>after</strong> a type initializer has been applied. In
 * other words, I require that all components have already been assigned a type.
 * When I am applied to a component of type t, I define a dependency on types
 * {i+1, i+2, ... } with probability {@link prob} each.
 */
public class ProbDependencyInitializer implements Control {

	// ------------------------------------------------------------------------
	// Parameters
	// ------------------------------------------------------------------------

	/**
	 * The probability to add a given type as a dependency
	 *
	 * @config
	 */
	private static final String PAR_PROB = "prob";

	/**
	 * The protocol to operate on.
	 * 
	 * @config
	 */
	private static final String PAR_PROT = "comp_prot";

	// ------------------------------------------------------------------------
	// Fields
	// ------------------------------------------------------------------------

	/** Protocol identifier, obtained from config property {@link #PAR_PROT}. */
	private final int protocolID;

	/**
	 * Probability that a type is a dependency, obtained from config proberty
	 * {@link #PAR_PROB}
	 */
	private static double prob;

	public static double getProb() {
		return prob;
	}

	public static void setProb(double prob) {
		ProbDependencyInitializer.prob = prob;
	}

	// ------------------------------------------------------------------------
	// Initialization
	// ------------------------------------------------------------------------
	/**
	 * Standard constructor that reads the configuration parameters. Invoked by the
	 * simulation engine.
	 * 
	 * @param prefix the configuration prefix for this class.
	 */
	public ProbDependencyInitializer(String prefix) {
		protocolID = Configuration.getPid(prefix + "." + PAR_PROT);
		prob = Configuration.getDouble(prefix + "." + PAR_PROB);
	}

	// ------------------------------------------------------------------------
	// Methods
	// ------------------------------------------------------------------------

	@Override
	public boolean execute() {

		OverloadComponentAssembly comp;

		// prob. dependency
		for (int i = 0; i < Network.size(); ++i) {
			
			comp = (OverloadComponentAssembly) Network.get(i).getProtocol(protocolID);
			
            int dep_num = 0;
            
			for (int t = comp.getType() + 1; t < comp.getTypes(); ++t) {
				double val = CommonState.r.nextDouble();
				if (val <= prob) {
					comp.setDependencyType(t);
					dep_num++;					
					if(t==comp.getType()) {
						System.err.println("Cannot set recursive dependencies");
						System.exit(0);
					}					
				}
			}
			
			comp.setDep_num(dep_num);
		}

//		// 0-1-2-3-4-5-6-7-8-9
//		for (int i = 0; i < Network.size(); ++i) {
//			comp = (OverloadComponentAssembly) Network.get(i).getProtocol(protocolID);
//			if(comp.getType()!=9) {
//				comp.setDependencyType(comp.getType() + 1);
//				comp.setDep_num(1);
//			}
//			
//		}

		return false;
	}
}
