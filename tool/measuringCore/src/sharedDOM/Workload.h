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

class Workload: public WorkloadData {
	void clearL1ICache();
	void clearCaches();
	void warmOrClearCaches(){
			// warm or clear caches
			if (getWarmCaches()){
				//measurer.start();
				//kernel.warmCaches();
				//measurer.stop();
			}
			else
			{
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
