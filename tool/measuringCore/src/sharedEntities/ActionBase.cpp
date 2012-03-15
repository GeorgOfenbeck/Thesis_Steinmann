/*
 * ActionBase.cpp
 *
 *  Created on: Feb 8, 2012
 *      Author: ruedi
 */

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
	innerAction->execute(event);
	barrier.open();
}

SharedEntityBase * SynchronousExecutionHelperAction::clone(std::map<PolymorphicBase*,PolymorphicBase*> &map){
	throw new Exception("Cloning not supported");
}

void ActionBase::execute(EventBase* event) {
	SynchronousExecutionHelperAction helperAction;
	helperAction.innerAction=this;
	startExecute(&helperAction,event);
	helperAction.barrier.waitIfClosed();
}

void ActionBase::startExecute(ActionBase* action, EventBase* event) {
	// is there any restriction on the execution thread?
	if (getWorkload()==NULL && getTid()==-1 ){
		action->executeDirect(event);
		return;
	}
	int tid=syscall(__NR_gettid);

	// was the workload set?
	if (getWorkload()!=NULL){
		ChildThread* workloadThread=getWorkload()->getChildThread();

		// if there is no thread for the workload yet, queue the action in the workload
		if (workloadThread==NULL){
			getWorkload()->queueAction(action,event);
			return;
		}

		// if there is a thread already, do we have to switch thread?
		if (workloadThread->getPid()!=getTid()){
			workloadThread->queueAction(action,event);
			return;
		}

		// we don't have to switch the thread
		action->executeDirect(event);

		return;
	}

	// otherwise, the child thread has to be set
	ChildThread *childThread=ChildThread::getChildThread(getTid());

	// is a switch necessary?
	if (childThread->getPid()!=getTid()){
		childThread->queueAction(action,event);
		return;
	}

	action->executeDirect(event);
}

