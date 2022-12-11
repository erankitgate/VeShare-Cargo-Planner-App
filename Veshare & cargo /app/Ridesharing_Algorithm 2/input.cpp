#include<bits/stdc++.h>
#include<iostream>
#include <omp.h>
#include "osrm_interface.h"
#include "parameters_getter.cpp"
#include <dirent.h>

using namespace std;

typedef unsigned long long int ulli;
typedef long long int lli;
typedef pair<int, int> pii;
typedef unsigned int ui;
typedef vector<vector<int> > vvi;
typedef vector<int> vi;

#define mod 1000000007
#define pb(x) push_back(x)
#define pp pop_back()
//#define mp make_pairf
#define tr(c,it) for(typeof((c).begin()) it = (c).begin(); it != (c).end(); it++)
//#define all(a) a.begin(),a.end()
#define asd(x)              cout<<__LINE__<<" :: "<<#x<< ": "<<x<<endl;
#define asdf(x, y)          cout<<__LINE__<<" :: "<<#x<< ": "<<x<<" | "<<#y<< ": "<<y<<endl;
#define print1DVector(w) for (unsigned int pvi = 0; pvi < w.size(); ++pvi){cout<<w[pvi]<<" ";}cout<<endl;
#define print2DVector(w) for (unsigned int pvi = 0; pvi < w.size(); ++pvi){for (unsigned int pvj = 0; pvj < w[pvi].size(); ++pvj)cout<<w[pvi][pvj]<<" ";cout<<endl;}

//int max_index=max_element(data.begin(),data.end())-data.begin();
//vector< vector<int> > vec2D(ROW,vector<int> (COL,0));
//vector< vector<int> > vec2D;      vec2D=vector< vector<int> > (ROW,vector<int> (COL,0));
//vector<int> vec1D; vec1D= vector<int> (SIZE,0);

//----------------

#include "input.h"

Input::Input(string reqFile, string distFile) {
	totalNodes = 0;
	driver_tolerance = 0;
	time_for_customer_pickup = 10;
	tmax = 0;
	
	

//code block to check what all files are present in the
/*
cout << "**************" << endl;
DIR *dir; struct dirent *diread;
    vector<char *> files;

    if ((dir = opendir("project/day1_20_21/unfiltered_data")) != nullptr) {
        while ((diread = readdir(dir)) != nullptr) {
            files.push_back(diread->d_name);
        }
        closedir (dir);
    } else {
        perror ("opendir");
        //return EXIT_FAILURE;
    }

    for (auto file : files) cout << file << "|";
    cout << endl;
cout << "**************" << endl;*/
	
	cout << "reqFile is : " << reqFile << endl;
	inputFile.open(reqFile.c_str());
	//inputFile.open(string("TestData/test1").c_str());
	if(inputFile.fail())	{
		cout << "error: " << strerror(errno) << endl;
		throw std::runtime_error("..");
	}
	else
		cout << "Successfully opened!!" <<endl;
	distMatrixFile.open(distFile.c_str());
	cout << "inputFile status: " << inputFile.is_open() << endl;
}

Input::~Input() {
	inputFile.close();
	distMatrixFile.close();
}

void Input::getDataFromFile() {
	string line1, line2;
	//cout << "in input.cpp   inputfile status : " << inputFile.is_open() << endl;
	getline(inputFile, line1);	//taking line1 and parsing
	istringstream is(line1);
	float tmax_float;
	is >> tmax_float >> driver_tolerance;
	tmax = (long long int) tmax_float;
	
	cout << "tmax in input.cpp is " << tmax << endl;
	
	//******Set parameters from parameter file*******
	parameters_getter pg;
	//set driver tolerance
	string driverTolStr = pg.fetchParameter("driver_tolerance");
	istringstream is1(driverTolStr);
	is1 >> driver_tolerance;

	float tmaxMultiplier;
	string tmaxStr = pg.fetchParameter("tmax_multiplier");
	istringstream is2(tmaxStr);
	is2 >> tmaxMultiplier;
	//cout << "Tmax before:" << tmax << endl;
	tmax = (tmax / 2.0) * tmaxMultiplier;
	//cout << "Tmax After:" << tmax << endl;

	//set customer tolerance
	long long int custTolerance;
	string custToleranceStr = pg.fetchParameter("customer_tolerance");
	istringstream is3(custToleranceStr);
	is3 >> custTolerance;

	//cout<<"tmax:"<<tmax<< " dr:"<<driver_tolerance<<endl;

	//cout<<"totalNodes:"<<totalNodes<<endl;
	//-------parsing source vertex--------
	string str;
	int count_vertex = 0;
	if (getline(inputFile, str)) {
		if (!str.empty()) {
			istringstream is(str);
			float sx, sy, dx, dy, profit;
			long long int starttime, driver_id = 0;

			is >> driver_id >> sx >> sy >> dx >> dy >> starttime >> profit;
			//is >> sx >> sy >> dx >> dy >> starttime;

			point p1 = { sx, sy, time_for_customer_pickup, profit, starttime,
					starttime + tmax, 1, count_vertex, count_vertex + 1, -1,
					starttime, 0, 0, 1, 0, -1, -1, 0, 0, 0, 0, driver_id };
			point p2 = { dx, dy, time_for_customer_pickup, profit, starttime,
					starttime + tmax, 0, count_vertex + 1, count_vertex, -1, 0,
					0, 0, 1, 0, -1, -1, 0, 0, 0, 0, driver_id };
			count_vertex += 2;

			vecp.push_back(p1);
			vecp.push_back(p2);
		}
	}
	//parsing other vertices
	while (getline(inputFile, str)) {
		if (!str.empty()) {
			istringstream is(str);
			float sx, sy, dx, dy, profit;
			long long int ot, ct, customer_id = 0;

			is >> customer_id >> sx >> sy >> dx >> dy >> profit >> ot >> ct;
			//is >> sx >> sy >> dx >> dy >> profit >> ot >> ct;

			//please set para carefully - vt yet to set
			point p1 = { sx, sy, time_for_customer_pickup, profit, ot
					- custTolerance / 2, ot + custTolerance / 2, 1,
					count_vertex, count_vertex + 1, -1, 0, 0, 0, 1, 0, -1, -1,
					0, 0, 0, 0, customer_id };
			point p2 = { dx, dy, time_for_customer_pickup, 0, ot,
					vecp[0].start_service_time + tmax, 0, count_vertex + 1,
					count_vertex, -1, 0, 0, 0, 1, 0, -1, -1, 0, 0, 0, 0,
					customer_id };
			count_vertex += 2;
			vecp.push_back(p1);
			vecp.push_back(p2);
		}
	}
	totalNodes = count_vertex;
}

void Input::printAllPoints() {
	//printing the points
	for (ui i = 0; i < vecp.size(); ++i) {
		cout << vecp[i].printPoint(false) << endl;
	}
}

void Input::computeDistance() {

	totalNodes++;	//line changed for OPTW

	d = vector<vector<long long int> >(totalNodes,
			vector<long long int>(totalNodes, 0));

	for (vector<point>::iterator it = vecp.begin(); it != vecp.end(); it++) {
		string str;
		getline(distMatrixFile, str);
		istringstream is(str);
		for (vector<point>::iterator it2 = vecp.begin(); it2 != vecp.end();
				it2++) {
			long long int tempDist;
			is >> tempDist;
			tempDist /= 10;
			d[it - vecp.begin()][it2 - vecp.begin()] = tempDist;
		}
	}
}

