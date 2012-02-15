/*
 * WaitForKernelAction.cpp
 *
 *  Created on: Feb 15, 2012
 *      Author: ruedi
 */

#include "WaitForWorkloadAction.h"
#include "baseClasses/Locator.h"

#include <string.h>

WaitForWorkloadAction::~WaitForWorkloadAction() {
	// TODO Auto-generated destructor stub
}

void WaitForWorkloadAction::execute(EventBase *event) {
	// initialize the action only once
	if (!initialized) {
		initialized = true;
		workloadStartRule.setWorkloadId(getWaitForWorkloadId());
		Locator::addEventListener(this);

		pthread_mutex_init(&mutex, NULL);
		pthread_cond_init(&condvar, NULL);
	}

	// wait for signal
	int rc;
	rc = pthread_mutex_lock(&mutex);
	if (rc != 0) {
		printf("mutex_lock: %s", strerror(rc));
		exit(1);
	}
	rc = pthread_cond_wait(&condvar, &mutex);
	if (rc != 0) {
		printf("pthread_cond_wait: %s", strerror(rc));
		exit(1);
	}

	rc = pthread_mutex_unlock(&mutex);
	if (rc != 0) {
		printf("pthread_mutex_unlock: %s", strerror(rc));
		exit(1);
	}
}

void WaitForWorkloadAction::handleEvent(EventBase *event) {
	int rc;
	if (workloadStartRule.doesMatch(event)) {
		rc = pthread_cond_signal(&condvar);
		if (rc != 0) {
			printf("pthread_cond_signal: %s", strerror(rc));
			exit(1);
		}
	}
}

