/*
 * DisposeMeasurerSetAction.cpp
 *
 *  Created on: Mar 15, 2012
 *      Author: ruedi
 */

#include "DisposeMeasurerSetAction.h"
#include "sharedEntities/MeasurerSet.h"

void DisposeMeasurerSetAction::executeImp(EventBase* event) {
	getMeasurerSet()->dispose();
}
