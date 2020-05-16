package lnu.mida.controller.observer;

import lnu.mida.entity.GeneralNode;
import lnu.mida.protocol.OverloadComponentAssembly;
import peersim.config.*;
import peersim.core.Control;
import peersim.core.Network;
import peersim.util.*;

/**
 * I am an observer that prints, at each assembly step, the minimum, average and
 * maximum quality and energy of all fully resolved services.
 */
public class QualityEnergyObserver implements Control {

	// ///////////////////////////////////////////////////////////////////////
	// Constants
	// ///////////////////////////////////////////////////////////////////////

	/**
	 * The protocol to operate on.
	 * 
	 * @config
	 */
	private static final String PAR_PROT = "protocol";

	/**
	 * Stop when the average utility is at least X%; default never stops
	 *
	 * @config
	 */
	private static final String PAR_STOPAT = "stopat";

	/**
	 * Stop when the minimum compound utility is at least this value; default never
	 * stops
	 *
	 * @config
	 */
	private static final String PAR_MINSTOPAT = "minstopat";

	// ///////////////////////////////////////////////////////////////////////
	// Fields
	// ///////////////////////////////////////////////////////////////////////

	/**
	 * The name of this observer in the configuration file. Initialized by the
	 * constructor parameter.
	 */
	private final String name;

	/** Protocol identifier, obtained from config property {@link #PAR_PROT}. */
	private final int pid;

	private final double stopat;

	private final double minstopat;

	// ///////////////////////////////////////////////////////////////////////
	// Constructor
	// ///////////////////////////////////////////////////////////////////////

	/**
	 * Standard constructor that reads the configuration parameters. Invoked by the
	 * simulation engine.
	 * 
	 * @param name the configuration prefix for this class.
	 */
	public QualityEnergyObserver(String name) {
		this.name = name;
		pid = Configuration.getPid(name + "." + PAR_PROT);
		stopat = Configuration.getDouble(name + "." + PAR_STOPAT, -1);
		minstopat = Configuration.getDouble(name + "." + PAR_MINSTOPAT, -1);
	}

	// ///////////////////////////////////////////////////////////////////////
	// Methods
	// ///////////////////////////////////////////////////////////////////////

	@Override
	public boolean execute() {

		long time = peersim.core.CommonState.getTime();

		IncrementalStats quality = new IncrementalStats();
		IncrementalStats energy = new IncrementalStats();



		int fully_resolved = 0;

		for (int i = 0; i < Network.size(); i++) {

			GeneralNode node = (GeneralNode) Network.get(i);
			OverloadComponentAssembly n = (OverloadComponentAssembly) node.getProtocol(pid);

			if (n.isFullyResolved()) {
				fully_resolved++;
			}

			// recursive quality calculation
			quality.add(n.getEffectiveCU());
			
			
			// recursive calculation of overall communication and computation cost (lamda)
			node.setE_comp(n.calculateOverallCPUEnergy());
			node.setE_comm(n.calculateOverallCommunicationEnergy());

			// recursive calculation of overall communication and computation cost (lamda)
			node.setE_comp_lambda(n.calculateOverallCPUEnergyLambda());
			node.setE_comm_lambda(n.calculateOverallCommunicationEnergyLambda());

			/**  Green Energy goes in Journal **/
//			double energyBalance = node.getG() - (node.getI_comp() + node.getI_comm());
//			energy.add(Math.min(0, energyBalance));
			
			energy.add(node.getI_comp_lambda() + node.getI_comm_lambda());
			

//			if(node.getG()-(node.getI_comp()+node.getI_comm())<0) {
//				System.err.println(node.getG()-(node.getI_comp()+node.getI_comm())+"--> energia consumate piu' di quella prodotta per nodo "+node.getID());
//				System.exit(1);
//			}

//			System.out.println("node "+node.getID()+" type="+n.getType()+" I_comp="+node.getI_comp()+" E_comp="+node.getE_comp()+" I_comm="+node.getI_comm()+" E_comm="+node.getE_comm()+" lambda="+n.getLambda_t());	

		}

//		System.out.println("fully resolved "+fully_resolved);

		int index = (int) ((time / Configuration.getInt("COMPOSITION_STEPS", 1)));

		// Quality
		FinalUtilityObserver.quality.get(index).add(quality.getAverage());
		IncrementalStats quality_jain_is = FinalUtilityObserver.quality_jain.get(index);
		// calculates the jain's fairness for quality
		double quality_jain_fairness = Math.pow(quality.getSum(), 2) / (quality.getN() * quality.getSqrSum());
		quality_jain_is.add(quality_jain_fairness);

		// Energy
		FinalUtilityObserver.energy.get(index).add(energy.getAverage());
		IncrementalStats energy_jain_is = FinalUtilityObserver.energy_jain.get(index);
		// calculates the jain's fairness for energy
		double energy_jain_fairness = Math.pow(energy.getSum(), 2) / (energy.getN() * energy.getSqrSum());
		energy_jain_is.add(energy_jain_fairness);

		return false;
	}

}