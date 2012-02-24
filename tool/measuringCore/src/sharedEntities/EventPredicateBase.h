/*
 * EventPredicateBase.h
 *
 *  Created on: Feb 16, 2012
 *      Author: ruedi
 */

#ifndef EVENTPREDICATEBASE_H_
#define EVENTPREDICATEBASE_H_

#include "sharedEntities/EventPredicateBaseData.h"

class EventBase;

class EventPredicateBase: public EventPredicateBaseData {
public:
	virtual bool doesMatch(EventBase *event)=0;
	virtual ~EventPredicateBase();
};

#endif /* EVENTPREDICATEBASE_H_ */
