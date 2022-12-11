#ifndef OSRM_INTERFACE_H
#define OSRM_INTERFACE_H

#include<bits/stdc++.h>
//#include "osrm/route_parameters.hpp"
//#include "osrm/table_parameters.hpp"
//#include "osrm/nearest_parameters.hpp"
//#include "osrm/trip_parameters.hpp"
//#include "osrm/match_parameters.hpp"

//#include "osrm/coordinate.hpp"
//#include "osrm/engine_config.hpp"
//#include "osrm/json_container.hpp"

//#include "osrm/status.hpp"
//#include "osrm/osrm.hpp"

#include <string>
#include <utility>
#include <iostream>
#include <exception>
#include <omp.h>

#include <cstdlib>
#include "point.h"

class OSRM_INTERFACE {
	//osrm::EngineConfig config;
	// Routing machine with several services (such as Route, Table, Nearest, Trip, Match)
	//osrm::OSRM *osrmCallObj;
public:
	OSRM_INTERFACE(std::string base_path);
	~OSRM_INTERFACE();
	long long int viarouteCall(vector<pair<float, float> > locations);
	vector<vector<long long int> > distanceMatrixCall(vector<point> vecp);
};
#endif
