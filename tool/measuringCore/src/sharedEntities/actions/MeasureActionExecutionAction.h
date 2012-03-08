/*
 * MeasureActionExecutionAction.h
 *
 *  Created on: Mar 8, 2012
 *      Author: ruedi
 */

#ifndef MEASUREACTIONEXECUTIONACTION_H_
#define MEASUREACTIONEXECUTIONACTION_H_

#include "sharedEntities/actions/MeasureActionExecutionActionData.h"
class MeasureActionExecutionAction:  public MeasureActionExecutionActionData {
public:
	virtual ~MeasureActionExecutionAction();
	virtual void execute(EventBase *event);
	void initialize();
	void dispose();
};

#endif /* MEASUREACTIONEXECUTIONACTION_H_ */
