package lnu.mida.protocol;

import java.util.ArrayList;

import com.lajv.location.Location;

import lnu.mida.entity.EnergyReputation;
import lnu.mida.entity.GeneralNode;
import lnu.mida.entity.QOSReputation;
import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.core.Cleanable;
import peersim.core.Node;

public class OverloadApplication implements CDProtocol, Cleanable {

	// ///////////////////////////////////////////////////////////////////////
	// Constants
	// ///////////////////////////////////////////////////////////////////////

	/**
	 * The component assambly protocol.
	 */
	private static final String COMP_PROT = "comp_prot";

	/**
	 * The strategy
	 */
	private static String STRATEGY = "";

	// ///////////////////////////////////////////////////////////////////////
	// Fields
	// ///////////////////////////////////////////////////////////////////////

	/**
	 * The component assembly protocol id.
	 */
	private final int component_assembly_pid;

	/** The learner's container */
	private ArrayList<QOSReputation> qosReputations;
	private ArrayList<EnergyReputation> energyReputations;

	/**
	 * Initialize this object by reading configuration parameters.
	 * 
	 * @param prefix the configuration prefix for this class.
	 */
	public OverloadApplication(String prefix) {
		super();
		STRATEGY = Configuration.getString("STRATEGY", "no strat");
		component_assembly_pid = Configuration.getPid(prefix + "." + COMP_PROT);
		qosReputations = new ArrayList<QOSReputation>();
		energyReputations = new ArrayList<EnergyReputation>();
	}

	public void addQoSHistoryExperience(OverloadComponentAssembly node, double experienced_utility, double declared_utility) {		
		int index = (int) node.getId();
		QOSReputation reputation = getOrCreateQOSReputation(index);
		reputation.setDeclared_utility(declared_utility);
		reputation.addExperiencedUtility(experienced_utility);
	}
	
	public void addEnergyHistoryExperience(OverloadComponentAssembly node, double declared_energy) {
		
		int index = (int) node.getId();
		EnergyReputation reputation = getOrCreateEnergyReputation(index);
		reputation.addDeclaredEnergy(declared_energy);
	}

//	public ArrayList<QOSReputation> getHistories() {
//		return qosReputations;
//	}

	/**
	 * Makes a copy of this object. Needs to be explicitly defined, since we have
	 * array members.
	 */
	@Override
	public Object clone() {
		OverloadApplication result = null;
		try {
			result = (OverloadApplication) super.clone();
		} catch (CloneNotSupportedException ex) {
			System.out.println(ex.getMessage());
			assert (false);
		}
		result.qosReputations = new ArrayList<QOSReputation>();
		result.energyReputations = new ArrayList<EnergyReputation>();
		return result;
	}

	// returns true if comp > old
	public boolean chooseByStrategy(OverloadComponentAssembly comp, OverloadComponentAssembly old, OverloadComponentAssembly ca) {

		// default composition strategy (best actual value)
		if (STRATEGY.equals("greedy")) {
			return chooseByDefaultStrategy(comp, old);
		}
		// random strategy
		if (STRATEGY.equals("random")) {
			return chooseByRandomStrategy(comp, old);
		}
		// average strategy - not used during paper
		if (STRATEGY.equals("average")) {
			return chooseByAverageStrategy(comp, old);
		}
		// future expected utility
		if (STRATEGY.equals("emergent")) {
			return chooseByFutureExpectedUtility(comp, old, ca);
		}
		// approach to challenge
		if (STRATEGY.equals("shaerf")) {
			return chooseByChallengeStrategy(comp, old);
		}
		// individual energy
		if (STRATEGY.equals("individual_energy")) {
			return chooseByIndividualEnergyStrategy(comp, old, ca);
		}
		// overall energy
		if (STRATEGY.equals("overall_energy")) {
			return chooseByOverallEnergyStrategy(comp, old, ca);
		}
		// fair energy
		if (STRATEGY.equals("fair_energy")) {
			return chooseByFairEnergyStrategy(comp, old, ca);
		}
		// exception is raised if a strategy is not selected
		else {
			try {
				throw new Exception("Strategy not selected");
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.exit(0);
			return false;
		}
	}

	// returns true if comp > old
	private boolean chooseByDefaultStrategy(OverloadComponentAssembly comp, OverloadComponentAssembly old) {

		if (comp.getDeclaredUtility() >= old.getDeclaredUtility())
			return true;
		else
			return false;

	}

	// chooses a random component
	private boolean chooseByRandomStrategy(OverloadComponentAssembly comp, OverloadComponentAssembly old) {
		if (Math.random() < 0.5)
			return true;
		else
			return false;
	}

	// returns true if Avg(comp) > Avg(old)
	private boolean chooseByAverageStrategy(OverloadComponentAssembly comp, OverloadComponentAssembly old) {

		QOSReputation compReputation = getOrCreateQOSReputation((int) comp.getId());
		QOSReputation oldReputation = getOrCreateQOSReputation((int) old.getId());

		if (compReputation.getK() == 0)
			compReputation.setQk(comp.getCompoundUtility());

		if (oldReputation.getK() == 0)
			oldReputation.setQk(comp.getCompoundUtility());

		if (compReputation.getWindowAverage() > oldReputation.getWindowAverage())
			return true;

		else
			return false;
	}

	// future expected utility: two layer of reinforcement learning
	private boolean chooseByFutureExpectedUtility(OverloadComponentAssembly comp, OverloadComponentAssembly old, OverloadComponentAssembly ca) {

		QOSReputation compReputation = getOrCreateQOSReputation((int) comp.getId());
		QOSReputation oldReputation = getOrCreateQOSReputation((int) old.getId());

		double compTrust = compReputation.getTk();
		double oldTrust = oldReputation.getTk();

		double compFEU = compTrust * comp.getDeclaredUtility() + ((1.0 - compTrust) * compReputation.getWindowAverage());
		double oldFEU = oldTrust * old.getDeclaredUtility() + ((1.0 - oldTrust) * oldReputation.getWindowAverage());
	
		
		// if no experiences do the average
		if (compReputation.getK() == 0) {
			int n = 0;
			int sum = 0;
			for (QOSReputation reputation : qosReputations) {
				double qk = reputation.getQk();
					sum += qk;
					n++;
			}
			compFEU = sum / n;
		}

		if (oldReputation.getK() == 0) {
			int n = 0;
			int sum = 0;
			for (QOSReputation reputation : qosReputations) {
				double qk = reputation.getQk();
					sum += qk;
					n++;
			}
			oldFEU = sum / n;
		}
		
		
		if (compFEU == oldFEU)
			return chooseByIndividualEnergyStrategy(comp, old, ca);
		
		// greedy selection
		if (compFEU > oldFEU) {
				return true;
		}
		else {
			return false;
		}
	}

	// approach to challenge Shaerf
	private boolean chooseByChallengeStrategy(OverloadComponentAssembly comp, OverloadComponentAssembly old) {

		QOSReputation compReputation = getOrCreateQOSReputation((int) comp.getId());
		QOSReputation oldReputation = getOrCreateQOSReputation((int) old.getId());

		if (compReputation.getK() == 0 || oldReputation.getK() == 0)
			return chooseByRandomStrategy(comp, old);

		double comp_ee = compReputation.getEe();
		double old_ee = oldReputation.getEe();

		// if no experiences do the average
		if (compReputation.getK() == 0) {
			int n = 0;
			int sum = 0;
			for (QOSReputation reputation : qosReputations) {
				double ee = reputation.getEe();
				if (ee != 0) {
					sum += ee;
					n++;
				}
			}
			comp_ee = sum / n;
		}

		if (oldReputation.getK() == 0) {
			int n = 0;
			int sum = 0;
			for (QOSReputation reputation : qosReputations) {
				double ee = reputation.getEe();
				if (ee != 0) {
					sum += ee;
					n++;
				}
			}
			old_ee = sum / n;
		}

		double comp_probl1 = Math.pow(comp_ee, 3);
		double old_probl1 = Math.pow(old_ee, 3);

		double sigma = comp_probl1 + old_probl1;

		double comp_probl = comp_probl1 / sigma;
		double old_probl = old_probl1 / sigma;

		if (old_probl < comp_probl)
			return true;
		else
			return false;
	}

	// individual energy strategy
	private boolean chooseByIndividualEnergyStrategy(OverloadComponentAssembly comp, OverloadComponentAssembly old, OverloadComponentAssembly ca) {

		
		GeneralNode nodeThis = GeneralNode.getNode(ca.getId());
		
		GeneralNode nodeComp = GeneralNode.getNode(comp.getId());
		GeneralNode nodeOld = GeneralNode.getNode(old.getId());


//      double IndividualEnergyConsumption_COMP = nodeComp.getG() - (nodeComp.getI_comp() + nodeComp.getI_comm());
//      double IndividualEnergyConsumption_OLD =  nodeOld.getG() - (nodeOld.getI_comp() + nodeOld.getI_comm());

		Location thisLoc = nodeThis.getLocation();
		Location compLoc = nodeComp.getLocation();
		Location oldLoc = nodeOld.getLocation();
		
		double oldLatency = thisLoc.latency(oldLoc);
		double newLatency = thisLoc.latency(compLoc);
		
		if(newLatency<oldLatency)
			return true;
		return false;
		
	}
	
	// overall energy strategy
	private boolean chooseByOverallEnergyStrategy(OverloadComponentAssembly comp, OverloadComponentAssembly old, OverloadComponentAssembly ca) {
		
		// at round 1 the overal energy is not known

		GeneralNode nodeComp = GeneralNode.getNode(comp.getId());
		GeneralNode nodeOld = GeneralNode.getNode(old.getId());
				
		double energyComp = nodeComp.getE_comp() + nodeComp.getE_comm();
		double energyOld =  nodeOld.getE_comp() + nodeOld.getE_comm();
		
		if(energyComp==energyOld)
			chooseByRandomStrategy(comp, old);
		
		if(energyComp<energyOld)
			return true;
		return false;		
	}
	
	// greedy fair energy strategy using Shaerf
	private boolean chooseByFairEnergyStrategy(OverloadComponentAssembly comp, OverloadComponentAssembly old, OverloadComponentAssembly ca) {		

		EnergyReputation compReputation = getOrCreateEnergyReputation((int) comp.getId());
		EnergyReputation oldReputation = getOrCreateEnergyReputation((int) old.getId());
		
		if (compReputation.getK() == 0 && oldReputation.getK() == 0)
			return chooseByRandomStrategy(comp, old);

		double comp_ee = compReputation.getEe();
		double old_ee = oldReputation.getEe();

		// if no experiences do the average
		if (compReputation.getK() == 0) {
			int n = 0;
			int sum = 0;
			for (EnergyReputation reputation : energyReputations) {
				double ee = reputation.getEe();
					sum += ee;
					n++;
			}
			comp_ee = sum / n;
		}

		if (oldReputation.getK() == 0) {
			int n = 0;
			int sum = 0;
			for (EnergyReputation reputation : energyReputations) {
				double ee = reputation.getEe();
					sum += ee;
					n++;
			}
			old_ee = sum / n;
		}

		double comp_probl1 = Math.pow(comp_ee, 17);
		double old_probl1 = Math.pow(old_ee, 17);

		double sigma = comp_probl1 + old_probl1;

		double comp_probl = comp_probl1 / sigma;
		double old_probl = old_probl1 / sigma;

		if (old_probl > comp_probl)
			return true;
		else
			return false;
	}
	

	@Override
	public void onKill() {
		// TODO Auto-generated method stub
	}

	@Override
	public void nextCycle(Node node, int protocolID) {


	}
	
	

	private QOSReputation getOrCreateQOSReputation(int nodeId) {
		for (QOSReputation reputation : qosReputations) {
			if (reputation.getNodeID() == nodeId) {
				return reputation;
			}
		}
		QOSReputation newReputation = new QOSReputation(nodeId);
		qosReputations.add(newReputation);
		return newReputation;
	}
	
	private EnergyReputation getOrCreateEnergyReputation(int nodeId) {
		for (EnergyReputation reputation : energyReputations) {
			if (reputation.getNodeID() == nodeId) {
				return reputation;
			}
		}
		EnergyReputation newReputation = new EnergyReputation(nodeId);
		energyReputations.add(newReputation);
		return newReputation;
	}
	
	public ArrayList<QOSReputation> getQoSReputations(){
		return qosReputations;
	}
	
	public void reset() {
		qosReputations = new ArrayList<QOSReputation>();
		energyReputations = new ArrayList<EnergyReputation>();
	}
	
	

}
