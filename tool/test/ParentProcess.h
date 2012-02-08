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

using namespace std;

enum ChildState{
	ChildState_New,
	ChildState_Running,
	ChildState_Processing,
};

enum NotifyEvent{
	NotifyEvent_Done,
};

class ParentProcess {
	map<pid_t,ChildState> childStates;
	map<pid_t,user_regs_struct> childRegs;

	void childExited(pid_t child);
	void childCloned(pid_t clonePid);
	void sendProcessCommandToChild(pid_t clonePid);
	void processingDone(pid_t clonePid);
	void trapOccured(pid_t child);
public:
	void traceLoop(pid_t childPid);
	static void notify(NotifyEvent event, uint32_t arg);
	static int32_t notifyAddress;
};

#endif /* PARENTPROCESS_H_ */

