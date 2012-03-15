/*
 * MeasurementRunEventPredicate.cpp
 *
 *  Created on: Mar 15, 2012
 *      Author: ruedi
 */

#include "MeasurementRunEventPredicate.h"
#include "baseClasses/events/MeasurementRunEvent.h"

MeasurementRunEventPredicate::~MeasurementRunEventPredicate() {
	// TODO Auto-generated destructor stub
}

bool MeasurementRunEventPredicate::doesMatch(EventBase* event) {
	MeasurementRunEvent *measurementRunEvent =
			dynamic_cast<MeasurementRunEvent*>(event);
	if (measurementRunEvent != NULL) {
		// check event
		if (measurementRunEvent->getEvent() != getEventNr()) {
			return false;
		}
		return true;
	}
	return false;
}

