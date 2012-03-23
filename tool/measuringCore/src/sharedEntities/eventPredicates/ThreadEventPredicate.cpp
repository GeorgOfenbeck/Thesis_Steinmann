/*
 * ThreadStartRule.cpp
 *
 *  Created on: Feb 9, 2012
 *      Author: ruedi
 */

#include "ThreadEventPredicate.h"
#include "baseClasses/events/ThreadEvent.h"

bool ThreadEventPredicate::doesMatch(EventBase *event) {
	ThreadEvent *threadStartEvent = dynamic_cast<ThreadEvent*>(event);
	if (threadStartEvent != NULL && threadStartEvent->getEvent()==getEventNr()) {
		return true;
	}
	return false;
}

