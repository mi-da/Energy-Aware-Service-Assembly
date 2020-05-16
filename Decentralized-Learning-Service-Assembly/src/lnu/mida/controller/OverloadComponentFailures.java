package lnu.mida.controller;

import peersim.config.Configuration;
import peersim.core.*;

/**
 * This class removes randomly chosen nodes from the network.
 */
public class OverloadComponentFailures implements Control {

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

	// ------------------------------------------------------------------------
	// Parameters
	// ------------------------------------------------------------------------

	/**
	 * Minimum number of services to remove
	 */
	protected static final String PAR_NUM = "num";

	// ------------------------------------------------------------------------
	// Fields
	// ------------------------------------------------------------------------
	private final int num;
	private final String name;
	private final int component_assembly_pid;
	private final int application_pid;


	// ------------------------------------------------------------------------
	// Initialization
	// ------------------------------------------------------------------------
	public OverloadComponentFailures(String name) {
		this.name = name;
		num = Configuration.getInt(name + "." + PAR_NUM);
		component_assembly_pid = Configuration.getPid(name + "." + COMP_PROT);
		application_pid = Configuration.getPid(name + "." + APPL_PROT);
	}

	// ------------------------------------------------------------------------
	// Methods
	// ------------------------------------------------------------------------
	@Override
	public boolean execute() {

		int i;

		for (i = 0; i < num; ++i) {
			int j = CommonState.r.nextInt(Network.size());
			Node n = Network.get(j);
			Network.remove(j);
		}
		return false;
		
	}

}
