/*
 * ParentProcess.h
 *
 *  Created on: Feb 8, 2012
 *      Author: ruedi
 */

#ifndef PARENTPROCESS_H_
#define PARENTPROCESS_H_

#include "Notifications.h"

#include <sys/types.h>
#include <sys/user.h>
#include <map>
#include <stdint.h>
#include <utility>
#include <vector>
#include <queue>

using namespace std;

enum ChildState{
	ChildState_New,
	ChildState_Running,
	ChildState_ProcessingNotification,
	ChildState_Stopping,
};

#ifndef PARENTPROCESS_CPP_
extern
#endif
const char *ChildStateNames[]
#ifdef PARENTPROCESS_CPP_
                            ={
		"New","Running","ProcessingNotification","Stopping"
}
#endif
;

class ParentProcess {
	map<pid_t,ChildState> childStates;
	map<pid_t,user_regs_struct> childRegs;
	map<pid_t,queue<pair<ChildNotification,long> >* > childNotificationQueue;

	pid_t mainChild;

	void handleChildExited(pid_t stoppedChild);
	void handleChildCloned(pid_t stoppedChild,pid_t clonePid);
	void setupChildNotification(pid_t stoppedChild);
	void handleTrapOccured(pid_t stoppedChild);
	bool handleNotification(pid_t stoppedChild, ParentNotification event, long arg);
	int handleSignalReceived(pid_t stoppedChild, int signal);
	void queueNotification(pid_t stoppedChild, pid_t receiver, ChildNotification event, long arg);

	bool hasPendingNotification(pid_t child);
    void setupChildNotification(pid_t stoppedChild, ChildNotification event, long arg);

    bool notificationSystemReady;
    unsigned long notifyAddress;
	unsigned long notificationProcedureEntry;
public:
	ParentProcess(pid_t mainChild){
		this->mainChild=mainChild;
		childStates[mainChild]=ChildState_New;
		notificationSystemReady=false;
	}
	int traceLoop();
};

#endif /* PARENTPROCESS_H_ */

