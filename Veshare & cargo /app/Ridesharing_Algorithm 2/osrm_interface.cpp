//#include "osrm/json_container.hpp"
//#include "osrm/engine_config.hpp"
//#include "osrm/route_parameters.hpp"
//#include "osrm/osrm.hpp"
#include <string>
#include <utility>
#include <iostream>
#include <exception>
#include <cstdlib>
#include <omp.h>
#include <typeinfo>
//#include "osrm_interface.h"
#include "point.h"
#include "new_osrm_interface.cpp"
//using namespace osrm;

/*OSRM_INTERFACE::OSRM_INTERFACE(std::string base_path) {
	config.storage_config = {base_path};
	config.use_shared_memory = false;
	osrmCallObj=new OSRM {config};
}



OSRM_INTERFACE::~OSRM_INTERFACE() {
	delete (osrmCallObj);
}*/

/*long long int OSRM_INTERFACE::viarouteCall(
		vector<pair<float, float> > locations) {*/
		
		
long long int viarouteCall(
		vector<pair<float, float> > locations) {













	/*// The following shows how to use the Route service; configure this service
	RouteParameters params;
	
	
	
	//--------Genrates latitude and Longitude of locations
	for (unsigned int i = 0; i < locations.size(); i++) {
		params.coordinates.push_back(
				{ util::FloatLongitude(locations[i].second),
						util::FloatLatitude(locations[i].first) });
	}
	//--------
	
	
	
	// Response is in JSON format
	json::Object result;

	// Execute routing request, this does the heavy lifting
	const auto status = (*osrmCallObj).Route(params, result);
	//const auto status = (*osrmCallObj)
	long long int duration = 0;
	if (status == Status::Ok) {
		auto &routes = result.values["routes"].get<json::Array>();

		// Let's just use the first route
		auto &route = routes.values.at(0).get<json::Object>();
		const auto distance = route.values["distance"].get<json::Number>().value;
		duration = route.values["duration"].get<json::Number>().value;

		// Warn users if extract does not contain the default Berlin coordinates from above
		if (distance == 0 or duration == 0) {
			std::cout << "Note: distance or duration is zero. ";
			std::cout
					<< "You are probably doing a query outside of the OSM extract.\n\n";
		}
		//std::cout << "Duration: " << duration << " seconds\n";
	} else if (status == Status::Error) {
		const auto code = result.values["code"].get<json::String>().value;
		const auto message = result.values["message"].get<json::String>().value;

		std::cout << "Code: " << code << "\n";
		std::cout << "Message: " << code << "\n";
	}*/

















	long long int duration = getDuration(locations);


	return duration;
}

/*vector<vector<long long int> > OSRM_INTERFACE::distanceMatrixCall(
		vector<point> vecp) {*/
		
vector<vector<long long int>> distanceMatrixCall(
		vector<point> vecp) {

	vector<vector<long long int> > d(vecp.size(),
			vector<long long int>(vecp.size(), 0));













/*// The following shows how to use the Route service; configure this service
	TableParameters params;

	for (unsigned int i = 0; i < vecp.size(); i++) {
		params.coordinates.push_back( { util::FloatLongitude(vecp[i].y),
				util::FloatLatitude(vecp[i].x) });
	}

	// Response is in JSON format
	json::Object json_result;

	// Execute routing request, this does the heavy lifting
	const auto status = (*osrmCallObj).Table(params, json_result);

//	cout << "result:" << result_code << endl;
	if (status == Status::Ok) {
//		ostream out;
		// Extract data out of JSON structure
		osrm::json::Array array = json_result.values["durations"].get<
				osrm::json::Array>();


		//		cout << "Size:" << array.values.size() << endl;
		for (unsigned int i = 0; i < array.values.size(); ++i) {
			//ay.values.at(0);
			auto &it = array.values[i];
			osrm::json::Array row = it.get<json::Array>();
			for (unsigned int j = 0; j < row.values.size(); ++j) {
				auto &it2 = row.values[j];
				long long int value = it2.get<osrm::json::Number>().value;
				d[i][j] = value;
//				cout << i << "," << j << " ";
//				cout << value << " ";
			}
//			cout << endl;
		}

	} else {
		cout << "Problem in generating distance matrix" << endl;
	}*/
	
	
	
	
	d = DistanceMatrix(vecp);
	
	
	
	
	
	
	
	return d;
}

//		string str=row[0].get<osrm::json::String>().value;
//		std::cout<<"Type:"<<typeid(str).name()<<std::endl;
//		std::cout<<"Type:"<<typeid(array.values[i]).name()<<std::endl;
//		cout<<"I size:"<<localArray.size()<<endl;
//		for (osrm::json::Array *it = array.values.cbegin(); it != array.values.cend(); ++it){
//			osrm::json::Array array =(*it);
//
//			mapbox::util::apply_visitor(osrm::util::json::Renderer(out), *it);
//			if (++it != end) {
//				out << ",";
//			}
//		}

//		auto &first = json_result.values;
//		//.get<osrm::json::String>().value;
//		//string str(first);
//		//cout<<"first:"<<first<<endl;
//		std::cout<<"Type:"<<typeid(summary).name()<<std::endl;
//		std::cout<<"Type:"<<typeid(first).name()<<std::endl;
