/*
 * MeasureActionExecutionAction.cpp
 *
 *  Created on: Mar 8, 2012
 *      Author: ruedi
 */

#include "MeasureActionExecutionAction.h"
#include "sharedEntities/MeasurerSet.h"
MeasureActionExecutionAction::~MeasureActionExecutionAction() {
	// TODO Auto-generated destructor stub
}

void MeasureActionExecutionAction::execute(EventBase *event)
{
	getMeasurerSet()->initialize();

	getMeasurerSet()->start();
	getAction()->execute(event);
	getMeasurerSet()->stop();

	getMeasurerSet()->dispose();
}

void MeasureActionExecutionAction::initialize()
{
	getAction()->initialize();

}



void MeasureActionExecutionAction::dispose()
{
	getAction()->dispose();

}





