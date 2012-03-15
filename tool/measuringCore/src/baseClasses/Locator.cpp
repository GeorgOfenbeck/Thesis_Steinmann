/*
 * EventDispatcher.cpp
 *
 *  Created on: Feb 15, 2012
 *      Author: ruedi
 */
#include "Logger.h"

#include "Locator.h"

#include "sharedEntities/Rule.h"
#include "sharedEntities/Measurement.h"
#include "sharedEntities/Workload.h"
#include "sharedEntities/ActionBase.h"
#include "sharedEntities/MeasurementRunOutput.h"

#include "utils.h"

#include <set>

using namespace std;

Measurement *Locator::measurement;
MeasurementRunOutput *Locator::runOutput;
Mutex Locator::mutex;

vector<IEventListener*> Locator::listeners;

void Locator::dispatchEvent(EventBase *event) {
	LENTER

	// copy the rules, or generate an empty vector if no measurement is present
	vector<Rule*> rules;
	mutex.lock();
	if (measurement != NULL) {
		rules = measurement->getRules();
	}
	mutex.unLock();

	LTRACE("evaluating rules")
	// evaluate all rules
	foreach(Rule *rule, rules) {
		rule->handle(event);
	}

	// copy the listeners
	mutex.lock();
	vector<IEventListener*> listenersCopy = listeners;
	mutex.unLock();

	// notify listeners
	foreach (IEventListener *listener, listenersCopy) {
		listener->handleEvent(event);
	}
	LLEAVE
}

void Locator::setMeasurement(Measurement *measurement,
		MeasurementRunOutput *runOutput) {
	mutex.lock();
	Locator::measurement = measurement;
	Locator::runOutput = runOutput;
	listeners.clear();
	mutex.unLock();
}

Workload* Locator::getWorkload(int id) {
	set<SharedEntityBase*> all;
	measurement->addAll(all);

	foreach(SharedEntityBase *obj, all) {
		Workload *workload = dynamic_cast<Workload*>(obj);
		if (workload != NULL && workload->getId() == id) {
			return workload;
		}
	}
	return NULL;
}

std::vector<Workload*> Locator::getWorkloads() {
	vector<Workload*> result;

	set<SharedEntityBase*> all;
	measurement->addAll(all);

	foreach(SharedEntityBase *obj, all) {
		Workload *workload = dynamic_cast<Workload*>(obj);
		if (workload != NULL) {
			result.push_back(workload);
		}
	}
	return result;
}

void Locator::addEventListener(IEventListener *listener) {
	mutex.lock();
	listeners.push_back(listener);
	mutex.unLock();
}

KernelBase *Locator::getKernel(int id) {
	set<SharedEntityBase*> all;
	measurement->addAll(all);

	foreach(SharedEntityBase *obj, all) {
		KernelBase *kernel = dynamic_cast<KernelBase*>(obj);
		if (kernel != NULL && kernel->getId() == id) {
			return kernel;
		}
	}
	return NULL;
}

IEventListener::~IEventListener() {
}

void Locator::addMeasurerSetOutput(MeasurerSetOutput* setOutput) {
	mutex.lock();
	runOutput->getMeasurerSetOutputs().push_back(setOutput);
	mutex.unLock();
}

void Locator::addRule(Rule* rule) {
	mutex.lock();
	measurement->getRules().push_back(rule);
	mutex.unLock();
}

