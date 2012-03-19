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
#include "utils.h"
#include <typeinfo>
#include "sharedEntities/Rule.h"

using namespace std;

static Mutex threadMapMutex;
map<pid_t, ChildThread*> ChildThread::threadMap;

void ChildThread::processNotification() {
	pid_t childPid;
	ChildNotification notification;
	uint32_t arg;
	asm("":"=a" (childPid), "=b" (notification), "=c" (arg)::);

	try {
		LDEBUG("pid %i, notification: %s, arg: %i",
				childPid, ChildNotificationNames[notification], arg);

		if (notification == ChildNotification_ChildExited) {

			threadMapMutex.lock();

			// check that the child is initialized
			if (threadMap.count(arg) > 0) {
				// remove the child from the thread map
				delete (threadMap[arg]);
				threadMap.erase(arg);
			}

			threadMapMutex.unLock();
		}

		if (notification == ChildNotification_ThreadStarted) {
			// add a child thread for the new thread
			threadMapMutex.lock();
			ChildThread* childThread = new ChildThread(childPid);
			threadMap[childPid] = childThread;
			threadMapMutex.unLock();

			ThreadStartEvent *event = new ThreadStartEvent(childThread);
			Locator::dispatchEvent(event);
		}

		if (notification == ChildNotification_ProcessActions) {
			// invoke child
			getChildThread(childPid)->processActions();
		}

		LLEAVE

		// notify the parent that the child is done processing
		ChildProcess::notifyParent(ParentNotification_ProcessingDone, 0);
	} catch (Exception *e) {
		LERROR("caught exception %s",e->get_message().c_str())
		e->print(2);
	}
}

ChildThread *ChildThread::getChildThread(pid_t childPid) {
	threadMapMutex.lock();
	if (threadMap.count(childPid) == 0) {
		LERROR("threadMap does not contain pid %i", childPid)
		throw new Exception("threadMap does not contain pid");
	}
	ChildThread *child = threadMap[childPid];
	threadMapMutex.unLock();
	return child;
}

vector<ChildThread*> ChildThread::getChildThreads() {
	vector<ChildThread*> result;
	threadMapMutex.lock();
	typedef pair<pid_t, ChildThread*> ThreadMapPair;
	foreach(ThreadMapPair pair,threadMap) {
		result.push_back(pair.second);
	}
	threadMapMutex.unLock();
	return result;
}

vector<ChildThread*> ChildThread::getChildThreadsAndAddRule(Rule *rule) {
	vector<ChildThread*> result;
	threadMapMutex.lock();
	typedef pair<pid_t, ChildThread*> ThreadMapPair;
	foreach(ThreadMapPair pair,threadMap) {
		result.push_back(pair.second);
	}
	Locator::addRule(rule);
	threadMapMutex.unLock();
	return result;
}

void ChildThread::processActions() {
	LENTER
	LDEBUG("pid: %i", pid)
	pair<ActionBase*, EventBase*> pair;
	while (actionQueue.pop(pair)) {
		LTRACE("%p", pair.first)
		LTRACE("processing action %s", typeid(*(pair.first)).name())
		pair.first->executeDirect(pair.second);
	}
	LLEAVE
}

void ChildThread::queueAction(ActionBase *action, EventBase *event) {
	LDEBUG("pid: %i, queuing action %p->%s",
			pid, action, typeid(*action).name())
	actionQueue.push(make_pair(action, event));
	ChildProcess::notifyParent(ParentNotification_QueueProcessActions, pid);
}

