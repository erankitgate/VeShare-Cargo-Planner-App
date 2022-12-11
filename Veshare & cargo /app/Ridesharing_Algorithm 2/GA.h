#ifndef GA_H
#define GA_H

#include<bits/stdc++.h>
#include "point.h"
#include "input.h"
using namespace std;

class GA {
	int totalNodes;		//number of nodes
public:
	long long int driver_tolerance;
	long long int tmax;	//time limit or budget
	vector<point> vecp;
	vector<point> usefulPoints;
	vector<vector<long long int> > d;

	//extra
	float bestScoreFound;
	vector<point> bestTourFound;

	//GA parameters
	int points_considered;
	int local_search_count;
	int initialCount;
	
	//extra GA parameters copied from orienteering with time windows
	int selection_parameter_k;
	int selection_parameter_tsize;

	//--functions starts here--
	GA(long long int tmax, int totalnodes, std::vector<point> v,
			vector<vector<long long int> > d, long long int dtol);//constructor - initializes parameters

	void controlFunction();	//control function from where everthing is called and main algorithm implemented

	//methods--helper functions
	void printTour(vector<point> tour, string str,bool flag);	//print tour in a proper format
	float calculateTourProfit(vector<point> tour);//calculates tour profit i.e. scores collected by the tour
	void copyTour(vector<point> source, vector<point> &destination);//function for tour copy
	void selection_sort(vector<point> &tour);//function for sorting points based on point heuristic
	string timeStampToHReadble(const long long int epoch_time);	//time converter
	void sort_customer_optimization(vector<pair<point, point> > &possible_customers); //sorting for optimization of which customer to pick
	float customerPickUpHeuristic(pair<point, point> customer);


	void sortTours(vector<vector<point> > &tour);
	int calculatePickupPoints(vector<point> tour);
	
	
	
	//copied from orienteering with time windows GA.cpp
	void selection_sort_tours(vector<vector<point> > &tours);//function for sorting tours based on fitness value
	
	

	//tour generator
	vector<point> greedyTour(bool flag_full_greedyTour);//function for generating 1 greedy tour based on probability

	//functions for generating tour
	vector<vector<point> > reducePopulationSize(
			vector<vector<point> > initialTours, int reqSize);
	vector<vector<point> > greedyProbTour(int number_of_tours_tobe_generated);//generates set of greedy tours using greedyTour() function
    vector<vector<point> > performCrossover(vector<vector<point> > initialTours);//crossover function
	vector<vector<point> > crossoverFunction(vector<point> p1, vector<point> p2);
	vector<point> mutateAndPerformlocalSearch(vector<point> tour);

	long long int isInsertionFeasible(vector<point> tour, int i, int k,
			point j);//checks whether insertion of point in the tour is feasible or not
	vector<point> selectBestPoint(vector<point> tour, vector<int> visited,
			int index_i, int index_k);//selects best points based on feasibility and pointHeuristic()
	void insertPoint(vector<point> &tour, int index_i, int index_k, point j);//inserts the point into the tour
	vector<point> isDestFeasible(vector<point> tour, point source);	//destination feasiblity
	void stuffToFull(vector<point> &tour);
	void removePoints(vector<point> &tour, int start, int end);
};

#endif
