/*
 * main.cpp
 *
 *  Created on: Feb 1, 2012
 *      Author: ruedi
 */

#include <cstdio>
#include <cstring>
#include <vector>
#include <string>
#include <iostream>
#include <sstream>

#include <boost/foreach.hpp>

#define foreach         BOOST_FOREACH
#define reverse_foreach BOOST_REVERSE_FOREACH

using namespace std;
#define MAX_CPU 128

void setGovernor(const char *governor, vector<int> &cpus){
	foreach(int cpu, cpus){
		stringstream fileName;
		fileName<<"/sys/devices/system/cpu/cpu"<<cpu<<"/cpufreq/scaling_governor";

		FILE *file=fopen(fileName.str().c_str(),"w");
		if (file==NULL){
			perror(("could not open "+fileName.str()+" for writing").c_str());
			exit(1);
		}
		else{
			if (fprintf(file,"%s",governor)<0){
				printf("Error writing governor of cpu %i",cpu);
				exit(1);
			}
			if (fclose(file)!=0){
				string msg="could not close file for cpu ";
				msg+=cpu;
				perror(msg.c_str());
				exit(1);
			}
		}
	}
}

int main(int argc, char *argv[]){
	if (argc>=4 && strcmp("governor",argv[1])==0){
		const char *governor=NULL;
		// parse the governor
		if (strcmp("powersave",argv[2])==0)
			governor="powersave";
		if (strcmp("performance",argv[2])==0)
			governor="performance";
		if (strcmp("ondemand",argv[2])==0)
			governor="ondemand";

		if (governor!=NULL){
			// parse the cpu list
			vector<int> cpus;
			for (int i=3; i<argc; i++){
				int cpu;
				if (sscanf(argv[i],"%i",&cpu)!=1){
					printf("can not parse cpu number %s\n",argv[i]);
					return 1;
				}

				if (cpu<0 || cpu>MAX_CPU){
					printf("cpu %i is out of range", cpu);
					return 1;
				}
				cpus.push_back(cpu);
			}

			// set the governor for the specified cpus
			setGovernor(governor, cpus);
			return 0;
		}

	}

	// print usage
	printf("usage: ");
	if (argc>0)
		printf("%s",argv[0]);
	else
		printf("systemConfigurator");
	printf(" governor (performance|powersafe) <cpu list>\n");

	return 1;
}

