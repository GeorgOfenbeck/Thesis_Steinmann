/*
 * ThreadStartRule.h
 *
 *  Created on: Feb 9, 2012
 *      Author: ruedi
 */

#ifndef THREADEVENTPREDICATE_H_
#define THREADEVENTPREDICATE_H_

#include "sharedEntities/eventPredicates/ThreadEventPredicateData.h"
class EventBase;

class ThreadEventPredicate : public ThreadEventPredicateData {
public:
	bool doesMatch(EventBase *event);
};

#endif
