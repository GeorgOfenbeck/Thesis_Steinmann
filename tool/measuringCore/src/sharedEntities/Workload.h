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

class Workload: public WorkloadData {
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

	pthread_t start();
	virtual ~Workload();
};

#endif /* WORKLOAD_H_ */
