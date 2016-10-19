# asac-pki
```
You will need a Java compiler to build.

To build:  
.../src$ mkdir bin  
.../src$ make  

To run:
.../src$ cd bin  
.../src/bin$ java base.SimulationWorker &
.../src/bin$ java base.SimulationManager ../../pki1.conf

When the simulation has finished, results will be in the output file, e.g. latest.tsv, specified in pki1.conf.

Notes:
1) Logging information will be in Logging*.txt file(s).
2) After a simulation completes, the SimulationWorker thread will still be running. You will likely want to kill it before doing another simulation.
3) It is possible to run using multiple SimulationWorker threads. This would involve running multiple Workers threads and editing the .conf file appropriately.
```
Reference:  
http://hdl.handle.net/2022/21038
