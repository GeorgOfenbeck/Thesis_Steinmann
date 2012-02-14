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
#include "sharedDOM/MeasurementRunOutput.h"
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
#include "sharedDOM/Measurement.h"
#include "sharedDOM/ConfiguratorBase.h"
#include "sharedDOM/Workload.h"

#define THREADCOUNT 2
using namespace std;

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

	printf("reading input\n");
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

	printf("notifying configurators: beforeMeasurement()\n");
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

		printf("cloning measurement\n");
		Measurement *measurementClone=(Measurement*)measurement->clone();

		// notify configurators
		printf("notifying configurators: beforeRun()\n");
		foreach (ConfiguratorBase *configurator, measurement->getConfigurators())
				{
					configurator->beforeRun();
				}

		// start workloads
		vector<pthread_t> threads;
		foreach(Workload *workload, measurementClone->getWorkloads())
				{
					printf("start Workload\n");
					threads.push_back(workload->start());
				}

		printf("waiting for exit of all workload threads\n");
		// wait for all workloads to exit
		foreach(pthread_t thread, threads)
				{
					if (pthread_join(thread, NULL) != 0) {
						perror("join failed");
						exit(1);
					}
				}
		printf("notifying configurators\n");
		// notify configurators
		reverse_foreach (ConfiguratorBase *configurator, measurementClone->getConfigurators())
				{
					configurator->afterRun();
				}
		printf("collecting output\n");

		// add the output
		MeasurementRunOutput *measurementRunOutput = new MeasurementRunOutput();

		foreach(Workload *workload, measurementClone->getWorkloads())
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

	return 0;
}

void ChildProcess::notifyParent(ParentNotification event, uint32_t arg) {
	asm("int3": : "c" (event), "d" (arg));
}
