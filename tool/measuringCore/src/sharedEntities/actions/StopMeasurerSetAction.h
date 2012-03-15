/*
 * StopMeasurerSetAction.h
 *
 *  Created on: Mar 15, 2012
 *      Author: ruedi
 */

#ifndef STOPMEASURERSETACTION_H_
#define STOPMEASURERSETACTION_H_

#include "sharedEntities/actions/StopMeasurerSetActionData.h"

class StopMeasurerSetAction : public StopMeasurerSetActionData{
public:
	virtual ~StopMeasurerSetAction();
	void executeImp(EventBase *event);
};

#endif /* STOPMEASURERSETACTION_H_ */
