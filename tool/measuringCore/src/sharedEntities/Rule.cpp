/*
 * RuleBase.cpp
 *
 *  Created on: Feb 8, 2012
 *      Author: ruedi
 */

#include "Rule.h"
#include "sharedEntities/EventPredicateBase.h"

Rule::~Rule() {
	// TODO Auto-generated destructor stub
}

void Rule::handle(EventBase *event)
{
	if (getPredicate()->doesMatch(event)){
		getAction()->execute(event);
	}
}




