/*
 * StopKernelAction.h
 *
 *  Created on: Feb 15, 2012
 *      Author: ruedi
 */

#ifndef STOPKERNELACTION_H_
#define STOPKERNELACTION_H_

#include "sharedEntities/actions/StopKernelActionData.h"
class StopKernelAction : public StopKernelActionData{
public:
	virtual ~StopKernelAction();
	void execute(EventBase *event);

};

#endif /* STOPKERNELACTION_H_ */
