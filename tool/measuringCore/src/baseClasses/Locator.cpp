/*
 * EventDispatcher.cpp
 *
 *  Created on: Feb 15, 2012
 *      Author: ruedi
 */

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

std::vector<IEventListener*> Locator::listeners;

void Locator::dispatchEvent(EventBase *event) {
	foreach(Rule *rule, measurement->getRules())
			{
				rule->handle(event);
			}

	foreach (IEventListener *listener, listeners)
			{
				listener->handleEvent(event);
			}
}

void Locator::setMeasurement(Measurement *measurement, MeasurementRunOutput *runOutput) {
	Locator::measurement = measurement;
	Locator::runOutput = runOutput;
}

Workload* Locator::getWorkload(int id) {
	set<SharedEntityBase*> all;
	measurement->addAll(all);

	foreach(SharedEntityBase *obj, all)
			{
				Workload *workload = dynamic_cast<Workload*>(obj);
				if (workload != NULL && workload->getId() == id) {
					return workload;
				}
			}
	return NULL;
}

void Locator::addEventListener(IEventListener *listener) {
	listeners.push_back(listener);
}

KernelBase *Locator::getKernel(int id) {
	set<SharedEntityBase*> all;
	measurement->addAll(all);

	foreach(SharedEntityBase *obj, all)
			{
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
	runOutput->getMeasurerSetOutputs().push_back(setOutput);
}


