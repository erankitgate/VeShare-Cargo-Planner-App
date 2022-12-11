/*	Author : Rohit Suryanarayan
	IIT Madras
*/



#include<bits/stdc++.h>
#include<iostream>
//#include "osrm_interface.h"
#include <omp.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>
#include "input.h"
#include "point.h"
//#include "parameters_getter.cpp"
#include "osrm_interface.cpp"
//#include "new_osrm_interface.cpp"
using namespace std;

class Filter {
	//OSRM_INTERFACE *osrmObj;

public:
	Filter() {
		parameters_getter pg;
		//string osrmURL = pg.fetchParameter("osrm_map_url");
		//osrmObj = new OSRM_INTERFACE(osrmURL);
	}

	void processRawData(string rawFilePath, string FilteredFilePath,
			long long int driverID, float tmax_multiplier) {

		Input inputObj(rawFilePath, "");
		cout << "************In processrawdata() of filter.cpp****************" << endl;
		inputObj.getDataFromFile();
		vector<point> vecp = inputObj.vecp;
		cout << "size of vecp is " << vecp.size() << endl;
		inputObj.inputFile.close();

		//*********Find Tmax value*****
		vector<pair<float, float> > locations; //pair - first=latitude and second=longitude
		locations.push_back(make_pair(vecp[0].x, vecp[0].y));
		locations.push_back(make_pair(vecp[1].x, vecp[1].y));

		/*
		 ostringstream os1;
		 os1
		 << "http://map.project-osrm.org/?z=12&center=40.728267%2C-73.978844";
		 for (unsigned int j = 0; j < locations.size(); j++) {
		 os1 << "&loc=" << locations[j].first << "," << locations[j].second;
		 }
		 os1 << "&hl=en&alt=0";
		 cout << os1.str() << endl;
		 */

		//long long int tmax = (*osrmObj).viarouteCall(locations);
		
		long long int tmax = viarouteCall(locations);
		cout << "tmax from routecall got is " << tmax << endl;			//call to osrm_interface.cpp
		tmax *= tmax_multiplier;
		//************

		//cout << "Old size:" << vecp.size() << endl;
		vector<point> newVecp;
		newVecp.push_back(vecp[0]);
		newVecp.push_back(vecp[1]);

		cout << "vecp size:" << vecp.size() << endl;
//#pragma omp parallel for
		for (unsigned int i = 2; i < vecp.size() - 1; i += 2) {
			cout << "i:" << i << endl;
			vector<pair<float, float> > locations; //pair - first=latitude and second=longitude
			locations.push_back(make_pair(vecp[0].x, vecp[0].y));
			locations.push_back(make_pair(vecp[i].x, vecp[i].y));
			locations.push_back(make_pair(vecp[i + 1].x, vecp[i + 1].y));
			locations.push_back(make_pair(vecp[1].x, vecp[1].y));

			ostringstream os;
			os << fixed << setprecision(8);
			os
					<< "http://map.project-osrm.org/?z=12&center=40.728267%2C-73.978844";
			for (unsigned int j = 0; j < locations.size(); j++) {
				os << "&loc=" << locations[j].first << ","
						<< locations[j].second;
			}
			os << "&hl=en&alt=0";
			cout << os.str() << endl;

			ostringstream bugfix;
			bugfix << fixed << setprecision(8);
			bugfix << "params.coordinates.push_back({util::FloatLongitude("
					<< vecp[0].y << "), util::FloatLatitude(" << vecp[0].x
					<< ")});" << endl;
			bugfix << "params.coordinates.push_back({util::FloatLongitude("
					<< vecp[i].y << "), util::FloatLatitude(" << vecp[i].x
					<< ")});" << endl;
			bugfix << "params.coordinates.push_back({util::FloatLongitude("
					<< vecp[i + 1].y << "), util::FloatLatitude("
					<< vecp[i + 1].x << ")});" << endl;
			bugfix << "params.coordinates.push_back({util::FloatLongitude("
					<< vecp[1].y << "), util::FloatLatitude(" << vecp[1].x
					<< ")});" << endl;
			cout << bugfix.str() << endl;

			//float timetaken = (*osrmObj).viarouteCall(locations);
			
			
			
			float timetaken = viarouteCall(locations);		//call to viaroutecall() of osrm_interface.cpp
			cout << "time:" << timetaken << " Tmax:" << tmax << endl;

			if (timetaken <= tmax) {
//#pragma omp critical
				{
					newVecp.push_back(vecp[i]);
					newVecp.push_back(vecp[i + 1]);
				}
			}
		}

		cout << "New size:" << newVecp.size() << endl;

		//write filtered requests to the file
		//according to file format
		ostringstream toReqFile;

		toReqFile << fixed << setprecision(8);
		toReqFile << tmax << " " << inputObj.driver_tolerance << endl;
		toReqFile << newVecp[0].ID << " " << newVecp[0].x << " " << newVecp[0].y
				<< " " << newVecp[1].x << " " << newVecp[1].y << " "
				<< newVecp[0].start_service_time << " " << newVecp[0].w << endl;
		for (unsigned int i = 2; i < newVecp.size(); i += 2) {
			toReqFile << newVecp[i].ID << " " << newVecp[i].x << " "
					<< newVecp[i].y << " " << newVecp[i + 1].x << " "
					<< newVecp[i + 1].y << " " << newVecp[i].w << " "
					<< newVecp[i].ot << " " << newVecp[i].ct << endl;
		}

		//cout<<toReqFile.str();

		//cout << "p:" << reqFilePath.str() << endl;
		ofstream reqFile;
		reqFile.open(FilteredFilePath);
		reqFile << toReqFile.str();
		reqFile.close();

	}

	void finalData_distMat_req_generation() {
		double starttime = omp_get_wtime();

		parameters_getter pg;
		string baseURL = pg.fetchParameter("baseurl");
		string driverPerStr = pg.fetchParameter("driver_percentage");
		istringstream istemp(driverPerStr);
		int driver_percentage;
		istemp >> driver_percentage;

		//get the tmax_multiplier from the file
		string tmax_multiplier_str = pg.fetchParameter("tmax_multiplier");

		//read details about drivers
		ifstream idListFile;
		idListFile.open(string(baseURL + "all_ids.txt").c_str());
		string str;
		vector<int> idList;
		while (getline(idListFile, str)) {
			if (!str.empty()) {
				istringstream is(str);
				long long int driverId;
				is >> driverId;
				idList.push_back(driverId);
			}
		}
		idListFile.close();
		
		
		cout << "$$$$$$$$$$$$$$$$$$$$ driver list size is " << idList.size() << "$$$$$$$$$$$$$$$$$$$$$$$$$" << endl;
		
		//read details about customers
		ifstream cList;
		cList.open(string(baseURL + "cust_ids.txt").c_str());
		map<long long int, int> custMap;
		while (getline(cList, str)) 
		{
			if (!str.empty()) 
			{
				istringstream is(str);
				long long int custId;
				is >> custId;
				//custMap.push_back(custId);
				custMap[custId] = 1;
			}
		}
		cList.close();

		srand((unsigned) time(0));
		vector<long long int> driverList;
		//unordered_map<long long int, int> custMap;
		for (unsigned int i = 0; i < idList.size(); ++i) {
			int rNum = rand() % 100 + 1;
			driverList.push_back(idList[i]);
			/*if (rNum <= driver_percentage) {
				driverList.push_back(idList[i]);
			} else {
				custMap[idList[i]] = 1;
			}*/
		}
		
		//cout << "$$$$$$$$$$$$$$$$$$$$4 driverList size is " << driverList.size() << " and custMap size is " << custMap.size() << endl;
		
		
		string finalDataURL = baseURL + "final_data_" + string(driverPerStr)
				+ "_tmax_" + string(tmax_multiplier_str) + "/";

		struct stat st = { 0 };
		string dirLoc = finalDataURL;
		if (stat(dirLoc.c_str(), &st) == -1) {
			mkdir(dirLoc.c_str(), 0700);
		}
		dirLoc = finalDataURL + "requests";
		if (stat(dirLoc.c_str(), &st) == -1) {
			mkdir(dirLoc.c_str(), 0700);
		}
		dirLoc = finalDataURL + "distance_matrix";
		if (stat(dirLoc.c_str(), &st) == -1) {
			mkdir(dirLoc.c_str(), 0700);
		}

		ostringstream outFileName;
		outFileName << finalDataURL << "drivers_list.txt";
		ofstream outputFile;
		outputFile.open(outFileName.str().c_str());
		//cout << "++++++++++++++++++++++++++++++++++++++++++ driverList size is " << driverList.size() << endl;
		for (unsigned int i = 0; i < driverList.size(); ++i) {
			outputFile << driverList[i] << endl;
		}
		outputFile.close();

		cout << "DriverCount:" << driverList.size() << " CustomerCount:"
				<< custMap.size() << endl;

		unsigned int noPickUpPossibleCount = 0;
		
		//#pragma omp parallel for
		
		for (unsigned int i = 0; i < driverList.size(); ++i) {
			double localstarttime = omp_get_wtime();

			/*********first remove drivers from the requests file************/
			ostringstream reqFileName;
			reqFileName << baseURL << "filtered_requests_folder/" << "requests"
					<< driverList[i] << ".txt";
			ostringstream outFileName;
			outFileName << finalDataURL << "requests/" << "final_requests"
					<< driverList[i] << ".txt";

			long long int tmax, driver_tol;

			ifstream inputFile;
			ofstream outputFile;
			inputFile.open(reqFileName.str().c_str());
			outputFile.open(outFileName.str().c_str());
			string line;
			getline(inputFile, line);	//get the first line
			istringstream is(line);
			is >> tmax >> driver_tol;
			float tmaxMultiplier;
			string tmaxStr = pg.fetchParameter("tmax_multiplier");
			istringstream is2(tmaxStr);
			is2 >> tmaxMultiplier;
			//cout << "Tmax before:" << tmax << endl;
			//tmax = (tmax / 2.0) * tmaxMultiplier;
			//cout << "Tmax after:" << tmax << endl;

			outputFile << tmax << " " << driver_tol << endl;

			getline(inputFile, line);	//add the second line - driver's details
			
			outputFile << line << endl;
			
			int lineCount = 0;
			std::unordered_map<long long int, int>::iterator it;
			while (getline(inputFile, line)) {
				if (!line.empty()) {
					lineCount++;
					istringstream is(line);
					long long int custID;
					is >> custID;
					if (custID == driverList[i])//if driver is there in the requests file then no need to consider
						continue;
					//it = custMap.find(custID);
					if (custMap.find(custID) != custMap.end()) {//that means ID is of customer, then add it to database
						outputFile << line << endl;
						//std::cout << it->first << " is " << it->second << endl;
					}
				}
			}
			inputFile.close();
			outputFile.close();

			/*********generate distance matrix************/
			ostringstream os;
			os << finalDataURL << "requests/" << "final_requests"
					<< driverList[i] << ".txt";

			/*
			 ostringstream ostemp;
			 ostemp << baseURL << "unfiltered_data/" << "requests3133.txt";
			 Input inputObj(ostemp.str(), "");
			 */

			Input inputObj(os.str(), "");
			inputObj.getDataFromFile();
			vector<point> newVecp = inputObj.vecp;
			cout << "New vecp size:" << newVecp.size() << endl;

			if (newVecp.size() == 2) {
//#pragma omp critical
				noPickUpPossibleCount++;
				cout << "Driver ID:" << driverList[i] << endl;
			}

			vector<vector<long long int> > d;
			if (!newVecp.empty()) {
				//d = (*osrmObj).distanceMatrixCall(newVecp);
				
				
				d = distanceMatrixCall(newVecp);			//call to GA.cpp
						//call to distanceMatrixCall() of osrm_interface.cpp
				//cout<<"dist:"<<d[1][3]<<endl;
				//cout<<"&loc="<<newVecp[1].x<<","<<newVecp[1].y<<"&loc="<<newVecp[3].x<<","<<newVecp[3].y<<endl;
			}








			//distance matrix file generation
			ostringstream distFilePath;
			distFilePath << finalDataURL << "distance_matrix/"
					<< "distance_matrix" << driverList[i] << ".txt";
			//cout << "p:" << distFilePath.str() << endl;
			ofstream distMatFile;
			distMatFile.open(distFilePath.str().c_str());

			for (unsigned int k = 0; k < d.size(); ++k) {
				for (unsigned int j = 0; j < d[k].size(); ++j) {
					distMatFile << d[k][j] << " ";
				}
				distMatFile << endl;
			}

			distMatFile.close();
			
			
			
			
			
			
			double localendtime = omp_get_wtime();
			cout << driverList[i] << " : Time taken this round:"
					<< localendtime - localstarttime << endl;
		}
		

		cout << "no pickup possible:" << noPickUpPossibleCount << endl;

		double endtime = omp_get_wtime();
		cout
				<< " Total Time taken for reprocess requests and distance matrix generation:"
				<< endtime - starttime << endl;
	}
};

/*
 int main(int argv, char **argc) {
 if (argv > 3 || argv < 2) {
 cout << "Enter proper arguments to the call in Filter.cpp.\n";
 return 1;
 }
 string option(argc[1]);
 parameters_getter pg;
 string baseURL = pg.fetchParameter("baseurl");

 if (option == "step1") {
 string a2(argc[2]);
 istringstream is(a2);

 long long int driverID;
 is >> driverID;

 ostringstream rawFilePath;
 rawFilePath << baseURL << "unfiltered_data/" << "requests" << driverID
 << ".txt";

 ostringstream filteredFilePath;
 filteredFilePath << baseURL << "filtered_requests_folder/" << "requests"
 << driverID << ".txt";

 std::ifstream filteredFile(filteredFilePath.str());
 double starttime = omp_get_wtime();
 if (filteredFile
 && filteredFile.peek() != std::ifstream::traits_type::eof()) {
 filteredFile.close();
 } else {
 Filter f;
 f.processRawData(rawFilePath.str(), filteredFilePath.str(),
 driverID, 2);
 filteredFile.close();
 }

 double endtime = omp_get_wtime();
 cout << driverID << " : Time taken this round:" << endtime - starttime
 << endl;
 } else if (option == "step2") {
 Filter f;
 f.finalData_distMat_req_generation();
 } else if (option == "debug") {
 parameters_getter pg;
 string osrmURL = pg.fetchParameter("osrm_map_url");
 OSRM_INTERFACE (*osrmObj)(osrmURL);

 vector<pair<float, float> > locations; //pair - first=latitude and second=longitude
 locations.push_back(make_pair(40.79143906, -73.96858978));
 locations.push_back(make_pair(40.76959610, -73.98965454));
 long long int answer = (*osrmObj).viarouteCall(locations);
 cout << "\nDuration:" << answer << endl;

 cout << "Distance Matrix: \n";
 vector<point> newVecp;
 point p1, p2;
 p1.x = 40.79143906;
 p1.y = -73.96858978;
 p2.x = 40.76959610;
 p2.y = -73.98965454;
 newVecp.push_back(p1);
 newVecp.push_back(p2);
 vector<vector<long long int> > d = (*osrmObj).distanceMatrixCall(newVecp);

 for (unsigned int i = 0; i < d.size(); ++i) {
 for (unsigned int j = 0; j < d[i].size(); ++j) {
 cout << d[i][j] << " ";
 }
 cout << endl;
 }
 }

 return 0;
 }
 */
