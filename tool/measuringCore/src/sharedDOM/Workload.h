/*
 * Workload.h
 *
 *  Created on: Feb 8, 2012
 *      Author: ruedi
 */

#ifndef WORKLOAD_H_
#define WORKLOAD_H_

#include "sharedDOM/WorkloadData.h"
#include "MeasurerSet.h"
#include "KernelBase.h"

class Workload: public WorkloadData {

	MeasurerSetOutput *output;

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

	static void *threadStart(void *arg);
	void startInThread();

public:

	/**
	 * The output is stored in the workload when the workload is done. Otherwise, the measurer set would
	 * have to be accessed from outside of the workload thread. Use this method to get the results
	 * at the end of the measurement run.
	 */
	MeasurerSetOutput *getOutput() {
		return output;
	}

	pthread_t start();
	virtual ~Workload();
};

#endif /* WORKLOAD_H_ */
