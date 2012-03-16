/*
 * CreateMeasurerOnThreadAction.h
 *
 *  Created on: Mar 15, 2012
 *      Author: ruedi
 */

#ifndef CREATEMEASURERONTHREADACTION_H_
#define CREATEMEASURERONTHREADACTION_H_

#include "sharedEntities/actions/CreateMeasurerOnThreadActionData.h"

class ChildThread;

class CreateMeasurerOnThreadAction: public CreateMeasurerOnThreadActionData {
public:
	virtual ~CreateMeasurerOnThreadAction();

	virtual void executeImp(EventBase *event);

private:
	void installMeasurer(ChildThread *childThread, EventBase* event);
	void createOnExistingNonMeasurementThreads(EventBase* event);
};

#endif /* CREATEMEASURERONTHREADACTION_H_ */
