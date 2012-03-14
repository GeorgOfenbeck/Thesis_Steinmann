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

using namespace std;
class ChildThread {
	queue<pair<ActionBase*,EventBase*> > actionQueue;
	pthread_mutex_t actionQueueMutex;
	bool isProcessing;
	pid_t pid;

public:

	pid_t getPid(){return pid;}

	ChildThread(pid_t pid){
		this->pid=pid;
		pthread_mutex_init(&actionQueueMutex,NULL);
		isProcessing=false;
	}

	~ChildThread(){
		pthread_mutex_destroy(&actionQueueMutex);
	}

	static map<pid_t, ChildThread*> threadMap;

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
	void pushAction(ActionBase *action, EventBase *event);
};

#endif /* CHILDTHREAD_H_ */
