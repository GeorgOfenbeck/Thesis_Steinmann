/*
 * RuleBase.h
 *
 *  Created on: Feb 8, 2012
 *      Author: ruedi
 */

#ifndef RULEBASE_H_
#define RULEBASE_H_

#include "sharedDOM/RuleBaseData.h"

class EventBase;

class RuleBase: public RuleBaseData {
public:
	virtual ~RuleBase();

	virtual void handleEvent(EventBase *event)=0;
};

#endif /* RULEBASE_H_ */
