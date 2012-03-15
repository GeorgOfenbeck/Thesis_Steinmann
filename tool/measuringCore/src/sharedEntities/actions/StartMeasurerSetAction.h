/*
 * StartMeasurerSetAction.h
 *
 *  Created on: Mar 15, 2012
 *      Author: ruedi
 */

#ifndef STARTMEASURERSETACTION_H_
#define STARTMEASURERSETACTION_H_
#include "sharedEntities/actions/StartMeasurerSetActionData.h"

class StartMeasurerSetAction :public StartMeasurerSetActionData{
protected:
	void executeImp(EventBase *event);
};

#endif /* STARTMEASURERSETACTION_H_ */
