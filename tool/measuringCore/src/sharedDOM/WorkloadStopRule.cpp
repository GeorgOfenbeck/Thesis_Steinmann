/*
 * WorkloadStopRule.cpp
 *
 *  Created on: Feb 9, 2012
 *      Author: ruedi
 */

#include "WorkloadStopRule.h"
#include "baseClasses/WorkloadStopEvent.h"
#include "sharedDOM/ActionBase.h"

#include <typeinfo>

void WorkloadStopRule::handleEvent(EventBase *event)
{
	printf("WorkloadStopRule::handleEvent()");
	// check if it is a stop event
	if (typeid(*event)==typeid(WorkloadStopEvent)){
		WorkloadStopEvent *stopEvent=(WorkloadStopEvent*)event;

		printf("WorkloadStopRule::handleEvent(): event id: %i myId: %i\n",stopEvent->getWorkloadId(), getWorkloadId());
		// check the workload id
		if (stopEvent->getWorkloadId()==getWorkloadId()){
			// execute the action
			getAction()->execute(event);
		}
	}
}

