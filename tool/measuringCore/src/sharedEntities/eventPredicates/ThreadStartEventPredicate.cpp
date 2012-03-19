/*
 * ThreadStartRule.cpp
 *
 *  Created on: Feb 9, 2012
 *      Author: ruedi
 */

#include "ThreadStartEventPredicate.h"
#include "baseClasses/events/ThreadEvent.h"

bool ThreadStartEventPredicate::doesMatch(EventBase *event) {
	ThreadEvent *threadStartEvent = dynamic_cast<ThreadEvent*>(event);
	if (threadStartEvent != NULL && threadStartEvent->getEvent()==ThreadEventEnum_Started) {
		return true;
	}
	return false;
}

