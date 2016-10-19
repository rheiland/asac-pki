JC = javac
JFLAGS = -d bin

BASE = base/
IBE = ibe/
IBEMSR = ibe/measure/
WEB = web/
WEBMSR = web/measure/

.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java
.PHONY: worker manager ibe web skel pki plotgen clean cleanlogs

#default: worker manager ibe plotgen
#default: worker manager ibe plotgen web
#default: worker manager plotgen web
#default: web
#default: worker skel
#default: worker pki
default: worker manager pki plotgen
#default: worker manager ibe plotgen

worker:
	$(JC) $(JFLAGS) $(BASE)SimulationWorker.java
	$(JC) $(JFLAGS) measures/TimeMeasure.java

manager:
	$(JC) $(JFLAGS) $(BASE)SimulationManager.java
	$(JC) $(JFLAGS) measures/TimeMeasure.java

ibe:
	$(JC) $(JFLAGS) $(IBE)*.java
	$(JC) $(JFLAGS) $(IBEMSR)*.java

web:
	$(JC) $(JFLAGS) $(WEB)*.java
	$(JC) $(JFLAGS) $(WEBMSR)*.java

skel:
	$(JC) $(JFLAGS) base/*.java
	$(JC) $(JFLAGS) skel/*.java
	$(JC) $(JFLAGS) skel/measure/*.java

pki:
	$(JC) $(JFLAGS) base/*.java
	$(JC) $(JFLAGS) pki/*.java
	$(JC) $(JFLAGS) pki/measure/*.java

plotgen:
	$(JC) $(JFLAGS) $(BASE)PlotGen.java

clean: cleanlogs
	$(RM) */*.class
	$(RM) */*/*.class

cleanlogs:
	$(RM) Logging*.txt
