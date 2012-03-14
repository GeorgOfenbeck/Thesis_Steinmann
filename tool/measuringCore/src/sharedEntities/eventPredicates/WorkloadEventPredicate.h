/*
 * WorkloadStartRule.h
 *
 *  Created on: Feb 9, 2012
 *      Author: ruedi
 */

#ifndef WORKLOADEVENTPREDICATE_H_
#define WORKLOADEVENTPREDICATE_H_

#include "sharedEntities/eventPredicates/WorkloadEventPredicateData.h"
#include "baseClasses/EventBase.h"

class WorkloadEventPredicate: public WorkloadEventPredicateData {
public:
	bool doesMatch(EventBase *event);
};

#endif
