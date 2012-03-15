/*
 * WorkloadStartRule.cpp
 *
 *  Created on: Feb 9, 2012
 *      Author: ruedi
 */

#include "WorkloadEventPredicate.h"

#include "baseClasses/events/WorkloadEvent.h"
#include "sharedEntities/Workload.h"

#include <typeinfo>

bool WorkloadEventPredicate::doesMatch(EventBase *event)
{
	WorkloadEvent *workloadEvent=dynamic_cast<WorkloadEvent*>(event);
	// check if it is a start event
	if (workloadEvent!=NULL){
		// check event
		if (workloadEvent->getEvent()!=getEventNr()){
			return false;
		}
		// check the workload id
		if (getWorkload()==NULL || getWorkload()==workloadEvent->getWorkload()){
			return true;
		}
	}
	return false;
}

