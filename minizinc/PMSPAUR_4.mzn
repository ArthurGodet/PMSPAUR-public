int: n_resources; % The number of unit resources
int: n_machines; % The number of machines
int: n_jobs; % The total number of jobs
int: horizon;
int: LB;

set of int: resources = 1..n_resources;

set of int: jobs = 1..n_jobs;

array [resources] of int: n_jobs_per_resource;
array [jobs] of int: duration;

% Decision variables

array[jobs] of var 0..horizon: starts;
array[jobs] of var 0..horizon: dur;
array[jobs] of var 1..1: resource_consumption;
var LB..horizon: makespan;
var n_machines .. n_machines: capacity;

% Constraints

constraint
    forall(i in jobs) (
        dur[i] = duration[i]
    );

include "cumulative.mzn";
constraint cumulative(starts, dur, resource_consumption, capacity); % global cumulative

set of int: resource1 = 1..n_jobs_per_resource[1];
set of int: resource2 = (1+n_jobs_per_resource[1])..(n_jobs_per_resource[1]+n_jobs_per_resource[2]);
set of int: resource3 = (1+n_jobs_per_resource[1]+n_jobs_per_resource[2])..(n_jobs_per_resource[1]+n_jobs_per_resource[2]+n_jobs_per_resource[3]);
set of int: resource4 = (1+n_jobs_per_resource[1]+n_jobs_per_resource[2]+n_jobs_per_resource[3])..(n_jobs_per_resource[1]+n_jobs_per_resource[2]+n_jobs_per_resource[3]+n_jobs_per_resource[4]);

% disjunctive on resources
constraint cumulative(starts[resource1], dur[resource1], resource_consumption[resource1], 1);
constraint cumulative(starts[resource2], dur[resource2], resource_consumption[resource2], 1);
constraint cumulative(starts[resource3], dur[resource3], resource_consumption[resource3], 1);
constraint cumulative(starts[resource4], dur[resource4], resource_consumption[resource4], 1);

constraint makespan = max(i in jobs) (starts[i]+duration[i]);

solve minimize makespan;