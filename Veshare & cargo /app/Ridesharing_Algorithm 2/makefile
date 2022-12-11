

all:objects #filter
	#done	

%.o:%.cpp
	g++ $< -g -c -o $@ -Wall -std=c++11 -fopenmp -O3 -L/usr/local/lib -losrm -I/usr/local/include -I ~/osrm5/osrm-backend/include/ -I ~/osrm5/osrm-backend/build/include/ -I ~/osrm5/osrm-backend/third_party\
  -lboost_date_time -lboost_filesystem -lboost_iostreams -lboost_program_options -lboost_regex -lboost_system -lboost_thread -lboost_unit_test_framework \
  -ltbb -ltbbmalloc \
  -pthread -lrt	

		
objects:input.o GA.o newOP.o parameters_getter.o Filter.o osrm_interface.o
	g++ -o algoRideshare.out $^ -Wall -Wextra -pedantic -std=c++11 -fopenmp -Wall -O3 \
	-L/usr/local/lib -losrm -I/usr/local/include -I ~/osrm5/osrm-backend/include/ -I ~/osrm5/osrm-backend/build/include/ -I ~/osrm5/osrm-backend/third_party\
  -lboost_date_time -lboost_filesystem -lboost_iostreams -lboost_program_options -lboost_regex -lboost_system -lboost_thread -lboost_unit_test_framework \
  -ltbb -ltbbmalloc \
  -pthread -lrt	
 
clean:
	rm -rf *~ *.o *.out
	
installv5:
	sudo rm -rf /usr/local/include/osrm
	sudo rm -rf /usr/local/lib/libosrm*
	cd /home/veshare/osrm5/osrm-backend/build/ && sudo cmake --build . --target install

installnormal:
	sudo rm -rf /usr/local/include/osrm
	sudo rm -rf /usr/local/lib/libosrm*
	cd /home/rohit/Downloads/NiravRoute16/Code/Ridesharing_System/osrm-backend/ && sudo cmake --build . --target install

