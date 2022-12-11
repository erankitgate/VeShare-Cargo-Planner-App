
#include <string>
#include <utility>
#include "ple.cpp"
#include <fstream> 
#include<bits/stdc++.h>
#include <curl/curl.h> //your directory may be different
#include <jsoncpp/json/json.h>
#include "point.h"
#include "input.h"

using namespace std;




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





long long int getDuration(vector<pair<float, float> > locations)
{ 
    
    CURL* curl; //our curl object

    curl_global_init(CURL_GLOBAL_ALL); //pretty obvious
    curl = curl_easy_init();

    
    //making http request to osrm
    
    string url = "http://router.project-osrm.org/route/v1/driving/";
    
    //string url = "http://0.0.0.0:5000/route/v1/driving/";
    
    
    
    string polylineEncoded = getEncodedString(locations);
    
    string toSearch = "?";
    string replaceStr = "%3F";
    
    size_t pos = polylineEncoded.find(toSearch);
    
    // Repeat till end is reached
    while( pos != std::string::npos)
    {
        // Replace this occurrence of Sub String
        polylineEncoded.replace(pos, toSearch.size(), replaceStr);
        // Get the next occurrence from the current position
        pos =polylineEncoded.find(toSearch);
    }
    
    
    
    url = url + "polyline(" + polylineEncoded + ")?overview=false";
    
    
    
    
    
  // Create and open a text file
  ofstream outfile;
  outfile.open("input_to_python//filename.txt", std::ios_base::app);
  outfile << url.c_str();
  outfile << "\n";
  outfile.close();

  
  //running python file via system call
  string filename = "osrm_check.py";
  string command = "python3 ";
  command += filename;
  system(command.c_str());  
    
    
    
    
    
    
    
    
    
    
    
    
    cout << "url = " << url << endl;
    //curl_easy_setopt(curl, CURLOPT_URL, "http://router.project-osrm.org/route/v1/driving/13.388860,52.517037;13.397634,52.529407;13.428555,52.523219?overview=false");
    
    curl_easy_setopt(curl, CURLOPT_URL, url.c_str());
    
    
    
    curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, &writeCallback);
    curl_easy_setopt(curl, CURLOPT_VERBOSE, 1L); //tell curl to output its progress

    curl_easy_perform(curl);

    cout << "Inside new_osrm_interface.cpp , getDuration()" << endl;
    //cout << "route data is \n" << data << endl;
    string val = "";
    
    for(int i = data.rfind("duration"); i < data.size(); i++)				//find() changed to rfind()
    {
    	if(data[i] >= '0' && data[i] <= '9' || data[i] == '.')
    		val = val + data[i];
    	else
    	{
    		if(val != "")
    			break;		
    	}
    }
    
    //cout << endl << stof(val) << endl;
    long long int dur;
    dur = (long long int)(stold(val));
    
    try{
        cout << "duration is " << dur << "\n";
    }
       
    // catch invalid_argument exception.
    catch(const std::invalid_argument){
        cerr << "Invalid argument for string to float conversion of duration" << "\n";
    }
    
    
    //cin.get();

    curl_easy_cleanup(curl);
    curl_global_cleanup();





	fstream newfile;
	string tp, nval;
	newfile.open("output_from_python//myfile.txt",ios::in); //open a file to perform read operation using file object
   	if (newfile.is_open()){   //checking whether the file is open
      	//cout << "yes opened!!" << endl;
      	while(getline(newfile, tp)){ //read data from file object and put it into string.
      		if(tp == "\n")	break;
         	//cout << "file duration is " << tp << "hello"; //print the data of the string
         	nval = tp;
      	}
      	
      	newfile.close(); //close the file object.
   	}

	ofstream ofs;
	ofs.open("output_from_python//myfile.txt", std::ofstream::out | std::ofstream::trunc);
	ofs.close();

	
	dur = (long long int)(stold(nval));
    return dur;
}








//Distance matrix part



vector<string> createVec(string str)
{
    vector<string> res;
    istringstream ss(str);
    string word;
    while (ss >> word) 
    {
        // print the read word
        //cout << word << "\n";
        res.push_back(word);
    }
 
    cout << "size is " << res.size() << endl;
    return res;
}



vector<vector<long long int>> getDistanceMatrix(string str)
{
	//Getting string after "distances" key 
	int pos = str.find("distances");
	
	int start = 0, end = 0;
	
	bool flag = false;
	
	string val;
	
	for(int i = pos; i < str.size(); i++)
	{
		if(str[i] == '[' && str[i+1] == '[' && flag != true)
		{
			start = i;
			flag = true;	
		}
		else if(str[i] == ']' && str[i+1] == ']')
		{
			end = i+1;
			break;	
		}
		if(flag)
			val = val + str[i];
	}
	
	
	val.erase(val.begin());
	
	//Erasing '[', ']' and replacing ',' with space character
	for(int i = 0; i < val.size(); i++)
	{
		if(val[i] == '[' || val[i] == ']')
			val.erase(val.begin()+i);
	}
	long long int totalEntries = 0;
	for(int i = 0; i < val.size(); i++)
	{
		if(val[i] == ',')
		{
			val[i] = ' ';
			totalEntries++;
		}
	}
	
	
	
	vector<vector<long long int>> distance_matrix;
	
	
	start = 0;
	
	int n = sqrt(++totalEntries);			//total number of entries = number of spaces + 1
	
	
	vector<string> entries = createVec(val);	//creating a vector of string out of the processed string
	
	/*cout << "entries are " << entries.size() << endl;
	
	for(int i=0;i<entries.size();i++)
	{
		//cout << entries[i] << "	";
	}
	cout << endl;*/
	
	//cout << "type of entry is " << typeid(entries[0]).name() << endl;
	
	long long int k = 0;
	
	//Paking entries in the distance matrix
	for(int i = 0; i < n; i++)
	{
		vector<long long int> row;
		for(int j = 0; j < n; j++)
		{
			row.push_back((long long int)stold(entries[k++]));
		}
		distance_matrix.push_back(row);
	}
	
	
	//Printing distance matrix
	/*for(int i = 0; i < n; i++)
	{
		for(int j = 0; j < n; j++)
		{
			cout << distance_matrix[i][j] << " ";
		}
		cout << endl;
	}*/
	
	return distance_matrix;
	
}






vector<vector<long long int>> DistanceMatrix(vector<point> vecp)
{
    //string data; //will hold the url's contents

    vector<pair<float, float>> locations;
    
    vector<vector<long long int>> distanceMatrix;

    for (unsigned int i = 0; i < vecp.size(); i++) 
    {
        locations.push_back(make_pair(vecp[i].y, vecp[i].x));
	//params.coordinates.push_back( { util::FloatLongitude(vecp[i].y),util::FloatLatitude(vecp[i].x) });
    }

	
    string url_2 = "http://router.project-osrm.org/table/v1/driving/";
    
    
    string polylineEncoded = getEncodedStringD(locations);
    
    //replace(polylineEncoded.begin(), polylineEncoded.end(), "?", "%3F");
    
    
    string toSearch = "?";
    string replaceStr = "%3F";
    
    size_t pos = polylineEncoded.find(toSearch);
    
    // Repeat till end is reached
    while( pos != std::string::npos)
    {
        // Replace this occurrence of Sub String
        polylineEncoded.replace(pos, toSearch.size(), replaceStr);
        // Get the next occurrence from the current position
        pos =polylineEncoded.find(toSearch);
    }
    
    
    cout << "encoded distance string is " << polylineEncoded << endl;
    url_2 = url_2 + "polyline(" + polylineEncoded + ")?annotations=distance";
    
    
    
    
    
    
    
    
  // Create and open a text file
  ofstream outfile;
  outfile.open("input_to_python//table_url.txt", std::ios_base::app);
  outfile << url_2.c_str();
  outfile << "\n";
  outfile.close();

  
  //running python file via system call
  string filename = "table.py";
  string command = "python3 ";
  command += filename;
  system(command.c_str());  


/*Read distance matrix*/







	data = "";




    CURL* curl; //our curl object

    curl_global_init(CURL_GLOBAL_ALL); //pretty obvious
    curl = curl_easy_init();
    
    
    
    
    //making http request to osrm for distance matrix
    
    curl_easy_setopt(curl, CURLOPT_URL, url_2.c_str());
    
    
    
    curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, &writeCallback);
    curl_easy_setopt(curl, CURLOPT_VERBOSE, 1L); //tell curl to output its progress

    curl_easy_perform(curl);

    cout << "Inside new_osrm_interface.cpp , DistanceMatrix()" << endl;
    
    
    //cout << "distance matrix data is \n" << data << endl;
    
    distanceMatrix = getDistanceMatrix(data);
    
    
    
    
    
    
    
    
    
    
    
    //cin.get();

    curl_easy_cleanup(curl);
    curl_global_cleanup();

    return distanceMatrix;
}
