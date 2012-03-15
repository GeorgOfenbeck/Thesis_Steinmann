/*
 * ChildThread.h
 *
 *  Created on: Feb 8, 2012
 *      Author: ruedi
 */

#ifndef CHILDTHREAD_H_
#define CHILDTHREAD_H_
#include "sharedEntities/ActionBase.h"
#include <map>
#include <sys/types.h>
#include <queue>
#include <pthread.h>
#include <utility>
#include "SynchronizedQueue.h"

class ChildThread {
	SynchronizedQueue<std::pair<ActionBase*,EventBase*> > actionQueue;
	bool isProcessing;
	pid_t pid;

public:

	pid_t getPid(){return pid;}

	ChildThread(pid_t pid){
		this->pid=pid;
		isProcessing=false;
	}

	~ChildThread(){
	}

	static std::map<pid_t, ChildThread*> threadMap;

	/**
	 * When called,
	 * eax contains the process to notify
	 * ebx contains the ChildNotification
	 * ecx contains the argument
	 */
	static void processNotification();

	static ChildThread* getChildThread(pid_t child);

	/**
	 * start processing of the actions
	 */
	void processActions();

	/**
	 * queue the action in this thread and send a stop notification
	 */
	void queueAction(ActionBase *action, EventBase *event);
};

#endif /* CHILDTHREAD_H_ */
