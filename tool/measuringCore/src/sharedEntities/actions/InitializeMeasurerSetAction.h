/*
 * InitializeMeasurerSetAction.h
 *
 *  Created on: Mar 15, 2012
 *      Author: ruedi
 */

#ifndef INITIALIZEMEASURERSETACTION_H_
#define INITIALIZEMEASURERSETACTION_H_

#include "sharedEntities/actions/InitializeMeasurerSetActionData.h"

class InitializeMeasurerSetAction: public InitializeMeasurerSetActionData {
protected:
	void executeImp(EventBase *event);
};

#endif /* INITIALIZEMEASURERSETACTION_H_ */
