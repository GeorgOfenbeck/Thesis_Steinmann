/*
 * StartMeasurerSetAction.cpp
 *
 *  Created on: Mar 15, 2012
 *      Author: ruedi
 */

#include "StartMeasurerSetAction.h"
#include "sharedEntities/MeasurerSet.h"

void StartMeasurerSetAction::executeImp(EventBase* event) {
	getMeasurerSet()->start();
}
