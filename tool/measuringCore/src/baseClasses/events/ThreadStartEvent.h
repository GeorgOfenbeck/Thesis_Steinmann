/*
 * ThreadStartEvent.h
 *
 *  Created on: Mar 14, 2012
 *      Author: ruedi
 */

#ifndef THREADSTARTEVENT_H_
#define THREADSTARTEVENT_H_
#include "baseClasses/EventBase.h"
#include <sys/types.h>

class ChildThread;

class ThreadStartEvent: public EventBase {
	ChildThread* childThread;
public:
	ThreadStartEvent(ChildThread* childThread){
		this->childThread=childThread;
	}

	virtual ~ThreadStartEvent();

	ChildThread* getChildThread() const {
		return childThread;
	}
};

#endif /* THREADSTARTEVENT_H_ */
