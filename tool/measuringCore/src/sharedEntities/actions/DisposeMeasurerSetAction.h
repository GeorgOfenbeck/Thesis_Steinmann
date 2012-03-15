/*
 * DisposeMeasurerSetAction.h
 *
 *  Created on: Mar 15, 2012
 *      Author: ruedi
 */

#ifndef DISPOSEMEASURERSETACTION_H_
#define DISPOSEMEASURERSETACTION_H_
#include "sharedEntities/actions/DisposeMeasurerSetActionData.h"

class DisposeMeasurerSetAction: public DisposeMeasurerSetActionData {
protected:
	void executeImp(EventBase *event);
};

#endif /* DISPOSEMEASURERSETACTION_H_ */
