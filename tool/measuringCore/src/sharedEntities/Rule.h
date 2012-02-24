/*
 * RuleBase.h
 *
 *  Created on: Feb 8, 2012
 *      Author: ruedi
 */

#ifndef RULE_H_
#define RULE_H_

#include "sharedEntities/RuleData.h"
#include "sharedEntities/ActionBase.h"

class EventBase;

class Rule: public RuleData {
public:
	virtual ~Rule();

	void handle(EventBase *event);

	void initialize(){
		if (getAction()!=NULL){
			getAction()->initialize();
		}
	}

	void dispose(){
		if (getAction()!=NULL){
			getAction()->dispose();
		}
	}
};

#endif /* RULEBASE_H_ */
