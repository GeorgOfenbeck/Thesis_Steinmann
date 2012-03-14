/*
 * Workload.h
 *
 *  Created on: Feb 8, 2012
 *      Author: ruedi
 */

#ifndef WORKLOAD_H_
#define WORKLOAD_H_

#include "sharedEntities/WorkloadData.h"
#include "MeasurerSet.h"
#include "KernelBase.h"

class ChildThread;

class Workload: public WorkloadData {
	ChildThread *childThread;
	void clearL1ICache();
	void clearCaches();
	void warmOrClearCaches() {
		// warm or clear caches
		if (getWarmCaches()) {
			getMeasurerSet()->startAdditionalMeasurers();
			getMeasurerSet()->stopAdditionalMeasurers();

			if (getMeasurerSet()->getMainMeasurer() != NULL) {
				getMeasurerSet()->getMainMeasurer()->start();
				getMeasurerSet()->getMainMeasurer()->stop();
			}

			getKernel()->warmCaches();
		} else {
			clearCaches();
		}
	}

	/**
	 * method executed in the workload thread
	 */
	void startInThread();

	/**
	 * static method used as jumppad for startInThread
	 */
	static void *threadStartHelper(void *arg);

public:

	ChildThread *getChildThread(){
		return childThread;
	}
	/**
	 * starts the workload in it's own thread
	 */
	pthread_t start();
	virtual ~Workload();
};

#endif /* WORKLOAD_H_ */
