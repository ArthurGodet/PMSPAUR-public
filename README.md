# Parallel Machine Scheduling Problem with Additional Unit Resources (PMSPAUR)
This repository contains all the code that was used in the experiments for the following scientific paper:
Godet, A., Hebrard, E., Lorca, X., Simonin, G.  2020.  Using approximation within Constraint Programming to solve the Parallel Machine Scheduling Problem with Additional Unit Resources. In Proceedings of the 34th National Conference on Artificial Intelligence (AAAI)

## Installing

This project is a standard maven project in Java. As such an executable jar file can be built with the following command:

```
mvn clean package
```

## Running

As soon as maven has finished building the executable jar file, you can find it in the target folder. Copy and paste the jar file named PMSPAUR.jar into the home directory of this project. Finally, you can execute it to solve an instance of PMSPAUR with the method of your choice.

```
java -jar PMSPAUR.jar timeLimitInMinutes ConfigurationName pathToInstanceJSONFile
```

For example, if you want to solve the instance at data/2_3/2_3_RANDOM_20_100.json with the Order approach with a 30 minutes time limit (as we use for our benchmark), you should execute the following command:

```
java -jar PMSPAUR.jar 30 Order "data/2_3/2_3_RANDOM_20_100.json"
```

By the end of any execution, the final line that was printed indicate the solving statistics as such:

```
instanceName;timeToProof;timeToBest;Objective;nbNodes;nbBacktracks;nbFails;
```

timeToProof and timeToBest are both expressed in milliseconds (ms). In the case of our example, the final line should look like to something like this:

```
2_3_RANDOM_20_100;4093;4090;1017;3122;6135;3013;
```

In my case, the Order approach found the optimal solution, which objective is 1017, in 4090ms and use 3 more ms (4093ms in total) to prove it was the optimal solution or to finish the program execution. In total 3122 nodes were used for a total of 3013 fails and 6135 backtracks.

## Minizinc
All Minizinc models and data can be found in the minizinc folder.

## Look into the code

If you want to have a look at the code, here is its packages organisation:
* **bench**: this package contains every piece of code useful to make benchmarking easier.
* **constraint**: 
  * The package *cumulative* contains the code of state-of-the-art propagators for the cumulative constraint.
  * The package *order* contains the code of the propagators used for our approaches Order, OrderA and OrderAM.
  * The package *settimes* contains the code used for the SetTimes approach.
  * Finally, the class PMSPAURModel.java builds a configurable Choco model of PMSPAUR for the instance given in parameter and offers an API for solving purposes. The class also contains a main method easy to execute inside an IDE and showing how to easy configure the model to be solved.
* **data**: this package contains code useful for input/output processing, especially inside the Factory.java class.

## Having a problem ?
For any encountered problem, do not hesitate to raise an issue or to directly contact me at arth.godet@gmail.com. I would be happy to answer any question with the code.
