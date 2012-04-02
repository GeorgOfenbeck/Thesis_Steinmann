/*
 * WaitForPressureBarrierAction.h
 *
 *  Created on: Mar 29, 2012
 *      Author: ruedi
 */

#ifndef WAITFORPRESSUREBARRIERACTION_H_
#define WAITFORPRESSUREBARRIERACTION_H_

#include "sharedEntities/actions/WaitForPressureBarrierActionData.h"

class WaitForPressureBarrierAction: public WaitForPressureBarrierActionData {
protected:
	void executeImp(EventBase *event);
public:
	virtual ~WaitForPressureBarrierAction();
};

#endif /* WAITFORPRESSUREBARRIERACTION_H_ */
