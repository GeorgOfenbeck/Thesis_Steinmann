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

#include "utils.h"

Measurement *Locator::measurement;
std::vector<IEventListener*> Locator::listeners;

void Locator::dispatchEvent(EventBase *event)
{
	foreach(Rule *rule, measurement->getRules()){
		rule->handle(event);
	}

	foreach (IEventListener *listener, listeners){
		listener->handleEvent(event);
	}
}



void Locator::setMeasurement(Measurement *measurement)
{
	Locator::measurement=measurement;
}

Workload* Locator::getWorkload(int id)
{
	foreach(Workload *workload, measurement->getWorkloads()){
		if (workload->getId()==id){
			return workload;
		}
	}
	return NULL;
}

void Locator::addEventListener(IEventListener *listener)
{
	listeners.push_back(listener);
}

KernelBase *Locator::getKernel(int id)
{
	foreach(Workload *workload, measurement->getWorkloads()){
		if (workload->getKernel()->getId()==id){
			return workload->getKernel();
		}
	}
	return NULL;
}

IEventListener::~IEventListener()
{
}









