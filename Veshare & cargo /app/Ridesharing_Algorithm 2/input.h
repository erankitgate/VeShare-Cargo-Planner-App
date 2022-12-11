#ifndef INPUT_H
#define INPUT_H

#include<bits/stdc++.h>
#include "point.h"
//#include "parameters_getter.cpp"

using namespace std;

class Input {
//Input::Input(string reqFile, string distFile);

public:
	long long int tmax;	//time limit or budget
	long long int driver_tolerance;	//driver tolerance to wait at customer source location in seconds
	int time_for_customer_pickup;	//time_for_customer_pickup in seconds
	int totalNodes;		//number of nodes
	vector<point> vecp;
	vector<vector<long long int> > d;
	Input(string reqFile,string distFile);
	~Input();
	ifstream inputFile;
	ifstream distMatrixFile;

	void getDataFromFile();		//takes input from file
	void computeDistance();		//computes distance matrix for points
	void printAllPoints();		//prints all points on terminal
};

#endif
