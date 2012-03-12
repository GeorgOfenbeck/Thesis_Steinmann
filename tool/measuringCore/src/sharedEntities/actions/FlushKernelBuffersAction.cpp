/*
 * FlushKernelBuffersAction.cpp
 *
 *  Created on: Mar 8, 2012
 *      Author: ruedi
 */

#include "FlushKernelBuffersAction.h"
#include "baseClasses/Locator.h"
#include "sharedEntities/KernelBase.h"
#include "Logger.h"

FlushKernelBuffersAction::~FlushKernelBuffersAction() {
	// TODO Auto-generated destructor stub
}

void FlushKernelBuffersAction::execute(EventBase *event)
{
	LENTER
	getKernel()->flushBuffers();
	LLEAVE
}



