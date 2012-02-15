/*
 * WorkloadStopRule.cpp
 *
 *  Created on: Feb 9, 2012
 *      Author: ruedi
 */

#include "WorkloadStopRule.h"
#include "baseClasses/WorkloadStopEvent.h"

#include <typeinfo>

bool WorkloadStopRule::doesMatch(EventBase *event)
{
	// check if it is a stop event
	if (typeid(*event)==typeid(WorkloadStopEvent)){
		WorkloadStopEvent *stopEvent=(WorkloadStopEvent*)event;

		// check the workload id
		if (stopEvent->getWorkloadId()==getWorkloadId()){
			return true;
		}
	}
	return false;
}

