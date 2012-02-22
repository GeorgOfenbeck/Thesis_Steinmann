/*
 * Workload.cpp
 *
 *  Created on: Feb 8, 2012
 *      Author: ruedi
 */

#define LOG_ADDITIONAL "id: %i ", getId()
#include "Logger.h"

#include "Workload.h"
#include "sharedDOM/KernelBase.h"
#include "baseClasses/Locator.h"
#include "baseClasses/WorkloadStartEvent.h"
#include "baseClasses/WorkloadStopEvent.h"

#include "utils.h"
#include <sched.h>
#include <pthread.h>
#include "Exception.h"
#include <string.h>
#include <cstdio>

using namespace std;

Workload::~Workload() {
	// TODO Auto-generated destructor stub
}

static char dummy;

void Workload::clearCaches() {
	LENTER

	getKernel()->clearCaches();

	clearL1ICache();
	// just access 10M of memory, which is the maximum cache size present in current processors
	size_t blockSize = 10 * (1 << 20) ;
	char *buffer = (char*) malloc(blockSize);

	// bring the whole buffer into memory
	for (size_t i = 0; i < blockSize; i++) {
		dummy += buffer[i];
	}

	free((void*) buffer);
	LLEAVE
}

void *Workload::threadStart(void *arg) {
	Workload *workload = (Workload*) arg;
	try {
		workload->startInThread();
	} catch (Exception e) {
		fprintf(stderr, "Exception occurred: %s\n", e.get_message().c_str());
		e.print(2);
		exit(1);
	} catch (string s) {
		fprintf(stderr, "Exception occurred: %s\n", s.c_str());
		exit(1);
	} catch (const char* str){
		fprintf(stderr, "Exception occurred: %s\n", str);
				exit(1);
	} catch (...) {
		fprintf(stderr, "Exception occurred \n");
		exit(1);
	}
	return NULL;
}

pthread_t Workload::start() {
	pthread_attr_t attr;
	pthread_attr_init(&attr);
	pthread_attr_setdetachstate(&attr, PTHREAD_CREATE_JOINABLE);

	pthread_t thread;
	pthread_create(&thread, &attr, Workload::threadStart, this);

	pthread_attr_destroy(&attr);
	return thread;
}

void Workload::startInThread() {
	LTRACE("setting CPU affinity");
	if (getCpu() != -1) {
		cpu_set_t *mask = CPU_ALLOC(getCpu());
		size_t size = CPU_ALLOC_SIZE(getCpu());
		CPU_ZERO_S(size, mask);
		CPU_SET_S(getCpu(), size, mask);
		sched_setaffinity(0, size, mask);
	}

	LTRACE("initializing Kernel");
	getKernel()->initialize();

	LTRACE("initializing Measurer Set");
	getMeasurerSet()->initialize();

	LTRACE("raise start event")
	{
		WorkloadStartEvent *startEvent = new WorkloadStartEvent(getId());
		Locator::dispatchEvent(startEvent);
		free(startEvent);
	}

	LTRACE("starting validation measurers")
	getMeasurerSet()->startValidationMeasurers();

	LTRACE("warm or clear caches")
	warmOrClearCaches();

	LTRACE("start additional measurers")
	getMeasurerSet()->startAdditionalMeasurers();

	LTRACE("start main measurer")
	if (getMeasurerSet()->getMainMeasurer()!=NULL){
		getMeasurerSet()->getMainMeasurer()->start();
	}

	LTRACE("run kernel")
	getKernel()->run();

	// stop main measurer
	LTRACE("stop main measurer")
	if (getMeasurerSet()->getMainMeasurer()!=NULL){
		getMeasurerSet()->getMainMeasurer()->stop();
	}

	LTRACE("stop additional measurers")
	getMeasurerSet()->stopAdditionalMeasurers();

	LTRACE("stop the validation measurers")
	getMeasurerSet()->stopValidationMeasurers();

	LTRACE("raise stop event")
	{
		WorkloadStopEvent *stopEvent = new WorkloadStopEvent(getId());
		Locator::dispatchEvent(stopEvent);
		free(stopEvent);
	}

	LTRACE("store output")
	output = getMeasurerSet()->getOutput();

	LTRACE("dispose kernel")
	getKernel()->dispose();

	LTRACE("dispose measurer set")
	getMeasurerSet()->dispose();
}

