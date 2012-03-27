/*
 * Workload.cpp
 *
 *  Created on: Feb 8, 2012
 *      Author: ruedi
 */

#define LOG_ADDITIONAL "id: %i ", getId()
#include "Logger.h"

#include "Workload.h"
#include "sharedEntities/KernelBase.h"
#include "baseClasses/Locator.h"
#include "baseClasses/events/WorkloadEvent.h"
#include "ChildThread.h"

#include "utils.h"
#include <sched.h>
#include <pthread.h>
#include "Exception.h"
#include <string.h>
#include <cstdio>
#include <sys/types.h>
#include <unistd.h>
#include <sys/syscall.h>

#include <typeinfo>

using namespace std;

Workload::~Workload() {
	// TODO Auto-generated destructor stub
}

static char dummy;

void Workload::clearCaches() {
	LENTER
	clearL1ICache();
	// just access 10M of memory, which is the maximum cache size present in current processors
	size_t blockSize = 10 * (1 << 20);
	char *buffer = (char*) malloc(blockSize);

	// bring the whole buffer into memory
	for (size_t i = 0; i < blockSize; i+=CacheLineSize) {
		dummy += buffer[i];
	}

	free((void*) buffer);
	LLEAVE
}

void *Workload::threadStartHelper(void *arg) {
	Workload *workload = (Workload*) arg;
	try {
		workload->startInThread();
	} catch (Exception &e) {
		fprintf(stderr, "Exception occurred: %s\n", e.get_message().c_str());
		e.print(2);
		exit(1);
	} catch (Exception *e) {
		fprintf(stderr, "Exception occurred: %s\n", e->get_message().c_str());
		e->print(2);
		exit(1);
	} catch (string &s) {
		fprintf(stderr, "Exception occurred: %s\n", s.c_str());
		exit(1);
	} catch (const char* str) {
		fprintf(stderr, "Exception occurred: %s\n", str);
		exit(1);
	} catch (...) {
		fprintf(stderr, "Exception occurred \n");
		exit(1);
	}
	return NULL;
}

pthread_t Workload::start() {
	LENTER
	pthread_attr_t attr;
	pthread_attr_init(&attr);
	pthread_attr_setdetachstate(&attr, PTHREAD_CREATE_JOINABLE);

	pthread_t thread;
	pthread_create(&thread, &attr, Workload::threadStartHelper, this);

	pthread_attr_destroy(&attr);
	LLEAVE
	return thread;
}

void Workload::startInThread() {
	LENTER

	int tid = syscall(__NR_gettid);

	// set the childThread and forward queued actions
	childThreadMutex.lock();
	childThread = ChildThread::getChildThread(tid);
	{
		pair<ActionBase*, EventBase*> pair;
		while (actionQueue.pop(pair)) {
			childThread->queueAction(pair.first, pair.second);
		}
	}
	childThreadMutex.unLock();

	LTRACE("setting CPU affinity");
	if (getCpu() != -1) {
		cpu_set_t *mask = CPU_ALLOC(getCpu());
		size_t size = CPU_ALLOC_SIZE(getCpu());
		CPU_ZERO_S(size, mask);
		CPU_SET_S(getCpu(), size, mask);
		sched_setaffinity(0, size, mask);
	}

	LTRACE("raise start event")
	{
		WorkloadEvent *event = new WorkloadEvent(this, WorkloadEvent_Start);
		Locator::dispatchEvent(event);
	}

	LTRACE("initializing Kernel");
	getKernel()->initialize();

	LTRACE("initializing Measurer Set");
	getMeasurerSet()->initialize();

	LTRACE("starting validation measurers")
	getMeasurerSet()->startValidationMeasurers();

	LTRACE("warm or clear caches")
	warmOrClearCaches();

	LTRACE("raise kernel start event")
	{
		WorkloadEvent *event = new WorkloadEvent(this,
				WorkloadEvent_KernelStart);
		Locator::dispatchEvent(event);
	}

	LTRACE("start additional measurers")
	getMeasurerSet()->startAdditionalMeasurers();

	LTRACE("start main measurer")
	if (getMeasurerSet()->getMainMeasurer() != NULL) {
		getMeasurerSet()->getMainMeasurer()->start();
	}

	LTRACE("run kernel")
	getKernel()->run();

	// stop main measurer
	LTRACE("stop main measurer")
	if (getMeasurerSet()->getMainMeasurer() != NULL) {
		getMeasurerSet()->getMainMeasurer()->stop();
	}

	LTRACE("stop additional measurers")
	getMeasurerSet()->stopAdditionalMeasurers();

	LTRACE("raise kernel stop event")
	{
		WorkloadEvent *event = new WorkloadEvent(this,
				WorkloadEvent_KernelStop);
		Locator::dispatchEvent(event);
	}

	LTRACE("stop the validation measurers")
	getMeasurerSet()->stopValidationMeasurers();

	LTRACE("raise stop event")
	{
		WorkloadEvent *event = new WorkloadEvent(this, WorkloadEvent_Stop);
		Locator::dispatchEvent(event);
	}

	LTRACE("add measurer outputs to the runOutput")
	Locator::addMeasurerSetOutput(getMeasurerSet()->getOutput());

	LTRACE("dispose kernel")
	getKernel()->dispose();

	LTRACE("dispose measurer set")
	getMeasurerSet()->dispose();

	LLEAVE
}

void Workload::warmOrClearCaches() {
	// warm up measurers
	getMeasurerSet()->startAdditionalMeasurers();
	getMeasurerSet()->stopAdditionalMeasurers();

	if (getMeasurerSet()->getMainMeasurer() != NULL) {
		getMeasurerSet()->getMainMeasurer()->start();
		getMeasurerSet()->getMainMeasurer()->stop();
	}

	// should the code cache be warm?
	if (getWarmCode()) {
		// Tell the kernel to warm the code cache, which usually
		// results in the kernel beeing executed once.
		getKernel()->warmCodeCache();

		// Should we clear the data?
		if (!getWarmData()) {
			getKernel()->flushBuffers();
		}
	} else { // Code cache should be cold.
		// Access a large memory buffer and execute a lot of code,
		// this clears the code cache.
		clearCaches();

		// Should the data be warm?
		if (getWarmData()) {
			// Warm the data cache by accessing each cache line of the
			// data buffer(s).
			getKernel()->warmDataCache();
		} else {
			// The data cache should be cold.
			getKernel()->flushBuffers();
		}
	}
}

void Workload::queueAction(ActionBase* action, EventBase* event) {
	LDEBUG("queuing action %p->%s", action, typeid(*action).name())
	childThreadMutex.lock();
	// if there is a child thread, forward the action
	if (childThread != NULL)
		childThread->queueAction(action, event);
	else
		actionQueue.push(make_pair(action, event));

	childThreadMutex.unLock();
}
