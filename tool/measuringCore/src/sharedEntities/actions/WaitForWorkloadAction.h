/*
 * WaitForKernelAction.h
 *
 *  Created on: Feb 15, 2012
 *      Author: ruedi
 */

#ifndef WAITFORWORKLOADACTION_H_
#define WAITFORWORKLOADACTION_H_

#include "sharedEntities/actions/WaitForWorkloadActionData.h"
#include "baseClasses/Locator.h"
#include "sharedEntities/eventPredicates/WorkloadEventPredicate.h"
#include "Barrier.h"

class WaitForWorkloadAction : public WaitForWorkloadActionData, IEventListener {
	WorkloadEventPredicate workloadStartEventPredicate;
	Barrier *workloadStartedBarrier;
public:

	virtual ~WaitForWorkloadAction();

	void executeImp(EventBase *event);
	void handleEvent(EventBase *event);

	/**
	 * initialize the action. Has to be called before the workload is started
	 */
	void initialize();

	void dispose();
};

#endif /* WAITFORKERNELACTION_H_ */
