package lnu.mida.controller.dynamic;

import java.util.Random;

import lnu.mida.controller.init.ProbDependencyInitializer;
import lnu.mida.protocol.OverloadComponentAssembly;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Node;
import peersim.dynamics.NodeInitializer;

public class OverloadNewNodeInitializer implements NodeInitializer {
	
	private static final String COMP_PROT = "comp_prot";
	
	private final int component_assembly_pid;
	
	
    public OverloadNewNodeInitializer(String prefix) {
    	component_assembly_pid = Configuration.getPid(prefix + "." + COMP_PROT);
    }


	@Override
	public void initialize(Node n) {
		
		System.out.println("CHECK INITIALIZED: OverloadNewNodeInitializer");
		System.exit(0);
		
		// new peer
		OverloadComponentAssembly comp = (OverloadComponentAssembly)n.getProtocol(component_assembly_pid);
		comp.setId((int) n.getID());
		
	    // queue parameter
		comp.setQueueParameter(1);
		
        // curve parameter between 0.2 and 1
		double curveParameter = (Math.random()*0.8)+0.2;		
		comp.setCurveParameter(curveParameter);
		
		// declared utility
		comp.setDeclared_utility(1);			
		
		// mette randomicamente servizi non affidabili con una certa percentuale
	    if(Math.random()<0.3) {
	    	comp.setQueueParameter(0.2);
	    	comp.setCurveParameter(0.2);
	    }	
	
		
		// random type 0...max types
		Random rand = new Random();		
		int type = rand.nextInt(comp.getTypes());		
		comp.setType(type);
		
		// random dependencies
		double prob = ProbDependencyInitializer.getProb();
		
		for (int t = comp.getType() + 1; t < comp.getTypes(); ++t) {
			double val = CommonState.r.nextDouble();
			if (val <= prob)
				comp.setDependencyType(t);
		}

	}

}
