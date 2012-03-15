/*
 * ActionBase.h
 *
 *  Created on: Feb 8, 2012
 *      Author: ruedi
 */

#ifndef ACTIONBASE_H_
#define ACTIONBASE_H_

#include "sharedEntities/ActionBaseData.h"
#include "baseClasses/EventBase.h"
class ActionBase: public ActionBaseData {
	void startExecute(ActionBase *action, EventBase *event);
protected:
	virtual void executeImp(EventBase *event)=0;
public:
	ActionBase(){
		setTid(-1);
		setWorkload(NULL);
	}
	virtual ~ActionBase();

	/**
	 * execute this action asynchronously if restricted to a different thread, synchronously
	 * if this action can be executed on the current thread
	 */
	void startExecute(EventBase *event);

	/**
	 * Execute synchronously
	 */
	void execute(EventBase *event);

	/**
	 * execute the action without checking the thread
	 */
	void executeDirect(EventBase *event) {
		executeImp(event);
	}

	virtual void initialize() {
	}
	;
	virtual void dispose() {
	}
	;
};

#endif /* ACTIONBASE_H_ */
