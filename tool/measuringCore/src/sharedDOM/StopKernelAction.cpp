/*
 * StopKernelAction.cpp
 *
 *  Created on: Feb 15, 2012
 *      Author: ruedi
 */

#include "StopKernelAction.h"
#include "baseClasses/Locator.h"
#include "sharedDOM/KernelBase.h"

StopKernelAction::~StopKernelAction() {
	// TODO Auto-generated destructor stub
}

void StopKernelAction::execute(EventBase *event)
{
	KernelBase *kernel=Locator::getKernel(getKernelId());
	kernel->setKeepRunning(false);
}



