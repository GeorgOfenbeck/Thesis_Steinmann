/*
 * Workload.cpp
 *
 *  Created on: Feb 8, 2012
 *      Author: ruedi
 */

#include "Workload.h"
#include "sharedDOM/KernelBase.h"

#include "utils.h"
#include <sched.h>
#include <pthread.h>

Workload::~Workload() {
	// TODO Auto-generated destructor stub
}

static int dummy;

void Workload::clearCaches() {
	clearL1ICache();
	// just access 10M of memory, which is the maximum cache size present in current processors
	size_t blockSize = 10 * (1 << 20);
	char *buffer = (char*) malloc(blockSize);
	for (size_t i = 0; i < blockSize; i++) {
		dummy += buffer[i];
		//buffer[i]=0;
	}
	free((void*) buffer);
}

void *Workload::threadStart(void *arg) {
	Workload *workload = (Workload*) arg;
	workload->startInThread();
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
	// set cpu affinity
	if (getCpu() != -1) {
		cpu_set_t *mask = CPU_ALLOC(getCpu());
		size_t size = CPU_ALLOC_SIZE(getCpu());
		CPU_ZERO_S(size, mask);
		CPU_SET_S(getCpu(), size, mask);
		sched_setaffinity(0, size, mask);
	}

	// start validation measurers. They should validate the warmup, too
	getMeasurerSet()->startValidationMeasurers();

	// prepare caches
	warmOrClearCaches();

	// warm up additional measurers
	getMeasurerSet()->startAdditionalMeasurers();
	getMeasurerSet()->stopAdditionalMeasurers();

	// warm up main measurer
	getMeasurerSet()->getMainMeasurer()->start();
	getMeasurerSet()->getMainMeasurer()->stop();


	// perform measurement

	// start additional measurers
	getMeasurerSet()->startAdditionalMeasurers();

	// start main measurer
	getMeasurerSet()->getMainMeasurer()->start();

	// run kernel
	getKernel()->run();

	// stop main measurer
	getMeasurerSet()->getMainMeasurer()->stop();

	// stop additional measurers
	getMeasurerSet()->stopAdditionalMeasurers();

	// stop the validation measurers
	getMeasurerSet()->stopValidationMeasurers();

	// store output
	output=getMeasurerSet()->getOutput();
}


