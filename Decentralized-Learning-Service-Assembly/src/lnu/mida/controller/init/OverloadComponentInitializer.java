/*
 * Copyright (c) 2012 Moreno Marzolla
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
 */

package lnu.mida.controller.init;

import lnu.mida.entity.GeneralNode;
import lnu.mida.entity.QOSReputation;
import lnu.mida.entityl.transferfunction.CustomTransferFunction;
import lnu.mida.entityl.transferfunction.TransferFunction;
import lnu.mida.entityl.transferfunction.UnityTransferFunction;
import lnu.mida.protocol.OverloadApplication;
import lnu.mida.protocol.OverloadComponentAssembly;
import peersim.config.*;
import peersim.core.*;

/**
 * I am an initialiizer which sets the type of the first two node to 0
 * and the others to 1,2,3,4...
 */
public class OverloadComponentInitializer implements Control {

    // ------------------------------------------------------------------------
    // Parameters
    // ------------------------------------------------------------------------

	/**
	 * The component assambly protocol.
	 */
	private static final String COMP_PROT = "comp_prot";
	
	private static final String APPL_PROT = "appl_prot";
	
	/**
	 * The maximum number of service types (default: 10)
	 */
	private static final String PAR_TYPES = "types";


    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------
   	
	private final int component_assembly_pid;
	private final int application_assembly_pid;
	
    public static int lastTypeInjected;
    
    private int max_types;


    // ------------------------------------------------------------------------
    // Initialization
    // ------------------------------------------------------------------------
    /**
     * Standard constructor that reads the configuration parameters. Invoked by
     * the simulation engine.
     * 
     * @param prefix
     *            the configuration prefix for this class.
     */
    public OverloadComponentInitializer(String prefix) {
    	component_assembly_pid = Configuration.getPid(prefix + "." + COMP_PROT);
    	application_assembly_pid = Configuration.getPid(prefix + "." + APPL_PROT);
    	lastTypeInjected = 0;
    	max_types = Configuration.getInt(prefix + "." + PAR_TYPES, 10);
    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    @Override
	public boolean execute() {

    	int m = Configuration.getInt("M",50000);
    	
    	int types = Configuration.getInt("TYPES",0);
    	
    	int max_num_per_type = Network.size()/types;

    	
    	QOSReputation.setM(m); // set the parameter m for the learner
    	
    	int current_type = 0;
		// types setting
    	int num_per_type = 0;
    	
    	
		for(int i=0;i<Network.size();i++) {
					

			GeneralNode n = (GeneralNode)Network.get(i);			
		    OverloadComponentAssembly ca = (OverloadComponentAssembly)n.getProtocol(component_assembly_pid);
		    
		    OverloadApplication appl = (OverloadApplication)n.getProtocol(application_assembly_pid);
		    appl.reset();
		    
		    
		    /**
		     * Energy parameters
		     */
		    
		    // set green energy generation rate	(for Journal)    
		    n.setG(2+2*Math.random()); // n.setG(2);
		    
		    	    
		    ca.setTransfer_func_CPU(new CustomTransferFunction(Math.random()));
		    
		    /**
		     * Quality parameters
		     */
		    	    
		    // queue parameter
			ca.setQueueParameter(Math.random());
			
            // curve parameter between 0.2 and 1
			double curveParameter = (Math.random()*0.8)+0.2;	// double curveParameter = (Math.random()*0.8)+0.2;		
			ca.setCurveParameter(curveParameter);
			
			// declared utility
			ca.setDeclared_utility(1);
			
//			if(Math.random()<0.5) {
//				ca.setDeclared_utility(0.99+(0.01*Math.random()));
//			}
			
			// setup transfer functions
			TransferFunction transfer_func[] = ca.getTransferFunctions();
			for (int j = 0; j < max_types; j++) {				
					transfer_func[j] = new CustomTransferFunction(Math.random()); // transfer_func[j] = new CustomTransferFunction(0.2);
//				    transfer_func[j] = new UnityTransferFunction();
			}	
				
			
		    /**
		     * Construct parameters
		     */
			
			// mette randomicamente servizi non affidabili con una certa percentuale
//		    if(Math.random()<0.3) { // old was 0.3
//				ca.setQueueParameter(0.2);
//				ca.setCurveParameter(0.2);
//		    }
		

		    
		    // type		
		    ca.setType(current_type);
		    ca.setId((int) n.getID());
		    
		    num_per_type++;
		    
		    // sigma: type 0 get requests from external users
		    if(current_type==0) {
		    	ca.setSigma(1.0);
		    	ca.setLambda_t(1.0);
		    }		    
		    
	        if(num_per_type == max_num_per_type) { // num_per_type==100	        	
	        	num_per_type=0;
	        	current_type++;
	        }
 
		}

		return false;
    }
    
}
