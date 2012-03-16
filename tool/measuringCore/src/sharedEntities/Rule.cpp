/*
 * RuleBase.cpp
 *
 *  Created on: Feb 8, 2012
 *      Author: ruedi
 */

#include "Logger.h"
#include "Rule.h"
#include "sharedEntities/EventPredicateBase.h"
#include <typeinfo>

Rule::~Rule() {
	// TODO Auto-generated destructor stub
}

void Rule::handle(EventBase *event) {
	if (getPredicate()->doesMatch(event)) {
		LTRACE("starting action %s", typeid(*getAction()).name())
		getAction()->execute(event);
	}
}

