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
#include <vector>

class ChildThread {
	SynchronizedQueue<std::pair<ActionBase*,EventBase*> > actionQueue;
	bool isProcessing;
	pid_t pid;
	static std::map<pid_t, ChildThread*> threadMap;

public:

	pid_t getPid(){return pid;}

	ChildThread(pid_t pid){
		this->pid=pid;
		isProcessing=false;
	}

	~ChildThread(){
	}

	/**
	 * When called,
	 * eax contains the process to notify
	 * ebx contains the ChildNotification
	 * ecx contains the argument
	 */
	static void processNotification();

	static ChildThread* getChildThread(pid_t child);

	static std::vector<ChildThread*> getChildThreads();

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
