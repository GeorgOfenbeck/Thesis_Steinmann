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
#include <csignal>
#include <fstream>
#include <typeinfo>
#include "coreSwitchTest.hpp"
#include "sharedDOM/MultiLanguageSerializationService.h"
#include "sharedDOM/MultiLanguageTestClass.h"
#include "sharedDOM/MemoryLoadKernelDescription.h"
#include "sharedDOM/MeasurementDescription.h"
#include "sharedDOM/MeasurerOutputCollection.h"
#include "sharedDOM/MeasurementCommand.h"
#include "typeRegistry/TypeRegistry.h"
#include "typeRegistry/TypeRegistryEntry.h"
#include "baseClasses/KernelBase.h"
#include "baseClasses/MeasurerBase.h"
#include "baseClasses/MeasurementSchemeBase.h"

#include "kernels/MemoryLoadKernel.h"
#include "baseClasses/SystemInitializer.h"

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


#include "sharedDOM/MemoryLoadKernelDescription.h"

/*
 * Print a progress bar. Completed is the amount of work which is completed already,
 * total is the total amount of work (100%).
 *
 * If called multiple time, the existing progress bar is overwritten.
 */
static void printProgress(int completed, int total){
	const int totalElements=40;

	int printedChars=0;

	// abort if there is no work to be done
	if (total==0){
		return;
	}

	// print the progress bar
	printedChars+=printf("|");
	int i;
	for (i=0; i<completed*totalElements/total && i<totalElements;i++){
		printedChars+=printf("=");
	}

	// fill remaining space
	for (; i<totalElements;i++){
		printedChars+=printf(" ");
	}

	// output the final bars and the precent number
	printedChars+=printf("| %i%%",completed*100/total);

	// move the cursor back to the beginning of the line
	//for (i=0; i<printedChars; i++) printf("\b");
	printf("\r");
	fflush(stdout);
}

/*
 * If set to true, the measurement loop will quit in the next iteration.
 * Used by the handler of CTRL+C to abort the measurement
 */
static bool abortMeasurement;

static void sigint_handler(int arg){
	abortMeasurement=true;
}
int doIt(int argc, char *argv[]){
	MultiLanguageSerializationService serializationService;

	if (argc==2 && strcmp(argv[1],"serializationTest")==0){
		// run the serialization test
		printf("Running Serialization Test\n");


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

	printf("Initializing system\n");
	SystemInitializer::initialize();

	printf("Reading input\n");
	ifstream input("config");
	if (!input.is_open()){
		printf("could not read measurement configuratio file <config>");
		exit(1);
	}
	MeasurementCommand *command=(MeasurementCommand *)serializationService.DeSerialize(input);
	input.close();

	MeasurementDescription *description=command->getMeasurement();

	printf("Setting up the measurement\n");

	printf("registered kernels:\n");
	TypeRegistry<KernelBase>::print();

	printf("registered measurers:\n");
	TypeRegistry<MeasurerBase>::print();

	printf("registered measurement schemes:\n");
	TypeRegistry<MeasurementSchemeBase>::print();

	// create kernel
	KernelBase *kernel=TypeRegistry<KernelBase>::createObject(description->getKernel());
	if (kernel==NULL){
		printf("kernel for %s not found\n",typeid(*description->getKernel()).name());
		exit(1);
	}

	// create measurer
	MeasurerBase *measurer=TypeRegistry<MeasurerBase>::createObject(description->getMeasurer());
	if (measurer==NULL){
		printf("measurer for %s not found\n",typeid(*description->getMeasurer()).name());
		exit(1);
	}

	// create measurement scheme
	MeasurementSchemeBase *scheme=TypeRegistry<MeasurementSchemeBase>::createObject(description->getScheme(),kernel,measurer);
	if (scheme==NULL){
		printf("measurement scheme for %s not found\n",typeid(*description->getScheme()).name());
		exit(1);
	}

	TypeRegistry<MeasurementSchemeBase>::find(description->getScheme(),kernel,measurer);

	MeasurerOutputCollection outputCollection;

	printf("Performing measurement\n");

	for (int i=0; !abortMeasurement && i<command->getNumberOfMeasurements(); i++){
		printProgress(i,command->getNumberOfMeasurements());

		MeasurerOutputBase *measurerOutput=scheme->measure();
		if (measurerOutput!=NULL){
			outputCollection.getMeasurerOutputs().push_back(measurerOutput);
		}
	}
	// print final progress bar
	printProgress(100,100);
	printf("\n");

	printf("tearing down\n");
	delete(kernel);
	delete(measurer);
	delete(scheme);

	printf("writing output\n");
	ofstream output("output");
	serializationService.Serialize(&outputCollection,output);
	output.close();

	printf("Shutting down system\n");
	SystemInitializer::shutdown();

	return 0;
}

int main(int argc, char *argv[]){
	// setup handler for SIGINT (Ctrl+C)
	abortMeasurement=false;
	signal(SIGINT, sigint_handler);

	// perform the measurement
	try {
		return doIt(argc,argv);
	}
	catch (char const *msg){
		printf("Exception: %s\n",msg);
	}
	catch (Exception exception){
		cout<<"Exception: "<<exception.get_message()<<"\n";
		exception.print(0);
	}
	catch (...){throw;}
}
