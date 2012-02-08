/*
 * ActionBase.h
 *
 *  Created on: Feb 8, 2012
 *      Author: ruedi
 */

#ifndef ACTIONBASE_H_
#define ACTIONBASE_H_

#include "EventBase.h"
class ActionBase {
public:
	virtual ~ActionBase();
	virtual void execute(EventBase *event)=0;
};

#endif /* ACTIONBASE_H_ */
