/*
 * WaitForKernelAction.h
 *
 *  Created on: Feb 15, 2012
 *      Author: ruedi
 */

#ifndef WAITFORWORKLOADACTION_H_
#define WAITFORWORKLOADACTION_H_

#include "sharedDOM/WaitForWorkloadActionData.h"
#include "baseClasses/Locator.h"
#include "sharedDOM/WorkloadStartRule.h"
#include <pthread.h>

class WaitForWorkloadAction : public WaitForWorkloadActionData, IEventListener {
	WorkloadStartRule workloadStartRule;
	bool initialized;
	pthread_mutex_t mutex;
	pthread_cond_t condvar;
public:
	WaitForWorkloadAction(){
		initialized=false;
	}
	virtual ~WaitForWorkloadAction();

	void execute(EventBase *event);
	void handleEvent(EventBase *event);
};

#endif /* WAITFORKERNELACTION_H_ */
