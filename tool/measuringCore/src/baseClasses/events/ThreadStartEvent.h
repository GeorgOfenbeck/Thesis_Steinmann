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

class ThreadStartEvent: public EventBase {
	pid_t tid;
public:
	ThreadStartEvent(pid_t tid){
		this->tid=tid;
	}

	virtual ~ThreadStartEvent();

	pid_t getTid() const {
		return tid;
	}
};

#endif /* THREADSTARTEVENT_H_ */
