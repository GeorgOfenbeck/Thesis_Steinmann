/*
 * WorkloadStartEvent.h
 *
 *  Created on: Feb 15, 2012
 *      Author: ruedi
 */

#ifndef WORKLOADSTARTEVENT_H_
#define WORKLOADSTARTEVENT_H_

#include "baseClasses/EventBase.h"

class WorkloadStartEvent: public EventBase {
    int workloadId;
public:
    WorkloadStartEvent(int workloadId){
    	this->workloadId=workloadId;
    }
    virtual ~WorkloadStartEvent();
    int getWorkloadId() const
    {
        return workloadId;
    }

    void setWorkloadId(int workloadId)
    {
        this->workloadId = workloadId;
    }

};

#endif /* WORKLOADSTARTEVENT_H_ */
