/*
 * ChildThread.h
 *
 *  Created on: Feb 8, 2012
 *      Author: ruedi
 */

#ifndef CHILDTHREAD_H_
#define CHILDTHREAD_H_
#include "ActionBase.h"
#include <map>
#include <sys/types.h>
#include <vector>
#include <pthread.h>

using namespace std;
class ChildThread {
	vector<ActionBase*> actionQueue;
	pthread_mutex_t mutex;
	bool isProcessing;
	pid_t pid;

public:

	pid_t getPid(){return pid;}

	ChildThread(pid_t pid){
		this->pid=pid;
		pthread_mutex_init(&mutex,NULL);
		isProcessing=false;
	}

	~ChildThread(){
		pthread_mutex_destroy(&mutex);
	}

	static map<pid_t, ChildThread*> threadMap;

	// eax contains pid of process to start
	static void processNotification();

	static ChildThread* getChildThread(pid_t child);

	/**
	 * start processing of the actions
	 */
	void processActions();

	bool pushAction(ActionBase *action);
};

#endif /* CHILDTHREAD_H_ */
