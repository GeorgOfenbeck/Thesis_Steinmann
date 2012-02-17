/*
 * WorkloadStopRule.cpp
 *
 *  Created on: Feb 9, 2012
 *      Author: ruedi
 */

#include "WorkloadStopEventPredicate.h"
#include "baseClasses/WorkloadStopEvent.h"

#include <typeinfo>

bool WorkloadStopEventPredicate::doesMatch(EventBase *event)
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

