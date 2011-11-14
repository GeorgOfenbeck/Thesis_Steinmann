//============================================================================
// Name        : tool.cpp
// Author      : 
// Version     :
// Copyright   : Your copyright notice
// Description : Hello World in C++, Ansi-style
//============================================================================

#include <iostream>
#include <pthread.h>
#include <vector>
#include <sched.h>
#include <cstdio>
#include <cstdlib>
#include <string.h>
#include <fstream>
#include "coreSwitchTest.hpp"
#include "MultiLanguageSerializationService.h"
#include "generatedC/MultiLanguageTestClass.h"
#include "generatedC/MemoryLoadKernelDescription.h"

#define THREADCOUNT 200
using namespace std;

/*
void *ThreadStart(void *arg){
	// abort all threads on the first cpu
	// this will cause threads from the other cpu to be put on the first cpu
	if (sched_getcpu()==0)
		return NULL;

	// do some work
	long sum=0;
	for (int i=0; i<100000; i++)
		for (int j=0; j<10000; j++)
			sum++;

	printf("%li; cpu: %i\n",sum,sched_getcpu());

	return NULL;
}

int main() {

	coreSwitchTest();

	return 0;

	vector<pthread_t> threads;
	 pthread_attr_t attr;
	 pthread_attr_init(&attr);
	 pthread_attr_setdetachstate(&attr, PTHREAD_CREATE_JOINABLE);

	for (int i=0; i<THREADCOUNT; i++){
		pthread_t thread;
		pthread_create(&thread,&attr,ThreadStart,NULL);
		threads.push_back(thread);
	}
	pthread_attr_destroy(&attr);

	// join all threads
	for (int i=0; i<THREADCOUNT; i++){
			void *ret=NULL;
			pthread_join(threads[i],&ret);
		}


	cout << "!!!Hello World!!!" << endl; // prints !!!Hello World!!!
	return 0;
}
*/

#include "generatedC/MemoryLoadKernelDescription.h"
int main(int argc, char *argv[]){
	if (argc==2 && strcmp(argv[1],"serializationTest")==0){
		// run the serialization test
		printf("Running Serialization Test\n");
		MultiLanguageSerializationService serializationService;

		// load input
		printf("Loading input\n");
		ifstream input("serializationTestInput");
		MultiLanguageTestClass *testObject;
		testObject=(MultiLanguageTestClass *)serializationService.DeSerialize(input);
		input.close();

		// write output
		printf("writing output\n");
		ofstream output("serializationTestOutput");
		serializationService.Serialize(testObject,output);
		output.close();

		return 0;
	}
	printf("Measuring\n");

}
