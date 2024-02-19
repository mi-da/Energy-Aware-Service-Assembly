# Autonomous Service Assembly for Self-Sustainable Edge Computing
This repository contains the simulation code to replicate the results of the experimantation of the paper "Autonomous Service Assembly for Self-Sustainable Edge Computing".

### Instructions
1. Download the Java Project and import it in your IDE as a Java project
2. Link the provided libraries in "ext-lib" to the project
3. Input the program argument "configs/mida-assembly-config.txt" (i.e., the configuration file of PeerSim)
4. The main class to run the experiments is "peersim.Simulator"

### Configuration Parameters
The file configs/mida-assembly-config.txt contains the configuration parameters for the simulation. The main parameters are:
- NETWORK_SIZE: The number of services to assemble
- SERVICES_PER_NODE: The number of services per node instance
- TYPES: Number of service types
- M: learning window of the algorithm
- STRATEGY: The selection criteria that the service adopt
