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

using namespace std;

CreateMeasurerOnThreadAction::~CreateMeasurerOnThreadAction() {
}

void CreateMeasurerOnThreadAction::executeImp(EventBase* event) {
	LENTER
	// get existing threads
	vector<ChildThread*> childThreads = ChildThread::getChildThreads();
	vector<Workload*> workloads=Locator::getWorkloads();

	foreach(ChildThread *childThread, childThreads) {
		// check if the childThread is no workload thread
		bool isWorkloadThread=false;
		foreach(Workload *workload, workloads){
			if (workload->getChildThread()==childThread){
				isWorkloadThread=true;
			}
		}
		if (isWorkloadThread) continue;

		pid_t pid = childThread->getPid();

		// clone measurer set
		MeasurerSet *measurerSetClone;
		{
			map<PolymorphicBase*, PolymorphicBase*> map;
			measurerSetClone = (MeasurerSet*) getMeasurerSet()->clone(map);
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
			Rule *rule = new Rule();
			rule->setPredicate(getStartPredicate());

			StartMeasurerSetAction *action = new StartMeasurerSetAction();
			action->setMeasurerSet(measurerSetClone);
			action->setTid(pid);
			rule->setAction(action);

			Locator::addRule(rule);
		}

		// setup stop rule
		{
			Rule *rule = new Rule();
			rule->setPredicate(getStopPredicate());

			StopMeasurerSetAction *action = new StopMeasurerSetAction();
			action->setMeasurerSet(measurerSetClone);
			action->setTid(pid);
			rule->setAction(action);

			Locator::addRule(rule);
		}

		// setup read rule
		{
			Rule *rule = new Rule();
			rule->setPredicate(getReadPredicate());

			ReadMeasurerSetAction *action = new ReadMeasurerSetAction();
			action->setMeasurerSet(measurerSetClone);
			action->setTid(pid);
			rule->setAction(action);

			Locator::addRule(rule);
		}

		// setup dispose rule
		{
			Rule *rule = new Rule();
			rule->setPredicate(getDisposePredicate());

			DisposeMeasurerSetAction *action = new DisposeMeasurerSetAction();
			action->setMeasurerSet(measurerSetClone);
			action->setTid(pid);
			rule->setAction(action);

			Locator::addRule(rule);
		}
	}
	LLEAVE
}

