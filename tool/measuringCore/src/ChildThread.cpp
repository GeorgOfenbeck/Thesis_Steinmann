/*
 * ChildThread.cpp
 *
 *  Created on: Feb 8, 2012
 *      Author: ruedi
 */

#include "Logger.h"
#include "ChildThread.h"
#include "Notifications.h"
#include "ChildProcess.h"

#include <cstdio>
#include <cstdlib>
#include "Exception.h"
#include "baseClasses/events/ThreadStartEvent.h"
#include "baseClasses/Locator.h"

static pthread_mutex_t threadMapMutex = PTHREAD_MUTEX_INITIALIZER;

map<pid_t, ChildThread*> ChildThread::threadMap;

void ChildThread::processNotification() {
	pid_t childPid;
	ChildNotification notification;
	uint32_t arg;
	asm("":"=a" (childPid), "=b" (notification), "=c" (arg)::);

	LDEBUG("pid %i, notification: %s, arg: %i", childPid,
			ChildNotificationNames[notification], arg);

	if (notification == ChildNotification_ChildExited) {
		pthread_mutex_lock(&threadMapMutex);

		// check that the child is initialized
		if (threadMap.count(arg) > 0) {
			// remove the child from the thread map
			delete (threadMap[arg]);
			threadMap.erase(arg);
		}

		pthread_mutex_unlock(&threadMapMutex);
	}

	if (notification == ChildNotification_ThreadStarted) {
		// add a child thread for the new thread
		pthread_mutex_lock(&threadMapMutex);
		threadMap[childPid] =  new ChildThread(childPid);
		pthread_mutex_unlock(&threadMapMutex);

		ThreadStartEvent *event=new ThreadStartEvent(childPid);
		Locator::dispatchEvent(event);
	}

	if (notification == ChildNotification_ProcessActions) {
		// invoke child
		getChildThread(childPid)->processActions();
	}

	LLEAVE

	// notify the parent that the child is done processing
	ChildProcess::notifyParent(ParentNotification_ProcessingDone, 0);
}

void ChildThread::processActions() {
	LENTER
	while (1) {
		// get the next action
		pair<ActionBase*, EventBase*> pair;
		pthread_mutex_lock(&actionQueueMutex);
		if (actionQueue.empty()) {
			// break the loop if the queue is empty
			pthread_mutex_unlock(&actionQueueMutex);
			LLEAVE
			return;
		}
		pair = actionQueue.front();
		actionQueue.pop();
		pthread_mutex_unlock(&actionQueueMutex);

		pair.first->execute(pair.second);
	}
}

ChildThread *ChildThread::getChildThread(pid_t childPid) {
	pthread_mutex_lock(&threadMapMutex);
	if (threadMap.count(childPid)==0){
		LERROR("threadMap does not contain pid %i",childPid)
		throw new Exception("threadMap does not contain pid");
	}
	ChildThread *child = threadMap[childPid];
	pthread_mutex_unlock(&threadMapMutex);
	return child;
}

void ChildThread::queueAction(ActionBase *action, EventBase *event) {
	pthread_mutex_lock(&actionQueueMutex);
	actionQueue.push(make_pair(action, event));
	pthread_mutex_unlock(&actionQueueMutex);

	ChildProcess::notifyParent(ParentNotification_QueueProcessActions,pid);
}

