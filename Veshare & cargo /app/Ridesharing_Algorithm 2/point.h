#ifndef POINT_H
#define POINT_H

#include<bits/stdc++.h>
using namespace std;

class point {
public:
	float x;
	float y;
	long long int vt;
	float w;
	long long int ot;
	long long int ct;
	int source;
	int index;
	int mate_index;
	int usefulIndex;
	//below are variable for insertion step
	long long int start_service_time;
	long long int arrival_time;
	long long int maxshift;
	long long int shift;
	long long int wait;
	//useful variables for implementation purpose
	int index_i;
	int index_k;
	float P;	//cummulative prbobability
	//variables for passing data to allocation module
	long long int customer_wait_time;
	long long int driver_wait_time;
	long long int customer_travelling_time;
	//driver and customer ID
	long long int ID;

	//function from point.h
	//if you change pointHeuristic then please change in point.h file
	float getPointHeuristicValue() {
		const point j = (*this);
		if (j.shift == 0) {
			return 0.01;
		}
		float ans = (((j.w * j.w) + 0.2 * (j.maxshift - j.wait)) / j.shift);
		return ans;
	}

	//time converter
	string timeStampToHReadble(const long long int epoch_time) {
		time_t rawtime = epoch_time;
		struct tm * dt;
		char buffer[30];
		dt = localtime(&rawtime);
		//"%d/%b/%Y-%H:%M:%S"
		strftime(buffer, sizeof(buffer), "%H:%M:%S", dt);
		return std::string(buffer);
	}

	//If flag is true then print human readable time
	//else print unix timestamp
	string printPoint(bool flag) {
		point p = (*this);
		ostringstream os;
		//os << "x: " << p.x << " y: " << p.y << " vt: " << p.vt << " w: " << p.w << " ot: " << p.ot << " ct: " << p.ct << " index: " << p.index;
		os << setprecision(2) << fixed;
		if (flag) {
			os << "ID: " << left << setw(7) << p.ID << " Index: " << left
					<< setw(4) << p.index << " vt: " << left << setw(5) << p.vt
					<< " w:" << left << setw(5) << p.w << " ot: " << left
					<< setw(5) << timeStampToHReadble(p.ot) << " ct: " << left
					<< setw(5) << timeStampToHReadble(p.ct) << " sst: " << left
					<< setw(8) << timeStampToHReadble(p.start_service_time)
					<< " at:" << left << setw(8)
					<< timeStampToHReadble(p.arrival_time) << " maxshift:"
					<< left << setw(7) << p.maxshift << " wait:" << left
					<< setw(7) << p.wait << " shift:" << left << setw(7)
					<< p.shift;
		} else {
			os << "ID: " << left << setw(7) << p.ID << " Index: " << left
					<< setw(4) << p.index << " vt: " << left << setw(5) << p.vt
					<< " w:" << left << setw(5) << p.w << " ot: " << left
					<< setw(5) << (p.ot) << " ct: " << left << setw(5) << (p.ct)
					<< " sst: " << left << setw(8) << (p.start_service_time)
					<< " at:" << left << setw(8) << (p.arrival_time)
					<< " maxshift:" << left << setw(7) << p.maxshift << " wait:"
					<< left << setw(7) << p.wait << " shift:" << left << setw(7)
					<< p.shift;
		}

		return os.str();
	}

	//printpoint function for allocation module
	string printPoint2() {
		point p = (*this);
		ostringstream os;
		os << setprecision(2) << fixed;
		os << "Index: " << left << setw(4) << p.index << " w:" << left
				<< setw(5) << p.w << " Customer waiting time: " << left
				<< setw(4) << p.customer_wait_time << " Driver waiting time:"
				<< left << setw(4) << p.driver_wait_time
				<< " Customer Travelling time:" << left << setw(7)
				<< p.customer_travelling_time;
		return os.str();
	}

};

#endif
