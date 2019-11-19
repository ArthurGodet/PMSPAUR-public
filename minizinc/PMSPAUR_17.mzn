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
set of int: resource5 = (1+n_jobs_per_resource[1]+n_jobs_per_resource[2]+n_jobs_per_resource[3]+n_jobs_per_resource[4])..(n_jobs_per_resource[1]+n_jobs_per_resource[2]+n_jobs_per_resource[3]+n_jobs_per_resource[4]+n_jobs_per_resource[5]);
set of int: resource6 = (1+n_jobs_per_resource[1]+n_jobs_per_resource[2]+n_jobs_per_resource[3]+n_jobs_per_resource[4]+n_jobs_per_resource[5])..(n_jobs_per_resource[1]+n_jobs_per_resource[2]+n_jobs_per_resource[3]+n_jobs_per_resource[4]+n_jobs_per_resource[5]+n_jobs_per_resource[6]);
set of int: resource7 = (1+n_jobs_per_resource[1]+n_jobs_per_resource[2]+n_jobs_per_resource[3]+n_jobs_per_resource[4]+n_jobs_per_resource[5]+n_jobs_per_resource[6])..(n_jobs_per_resource[1]+n_jobs_per_resource[2]+n_jobs_per_resource[3]+n_jobs_per_resource[4]+n_jobs_per_resource[5]+n_jobs_per_resource[6]+n_jobs_per_resource[7]);
set of int: resource8 = (1+n_jobs_per_resource[1]+n_jobs_per_resource[2]+n_jobs_per_resource[3]+n_jobs_per_resource[4]+n_jobs_per_resource[5]+n_jobs_per_resource[6]+n_jobs_per_resource[7])..(n_jobs_per_resource[1]+n_jobs_per_resource[2]+n_jobs_per_resource[3]+n_jobs_per_resource[4]+n_jobs_per_resource[5]+n_jobs_per_resource[6]+n_jobs_per_resource[7]+n_jobs_per_resource[8]);
set of int: resource9 = (1+n_jobs_per_resource[1]+n_jobs_per_resource[2]+n_jobs_per_resource[3]+n_jobs_per_resource[4]+n_jobs_per_resource[5]+n_jobs_per_resource[6]+n_jobs_per_resource[7]+n_jobs_per_resource[8])..(n_jobs_per_resource[1]+n_jobs_per_resource[2]+n_jobs_per_resource[3]+n_jobs_per_resource[4]+n_jobs_per_resource[5]+n_jobs_per_resource[6]+n_jobs_per_resource[7]+n_jobs_per_resource[8]+n_jobs_per_resource[9]);
set of int: resource10 = (1+n_jobs_per_resource[1]+n_jobs_per_resource[2]+n_jobs_per_resource[3]+n_jobs_per_resource[4]+n_jobs_per_resource[5]+n_jobs_per_resource[6]+n_jobs_per_resource[7]+n_jobs_per_resource[8]+n_jobs_per_resource[9])..(n_jobs_per_resource[1]+n_jobs_per_resource[2]+n_jobs_per_resource[3]+n_jobs_per_resource[4]+n_jobs_per_resource[5]+n_jobs_per_resource[6]+n_jobs_per_resource[7]+n_jobs_per_resource[8]+n_jobs_per_resource[9]+n_jobs_per_resource[10]);
set of int: resource11 = (1+n_jobs_per_resource[1]+n_jobs_per_resource[2]+n_jobs_per_resource[3]+n_jobs_per_resource[4]+n_jobs_per_resource[5]+n_jobs_per_resource[6]+n_jobs_per_resource[7]+n_jobs_per_resource[8]+n_jobs_per_resource[9]+n_jobs_per_resource[10])..(n_jobs_per_resource[1]+n_jobs_per_resource[2]+n_jobs_per_resource[3]+n_jobs_per_resource[4]+n_jobs_per_resource[5]+n_jobs_per_resource[6]+n_jobs_per_resource[7]+n_jobs_per_resource[8]+n_jobs_per_resource[9]+n_jobs_per_resource[10]+n_jobs_per_resource[11]);
set of int: resource12 = (1+n_jobs_per_resource[1]+n_jobs_per_resource[2]+n_jobs_per_resource[3]+n_jobs_per_resource[4]+n_jobs_per_resource[5]+n_jobs_per_resource[6]+n_jobs_per_resource[7]+n_jobs_per_resource[8]+n_jobs_per_resource[9]+n_jobs_per_resource[10]+n_jobs_per_resource[11])..(n_jobs_per_resource[1]+n_jobs_per_resource[2]+n_jobs_per_resource[3]+n_jobs_per_resource[4]+n_jobs_per_resource[5]+n_jobs_per_resource[6]+n_jobs_per_resource[7]+n_jobs_per_resource[8]+n_jobs_per_resource[9]+n_jobs_per_resource[10]+n_jobs_per_resource[11]+n_jobs_per_resource[12]);
set of int: resource13 = (1+n_jobs_per_resource[1]+n_jobs_per_resource[2]+n_jobs_per_resource[3]+n_jobs_per_resource[4]+n_jobs_per_resource[5]+n_jobs_per_resource[6]+n_jobs_per_resource[7]+n_jobs_per_resource[8]+n_jobs_per_resource[9]+n_jobs_per_resource[10]+n_jobs_per_resource[11]+n_jobs_per_resource[12])..(n_jobs_per_resource[1]+n_jobs_per_resource[2]+n_jobs_per_resource[3]+n_jobs_per_resource[4]+n_jobs_per_resource[5]+n_jobs_per_resource[6]+n_jobs_per_resource[7]+n_jobs_per_resource[8]+n_jobs_per_resource[9]+n_jobs_per_resource[10]+n_jobs_per_resource[11]+n_jobs_per_resource[12]+n_jobs_per_resource[13]);
set of int: resource14 = (1+n_jobs_per_resource[1]+n_jobs_per_resource[2]+n_jobs_per_resource[3]+n_jobs_per_resource[4]+n_jobs_per_resource[5]+n_jobs_per_resource[6]+n_jobs_per_resource[7]+n_jobs_per_resource[8]+n_jobs_per_resource[9]+n_jobs_per_resource[10]+n_jobs_per_resource[11]+n_jobs_per_resource[12]+n_jobs_per_resource[13])..(n_jobs_per_resource[1]+n_jobs_per_resource[2]+n_jobs_per_resource[3]+n_jobs_per_resource[4]+n_jobs_per_resource[5]+n_jobs_per_resource[6]+n_jobs_per_resource[7]+n_jobs_per_resource[8]+n_jobs_per_resource[9]+n_jobs_per_resource[10]+n_jobs_per_resource[11]+n_jobs_per_resource[12]+n_jobs_per_resource[13]+n_jobs_per_resource[14]);
set of int: resource15 = (1+n_jobs_per_resource[1]+n_jobs_per_resource[2]+n_jobs_per_resource[3]+n_jobs_per_resource[4]+n_jobs_per_resource[5]+n_jobs_per_resource[6]+n_jobs_per_resource[7]+n_jobs_per_resource[8]+n_jobs_per_resource[9]+n_jobs_per_resource[10]+n_jobs_per_resource[11]+n_jobs_per_resource[12]+n_jobs_per_resource[13]+n_jobs_per_resource[14])..(n_jobs_per_resource[1]+n_jobs_per_resource[2]+n_jobs_per_resource[3]+n_jobs_per_resource[4]+n_jobs_per_resource[5]+n_jobs_per_resource[6]+n_jobs_per_resource[7]+n_jobs_per_resource[8]+n_jobs_per_resource[9]+n_jobs_per_resource[10]+n_jobs_per_resource[11]+n_jobs_per_resource[12]+n_jobs_per_resource[13]+n_jobs_per_resource[14]+n_jobs_per_resource[15]);
set of int: resource16 = (1+n_jobs_per_resource[1]+n_jobs_per_resource[2]+n_jobs_per_resource[3]+n_jobs_per_resource[4]+n_jobs_per_resource[5]+n_jobs_per_resource[6]+n_jobs_per_resource[7]+n_jobs_per_resource[8]+n_jobs_per_resource[9]+n_jobs_per_resource[10]+n_jobs_per_resource[11]+n_jobs_per_resource[12]+n_jobs_per_resource[13]+n_jobs_per_resource[14]+n_jobs_per_resource[15])..(n_jobs_per_resource[1]+n_jobs_per_resource[2]+n_jobs_per_resource[3]+n_jobs_per_resource[4]+n_jobs_per_resource[5]+n_jobs_per_resource[6]+n_jobs_per_resource[7]+n_jobs_per_resource[8]+n_jobs_per_resource[9]+n_jobs_per_resource[10]+n_jobs_per_resource[11]+n_jobs_per_resource[12]+n_jobs_per_resource[13]+n_jobs_per_resource[14]+n_jobs_per_resource[15]+n_jobs_per_resource[16]);
set of int: resource17 = (1+n_jobs_per_resource[1]+n_jobs_per_resource[2]+n_jobs_per_resource[3]+n_jobs_per_resource[4]+n_jobs_per_resource[5]+n_jobs_per_resource[6]+n_jobs_per_resource[7]+n_jobs_per_resource[8]+n_jobs_per_resource[9]+n_jobs_per_resource[10]+n_jobs_per_resource[11]+n_jobs_per_resource[12]+n_jobs_per_resource[13]+n_jobs_per_resource[14]+n_jobs_per_resource[15]+n_jobs_per_resource[16])..(n_jobs_per_resource[1]+n_jobs_per_resource[2]+n_jobs_per_resource[3]+n_jobs_per_resource[4]+n_jobs_per_resource[5]+n_jobs_per_resource[6]+n_jobs_per_resource[7]+n_jobs_per_resource[8]+n_jobs_per_resource[9]+n_jobs_per_resource[10]+n_jobs_per_resource[11]+n_jobs_per_resource[12]+n_jobs_per_resource[13]+n_jobs_per_resource[14]+n_jobs_per_resource[15]+n_jobs_per_resource[16]+n_jobs_per_resource[17]);

% disjunctive on resources
constraint cumulative(starts[resource1], dur[resource1], resource_consumption[resource1], 1);
constraint cumulative(starts[resource2], dur[resource2], resource_consumption[resource2], 1);
constraint cumulative(starts[resource3], dur[resource3], resource_consumption[resource3], 1);
constraint cumulative(starts[resource4], dur[resource4], resource_consumption[resource4], 1);
constraint cumulative(starts[resource5], dur[resource5], resource_consumption[resource5], 1);
constraint cumulative(starts[resource6], dur[resource6], resource_consumption[resource6], 1);
constraint cumulative(starts[resource7], dur[resource7], resource_consumption[resource7], 1);
constraint cumulative(starts[resource8], dur[resource8], resource_consumption[resource8], 1);
constraint cumulative(starts[resource9], dur[resource9], resource_consumption[resource9], 1);
constraint cumulative(starts[resource10], dur[resource10], resource_consumption[resource10], 1);
constraint cumulative(starts[resource11], dur[resource11], resource_consumption[resource11], 1);
constraint cumulative(starts[resource12], dur[resource12], resource_consumption[resource12], 1);
constraint cumulative(starts[resource13], dur[resource13], resource_consumption[resource13], 1);
constraint cumulative(starts[resource14], dur[resource14], resource_consumption[resource14], 1);
constraint cumulative(starts[resource15], dur[resource15], resource_consumption[resource15], 1);
constraint cumulative(starts[resource16], dur[resource16], resource_consumption[resource16], 1);
constraint cumulative(starts[resource17], dur[resource17], resource_consumption[resource17], 1);

constraint makespan = max(i in jobs) (starts[i]+duration[i]);

solve minimize makespan;