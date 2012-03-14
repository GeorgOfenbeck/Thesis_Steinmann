/*
 * MeasureActionExecutionAction.cpp
 *
 *  Created on: Mar 8, 2012
 *      Author: ruedi
 */
#include "Logger.h"
#include "MeasureActionExecutionAction.h"
#include "sharedEntities/MeasurerSet.h"
#include "baseClasses/Locator.h"

MeasureActionExecutionAction::~MeasureActionExecutionAction() {
	// TODO Auto-generated destructor stub
}

void MeasureActionExecutionAction::execute(EventBase *event)
{
	LENTER

	// initialize
	getAction()->initialize();
	getMeasurerSet()->initialize();

	// measure action
	getMeasurerSet()->start();
	getAction()->execute(event);
	getMeasurerSet()->stop();

	// store action result
	Locator::addMeasurerSetOutput(getMeasurerSet()->getOutput());

	// dispose
	getMeasurerSet()->dispose();
	getAction()->dispose();

	LLEAVE
}




