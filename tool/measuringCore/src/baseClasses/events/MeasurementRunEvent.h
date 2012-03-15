/*
 * WorkloadStartEvent.h
 *
 *  Created on: Feb 15, 2012
 *      Author: ruedi
 */

#ifndef MEASUREMENTRUNEVENT_H_
#define MEASUREMENTRUNEVENT_H_

#include "baseClasses/EventBase.h"

enum MeasurementRunEventEnum {
	MeasurementRunEvent_Start,
	MeasurementRunEvent_Stop,
};

class MeasurementRunEvent: public EventBase {
	MeasurementRunEventEnum event;

public:
    MeasurementRunEvent(MeasurementRunEventEnum event)
    {
        this->event = event;
    }

    virtual ~MeasurementRunEvent();

    MeasurementRunEventEnum getEvent() const
    {
        return event;
    }
};

#endif
