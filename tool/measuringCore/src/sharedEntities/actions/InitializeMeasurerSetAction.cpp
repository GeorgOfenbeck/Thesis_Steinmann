/*
 * InitializeMeasurerSetAction.cpp
 *
 *  Created on: Mar 15, 2012
 *      Author: ruedi
 */

#include "InitializeMeasurerSetAction.h"
#include "sharedEntities/MeasurerSet.h"


void InitializeMeasurerSetAction::executeImp(EventBase* event) {
	getMeasurerSet()->initialize();
}
