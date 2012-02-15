/*
 * EventDispatcher.cpp
 *
 *  Created on: Feb 15, 2012
 *      Author: ruedi
 */

#include "Locator.h"

#include "sharedDOM/RuleBase.h"
#include "sharedDOM/Measurement.h"
#include "sharedDOM/Workload.h"

#include "utils.h"

Measurement *Locator::measurement;

void Locator::dispatchEvent(EventBase *event)
{
	foreach(RuleBase *rule, measurement->getRules()){
		printf("Locator::dispatchEvnet(): loop\n");
		rule->handleEvent(event);
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

KernelBase *Locator::getKernel(int id)
{
	foreach(Workload *workload, measurement->getWorkloads()){
		if (workload->getKernel()->getId()==id){
			return workload->getKernel();
		}
	}
	return NULL;
}







