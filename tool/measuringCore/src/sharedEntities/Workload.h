/*
 * Workload.h
 *
 *  Created on: Feb 8, 2012
 *      Author: ruedi
 */

#ifndef WORKLOAD_H_
#define WORKLOAD_H_

#include "sharedEntities/WorkloadData.h"
#include "MeasurerSet.h"
#include "KernelBase.h"
#include "SynchronizedQueue.h"

class ChildThread;
class ActionBase;
class EventBase;

class Workload: public WorkloadData {
	SynchronizedQueue<std::pair<ActionBase*,EventBase*> > actionQueue;

	Mutex childThreadMutex;
	ChildThread *childThread;

	void clearL1ICache();
	void clearCaches();
	void warmOrClearCaches();

	/**
	 * method executed in the workload thread
	 */
	void startInThread();

	/**
	 * static method used as jumppad for startInThread
	 */
	static void *threadStartHelper(void *arg);

public:

	ChildThread *getChildThread(){
		return childThread;
	}
	/**
	 * starts the workload in it's own thread
	 */
	pthread_t start();
	virtual ~Workload();

	void queueAction(ActionBase *action, EventBase *event);
};

#endif /* WORKLOAD_H_ */
