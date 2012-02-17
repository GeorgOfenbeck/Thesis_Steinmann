/*
 * WorkloadStartRule.h
 *
 *  Created on: Feb 9, 2012
 *      Author: ruedi
 */

#ifndef WORKLOADSTARTRULE_H_
#define WORKLOADSTARTRULE_H_

#include "sharedDOM/WorkloadStartEventPredicateData.h"
#include "baseClasses/EventBase.h"

class WorkloadStartEventPredicate: public WorkloadStartEventPredicateData {
public:
	bool doesMatch(EventBase *event);
};

#endif /* WORKLOADSTARTRULE_H_ */
