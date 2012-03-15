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

void WaitForWorkloadAction::executeImp(EventBase *event) {
	workloadStartedBarrier->waitIfClosed();
}

void WaitForWorkloadAction::handleEvent(EventBase *event) {
	workloadStartedBarrier->open();
}

void WaitForWorkloadAction::initialize() {
	// create barrier
	workloadStartedBarrier=new Barrier();

	// listen for start events
	workloadStartEventPredicate.setWorkload(getWaitForWorkload());
	Locator::addEventListener(this);
}

void WaitForWorkloadAction::dispose() {
	free(workloadStartedBarrier);
}

