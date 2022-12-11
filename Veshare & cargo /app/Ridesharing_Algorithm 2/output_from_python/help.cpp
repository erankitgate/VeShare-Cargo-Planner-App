#include<iostream>
#include<bits/stdc++.h>

using namespace std;

int main()
{
	fstream newfile;
	string tp, pp;
	newfile.open("myfile.txt",ios::in); //open a file to perform read operation using file object
   	if (newfile.is_open()){   //checking whether the file is open
      	cout << "yes opened!!" << endl;
      	while(getline(newfile, tp)){ //read data from file object and put it into string.
      	if(tp == "\n")	break;
         	cout << "file duration is " << tp << endl; //print the data of the string
         	pp = tp;
      	}
      	
      	newfile.close(); //close the file object.
   	}
   	
   	tp.erase(std::remove(tp.begin(), tp.end(), '\n'), tp.end());

   	
   		cout << tp << endl;
   	long long int dur = (long long int)(stold(pp));
   	
   	cout << dur << endl;
}
