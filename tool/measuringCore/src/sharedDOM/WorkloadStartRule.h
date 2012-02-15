/*
 * WorkloadStartRule.h
 *
 *  Created on: Feb 9, 2012
 *      Author: ruedi
 */

#ifndef WORKLOADSTARTRULE_H_
#define WORKLOADSTARTRULE_H_

#include "sharedDOM/WorkloadStartRuleData.h"
class WorkloadStartRule: public WorkloadStartRuleData {
public:
	bool doesMatch(EventBase *event);
};

#endif /* WORKLOADSTARTRULE_H_ */
