/*
 * WaitForKernelAction.cpp
 *
 *  Created on: Feb 15, 2012
 *      Author: ruedi
 */

#include "WaitForWorkloadAction.h"
#include "baseClasses/Locator.h"

#include <string.h>
#include <cstdio>
#include <cstdlib>

WaitForWorkloadAction::~WaitForWorkloadAction() {
	// TODO Auto-generated destructor stub
}

void WaitForWorkloadAction::execute(EventBase *event) {
	// don't wait if the workload is already started
	if (workloadStarted)
		return;

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
	if (workloadStartEventPredicate.doesMatch(event)) {
		rc = pthread_mutex_lock(&mutex);
		if (rc != 0) {
			printf("mutex_lock: %s", strerror(rc));
			exit(1);
		}

		workloadStarted=true;

		rc = pthread_cond_broadcast(&condvar);
		if (rc != 0) {
			printf("pthread_cond_signal: %s", strerror(rc));
			exit(1);
		}

		rc = pthread_mutex_unlock(&mutex);
		if (rc != 0) {
			printf("pthread_mutex_unlock: %s", strerror(rc));
			exit(1);
		}
	}
}

void WaitForWorkloadAction::initialize() {
	// initialize the mutexes
	pthread_mutex_init(&mutex, NULL);
	pthread_cond_init(&condvar, NULL);

	// listen for start events
	workloadStartEventPredicate.setWorkloadId(getWaitForWorkloadId());
	Locator::addEventListener(this);
}

void WaitForWorkloadAction::dispose() {
	pthread_mutex_destroy(&mutex);
	pthread_cond_destroy(&condvar);
}

