#include<bits/stdc++.h>
using namespace std;

class parameters_getter {
	unordered_map<string, string> mapObj;
	unordered_map<string, string>::const_iterator it;
public:
	parameters_getter() {
		ifstream inputFile;
		inputFile.open("./ridesharing_parameters.txt");
		string line;
		while (getline(inputFile, line)) {
			if (!line.empty()) {
				istringstream is(line);
				string first, second;
				is >> first >> second;
				mapObj[first] = second;
			}
		}

		inputFile.close();
	}

	string fetchParameter(string name) {
		string output;
		it = mapObj.find(name);
		if (it != mapObj.end()) {
			//std::cout << it->first << " is " << it->second << endl;
			output = it->second;
		} else {
			cout << name<< " -- NOT FOUND in parameters_getter" << endl;
		}
		return output;
	}
};
