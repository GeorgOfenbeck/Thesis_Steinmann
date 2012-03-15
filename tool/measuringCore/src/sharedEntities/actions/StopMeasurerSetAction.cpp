/*
 * StopMeasurerSetAction.cpp
 *
 *  Created on: Mar 15, 2012
 *      Author: ruedi
 */

#include "StopMeasurerSetAction.h"
#include "sharedEntities/MeasurerSet.h"

StopMeasurerSetAction::~StopMeasurerSetAction() {
	// TODO Auto-generated destructor stub
}

void StopMeasurerSetAction::executeImp(EventBase* event) {
	getMeasurerSet()->stop();
}


