/*
 * ParentProcess.cpp
 *
 *  Created on: Feb 8, 2012
 *      Author: ruedi
 */
#define PARENTPROCESS_CPP_

#include "ParentProcess.h"
#include "Logger.h"

#include <sys/wait.h>
#include <sys/ptrace.h>
#include <cstdio>
#include <cstdlib>
#include <sys/syscall.h>
#include <unistd.h>
#include <errno.h>

#include <boost/foreach.hpp>

#define foreach         BOOST_FOREACH
#define reverse_foreach BOOST_REVERSE_FOREACH

void ParentProcess::handleChildExited(pid_t stoppedChild) {
	LENTER
	switch (childStates[stoppedChild]) {
	case ChildState_Stopping:
	case ChildState_New:
	case ChildState_Running:
		childStates.erase(stoppedChild);
		childNotificationQueue.erase(stoppedChild);
		childRegs.erase(stoppedChild);
		queueNotification(stoppedChild, mainChild,
				ChildNotification_ChildExited, stoppedChild);
		LLEAVE
		return;
	default:
		break;
	}
	printf("handleChildExited: transition not supported");
	exit(1);

}

void ParentProcess::handleChildCloned(pid_t stoppedChild, pid_t clonePid) {
	LDEBUG("cloned pid: %i",clonePid)

	switch (childStates[stoppedChild]) {
	case ChildState_Running:
	case ChildState_Stopping:
	case ChildState_New:
		childStates[clonePid] = ChildState_New;
		return;
	default:
		break;
	}
	printf("handleChildCloned: transition not supported");
	exit(1);
}

int ParentProcess::handleSignalReceived(pid_t stoppedChild, int signal) {
	LDEBUG("signal: %s",strsignal(signal))
	switch (childStates[stoppedChild]) {
	case ChildState_New:
		// only accept the initial SigStop notification
		if (signal == SIGSTOP) {
			if (notificationSystemReady) {
				printf("handleSignalReceived: notificationSystemReady\n");
				// setup the initilization notification
				setupChildNotification(stoppedChild, ChildNotification_Started,
						0);
			} else {
				queueNotification(stoppedChild, stoppedChild,
						ChildNotification_Started, 0);
			}
			LLEAVE
			return 0;
		}
		printf("childstate: NEW, signal=%i\n", signal);
		break;
	case ChildState_Running:
	case ChildState_Stopping:
	case ChildState_ProcessingNotification:
		// forward the signal to the child
		LTRACE("forwarding signal to child")
		return signal;
	}

	printf("handleSignalReceived: transition not supported");
	exit(1);
	return 1;
}

void ParentProcess::handleTrapOccured(pid_t stoppedChild) {
	LENTER
	// read the child registers
	user_regs_struct regs;
	if (ptrace(PTRACE_GETREGS, stoppedChild, NULL, &regs) < 0) {
		perror("trapOccured:error reading child regs");
		exit(1);
	}

	ParentNotification notification = (ParentNotification) regs.ecx;
	uint32_t arg = regs.edx;

	switch (childStates[stoppedChild]) {
	case ChildState_New:
		if (stoppedChild == mainChild
				&& notification == ParentNotification_Startup) {
			// this is the first trap in the main child, which notifies us of the
			// position of the notification int3 instruction and the child notification
			// entry point
			notifyAddress = regs.eip;
			notificationProcedureEntry = regs.edx;

			LDEBUG("got startup notification")
			LDEBUG("child procedure entry: %x",notificationProcedureEntry)
			LDEBUG("address notifications come from child: %x",notifyAddress);
			notificationSystemReady = true;

			// stop processes with pending notifications
			typedef map<pid_t, ChildState>::value_type StatePair;
			foreach(StatePair state, childStates)
					{
						if (state.second == ChildState_Running) {
							syscall(__NR_tgkill, mainChild, state.first,
									SIGTRAP);
							childStates[state.first] = ChildState_Stopping;
						}
					}

			childStates[mainChild] = ChildState_Running;
			return;
		}
		break;
	case ChildState_Stopping:
	case ChildState_Running:

		// check if a notification was sent
		if (regs.eip == notifyAddress) {
			LTRACE("trap comes from the parent notification address of the child")
			if (!handleNotification(stoppedChild, notification, arg)) {
				LWARNING("unhandled notification received: %s", ParentNotificationNames[notification]);
				//exit(1);
			}
		}

		// wether there was a notification or this was a wakeup signal, check the notification queue
		if (hasPendingNotification(stoppedChild) && notificationSystemReady) {
			setupChildNotification(stoppedChild);
		}

		return;
	case ChildState_ProcessingNotification:
		// check if a processing done notification was sent
		if (regs.eip == notifyAddress) {

			if (notification == ParentNotification_ProcessingDone) {
				LDEBUG("child %i finished processing a notification",stoppedChild);

				// we are done with processing, check the queue
				if (hasPendingNotification(stoppedChild)) {
					LDEBUG("there are more pending notifications, setting up the next for processing");
					setupChildNotification(stoppedChild);
				} else {
					// there are no more notifications

					// set the new state
					childStates[stoppedChild] = ChildState_Running;
					user_regs_struct childReg = childRegs[stoppedChild];

					LDEBUG("there are no more pending notification, continuing");

					// restore previous registers
					ptrace(PTRACE_SETREGS, stoppedChild, NULL, &childReg);

					// remove previous state from store
					childRegs.erase(stoppedChild);
				}
				return;
			} else {

				// handle normal notifications
				if (handleNotification(stoppedChild, notification, arg))
					return;
			}
		} else {
			// we did not receive a notification, thus the trap must come from a wakeup call, just ignore it
			return;
		}
		break;
	default:
		break;
	}

	LERROR("transition not supported. ChildState: %i\n",
			childStates[stoppedChild]);
	exit(1);
}

bool ParentProcess::handleNotification(pid_t stoppedChild,
		ParentNotification event, uint32_t arg) {
	switch (event) {
	case ParentNotification_QueueProcessActions: {
		uint32_t p = arg;
		// iterate over array pointed to by arg
		while (1) {
			// read word from array
			long word = ptrace(PTRACE_PEEKDATA, stoppedChild, p, NULL);
			if (word == -1 && errno) {
				perror("peekdata");
				exit(1);
			}

			// check if the end of the array is reached
			if (word == 0) {
				break;
			}

			// queue the process actions
			queueNotification(stoppedChild, word,
					ChildNotification_ProcessActions, 0);

			// advance pointer
			p += 4;
		}
		return true;
	}
	default:
		break;
	}
	return false;
}

void ParentProcess::setupChildNotification(pid_t stoppedChild,
		ChildNotification event, uint32_t arg) {

	LDEBUG("stopped child: %i, event: %s, arg: %i",
			stoppedChild, ChildNotificationNames[event], arg);

	if (notificationSystemReady == false) {
		LWARNING("Notification System Not Ready!!!");
	}
	childStates[stoppedChild] = ChildState_ProcessingNotification;
	user_regs_struct regs;
	// are the registers of the child saved already?
	if (childRegs.count(stoppedChild) > 0) {
		// reuse them
		regs = childRegs[stoppedChild];
	} else {
		// read the stoppedChild registers
		if (ptrace(PTRACE_GETREGS, stoppedChild, NULL, &regs) < 0) {
			perror("error reading child regs");
			exit(1);
		}

		// store them
		childRegs[stoppedChild] = regs;
	}

	// set the target address
	regs.eip = notificationProcedureEntry;
	regs.eax = stoppedChild;
	regs.ebx = event;
	regs.ecx = arg;

	// set the modified registers
	ptrace(PTRACE_SETREGS, stoppedChild, NULL, &regs);
}

void ParentProcess::setupChildNotification(pid_t stoppedChild) {
	ChildNotification event;
	uint32_t arg;

	// check if there is a notification
	if (!hasPendingNotification(stoppedChild)) {
		LERROR("queue was empty");
		exit(1);
	}
	// pop notification
	{
		queue<pair<ChildNotification, uint32_t> >* queue =
				childNotificationQueue[stoppedChild];
		event = queue->front().first;
		arg = queue->front().second;
		queue->pop();
	}

	setupChildNotification(stoppedChild, event, arg);
}

void ParentProcess::traceLoop() {
	while (1) {
		int status;
		pid_t stoppedPid;

		// wait for the child
		LTRACE("Parent: waitForChild");
		stoppedPid = waitpid(-1, &status, __WALL);
		if (stoppedPid < 0) {
			perror("mainloop: error on wait");
			exit(1);
		}

		LDEBUG("stoppedPid: %i", stoppedPid);

		// check if the child exited
		if (WIFEXITED(status)) {
			// the child exited
			if (stoppedPid == mainChild)
				break;

			LDEBUG("WIFEXITED\n");
			// check if the child has been exited already, if not, do it
			if (childStates.count(stoppedPid) > 0)
				handleChildExited(stoppedPid);
			continue;
		}

		// check if the child is known
		if (childStates.count(stoppedPid) == 0) {
			LWARNING("unknown child stopped %i", stoppedPid);
			if (WIFSTOPPED(status)) {
				int stopSig = WSTOPSIG(status);
				LDEBUG("stopped by signal %s",strsignal(stopSig))
				if (ptrace(PTRACE_CONT, stoppedPid, 0, 0) < 0) {
					perror("cont: error on ptrace syscall");
					exit(1);
				}
			}
			continue;
		}

		LDEBUG("childState: %s", ChildStateNames[childStates[stoppedPid]]);

		// check if the child is stopped
		if (WIFSTOPPED(status)) {
			int stopSig = WSTOPSIG(status);
			int sendSig = 0;
			// check if we have a trap
			if (stopSig == SIGTRAP) {
				// get the event which caused the trap
				int event = (status >> 16) & 0xFF;
				LDEBUG("got sigtrap, event: %i", event);
				if (event == PTRACE_EVENT_EXIT) {
					LDEBUG("SIGTRAP | EVENT_EXIT<<16 %i", stoppedPid);
					if (stoppedPid == mainChild)
						break;
					handleChildExited(stoppedPid);
				} else if (event == PTRACE_EVENT_CLONE) {
					uint32_t newChildPid;
					ptrace(PTRACE_GETEVENTMSG, stoppedPid, 0, &newChildPid);
					handleChildCloned(stoppedPid, newChildPid);
				} else if (event == 0) {
					handleTrapOccured(stoppedPid);
				} else
					LWARNING("unknown event %i\n", event)

			} else {
				sendSig = handleSignalReceived(stoppedPid, stopSig);
			}

			// continue the child
			//printf("continue %i\n",stoppedPid);
			if (ptrace(PTRACE_CONT, stoppedPid, 0, sendSig) < 0) {
				perror("cont: error on ptrace syscall");
				exit(1);
			}
		}
	}

	// let the main child continue
	if (ptrace(PTRACE_DETACH, mainChild, 0, 0) < 0) {
		perror("cont: error on ptrace syscall");
		exit(1);
	}

}

void ParentProcess::queueNotification(pid_t stoppedChild, pid_t receiver,
		ChildNotification event, uint32_t arg) {
	queue<pair<ChildNotification, uint32_t> >* queue;
	if (childNotificationQueue.count(receiver) > 0) {
		queue = childNotificationQueue[receiver];
	} else {
		queue = new ::queue<pair<ChildNotification, uint32_t> >();
		childNotificationQueue[receiver] = queue;
	}
	queue->push(make_pair(event, arg));

	if (stoppedChild != receiver) {
		switch (childStates[receiver]) {
		case ChildState_ProcessingNotification:
		case ChildState_Stopping:
		case ChildState_New:
			// child will stop anyways
			break;
		case ChildState_Running:
			syscall(__NR_tgkill, mainChild, receiver, SIGTRAP);
			childStates[receiver] = ChildState_Stopping;
			break;
		}
	}
}

bool ParentProcess::hasPendingNotification(pid_t child) {
	return childNotificationQueue.count(child) > 0
			&& !childNotificationQueue[child]->empty();
}

