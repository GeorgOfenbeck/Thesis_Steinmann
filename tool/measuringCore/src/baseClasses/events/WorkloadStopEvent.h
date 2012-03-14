/*
 * WorkloadStopEvent.h
 *
 *  Created on: Feb 15, 2012
 *      Author: ruedi
 */

#ifndef WORKLOADSTOPEVENT_H_
#define WORKLOADSTOPEVENT_H_

#include "baseClasses/EventBase.h"

class WorkloadStopEvent: public EventBase {
    int workloadId;
public:
    WorkloadStopEvent(int id){
    	workloadId=id;
    }
    virtual ~WorkloadStopEvent();

    int getWorkloadId() const
    {
        return workloadId;
    }

    void setWorkloadId(int workloadId)
    {
        this->workloadId = workloadId;
    }

};

#endif /* WORKLOADSTOPEVENT_H_ */
