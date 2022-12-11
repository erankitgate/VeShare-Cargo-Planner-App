#include<bits/stdc++.h>

using namespace std;

#include <time.h>
#include <sys/time.h>
#include "input.h"
#include "input.cpp"
#include "point.h"
#include "GA.cpp"
#include "GA.h"
#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>

#include <omp.h>
#include "Filter.cpp"
//#include "parameters_getter.cpp"

typedef unsigned long long int ulli;
typedef long long int lli;
typedef pair<int, int> pii;
typedef unsigned int ui;
typedef vector<vector<int> > vvi;
typedef vector<int> vi;

#define mod 1000000007
#define pb(x) push_back(x)
#define pp pop_back()
#define mp make_pair
#define tr(c,it) for(typeof((c).begin()) it = (c).begin(); it != (c).end(); it++)
#define all(a) a.begin(),a.end()
#define asd(x)              cout<<__LINE__<<" :: "<<#x<< ": "<<x<<endl;
#define asdf(x, y)          cout<<__LINE__<<" :: "<<#x<< ": "<<x<<" | "<<#y<< ": "<<y<<endl;
#define print1DVector(w) for (unsigned int pvi = 0; pvi < w.size(); ++pvi){cout<<w[pvi]<<" ";}cout<<endl;
#define print2DVector(w) for (unsigned int pvi = 0; pvi < w.size(); ++pvi){for (unsigned int pvj = 0; pvj < w[pvi].size(); ++pvj)cout<<w[pvi][pvj]<<" ";cout<<endl;}

//int max_index=max_element(data.begin(),data.end())-data.begin();
//vector< vector<int> > vec2D(ROW,vector<int> (COL,0));
//vector< vector<int> > vec2D;      vec2D=vector< vector<int> > (ROW,vector<int> (COL,0));
//vector<int> vec1D; vec1D= vector<int> (SIZE,0);

//function to support running GA on a single test file..!
void runGA(string inputFileStr, string distanceMatrixFile) {


	cout << "runGA dist File path = " << distanceMatrixFile << endl;

	Input inputObj(inputFileStr.c_str(), distanceMatrixFile.c_str());
	inputObj.getDataFromFile();

	inputObj.computeDistance();

	//inputObj.printAllPoints();

	int totalNodes = inputObj.totalNodes;
	vector<point> vecp = inputObj.vecp;
	vector<vector<long long int> > d = inputObj.d;
	long long int dr_tolerance = inputObj.driver_tolerance;

//	asd(inputObj.tmax);
//	asd(totalNodes);
//	asd(dr_tolerance);

	for (unsigned i = 0; i < 1; ++i) {
		GA ga(inputObj.tmax, totalNodes, vecp, d, dr_tolerance);			//entry to GA.cpp
		ga.controlFunction();
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
}

void deserializeTourNewOP(string filename) {
	//deserialization

	ifstream ifs(filename.c_str(), ios::binary);
	int size = 0;
	vector<point> retTour;
	ifs.read((char *) &size, sizeof(size));
	while (ifs) {
		//cout<<"SIze found:"<<size<<endl;
		retTour.clear();
		while (size--) {
			point p;
			ifs.read((char *) &p, sizeof(p));
			retTour.push_back(p);
		}
		cout << "\n\n---PRinting tour---\n";
		for (unsigned int i = 0; i < retTour.size(); i++) {
			cout << retTour[i].printPoint(true) << endl;
		}

		ostringstream os;
		os << "http://map.project-osrm.org/?z=12&center=40.701164,-73.884913";
		for (unsigned int i = 0; i < retTour.size(); i++) {
			os << "&loc=" << retTour[i].x << "," << retTour[i].y;
		}
		os << "&hl=en&ly=&alt=&df=&srv=";
		cout << os.str() << endl;

		ifs.read((char *) &size, sizeof(size));
	}

	ifs.close();
}

void process_step1() {
	parameters_getter pg;
	string baseURL = pg.fetchParameter("baseurl");

	cout << "base URL is " << baseURL << endl;

	//read details about drivers
	ifstream inputFile;
	inputFile.open(string(baseURL + "all_ids.txt").c_str());
	//cout << string(baseURL + "all_ids.txt") << endl;
	
	string str;
	vector<int> driverList;
	while (getline(inputFile, str)) {
		if (!str.empty()) {
			istringstream is(str);
			long long int driverId;
			is >> driverId;
			cout << "driver id is " << driverId << endl;
			driverList.push_back(driverId);
		}
		else
			cout << "string is empty" << endl;
	}
	inputFile.close();
	inputFile.clear();
	//cout << "status after closing: " << inputFile.is_open() << endl;

	double starttime = omp_get_wtime();

	struct stat st = { 0 };
	string dirLoc = baseURL + "filtered_requests_folder";
	if (stat(dirLoc.c_str(), &st) == -1) {
		mkdir(dirLoc.c_str(), 0700);
	}

	unsigned int n;
	n = driverList.size();
	Filter f;
	//n = 10;
	//for each driver generate required files
//#pragma omp parallel for
	for (unsigned int i = 0; i < n; i++) {

		double local_starttime = omp_get_wtime();
		ostringstream os;
		cout << "Running : " << i + 1 << " : " << driverList[i] << endl;

		ostringstream rawFilePath;
		rawFilePath << baseURL << "unfiltered_data/" << "requests"
				<< driverList[i] << ".txt";
				
		//rawFilePath << baseURL << "unfiltered_data/" << "requests"
				//<< driverList[i];
				
		//cout << "rawFilePath path: " << rawFilePath.str() << endl;

		ostringstream filteredFilePath;
		filteredFilePath << baseURL << "filtered_requests_folder/" << "requests"
				<< driverList[i] << ".txt";
		
		//cout << "filteredFilePath path: " << filteredFilePath.str() << endl;
		
		std::ifstream filteredFile(filteredFilePath.str());
		
		if (filteredFile
				&& filteredFile.peek() != std::ifstream::traits_type::eof()) {
				cout << "filteredFile true " << endl;
			filteredFile.close();
		} else {
		cout << "here " << endl;
		filteredFile.close(); filteredFile.clear();
			f.processRawData(rawFilePath.str(), filteredFilePath.str(),
					driverList[i], 2);
			
		}
		double local_endtime = omp_get_wtime();
		cout << driverList[i] << " : Total Time taken for driver:"
				<< local_endtime - local_starttime << endl;
	}

	double endtime = omp_get_wtime();

	cout << "Total Time taken for step1:" << endtime - starttime << endl;
	cout << "\n--DONE Step1--\n";
}

void process_step2() {
	//next step to generate final data
	ostringstream cmd;
	Filter f;								//entry to filter.cpp
	f.finalData_distMat_req_generation();
	cout << "\n--DONE step2--\n";
}

void process_step3() {
	parameters_getter pg;
	string baseURL = pg.fetchParameter("baseurl");

	string driverPerStr = pg.fetchParameter("driver_percentage");
	istringstream istemp(driverPerStr);
	int driver_percentage;
	istemp >> driver_percentage;

	string tmax_multiplier_str = pg.fetchParameter("tmax_multiplier");

	string finalDataURL = baseURL + "final_data_" + string(driverPerStr)
			+ "_tmax_" + string(tmax_multiplier_str) + "/";

	//read details about drivers
	ifstream inputFile;
	inputFile.open(string(finalDataURL + "drivers_list.txt").c_str());
	string str;
	vector<int> driverList;
	while (getline(inputFile, str)) {
		if (!str.empty()) {
			istringstream is(str);
			long long int driverId;
			is >> driverId;
			driverList.push_back(driverId);
		}
	}
	inputFile.close();

	//empty the file
	ofstream ofs(baseURL + "serial.txt", ios::binary);
	ofs.close();

	double starttime = omp_get_wtime();

	cout << "start" << endl;
	//for each driver generate required files
	//#pragma omp parallel for

	unsigned int n;
	n = driverList.size();
	//n = 1;
	
	
	
	
	for(int l=0;l<n;l++)
	{
		cout << "driver ==== " << driverList[l] << endl;
	}
	
	
	
	
	

#pragma omp parallel for
	for (int i = 0; i < n; i++) {
	
		ostringstream os;
		cout << "Running : " << i + 1 << " : " << finalDataURL << "requests/"
				<< "final_requests" << driverList[i] << ".txt" << endl;

		ostringstream reqFilePath;

		reqFilePath << finalDataURL << "requests/" << "final_requests"
				<< driverList[i] << ".txt";
		ostringstream distFilePath;
		distFilePath << finalDataURL << "distance_matrix/" << "distance_matrix"
				<< driverList[i] << ".txt";
		runGA(reqFilePath.str(), distFilePath.str());
		
							//-------Entry to GA.cpp
	}

	double endtime = omp_get_wtime();

	cout << "Total Time taken:" << endtime - starttime << endl;

	//*****
	//copy of profit and parameters file
	std::ifstream src1(baseURL + "profits.txt", std::ios::binary);
	std::ofstream dst1(finalDataURL + +"profits.txt", std::ios::binary);
	dst1 << src1.rdbuf();

	std::ifstream src2("./ridesharing_parameters.txt", std::ios::binary);
	std::ofstream dst2(finalDataURL + +"ridesharing_parameters.txt",
			std::ios::binary);
	dst2 << src2.rdbuf();

	std::ifstream src3(baseURL + "serial.txt", std::ios::binary);
	std::ofstream dst3(finalDataURL + +"serial.txt", std::ios::binary);
	dst3 << src3.rdbuf();

	//****

	//deserializeTourNewOP(serialFilePath);
	cout << "\n--DONE step3-Running Algorithm--\n";
}

int main(int argv, char **argc) {

	if (argv != 2) {
		cout << "Enter proper arguments to the call.\n";
		return 1;
	}

	string option(argc[1]);
	//step1 = filter raw data and generate new requests files
	//step2 = remove drivers from data and generate final files
	//step3 = run algorithm on given data

	if (option == "step1") {
		process_step1();	//use unfiltered data and generate filtered data
	} else if (option == "step2") {
		process_step2();//generate final files according to driver percentage and remove drivers from data
	} else if (option == "step3") {
		process_step3();	//run final algorithm on data generated using step2
	}

	return 0;
}
