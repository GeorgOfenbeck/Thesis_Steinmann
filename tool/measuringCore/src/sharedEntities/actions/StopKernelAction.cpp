/*
 * StopKernelAction.cpp
 *
 *  Created on: Feb 15, 2012
 *      Author: ruedi
 */

#include "StopKernelAction.h"
#include "baseClasses/Locator.h"
#include "sharedEntities/KernelBase.h"

StopKernelAction::~StopKernelAction() {
	// TODO Auto-generated destructor stub
}

void StopKernelAction::executeImp(EventBase *event)
{
	getKernel()->setKeepRunning(false);
}



