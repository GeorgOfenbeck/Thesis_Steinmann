/*
 * ActionBase.cpp
 *
 *  Created on: Feb 8, 2012
 *      Author: ruedi
 */

#include "Logger.h"
#include "ActionBase.h"
#include <unistd.h>
#include <sys/syscall.h>
#include "Barrier.h"
#include "Exception.h"
#include "ChildThread.h"
#include "Workload.h"


ActionBase::~ActionBase()
{
}

void ActionBase::startExecute(EventBase* event) {

}

class SynchronousExecutionHelperAction: public ActionBase{

public:
	 void executeImp(EventBase *event);
	 Barrier barrier;
	 ActionBase *innerAction;
	 SharedEntityBase * clone(std::map<PolymorphicBase*,PolymorphicBase*> &map);
};

void SynchronousExecutionHelperAction::executeImp(EventBase *event){
	LENTER
	innerAction->executeDirect(event);
	barrier.open();
	LLEAVE
}

SharedEntityBase * SynchronousExecutionHelperAction::clone(std::map<PolymorphicBase*,PolymorphicBase*> &map){
	throw new Exception("Cloning not supported");
}

void ActionBase::execute(EventBase* event) {
	LENTER
	SynchronousExecutionHelperAction helperAction;
	helperAction.innerAction=this;
	startExecute(&helperAction,event);
	helperAction.barrier.waitIfClosed();
	LLEAVE
}

void ActionBase::startExecute(ActionBase* action, EventBase* event) {
	LENTER
	// is there any restriction on the execution thread?
	if (getWorkload()==NULL && getTid()==-1 ){
		LTRACE()
		action->executeDirect(event);
		LLEAVE
		return;
	}

	LTRACE()
	int tid=syscall(__NR_gettid);

	// was the workload set?
	if (getWorkload()!=NULL){
		LTRACE()
		ChildThread* workloadThread=getWorkload()->getChildThread();

		LTRACE()
		// if there is no thread for the workload yet, queue the action in the workload
		if (workloadThread==NULL){
			getWorkload()->queueAction(action,event);
			LLEAVE
			return;
		}

		LTRACE("%p",workloadThread)
		LTRACE("%i",workloadThread->getPid())

		// if there is a thread already, do we have to switch thread?
		if (workloadThread->getPid()!=tid){
			LTRACE()
			workloadThread->queueAction(action,event);
			LLEAVE
			return;
		}

		LTRACE()
		// we don't have to switch the thread
		action->executeDirect(event);

		LLEAVE
		return;
	}

	// otherwise, the child thread must be defined

	// is a switch necessary?
	if (tid!=getTid()){
		LTRACE()
		ChildThread *childThread=ChildThread::getChildThread(getTid());

		if (childThread==NULL)
			throw new Exception("there was no ChildThread for the specified tid");

		childThread->queueAction(action,event);
		return;
	}

	action->executeDirect(event);
}

