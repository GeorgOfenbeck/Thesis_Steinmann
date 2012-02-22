/*
 * ChildProcess.cpp
 *
 *  Created on: Feb 8, 2012
 *      Author: ruedi
 */

#include "Logger.h"
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
#include "baseClasses/Locator.h"
#include "sharedDOM/Rule.h"

#define THREADCOUNT 2
using namespace std;

void handleSerializationTest() {
	MultiLanguageSerializationService serializationService;

	// run the serialization test
	LTRACE("Running Serialization Test");

	// load input
	LTRACE("Loading input");
	ifstream input("serializationTestInput");
	MultiLanguageTestClass *testObject;
	testObject = (MultiLanguageTestClass *) serializationService.DeSerialize(
			input);
	input.close();

	// write output
	LTRACE("writing output");
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

	LTRACE("reading input");
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

	LTRACE("notifying configurators: beforeMeasurement()");
	// notify configurators
	foreach (ConfiguratorBase *configurator, measurement->getConfigurators())
			{
				configurator->beforeMeasurement();
			}

	MeasurementRunOutputCollection outputCollection;

	// perform measurements
	for (int runNumber = 0;
			runNumber < command->getRunCount();
			runNumber++) {

		LTRACE("cloning measurement");
		Measurement *measurementClone = (Measurement*) measurement->clone();

		// set the measurement in the locator
		Locator::setMeasurement(measurementClone);

		// initialize overall measurer set
		if (measurementClone->getOverallMeasurerSet() != NULL) {
			measurementClone->getOverallMeasurerSet()->initialize();
		}

		// initialize rules
		foreach (Rule *rule, measurementClone->getRules()){
			rule->initialize();
		}

		// notify configurators
		LTRACE("notifying configurators: beforeRun()");
		foreach (ConfiguratorBase *configurator, measurement->getConfigurators())
				{
					configurator->beforeRun();
				}

		// start overall measurer set
		if (measurementClone->getOverallMeasurerSet() != NULL) {
			measurementClone->getOverallMeasurerSet()->start();
		}
		// start workloads
		vector<pthread_t> threads;
		foreach(Workload *workload, measurementClone->getWorkloads())
				{
					threads.push_back(workload->start());
				}

		LTRACE("waiting for exit of all workload threads");
		// wait for all workloads to exit
		foreach(pthread_t thread, threads)
				{
					if (pthread_join(thread, NULL) != 0) {
						perror("join failed");
						exit(1);
					}
				}

		// stop overall measurer set
		if (measurementClone->getOverallMeasurerSet() != NULL) {
			measurementClone->getOverallMeasurerSet()->stop();
		}

		LTRACE("notifying configurators: afterRun()");
		// notify configurators
		reverse_foreach (ConfiguratorBase *configurator, measurementClone->getConfigurators())
				{
					configurator->afterRun();
				}

		LTRACE("collecting output");

		// add the output
		MeasurementRunOutput *measurementRunOutput = new MeasurementRunOutput();

		foreach(Workload *workload, measurementClone->getWorkloads())
				{
					measurementRunOutput->getMeasurerSetOutputs().push_back(
							workload->getOutput());
				}

		// add the output of the overall measurer set
		if (measurementClone->getOverallMeasurerSet() != NULL) {
			measurementRunOutput->getMeasurerSetOutputs().push_back(
					measurementClone->getOverallMeasurerSet()->getOutput());
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
	LTRACE("writing output");
	ofstream output("output");
	serializationService.Serialize(&outputCollection, output);
	output.close();

	LLEAVE
	return 0;
}

void ChildProcess::notifyParent(ParentNotification event, uint32_t arg) {
	asm("int3": : "c" (event), "d" (arg));
}
