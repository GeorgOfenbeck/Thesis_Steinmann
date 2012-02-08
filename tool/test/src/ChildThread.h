/*
 * ChildThread.h
 *
 *  Created on: Feb 8, 2012
 *      Author: ruedi
 */

#ifndef CHILDTHREAD_H_
#define CHILDTHREAD_H_
#include "baseClasses/ActionBase.h"
#include <map>
#include <sys/types.h>
#include <queue>
#include <pthread.h>

using namespace std;
class ChildThread {
	queue<ActionBase*> actionQueue;
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

	// eax contains pid of process to start
	static void processNotification();

	static ChildThread* getChildThread(pid_t child);

	/**
	 * start processing of the actions
	 */
	void processActions();

	void pushAction(ActionBase *action);
};

#endif /* CHILDTHREAD_H_ */
