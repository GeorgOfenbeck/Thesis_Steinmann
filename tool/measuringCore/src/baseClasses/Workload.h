/*
 * Workload.h
 *
 *  Created on: Feb 8, 2012
 *      Author: ruedi
 */

#ifndef WORKLOAD_H_
#define WORKLOAD_H_

#include "sharedDOM/WorkloadDescription.h"
#include "MeasurerSet.h"

class WorkloadBase {
	void clearL1ICache();
	void clearCaches();
public:
	typedef WorkloadDescription tDescriptionBase;

	virtual ~Workload();
};

template<class TKernel, class TMeasurer>
class Workload: public WorkloadBase{
protected:
	WorkloadDescription *description;

	void warmOrClearCaches(){
		// warm or clear caches
		if (description->getWarmCaches()){
			measurer.start();
			kernel.warmCaches();
			measurer.stop();
		}
		else
		{
			clearCaches();
		}
	}
public:
	typedef WorkloadDescription tDescription;
	typedef WorkloadBase tBase;

	/**
	 * the measurer argument is thrown away
	 */
	Workload(WorkloadDescription *description, TKernel *kernel, TMeasurer *measurer){

	}
};

#endif /* WORKLOAD_H_ */
