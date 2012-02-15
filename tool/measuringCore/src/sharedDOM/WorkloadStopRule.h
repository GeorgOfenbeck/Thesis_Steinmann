/*
 * WorkloadStopRule.h
 *
 *  Created on: Feb 9, 2012
 *      Author: ruedi
 */

#ifndef WORKLOADSTOPRULE_H_
#define WORKLOADSTOPRULE_H_

#include "sharedDOM/WorkloadStopRuleData.h"
class WorkloadStopRule : public WorkloadStopRuleData{
public:
	void handleEvent(EventBase *event);
};

#endif /* WORKLOADSTOPRULE_H_ */
