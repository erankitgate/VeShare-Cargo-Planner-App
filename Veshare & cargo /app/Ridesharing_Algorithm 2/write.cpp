#include<iostream>
#include<fstream>

using namespace std;

int main()
{
	ofstream outfile;
  outfile.open("inputtopython//filename.txt", std::ios_base::app);
  outfile << "Hello!!";
  outfile << "\n";
  outfile.close();

}
