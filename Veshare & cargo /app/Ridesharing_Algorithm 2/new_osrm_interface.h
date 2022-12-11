#include<bits/stdc++.h>
#include "point.h"
#include "input.h"

using namespace std;

class new_osrm_interface{
public : 
	string data;
	size_t writeCallback(char* buf, size_t size, size_t nmemb, void* up)
	{ //callback must have this declaration
    		//buf is a pointer to the data that curl has for us
    		//size*nmemb is the size of the buffer

    	for (int c = 0; c<size*nmemb; c++)
    	{
        	data.push_back(buf[c]);
    	}
    	return size*nmemb; //tell curl how many bytes we handled
    	}
    	long long int getDuration(vector<pair<float, float> > locations);
    	vector<string> createVec(string str);
    	vector<vector<long long int>> getDistanceMatrix(string str);
    	vector<vector<long long int>> DistanceMatrix(vector<point> vecp);
    	


};
