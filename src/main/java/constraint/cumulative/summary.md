This document reminds the different filtering algorithms available in CumulativeConstraintFactory and the corresponding papers from the literature.

## Papers

(1) Fahimi, H., Ouellet, Y., Quimper, C.-G.: Linear-Time Filtering Algorithms for the Disjunctive Constraint and a Quadratic Filtering Algorithm for the Cumulative Not-First Not-Last. Constraints 23(3), pages 272–293 (2018). https://doi.org/10.1007/s10601-018-9282-9  
(2) Ouellet, P., Quimper, C.-G.: Time-table-extended-edge-finding for the cumulative constraint. In: Proceedings of the 19th International Conference on Principles and Practice of Constraint Programming (CP 2013), pp. 562-577 (2013). https://doi.org/10.1007/978-3-642-40627-0_42  
(3) Vilim, P.: Edge finding filtering algorithm for discrete cumulative resources in O(k n log(n)). In: Proceedings of the 15th International Conference on Principles and Practice of Constraint Programming (CP 2009), pp. 802-816 (2009). https://doi.org/10.1007/978-3-642-04244-7_62  

## Cumulative

| Papers | Overload Checking |  Timetable  | (Extended) Edge-Finding | Timetable-Extended-Edge-Finding | Not-First/Not-Last | Energetic Reasoning |
|:------:|:-----------------:|:-----------:|:-----------------------:|:-------------------------------:|:------------------:|:-------------------:|
|   (1)  |        O(n)       |             |                         |                                 |       O(n²))       |                     |
|   (2)  |                   | O(n log(n)) |      O(k n log(n))      |          O(k n log(n))          |                    |                     |
|   (3)  |                   |             |      O(k n log(n))      |                                 |                    |                     |


## Disjunctive

| Papers | Overload Checking |  Timetable  | (Extended) Edge-Finding | Timetable-Extended-Edge-Finding | Not-First/Not-Last | Energetic Reasoning |
|:------:|:-----------------:|:-----------:|:-----------------------:|:-------------------------------:|:------------------:|:-------------------:|
|   (1)  |        O(n)       |     O(n)    |           O(n)          |                                 |                    |                     |
