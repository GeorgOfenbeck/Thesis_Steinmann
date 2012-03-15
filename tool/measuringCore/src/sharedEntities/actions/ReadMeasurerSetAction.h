/*
 * ReadMeasurerSetAction.h
 *
 *  Created on: Mar 15, 2012
 *      Author: ruedi
 */

#ifndef READMEASURERSETACTION_H_
#define READMEASURERSETACTION_H_
#include "sharedEntities/actions/ReadMeasurerSetActionData.h"

class ReadMeasurerSetAction: public ReadMeasurerSetActionData {
protected:
	void executeImp(EventBase *event);
};

#endif /* READMEASURERSETACTION_H_ */
