/*
 * ChildProcess.cpp
 *
 *  Created on: Feb 8, 2012
 *      Author: ruedi
 */

#include "ParentProcess.h"
#include "sharedDOM/MultiLanguageSerializationService.h"
#include "sharedDOM/MultiLanguageTestClass.h"
#include "sharedDOM/MeasurementCommand.h"
#include "sharedDOM/MeasurementRunOutputCollection.h"
#include "baseClasses/SystemInitializer.h"

#include "utils.h"

#include "ChildProcess.h"

#include <vector>
#include <cstdio>
#include <cstdlib>
#include <unistd.h>
#include <syscall.h>
#include <stdint.h>
#include <iostream>
#include <fstream>
#include <string>
#include <cstring>
#include <pthread.h>

#define THREADCOUNT 2
using namespace std;

void foo(uint32_t pid) {
	uint32_t a[2];
	a[0] = pid;
	a[1] = 0;
	uint32_t p = (uint32_t) a;
	printf("foo: %i\n", *(uint32_t*) p);
	ParentProcess::notifyParent(ParentNotification_QueueProcessActions,
			(uint32_t) a);
}

void handleSerializationTest() {
	MultiLanguageSerializationService serializationService;

	// run the serialization test
	printf("Running Serialization Test\n");

	// load input
	printf("Loading input\n");
	ifstream input("serializationTestInput");
	MultiLanguageTestClass *testObject;
	testObject = (MultiLanguageTestClass *) serializationService.DeSerialize(
			input);
	input.close();

	// write output
	printf("writing output\n");
	ofstream output("serializationTestOutput");
	serializationService.Serialize(testObject, output);
	output.close();

}

int ChildProcess::main(int argc, char* argv[]) {
	// check if doing a serialization test is requested
	if (argc == 2 && strcmp(argv[1], "serializationTest") == 0) {
		handleSerializationTest();

		// end program
		return 0;
	}

	// notify system initializers
	foreach (SystemInitializer *initializer, SystemInitializer::getInitializers())
			{
				initializer->start();
			}

	MultiLanguageSerializationService serializationService;

	// read input
	ifstream input("config");
	if (!input.is_open()) {
		printf("could not read measurement configuratio file <config>");
		exit(1);
	}
	MeasurementCommand *command =
			(MeasurementCommand *) serializationService.DeSerialize(input);

	input.close();

	Measurement *measurement = command->getMeasurement();

	// notify configurators
	foreach (ConfiguratorBase *configurator, measurement->getConfigurators())
			{
				configurator->beforeMeasurement();
			}

	MeasurementRunOutputCollection outputCollection;

	// perform measurements
	for (int measurementNumber = 0;
			measurementNumber < command->getNumberOfMeasurements();
			measurementNumber++) {
		// notify configurators
		foreach (ConfiguratorBase *configurator, measurement->getConfigurators())
				{
					configurator->beforeRun();
				}

		// start workloads
		vector<pthread_t> threads;
		foreach(Workload *workload, measurement->getWorkloads())
				{
					printf("start Workload\n");
					threads.push_back(workload->start());
				}

		// wait for all workloads to exit
		foreach(pthread_t thread, threads)
				{
					if (pthread_join(thread, NULL) != 0) {
						perror("join failed");
						exit(1);
					}
				}

		// notify configurators
		reverse_foreach (ConfiguratorBase *configurator, measurement->getConfigurators())
				{
					configurator->afterRun();
				}

		// add the output
		MeasurementRunOutput *measurementRunOutput = new MeasurementRunOutput();

		foreach(Workload *workload, measurement->getWorkloads())
				{
					measurementRunOutput->getMeasurerSetOutputs().push_back(
							workload->getOutput());
				}
		outputCollection.getOutputs().push_back(measurementRunOutput);
	}

	// notify configurators
	reverse_foreach (ConfiguratorBase *configurator, measurement->getConfigurators())
			{
				configurator->afterMeasurement();
			}

	reverse_foreach (SystemInitializer *initializer, SystemInitializer::getInitializers())
			{
				initializer->stop();
			}

	// write output
	printf("writing output\n");
	ofstream output("output");
	serializationService.Serialize(&outputCollection, output);
	output.close();

	/*
	 vector<pthread_t> threads;
	 pthread_attr_t attr;
	 pthread_attr_init(&attr);
	 pthread_attr_setdetachstate(&attr, PTHREAD_CREATE_JOINABLE);

	 for (int i = 0; i < THREADCOUNT; i++) {
	 pthread_t thread;
	 pthread_create(&thread, &attr, ThreadStart, NULL);
	 threads.push_back(thread);
	 }
	 pthread_attr_destroy(&attr);

	 // join all threads
	 for (int i = 0; i < THREADCOUNT; i++) {
	 printf("child: joining thead %i\n", i);
	 if (pthread_join(threads[i], NULL) != 0) {
	 printf("child: error on join\n");
	 perror("child: join");
	 exit(1);
	 }
	 printf("child: joined thead %i\n", i);
	 }

	 ThreadStart(NULL);
	 */
	return 0;
}

