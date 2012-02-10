/*
 * ParentProcess.h
 *
 *  Created on: Feb 8, 2012
 *      Author: ruedi
 */

#ifndef PARENTPROCESS_H_
#define PARENTPROCESS_H_

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

enum ChildEvent{

};

enum ParentNotification{
	ParentNotification_ProcessingDone,
	ParentNotification_QueueProcessActions,
};

enum ChildNotification{
	ChildNotification_Started,
	ChildNotification_ProcessActions,
	ChildNotification_ChildExited,
};

class ParentProcess {
	map<pid_t,ChildState> childStates;
	map<pid_t,user_regs_struct> childRegs;
	map<pid_t,queue<pair<ChildNotification,uint32_t> >* > childNotificationQueue;

	pid_t mainChild;

	void handleChildExited(pid_t stoppedChild);
	void handleChildCloned(pid_t clonePid, pid_t stoppedChild);
	void setupChildNotification(pid_t stoppedChild);
	void handleTrapOccured(pid_t stoppedChild);
	bool handleNotification(pid_t stoppedChild, ParentNotification event, uint32_t arg);
	int handleSignalReceived(pid_t stoppedChild, int signal);
	void queueNotification(pid_t stoppedChild, pid_t receiver, ChildNotification event, uint32_t arg);

	bool hasPendingNotification(pid_t child);
    void setupChildNotification(pid_t stoppedChild, ChildNotification event, uint32_t arg);

public:
	ParentProcess(pid_t mainChild){
		this->mainChild=mainChild;
	}
	void traceLoop();
	static void notifyParent(ParentNotification event, uint32_t arg);
	static int32_t notifyAddress;
};

#endif /* PARENTPROCESS_H_ */

