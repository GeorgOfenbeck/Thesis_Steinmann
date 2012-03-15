/*
 * ReadMeasurerSetAction.cpp
 *
 *  Created on: Mar 15, 2012
 *      Author: ruedi
 */

#include "ReadMeasurerSetAction.h"
#include "sharedEntities/MeasurerSet.h"
#include "baseClasses/Locator.h"

void ReadMeasurerSetAction::executeImp(EventBase* event) {
	Locator::addMeasurerSetOutput(getMeasurerSet()->getOutput());
}
