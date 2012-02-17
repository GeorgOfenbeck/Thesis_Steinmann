/*
 * ActionBase.h
 *
 *  Created on: Feb 8, 2012
 *      Author: ruedi
 */

#ifndef ACTIONBASE_H_
#define ACTIONBASE_H_

#include "sharedDOM/ActionBaseData.h"
#include "baseClasses/EventBase.h"
class ActionBase: public ActionBaseData {
public:
	virtual ~ActionBase();
	virtual void execute(EventBase *event)=0;
	virtual void initialize(){};
	virtual void dispose(){};
};

#endif /* ACTIONBASE_H_ */
