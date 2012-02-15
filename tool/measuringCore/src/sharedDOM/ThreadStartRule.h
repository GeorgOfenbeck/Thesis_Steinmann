/*
 * ThreadStartRule.h
 *
 *  Created on: Feb 9, 2012
 *      Author: ruedi
 */

#ifndef THREADSTARTRULE_H_
#define THREADSTARTRULE_H_

#include "sharedDOM/ThreadStartRuleData.h"
class ThreadStartRule : public ThreadStartRuleData {
public:
	bool doesMatch(EventBase *event);
};

#endif /* THREADSTARTRULE_H_ */
