#include <bits/stdc++.h>
#include<ctime>

#include "GA.h"
//#include "parameters_getter.cpp"
//#include "GA.h"
using namespace std;

typedef unsigned int ui;
typedef unsigned long long int ulli;
typedef long long int lli;
typedef pair<int, int> pii;

#define mod 1000000007
#define pb(x) push_back(x)
#define pp pop_back()
#define mp make_pair
#define fi first
#define se second
#define tr(c,it) for(typeof((c).begin()) it = (c).begin(); it != (c).end(); it++)
//#define all(a) a.begin(),a.end()
#define asd(x)              cout<<__LINE__<<" :: "<<#x<< ": "<<x<<endl;
#define asdf(x, y)          cout<<__LINE__<<" :: "<<#x<< ": "<<x<<" | "<<#y<< ": "<<y<<endl;
#define print1DVector(w) for (unsigned int pvi = 0; pvi < w.size(); ++pvi){cout<<w[pvi]<<" ";}cout<<endl;
#define print2DVector(w) for (unsigned int pvi = 0; pvi < w.size(); ++pvi){for (unsigned int pvj = 0; pvj < w[pvi].size(); ++pvj)cout<<w[pvi][pvj]<<" ";cout<<endl;}

//constructor - initializing all necessary data from file...
GA::GA(long long int t, int n, std::vector<point> v,
		vector<vector<long long int> > dt, long long int dtol) {
	tmax = t;
	totalNodes = n;
	vecp = v;
	d = dt;
	srand((unsigned) time(0));
	bestScoreFound = FLT_MIN;

//	GA parameters
	local_search_count = 10;
	points_considered = 4;			//made 6 from 4 for debugging purpose
	driver_tolerance = dtol;
	
	//via orienteering with time windows
	selection_parameter_k = 10;
	selection_parameter_tsize = 3;
}

//***********

//time converter
string GA::timeStampToHReadble(const long long int epoch_time) {
	time_t rawtime = epoch_time;
	struct tm * dt;
	char buffer[30];
	dt = localtime(&rawtime);
	//"%d/%b/%Y-%H:%M:%S"
	strftime(buffer, sizeof(buffer), "%H:%M:%S", dt);
	return std::string(buffer);
}
//***********

//If flag is true then print human readable time
//else print unix timestamp
void GA::printTour(vector<point> tour, string str, bool flag) {

	cout << "\n-- " << str << " ==> Score: " << calculateTourProfit(tour)
			<< " --\n";
	if (tour.size() == 0) {
		//cout << "***PROBLEM***" << endl;
		return;
	}

	for (unsigned int i = 0; i < tour.size() - 1; ++i) {
		cout << setprecision(2) << fixed << tour[i].printPoint(flag)
				<< " Finish_Time: " << left << setw(10)
				<< (tour[i].start_service_time + tour[i].vt) << "--"
				<< timeStampToHReadble(tour[i].start_service_time + tour[i].vt)
				<< " Dist_next: " << left << setw(10)
				<< d[tour[i].index][tour[i + 1].index] << endl;
	}
	cout << tour[tour.size() - 1].printPoint(flag) << endl;
	cout << "-- " << str << " END --\n";
}

//copy tour from source to destination tour
void GA::copyTour(vector<point> source, vector<point> &destination) {
	destination.clear();
	for (unsigned i = 0; i < source.size(); ++i) {
		destination.push_back(source[i]);
	}
}

float GA::calculateTourProfit(vector<point> tour) {
	float sum = 0;
	for (unsigned int i = 0; i < tour.size(); ++i) {
		sum += tour[i].w;
	}

	if (sum > bestScoreFound) {
		cout << "===> Best Score updated from " << bestScoreFound << " to "
				<< sum << " <===" << endl;
		bestScoreFound = sum;
		copyTour(tour, bestTourFound);
	}

	return sum;
}

/*
 serializeTour(tour, "serial.txt");
 printTour(deserializeTour("serial.txt"), "Ret tour", true);
 */

void serializeTour(vector<point> tour, string filename) {
	//serialization
	ofstream ofs(filename.c_str(), ios::binary | ios::app);
	int size = tour.size();
	ofs.write((char *) &size, sizeof(size));
	for (unsigned int i = 0; i < tour.size(); i++) {
		ofs.write((char *) &tour[i], sizeof(tour[i]));
	}
	ofs.close();
}

vector<point> deserializeTour(string filename) {
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

		ifs.read((char *) &size, sizeof(size));
	}

	ifs.close();

	return retTour;
}

int GA::calculatePickupPoints(vector<point> tour) {
	int count = 0;
	for (unsigned int i = 0; i < tour.size(); i++) {
		if (tour[i].source == 1)
			count++;
	}
	return count;
}
static int staticNoPossCount = 0;
void GA::sortTours(vector<vector<point> > &tour) {
	if (tour.size() == 0 || tour.size() == 1)
		return;

	int min_ele_loc;
	for (unsigned int i = 0; i < tour.size() - 1; ++i) {
		//Find minimum element in the unsorted array
		//Assume it's the first element
		min_ele_loc = i;

		//Loop through the array to find it
		for (unsigned int j = i + 1; j < tour.size(); ++j) {
			if (calculatePickupPoints(tour[j]) * -1
					< calculatePickupPoints(tour[min_ele_loc]) * -1) {
				//Found new minimum position, if present
				min_ele_loc = j;
			}
		}

		vector<point> temp = tour[i];
		tour[i] = tour[min_ele_loc];
		tour[min_ele_loc] = temp;
	}
}








//selection_sort_tours() copied from orienteering with time windows GA.cpp
void GA::selection_sort_tours(vector<vector<point> > &tours) {
	if (tours.size() == 0 || tours.size() == 1) {
		return;
	}

	vector<float> hvalue(tours.size(), 0);

	for (unsigned int i = 0; i < tours.size(); ++i) {
		float profit = calculateTourProfit(tours[i]);
		point lastPoint = tours[i][tours[i].size() - 1];
		point firstPoint = tours[i][0];
		float travelTime = lastPoint.arrival_time + lastPoint.vt
				- firstPoint.start_service_time;
		hvalue[i] = pow(profit, 3) / travelTime;
		//cout << "HB:" << hvalue[i] << endl;
	}

	int min_loc = -1;
	for (unsigned int i = 0; i < tours.size(); ++i) {
		min_loc = i;
		for (unsigned int j = i + 1; j < tours.size(); ++j) {

			if (hvalue[j] * -1 < hvalue[min_loc] * -1) {
				min_loc = j;
			}
		}

		vector<point> temptour = tours[i];
		tours[i] = tours[min_loc];
		tours[min_loc] = temptour;

		int temphvalue = hvalue[i];
		hvalue[i] = hvalue[min_loc];
		hvalue[min_loc] = temphvalue;
	}
}
















//reducePopulationSize() function copied from GA.cpp of orienteering_with_time_windows project

vector<vector<point> > GA::reducePopulationSize(
		vector<vector<point> > initialTours, int reqSize) {
	int originalSize = initialTours.size();
	if (originalSize <= reqSize) {
		cout << "Doing nothing reducePopulationSize\n";
		return initialTours;
	}
	//cout << "Original: " << originalSize << " Req:" << reqSize << endl;

	float factor = (float) originalSize / (float) reqSize;

	//cout << "Factor:" << factor << endl;

	vector<vector<point> > groups[selection_parameter_k];

	//divide the given individuals in k groups
	for (unsigned int i = 0; i < initialTours.size(); i++) {
		int groupNumber = (i % selection_parameter_k);
		groups[groupNumber].push_back(initialTours[i]);
	}

	vector<vector<point> > answer;
	//do it for every group
	for (int i = 0; i < selection_parameter_k; ++i) {
		float count = round((float) groups[i].size() / factor);
		/*cout << "\nBEFORE:\n";
		 for (unsigned int x = 0; x < groups[i].size(); ++x) {
		 cout << calculateTourProfit(groups[i][x]) << " , ";
		 }
		 cout << endl;*/
		unsigned int actualCount = count;
		//Note: select 50% based on fitness value
		vector<vector<point> > selectedTours;
		for (unsigned int j = 0; j < actualCount / 2; j++) {
			int tempCount = selection_parameter_tsize;
			while (tempCount--) {
				int randNumber = rand() % groups[i].size();
				selectedTours.push_back(groups[i][randNumber]);
			}
			selection_sort_tours(selectedTours);
			answer.push_back(selectedTours[0]);
		}
		//select 50% randomly
		actualCount = actualCount / 2;
		while (actualCount--) {
			int randNum = rand() % groups[i].size();
			answer.push_back(groups[i][randNum]);
		}

		/*cout << "\nAFTER:\n";
		 for (unsigned int x = 0; x < selectedTours.size(); ++x) {
		 cout << calculateTourProfit(selectedTours[x]) << " , ";
		 }
		 cout << endl;*/
	}

	//if there is any mismatch in final size, push some random new individuals
	int diff = reqSize - answer.size();
	while (diff > 0) {
		int ranNum = rand() % initialTours.size();
		answer.push_back(initialTours[ranNum]);
		diff--;
	}

	/*sum = 0;
	 cout << "\nAfter: " << answer.size() << endl;
	 for (unsigned int w = 0; w < answer.size(); ++w) {
	 sum += calculateTourProfit(answer[w]);
	 }
	 cout << "Sum:" << sum << endl;*/

	return answer;
}






















void GA::controlFunction() {
	initialCount = 70;
	local_search_count = 10;
	points_considered = 4;

	vector<vector<point> > initialTours;

	int change_points_considered[] = { 4, 4, 4, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
			3, 3, 3, 3, 3, 3, 2, 2, 2, 2, 2, 1, 1, 1, 1 };
	int generationCount = sizeof(change_points_considered)
			/ sizeof(change_points_considered[0]);

	for (int var = 0; var < initialCount; ++var) {
		vector<point> tour = greedyTour(true);
		initialTours.push_back(tour);
//		printTour(tour, "Greedy", true);
	}
	
	for (int generation_iterator = 0; generation_iterator < generationCount;
			++generation_iterator) {
		cout << "********************Starting Generation: "
				<< generation_iterator + 1 << "********************" << endl;

		cout << "1 Size in new Generation: " << initialTours.size() << endl;
		initialTours = reducePopulationSize(initialTours, initialCount);
		cout << "2 Size in new Generation: " << initialTours.size() << endl;

		//Add some new individuals using probabilistic method
		points_considered = change_points_considered[generation_iterator];
		vector< vector<point>> newRandomTours = greedyProbTour(initialCount);
		initialTours.insert(initialTours.end(), newRandomTours.begin(), newRandomTours.end());

		cout << "3 Size in new Generation: " << initialTours.size() << endl;

		initialTours = reducePopulationSize(initialTours, initialCount);

		cout << "4 Size in new Generation: " << initialTours.size() << endl;

		//crossover
		vector<vector<point> > children = performCrossover(initialTours);
		cout << "Children Size Before:" << children.size() << endl;
		initialTours = reducePopulationSize(children, initialCount);
		initialTours = children;
		//cout << "Children Size After:" << children.size() << endl;
		
		//Mutation and localsearch
		vector<vector<point> > mutatedToursWithLocalSearch;

		for (unsigned i = 0; i < initialTours.size(); ++i) {
			vector<point> retTour = mutateAndPerformlocalSearch(initialTours[i]);
			mutatedToursWithLocalSearch.push_back(retTour);
		}
		initialTours = reducePopulationSize(initialTours, initialCount);
	}

	sortTours(initialTours);

//	for(unsigned int i=0;i<initialTours.size();i++){
//		cout<<calculatePickupPoints(initialTours[i])<<endl;
//	}

	vector<point> tour = initialTours[0];
	//calculate wait time of driver and customer at each pick up point
	//needed by allocation module
	for (unsigned int i = 0; i < tour.size(); i++) {
		if (tour[i].source == 1) {
			tour[i].customer_wait_time = max((long long int) 0,
					tour[i].arrival_time - tour[i].ot);
			tour[i].driver_wait_time = tour[i].wait;

			for (unsigned int j = i + 1; j < tour.size(); j++) {
				if (tour[j].index == tour[i].mate_index) {
					tour[i].customer_travelling_time = tour[j].arrival_time
							- tour[i].start_service_time;
				}
			}
			//				cout<<"TT:"<<tour[i].customer_travelling_time<<endl;
			//				cout<<"Cw:"<<tour[i].customer_wait_time<<" Dw:"<<tour[i].driver_wait_time<<endl;
		}
	}

	parameters_getter pg;
	string baseURL = pg.fetchParameter("baseurl");
	serializeTour(tour, baseURL + "serial.txt");                       //------ commented for free() invalid next size error
	//printTour(deserializeTour("serial.txt"), "Ret tour", true);
	if (tour.size() == 2) {
		staticNoPossCount++;
	}
	cout << "NO PICKUP COUNT:" << staticNoPossCount << endl;
}

vector<vector<point> > GA::performCrossover(vector<vector<point> > initialTours){
	vector<vector<point> > children;
	children.clear();
	for (int count = 0; count < initialCount; ++count) {

		int r1 = rand() % (initialTours.size());
		int r2 = rand() % (initialTours.size());
		if (r1 == r2) {
			count++;
			continue;
		}
		vector<vector<point> > retTours = crossoverFunction(initialTours[r1], initialTours[r2]);

		sortTours(retTours);
		
		children.insert(children.end(), retTours.begin(), retTours.end());
	}

	return children;
}

vector<vector<point> > GA::crossoverFunction(vector<point> p1, vector<point> p2) {

	vector<vector<point> > answer; //vector for storing answer tours after crossover

//let's first push the parents
	answer.push_back(p1);
	answer.push_back(p2);

//select a common point
	int cross_i = -1, cross_j = -1;
	for (unsigned i = 2; i < p1.size() - 1; ++i) {
		for (unsigned j = p2.size() - 3; j > 1; --j) {
			if (p1[i].index == p2[j].index) {
				cross_i = i;
				cross_j = j;
				break;
			}
		}
	}

//check if there is a common point
	if (cross_i == -1 || cross_j == -1) {
		//cout << "crossover not possible..!\n";
		return answer;
	}

//cout << "i:" << cross_i << " j:" << cross_j << " Index:" << p1[cross_i].index << endl;

	vector<point> c1, c2;	//child tours

//-----------------------create c1-------------
//first put all points before crossover
	for (int j = 0; j <= cross_i; ++j) {
		c1.push_back(p1[j]);
	}

//now add destination point with modified arrival time
	point last_point_c1 = c1[c1.size() - 1];
	float arrival_time_to_dest = last_point_c1.start_service_time
			+ last_point_c1.vt + d[last_point_c1.index][0];

	point dest = vecp[0];
	dest.maxshift = tmax - arrival_time_to_dest;
	dest.arrival_time = arrival_time_to_dest;
	dest.start_service_time = arrival_time_to_dest;

	c1.push_back(dest);

//now again make visited array
	vector<int> visited(totalNodes + 1, 0);
	for (unsigned i = 0; i < c1.size(); ++i) {
		visited[c1[i].index] = 1;
	}

//print initial tour with first half of p1
//printTour(c1, "Debug - c1_initial");

//now add remaining points from p2
	for (unsigned x = cross_j + 1; x < p2.size() - 1; ++x) {
		int index_i = c1.size() - 2;
		int index_k = c1.size() - 1;
		//cout << " Index i:" << index_i << " Index k:" << index_k << endl;
		//now let's add points from p2 to c1
		point j = p2[x];

		//if the new point is there in c1 then ignore it..
		if (visited[j.index] == 1)
			continue;

		float wait_j = isInsertionFeasible(c1, index_i, index_k, j);
		if (wait_j >= 0) {

			point i = c1[index_i];
			point k = c1[index_k];

			//cout << "Wait(j): " << wait_j << endl;

			j.wait = wait_j;
			j.arrival_time = i.start_service_time + i.vt + d[i.index][j.index];	//compute arrival time for j
			j.start_service_time = max((float)j.arrival_time, (float) j.ot);
			float shift_j = d[i.index][j.index] + wait_j + j.vt
					+ d[j.index][k.index] - d[i.index][k.index];//compute shift of j
			j.shift = shift_j;

			float oldwait = k.wait;
			k.wait = max(0.0, (double) (oldwait - j.shift));
			k.arrival_time += j.shift;
			k.shift = max(0.0, (double) (j.shift - oldwait));
			k.start_service_time += k.shift;
			k.maxshift -= k.shift;

			float maxshift = min((double) (j.ct - j.start_service_time),
					(double) (k.wait + k.maxshift));
			j.maxshift = maxshift;

			//update the values of index_i and index_k in point
			j.index_i = index_i;
			j.index_k = index_k;

			//now point is selected
			//first visit the point
			visited[j.index] = 1;

			//Now, insert the point in the c1 and update data structure
			insertPoint(c1, j.index_i, j.index_k, j);
		}
	}

//printTour(c1, "Debug - c1_final");

//cout << "Profit:" << calculateTourProfit(c1) << endl;

//-----------------------create c2-------------
//first put all points before crossover
	for (int j = 0; j <= cross_j; ++j) {
		c2.push_back(p2[j]);
	}

//now add destination point with modified arrival time
	point last_point_c2 = c2[c2.size() - 1];
	float arrival_time_to_dest2 = last_point_c2.start_service_time
			+ last_point_c2.vt + d[last_point_c2.index][0];

	point dest2 = vecp[0];
	dest2.maxshift = tmax - arrival_time_to_dest2;
	dest2.arrival_time = arrival_time_to_dest2;
	dest2.start_service_time = arrival_time_to_dest2;

	c2.push_back(dest2);

//now again make visited array
	visited = std::vector<int>(totalNodes + 1, 0);
	for (unsigned i = 0; i < c2.size(); ++i) {
		visited[c2[i].index] = 1;
	}

//print initial tour with first half of p2
//printTour(c2, "Debug - c2_1");

//now add remaining points from p1
	for (unsigned x = cross_j + 1; x < p1.size() - 1; ++x) {
		int index_i = c2.size() - 2;
		int index_k = c2.size() - 1;
		//cout << " Index i:" << index_i << " Index k:" << index_k << endl;
		//now let's add points from p1 to c2
		point j = p1[x];

		//if the new point is there in c2 then ignore it..
		if (visited[j.index] == 1)
			continue;

		float wait_j = isInsertionFeasible(c2, index_i, index_k, j);
		if (wait_j >= 0) {

			point i = c2[index_i];
			point k = c2[index_k];

			//cout << "Wait(j): " << wait_j << endl;

			j.wait = wait_j;
			j.arrival_time = i.start_service_time + i.vt + d[i.index][j.index];	//compute arrival time for j
			j.start_service_time = max((float)j.arrival_time, (float) j.ot);
			float shift_j = d[i.index][j.index] + wait_j + j.vt
					+ d[j.index][k.index] - d[i.index][k.index];//compute shift of j
			j.shift = shift_j;

			float oldwait = k.wait;
			k.wait = max(0.0, (double) (oldwait - j.shift));
			k.arrival_time += j.shift;
			k.shift = max(0.0, (double) (j.shift - oldwait));
			k.start_service_time += k.shift;
			k.maxshift -= k.shift;

			float maxshift = min((double) (j.ct - j.start_service_time),
					(double) (k.wait + k.maxshift));
			j.maxshift = maxshift;

			//update the values of index_i and index_k in point
			j.index_i = index_i;
			j.index_k = index_k;

			//now point is selected
			//first visit the point
			visited[j.index] = 1;

			//Now, insert the point in the c2 and update data structure
			insertPoint(c2, j.index_i, j.index_k, j);
		}
	}

//printTour(c2, "C2-Debug");

//cout << "Profit C1:" << calculateTourProfit(c1) << endl;
//cout << "Profit C2:" << calculateTourProfit(c2) << endl;
	answer.push_back(c1);
	answer.push_back(c2);

	return answer;
}

//generates x number of tours based on probability using pointHeuristic() function
//returns set of tours
vector<vector<point> > GA::greedyProbTour(int number_of_tours_tobe_generated) {
//cout << "\n--In function greedyProbTour--\n";

	vector<vector<point> > generated_tours;
	for (int w = 0; w < number_of_tours_tobe_generated; ++w) {
		std::vector<point> new_tour = greedyTour(false);
//		printTour(new_tour, "Tour found");
//		cout << "\nTotal profit " << w + 1 << " : " << tour_profit << endl;
		generated_tours.push_back(new_tour);
	}

	return generated_tours;
}

//function generates a single tour6
//if flag is on then it generates full greedy tour based on point heuristic
//if flag is off then it generated tour based on probability on point heuristic
vector<point> GA::greedyTour(bool flag_full_greedyTour) {
	//cout << "\n--In function greedyTour--\n";
	vector<point> tour;						//vector for storing tour
	vector<int> visited(totalNodes + 1, 0);	//maintains which points are visited by the tour

	//--Please note that start and end point are same--
	tour.push_back(vecp[0]);	//start point
	tour.push_back(vecp[1]);	//end point

	visited[0] = 1;	//visited of source point is 1

	//carefully set the initial tour parameters
	tour[1].maxshift = tmax - d[0][1];
	tour[0].maxshift = tour[1].maxshift;
	tour[0].arrival_time = vecp[0].start_service_time;
	tour[1].start_service_time = vecp[0].start_service_time + d[0][1];
	tour[1].arrival_time = vecp[0].start_service_time + d[0][1];

	//print initial tour with	only 2 points
//	printTour(tour, "Initial Tour");

	int iteration = 0;
	while (1) {
		iteration++;
		vector<point> vec_best_points;
		for (unsigned int index_i = 0; index_i < tour.size() - 1; ++index_i) {
			int index_k = index_i + 1;
			vector<point> vecp_temp_points = selectBestPoint(tour, visited,
					index_i, index_k);

			if (vecp_temp_points.size() == 0) {
				continue;
			}

			//pushing all possible points in the arry vec_best_points
			for (unsigned int x = 0; x < vecp_temp_points.size(); ++x) {
				vec_best_points.push_back(vecp_temp_points[x]);
			}
		}

		selection_sort(vec_best_points);
		pair<point, point> selected_point;		//variable for storing selected point
		
		vector<pair<point, point> > possible_customers;
			for (unsigned int i = 0; i < vec_best_points.size(); ++i) {
				if (vec_best_points[i].wait <= driver_tolerance) {
					//cout << "Customer Source: " << vec_best_points[i].printPoint(false) << " \n";
					vector<point> poss = isDestFeasible(tour, vec_best_points[i]);
					if (poss.size() != 0) {
						pair<point, point> tempPair(vec_best_points[i], poss[0]);
						possible_customers.push_back(tempPair);
					}
				}
			}

		if (possible_customers.size() == 0) {
			break;
		} else {
			if (flag_full_greedyTour) {
				//OPTION -- full greedy tours
				selected_point = possible_customers[0];
			} else {
				//Tours based on temperature
				
				int total_points = -1;
				if (possible_customers.size() >= (unsigned int) points_considered) {
					total_points = points_considered;
				} else {
					total_points = possible_customers.size();
				}

				float sum_of_h = 0;
				for(int i=0;i<total_points;i++){
					sum_of_h += possible_customers[i].first.getPointHeuristicValue();
				}

				float temp_prob = 0;
				for (int i = 0; i < total_points; ++i) {
					temp_prob += possible_customers[i].first.getPointHeuristicValue() / sum_of_h;
					vec_best_points[i].P = temp_prob;
					//cout << i + 1 << " : " << temp_prob;
				}
				float rnd_number = ((double) rand() / (RAND_MAX));

				//now choose point using generated random number

				for (int i = 0; i < total_points - 1; ++i) {
					if (possible_customers[i].first.P <= rnd_number) {
						selected_point = possible_customers[i];
					}
				}
			}

	//		cout << "\nSize of possible customer pickup source:"
	//				<< vec_best_points.size() << endl;

			
			
			
			
			point cus_souce = selected_point.first;
			point cus_dest = selected_point.second;

			visited[cus_souce.index] = 1;
			//Now, insert the point in the tour and update data structure
			insertPoint(tour, cus_souce.index_i, cus_souce.index_k, cus_souce);
			insertPoint(tour, cus_dest.index_i, cus_dest.index_k, cus_dest);
		}

//		printTour(tour, "New Tour");
	}
	return tour;
}

//int GA::selectBasedOnProbabilityUsingArray(){
//
//}

float GA::customerPickUpHeuristic(pair<point, point> customer) {
//	cout<<customer.first.printPoint(true)<<endl;
//	cout<<customer.second.printPoint(true)<<endl;
	float sharedTime = customer.second.arrival_time
			- customer.first.start_service_time;
//	cout<<"SharedTime:"<<sharedTime<<endl;
	return sharedTime;
}

// Selection Sort
void GA::sort_customer_optimization(
		vector<pair<point, point> > &possible_customers) {
	if (possible_customers.size() == 0 || possible_customers.size() == 1)
		return;

	int min_ele_loc;
	for (unsigned int i = 0; i < possible_customers.size() - 1; ++i) {
		//Find minimum element in the unsorted array
		//Assume it's the first element
		min_ele_loc = i;

		//Loop through the array to find it
		for (unsigned int j = i + 1; j < possible_customers.size(); ++j) {
			if (customerPickUpHeuristic(possible_customers[j]) * -1
					< customerPickUpHeuristic(possible_customers[min_ele_loc])
							* -1) {
				//Found new minimum position, if present
				min_ele_loc = j;
			}
		}

		pair<point, point> temp = possible_customers[i];
		possible_customers[i] = possible_customers[min_ele_loc];
		possible_customers[min_ele_loc] = temp;
	}
}

// Selection Sort
void GA::selection_sort(vector<point> &tour) {
	if (tour.size() == 0 || tour.size() == 1)
		return;

	int min_ele_loc;
	for (unsigned int i = 0; i < tour.size() - 1; ++i) {
		//Find minimum element in the unsorted array
		//Assume it's the first element
		min_ele_loc = i;

		//Loop through the array to find it
		for (unsigned int j = i + 1; j < tour.size(); ++j) {
			if (tour[j].getPointHeuristicValue() * -1
					< tour[min_ele_loc].getPointHeuristicValue() * -1) {
				//Found new minimum position, if present
				min_ele_loc = j;
			}
		}

		point temp = tour[i];
		tour[i] = tour[min_ele_loc];
		tour[min_ele_loc] = temp;
	}
}

//select best point to be inserted between point at index i and index k in the tour
vector<point> GA::selectBestPoint(vector<point> tour, vector<int> visited, int index_i, int index_k) {
	//cout << "\n--In function selectBestPoint--\n";
	vector<point> vec_points;

	for (unsigned int p = 1; p < vecp.size(); ++p) {
	
	//cout << "p in selectBestPoint = " << p << endl;
		point j = vecp[p];	//point j is the point to be inserted

		//if point is already visited then please continue
		//also if point is destination then please continue
		if (visited[j.index] == 1 || j.source == 0)
			continue;

		//cout << "Insertion of point " << j.index << " between " << index_i << " and " << index_k << endl;

		//check feasibility to insert the point
		long long int wait_j = isInsertionFeasible(tour, index_i, index_k, j);

		if (wait_j >= 0) {
			//cout << "Wait(j): " << wait_j << endl;
			point i = tour[index_i];
			point k = tour[index_k];

			j.wait = wait_j;
			j.arrival_time = i.start_service_time + i.vt + d[i.index][j.index];	//compute arrival time for j
			j.start_service_time = max(j.arrival_time, j.ot);
			long long int shift_j = d[i.index][j.index] + wait_j + j.vt
					+ d[j.index][k.index] - d[i.index][k.index];//compute shift of j
			j.shift = shift_j;

			long long int zero = 0;
			long long int oldwait = k.wait;
			k.wait = max(zero, (oldwait - j.shift));
			k.arrival_time += j.shift;
			k.shift = max(zero, (j.shift - oldwait));
			k.start_service_time += k.shift;
			k.maxshift -= k.shift;

			long long int maxshift = min((j.ct - j.start_service_time),
					(k.wait + k.maxshift));
			j.maxshift = maxshift;

			//update the values of index_i and index_k in point
			j.index_i = index_i;
			j.index_k = index_k;

			vec_points.push_back(j);
		}
	}
	//cout << printPoint(minshift_point) << endl;
	//asd(minshift_vecp_index);
	
	
	//cout << "successfully out of selecBestPoint in GA.cpp " << endl;
	
	return vec_points;
}

//function checks whether
//is insertion of "point" j possible between ith and kth node in tour..?
//function returns wait time of insertion point if it is feasible to insert that point
//else it returns -1
long long int GA::isInsertionFeasible(vector<point> tour, int pi, int pk,
		point j) {
	//cout << "\n--In function isInsertionFeasible--\n";

	//first clear out variables
	point i = tour[pi];
	point k = tour[pk];

	//change the arrival time of the point
	j.arrival_time = i.start_service_time + i.vt + d[i.index][j.index];

	//if arrival not possible upto closing time then we cannot visit that point
	if (j.arrival_time > j.ct)
		return -1;

	//wait time wait(j)=max(0,ot-arrival_time)
	long long int wait_j = max((long long int) 0, (j.ot - j.arrival_time));
	//cout<<"wait(j)="<<wait_j<<endl;

	long long int shift_j = d[i.index][j.index] + wait_j + j.vt
			+ d[j.index][k.index] - d[i.index][k.index];

	//cout<<"Sj:"<<shift_j<<endl;
	//check feasibility
	if (shift_j <= (k.wait + k.maxshift)) {
		return wait_j;
	}
	return -1;
}

vector<point> GA::isDestFeasible(vector<point> tour, point source) {
//	cout << "Checking feasibility:";
//	printTour(tour, "Before");
	insertPoint(tour, source.index_i, source.index_k, source);
//	printTour(tour, "After");

	vector<point> vec_points;

	for (unsigned int index_i = source.index_i + 1; index_i < tour.size() - 1;
			index_i++) {
		int index_k = index_i + 1;

		point j = vecp[source.mate_index];//point j is the point to be inserted
		//check feasibility to insert the point
		long long int wait_j = isInsertionFeasible(tour, index_i, index_k, j);

		if (wait_j >= 0) {
			//cout << "Wait(j): " << wait_j << endl;
			point i = tour[index_i];
			point k = tour[index_k];

			j.wait = wait_j;
			j.arrival_time = i.start_service_time + i.vt + d[i.index][j.index];	//compute arrival time for j
			j.start_service_time = max(j.arrival_time, j.ot);
			long long int shift_j = d[i.index][j.index] + wait_j + j.vt
					+ d[j.index][k.index] - d[i.index][k.index];//compute shift of j
			j.shift = shift_j;

			long long int zero = 0;
			long long int oldwait = k.wait;
			k.wait = max(zero, (oldwait - j.shift));
			k.arrival_time += j.shift;
			k.shift = max(zero, (j.shift - oldwait));
			k.start_service_time += k.shift;
			k.maxshift -= k.shift;

			long long int maxshift = min((j.ct - j.start_service_time),
					(k.wait + k.maxshift));
			j.maxshift = maxshift;

			//update the values of index_i and index_k in point
			j.index_i = index_i;
			j.index_k = index_k;

			vec_points.push_back(j);
		}
	}

//	cout << "\nPOSS Locations : " << vec_points.size() << endl;
//
//	for (unsigned int var = 0; var < vec_points.size(); ++var) {
//		cout << printPoint(vec_points[var]) << endl;
//	}

	selection_sort(vec_points);
	return vec_points;
}

//insert new point j in the tour between point at index i and index k in the tour and update data structure
void GA::insertPoint(vector<point> &tour, int index_i, int index_k, point j) {

	//cout << "\n--In function insertPoint--\n";

	// float wait_j = isInsertionFeasible(tour, index_i, index_k, j);
	// cout << "DEBUG:" << wait_j << endl;
	// cout << "index_i:" << index_i << " index_k:" << index_k << endl;
	// cout << "Point j:" << printPoint(j) << endl;
	// cout << "Point k:" << printPoint(tour[index_k]) << endl;

	tour.insert(tour.begin() + index_i + 1, j);		//insert point j in the tour

	//loop for updating variables for all visit after j
	for (unsigned int position = index_k + 1; position < tour.size();
			++position) {
		point k = tour[position];
		j = tour[position - 1];
		long long int oldwait = k.wait;
		long long int zero = 0;
		k.wait = max(zero, (oldwait - j.shift));
		k.arrival_time += j.shift;
		k.shift = max(zero, (j.shift - oldwait));
		k.start_service_time += k.shift;
		k.maxshift -= k.shift;

		tour[position] = k;
		if (k.shift == 0)//if shift becomes zero then no need to update further points
			break;
	}

	//loop for updating maxshift of all visits before j
	for (int position = index_k; position >= 0; --position) {
		point tp = tour[position];
		point next = tour[position + 1];
		long long int maxshift = min((tp.ct - tp.start_service_time),
				(next.wait + next.maxshift));
		tour[position].maxshift = maxshift;
	}

}

vector<point> GA::mutateAndPerformlocalSearch(vector<point> tour) {
//cout << "\n--In function localSearch--\n";

	int S = 1, R = 1;
	int count_noImprovements = 0;
	float localMax = 0;
	for (unsigned int i = 0; i < tour.size(); ++i) {
		localMax += tour[i].w;
	}

	vector<point> tempTour = tour;
	vector<point> maxTour = tour;

	while (count_noImprovements < local_search_count) {
		stuffToFull(tempTour);
		float profit = 0;
		for (unsigned int i = 0; i < tempTour.size(); ++i) {
			profit += tempTour[i].w;
		}

		if (profit > localMax) {
			copyTour(tempTour, maxTour);
			localMax = profit;
			R = 1;
			count_noImprovements = 0;
		} else {
			count_noImprovements++;
		}
		removePoints(tempTour, S, S + R);
		S = S + R;
		R++;
		if (S >= (int) tempTour.size()) {
			S = 1;
		}
		if (R == totalNodes / 3) {
			R = 1;
		}
	}

	return maxTour;
}

void GA::stuffToFull(vector<point> &tour) {
//cout << "\n--In function stuffToFull--\n";
	std::vector<int> visited(tour.size(), 0);

	for (unsigned i = 0; i < tour.size(); ++i) {
		visited[tour[i].index] = 1;
	}

	while (1) {
		vector<point> vec_best_points;
		vec_best_points.clear();

		//select points based on the point heristic
		for (unsigned int index_i = 0; index_i < tour.size() - 1; ++index_i) {
			int index_k = index_i + 1;
			vector<point> vecp_temp_points = selectBestPoint(tour, visited,
					index_i, index_k);

			if (vecp_temp_points.size() == 0) {
				continue;
			}

			//pushing all possible points in the arry vec_best_points
			for (unsigned int x = 0; x < vecp_temp_points.size(); ++x) {
				vec_best_points.push_back(vecp_temp_points[x]);
			}
		}

		//no possibility of adding point - terminating condition
		if (vec_best_points.size() == 0) {
			break;
		}

		point selected_point = vec_best_points[0];
		//now point is selected
		//first visit the point
		visited[selected_point.index] = 1;

		//Now, insert the point in the tour and update data structure
		insertPoint(tour, selected_point.index_i, selected_point.index_k,
				selected_point);

		//printTour(tour, "After Insertion");
	}
}

void GA::removePoints(vector<point> &tour, int start, int end) {
//cout << "\n--In function removePoints--\n";

	if (start > (int) tour.size() || end > (int) tour.size()) {
		return;
	}

}
