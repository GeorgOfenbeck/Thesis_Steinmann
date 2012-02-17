/*
 * WorkloadStartRule.cpp
 *
 *  Created on: Feb 9, 2012
 *      Author: ruedi
 */

#include "WorkloadStartEventPredicate.h"

#include "baseClasses/WorkloadStartEvent.h"

#include <typeinfo>

bool WorkloadStartEventPredicate::doesMatch(EventBase *event)
{
	// check if it is a start event
	if (typeid(*event)==typeid(WorkloadStartEvent)){
		WorkloadStartEvent *startEvent=(WorkloadStartEvent*)event;

		// check the workload id
		if (startEvent->getWorkloadId()==getWorkloadId()){
			return true;
		}
	}
	return false;
}

