/*
 * ThreadStartEvent.h
 *
 *  Created on: Mar 14, 2012
 *      Author: ruedi
 */

#ifndef THREADEVENT_H_
#define THREADEVENT_H_
#include "baseClasses/EventBase.h"
#include <sys/types.h>

class ChildThread;

enum ThreadEventEnum{
	ThreadEventEnum_Started,
	ThreadEventEnum_Exiting,
};

class ThreadEvent: public EventBase {
	ChildThread* childThread;
	ThreadEventEnum event;
public:
	ThreadEvent(ChildThread* childThread, ThreadEventEnum event){
		this->childThread=childThread;
		this->event=event;
	}

	virtual ~ThreadEvent();

	ChildThread* getChildThread() const {
		return childThread;
	}

	ThreadEventEnum getEvent() const {
		return event;
	}
};

#endif /* THREADSTARTEVENT_H_ */
