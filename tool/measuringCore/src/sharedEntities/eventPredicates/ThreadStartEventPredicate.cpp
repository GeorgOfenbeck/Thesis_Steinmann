/*
 * ThreadStartRule.cpp
 *
 *  Created on: Feb 9, 2012
 *      Author: ruedi
 */

#include "ThreadStartEventPredicate.h"

bool ThreadStartEventPredicate::doesMatch(EventBase *event) {
	ThreadStartEvent *threadStartEvent = dynamic_cast<ThreadStartEvent*>(event);
	if (threadStartEvent != NULL) {
		return true;
	}
	return false;
}

