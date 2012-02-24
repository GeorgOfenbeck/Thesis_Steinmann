/*
 * ThreadStartRule.h
 *
 *  Created on: Feb 9, 2012
 *      Author: ruedi
 */

#ifndef THREADSTARTEVENTPREDICATE_H_
#define THREADSTARTEVENTPREDICATE_H_

#include "sharedEntities/eventPredicates/ThreadStartEventPredicateData.h"
class EventBase;

class ThreadStartEventPredicate : public ThreadStartEventPredicateData {
public:
	bool doesMatch(EventBase *event);
};

#endif /* THREADSTARTRULE_H_ */
