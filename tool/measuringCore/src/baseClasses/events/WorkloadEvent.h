/*
 * WorkloadStartEvent.h
 *
 *  Created on: Feb 15, 2012
 *      Author: ruedi
 */

#ifndef WORKLOADEVENT_H_
#define WORKLOADEVENT_H_

#include "baseClasses/EventBase.h"

class Workload;

enum WorkloadEventEnum {
	WorkloadEvent_Start,
	WorkloadEvent_Stop,
	WorkloadEvent_KernelStart,
	WorkloadEvent_KernelStop,
};

class WorkloadEvent: public EventBase {
    Workload *workload;
    WorkloadEventEnum event;

public:
    WorkloadEvent(Workload *workload, WorkloadEventEnum event)
    {
        this->workload = workload;
        this->event = event;
    }

    virtual ~WorkloadEvent();

    WorkloadEventEnum getEvent() const
    {
        return event;
    }

    Workload* getWorkload() const
    {
        return workload;
    }
};

#endif /* WORKLOADEVENT_H_ */
