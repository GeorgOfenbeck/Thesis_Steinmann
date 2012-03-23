/*
 * CreateMeasurerOnThreadAction.cpp
 *
 *  Created on: Mar 15, 2012
 *      Author: ruedi
 */

#include "Logger.h"
#include "CreateMeasurerOnThreadAction.h"
#include "ChildThread.h"
#include <vector>
#include "utils.h"
#include <map>
#include "InitializeMeasurerSetAction.h"
#include "StartMeasurerSetAction.h"
#include "StopMeasurerSetAction.h"
#include "ReadMeasurerSetAction.h"
#include "DisposeMeasurerSetAction.h"
#include "sharedEntities/Rule.h"
#include "sharedEntities/MeasurerSet.h"
#include "baseClasses/Locator.h"
#include "sharedEntities/Workload.h"

#include <unistd.h>
#include <sys/syscall.h>
#include "sharedEntities/EventPredicateBase.h"
#include "baseClasses/events/ThreadEvent.h"
#include "sharedEntities/eventPredicates/ThreadEventPredicate.h"

using namespace std;

CreateMeasurerOnThreadAction::~CreateMeasurerOnThreadAction() {
}

void CreateMeasurerOnThreadAction::installMeasurer(ChildThread* childThread,
		EventBase* event) {
	pid_t pid = childThread->getPid();
	// clone measurer set
	MeasurerSet* measurerSetClone;
	{
		map<PolymorphicBase*, PolymorphicBase*> map;
		measurerSetClone = (MeasurerSet*) (getMeasurerSet()->clone(map));
	}
	// initialize measurer set
	{
		InitializeMeasurerSetAction action;
		action.setMeasurerSet(measurerSetClone);
		action.setTid(pid);
		action.execute(NULL);
	}

	// setup start rule
	{
		StartMeasurerSetAction* action = new StartMeasurerSetAction();
		action->setMeasurerSet(measurerSetClone);
		action->setTid(pid);

		if (getStartPredicate() == NULL) {
			// if no start predicat is set, start the measurer immediately
			action->execute(event);
		} else {
			Rule* rule = new Rule();
			rule->setPredicate(getStartPredicate());
			rule->setAction(action);
			Locator::addRule(rule);

			// in case we want the measurer to be started immediately, we have
			// to start the action ourselves, since the Locator will not see the rule
			// while processing the current event
			if (getStartPredicate()->doesMatch(event)) {
				action->execute(event);
			}
		}
	}

	// setup stop rule
	{
		Rule* rule = new Rule();
		rule->setPredicate(getStopPredicate());
		StopMeasurerSetAction* action = new StopMeasurerSetAction();
		action->setMeasurerSet(measurerSetClone);
		action->setTid(pid);
		rule->setAction(action);
		Locator::addRule(rule);
	}

	// setup read rule
	{
		Rule* rule = new Rule();
		rule->setPredicate(getReadPredicate());
		ReadMeasurerSetAction* action = new ReadMeasurerSetAction();
		action->setMeasurerSet(measurerSetClone);
		action->setTid(pid);
		rule->setAction(action);
		Locator::addRule(rule);
	}

	// setup dispose rule
	{
		Rule* rule = new Rule();
		rule->setPredicate(getDisposePredicate());
		DisposeMeasurerSetAction* action = new DisposeMeasurerSetAction();
		action->setMeasurerSet(measurerSetClone);
		action->setTid(pid);
		rule->setAction(action);
		Locator::addRule(rule);
	}
}

void CreateMeasurerOnThreadAction::createOnExistingNonMeasurementThreads(
		EventBase* event) {

	// get existing threads and add rule to get notified if new threads are spawned
	// this is done in one call, to avoid race condition
	vector<ChildThread*> childThreads;
	{
		Rule *rule=new Rule();
		rule->setPredicate(new ThreadEventPredicate());
		rule->setAction(this);

		childThreads = ChildThread::getChildThreadsAndAddRule(rule);
	}

	vector<Workload*> workloads = Locator::getWorkloads();
	foreach(ChildThread * childThread, childThreads)
	{

		// check if the childThread is no workload thread
		bool isWorkloadThread = false;
		foreach(Workload * workload, workloads)
		{
			if (workload->getChildThread() == childThread) {
				isWorkloadThread = true;
			}
		}
		if (isWorkloadThread)
			continue;

		// make sure we are not instrumenting the current thread
		{
			int tid = syscall(__NR_gettid);
			if (childThread->getPid() == tid)
				continue;
		}

		installMeasurer(childThread, event);
	}
}

void CreateMeasurerOnThreadAction::executeImp(EventBase* event) {
	LENTER
	ThreadEvent *startEvent=dynamic_cast<ThreadEvent*>(event);

	if (startEvent!=NULL && startEvent->getEvent()==ThreadEventEnum_Started){
		installMeasurer(startEvent->getChildThread(),event);
	}
	else{
		createOnExistingNonMeasurementThreads(event);
	}
	LLEAVE
}

