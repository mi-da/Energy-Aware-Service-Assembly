package lnu.mida.entity;

public class QOSReputation implements Cloneable  {
	
	
	// node id for which the experience is evaluated
	private int nodeID;
	// last declared utility
	private double declared_utility;
	// number of time the experience is done
	private int k;
	// current average (influenced by the window period of the learner)
	private double Qk;
	// trust value
	private double Tk;
	
	// window period of the learner
	private static int M;
	
	// approach to challenge
	private double ee;
	
	public QOSReputation(int nodeId) {
		this.nodeID=nodeId;
		declared_utility=-1;
		k=0;
		Qk=1; // was 0
		Tk=1;
		ee=0;
	}
	
	public void addExperiencedUtility(double experienced_utility) {
		
		// Updating the Average
        double beta = (k%M)+1;
		double Qk_new = Qk + ((experienced_utility-Qk)/beta);	

		Qk = Qk_new;
		
		// Updating the trust value
		
		double efficiency = (1 - ( Math.abs(declared_utility - experienced_utility) / Math.abs(declared_utility) ));
		
		// trust update
		double Tk_new = Tk + ((efficiency-Tk)/beta); 
		// step trust update
//		double Tk_old=Tk;
//		if(experienced_utility<declared_utility) {
//			if(Tk>0)
//				Tk-=0.1;
//		}
//		else {
//			if(Tk<1)
//				Tk+=0.1;
//		}

		Tk=Tk_new;		
		
//		System.out.println("dec="+declared_utility+" exp="+experienced_utility+" Tk="+Tk_old+" Tk_new="+Tk);
		
		
		// Approach to Challenge - Shaerf quality	
		double W = 0.2 + (0.8/k);
		if(k==0)
			W=1;
		
		double ee_new = W*experienced_utility + ( (1-W)*ee );	
		
//		System.out.println(W);
		
		ee = ee_new;
		
		k++;
	}
	
	public double getWindowAverage() {
        return Qk;
	}
	
	@Override
	public Object clone() {	
		QOSReputation result = null;
		try {
			result = (QOSReputation) super.clone();
		} catch (CloneNotSupportedException ex) {
			System.out.println(ex.getMessage());
			assert(false);
		}
		return result;
	}

	public int getK() {
		return k;
	}
	
	public void setK(int k) {
		this.k=k;
	}

	public static int getM() {
		return M;
	}

	public static void setM(int m) {
		M = m;
	}

	public double getDeclared_utility() {
		return declared_utility;
	}

	public void setDeclared_utility(double declared_utility) {
		this.declared_utility = declared_utility;
	}

	public double getTk() {
		return Tk;
	}

	public void setTk(double tk) {
		Tk = tk;                                                              
	}                                                                                

	public void setQk(double qk) {
		Qk = qk;
	}
	
	public double getQk() {
		return Qk;
	}

	public double getEe() {
		return ee;
	}
	
	public int getNodeID() {
		return nodeID;
	}

}
