/*
 * FlushKernelBuffersAction.h
 *
 *  Created on: Mar 8, 2012
 *      Author: ruedi
 */

#ifndef FLUSHKERNELBUFFERSACTION_H_
#define FLUSHKERNELBUFFERSACTION_H_

#include "sharedEntities/actions/FlushKernelBuffersActionData.h"
class FlushKernelBuffersAction: public FlushKernelBuffersActionData {
public:
	virtual ~FlushKernelBuffersAction();

	virtual void execute(EventBase *event);
};

#endif /* FLUSHKERNELBUFFERSACTION_H_ */
