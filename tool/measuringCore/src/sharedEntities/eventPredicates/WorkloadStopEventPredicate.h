/*
 * WorkloadStopRule.h
 *
 *  Created on: Feb 9, 2012
 *      Author: ruedi
 */

#ifndef WORKLOADSTOPEVENTPREDICATE_H_
#define WORKLOADSTOPEVENTPREDICATE_H_

#include "sharedEntities/eventPredicates/WorkloadStopEventPredicateData.h"
class EventBase;
class WorkloadStopEventPredicate : public WorkloadStopEventPredicateData{
public:
	bool doesMatch(EventBase *event);
};

#endif /* WORKLOADSTOPRULE_H_ */
