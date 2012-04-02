/*
 * WaitForPressureBarrierAction.cpp
 *
 *  Created on: Mar 29, 2012
 *      Author: ruedi
 */

#include "WaitForPressureBarrierAction.h"
#include "sharedEntities/actions/PressureBarrier.h"

void WaitForPressureBarrierAction::executeImp(EventBase* event) {
	getBarrier()->wait(getPressure());
}

WaitForPressureBarrierAction::~WaitForPressureBarrierAction() {
	// TODO Auto-generated destructor stub
}

