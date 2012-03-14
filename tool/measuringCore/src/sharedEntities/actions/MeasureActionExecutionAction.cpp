/*
 * MeasureActionExecutionAction.cpp
 *
 *  Created on: Mar 8, 2012
 *      Author: ruedi
 */
#include "Logger.h"
#include "MeasureActionExecutionAction.h"
#include "sharedEntities/MeasurerSet.h"
MeasureActionExecutionAction::~MeasureActionExecutionAction() {
	// TODO Auto-generated destructor stub
}

void MeasureActionExecutionAction::execute(EventBase *event)
{
	LENTER
	getAction()->initialize();
	getMeasurerSet()->initialize();

	getMeasurerSet()->start();
	getAction()->execute(event);
	getMeasurerSet()->stop();

	getMeasurerSet()->dispose();
	getAction()->dispose();

	LLEAVE
}




