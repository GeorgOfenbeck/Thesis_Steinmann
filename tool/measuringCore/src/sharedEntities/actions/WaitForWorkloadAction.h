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
#include "sharedEntities/eventPredicates/WorkloadStartEventPredicate.h"
#include <pthread.h>

class WaitForWorkloadAction : public WaitForWorkloadActionData, IEventListener {
	WorkloadStartEventPredicate workloadStartEventPredicate;
	pthread_mutex_t mutex;
	pthread_cond_t condvar;
	bool workloadStarted;
public:
	WaitForWorkloadAction(){
		workloadStarted=false;
	}
	virtual ~WaitForWorkloadAction();

	void execute(EventBase *event);
	void handleEvent(EventBase *event);
	void initialize();
	void dispose();
};

#endif /* WAITFORKERNELACTION_H_ */
