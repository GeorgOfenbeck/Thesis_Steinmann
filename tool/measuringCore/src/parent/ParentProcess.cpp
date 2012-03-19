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

#ifdef _LP64
#define REG(regs,reg) (regs).r##reg
#else
#define REG(regs,reg) (regs).e##reg
#endif

void ParentProcess::handleChildThreadExited(pid_t stoppedChild) {
	LENTER
	// exiting is always allowed. We can't do anything about it, anyways
	childStates.erase(stoppedChild);
	childNotificationQueue.erase(stoppedChild);
	childRegs.erase(stoppedChild);
	queueNotification(stoppedChild, mainChild, ChildNotification_ThreadExited,
			stoppedChild);
	LLEAVE
}

void ParentProcess::handleTrapOccured(pid_t stoppedChild) {
	LENTER
	// read the child registers
	user_regs_struct regs;
	if (ptrace(PTRACE_GETREGS, stoppedChild, NULL, &regs) < 0) {
		perror("trapOccured:error reading child regs");
		exit(1);
	}
	ParentNotification notification = (ParentNotification) REG(regs,cx);
	uint32_t arg = REG(regs,dx);

	if (stoppedChild == mainChild && notification == ParentNotification_Startup
			&& !notificationSystemReady) {
		// this is the first trap in the main child, which notifies us of the
		// position of the notification int3 instruction and the child notification
		// entry point
		notifyAddress = REG(regs,ip);
		notificationProcedureEntry = REG(regs,dx);

		LDEBUG("got startup notification")
		LDEBUG("child procedure entry: %lx", notificationProcedureEntry)
		LDEBUG("address notifications come from child: %lx", notifyAddress);
		notificationSystemReady = true;

		// stop processes with pending notifications
		typedef map<pid_t, ChildState>::value_type StatePair;
		foreach(StatePair state, childStates) {
			if (state.second == ChildState_Running
					&& hasPendingNotification(state.first)) {
				syscall(__NR_tgkill, mainChild, state.first, SIGTRAP);
				childStates[state.first] = ChildState_Stopping;
			}
		}

		childStates[mainChild] = ChildState_Running;
		return;
	}

	switch (childStates[stoppedChild]) {
	case ChildState_Stopping:
	case ChildState_Running:

		// check if a notification was sent
		if ((unsigned long) REG(regs,ip) == notifyAddress) {
			LTRACE(
					"trap comes from the parent notification address of the child")
			if (!handleNotification(stoppedChild, notification, arg)) {
				LWARNING("unhandled notification received: %s",
						ParentNotificationNames[notification]);
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
		if ((unsigned long) REG(regs,ip) == notifyAddress) {

			if (notification == ParentNotification_ProcessingDone) {
				LDEBUG("child %i finished processing a notification",
						stoppedChild);

				// we are done with processing, check the queue
				if (hasPendingNotification(stoppedChild)) {
					LDEBUG(
							"there are more pending notifications, setting up the next for processing");
					setupChildNotification(stoppedChild);
				} else {
					// there are no more notifications

					// set the state back to running
					childStates[stoppedChild] = ChildState_Running;
					user_regs_struct childReg = childRegs[stoppedChild];

					LDEBUG(
							"there are no more pending notification, continuing");

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
		ParentNotification event, long arg) {
	switch (event) {
	case ParentNotification_QueueProcessActions: {
		// queue the process actions
		queueNotification(stoppedChild, arg,
				ChildNotification_ProcessActions, 0);
		return true;
	}
	default:
		break;
	}
	return false;
}

void ParentProcess::setupChildNotification(pid_t stoppedChild,
		ChildNotification event, long arg) {

	LDEBUG("stopped child: %i, event: %s, arg: %li",
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

#ifdef _LP64
	regs.rip = notificationProcedureEntry;
	regs.rax = stoppedChild;
	regs.rbx = event;
	regs.rcx = arg;
#else
	regs.eip = notificationProcedureEntry;
	regs.eax = stoppedChild;
	regs.ebx = event;
	regs.ecx = arg;
#endif
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
		queue<pair<ChildNotification, long> >* queue =
				childNotificationQueue[stoppedChild];
		event = queue->front().first;
		arg = queue->front().second;
		queue->pop();
	}

	setupChildNotification(stoppedChild, event, arg);
}

int ParentProcess::traceLoop() {
	childStates[mainChild]=ChildState_Running;

	int exitStatus = 0;
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
			if (stoppedPid == mainChild) {
				exitStatus = WEXITSTATUS(status);
				break;
			}

			LDEBUG("WIFEXITED\n");
			// check if the child has been exited already, if not, do it
			if (childStates.count(stoppedPid) > 0)
				handleChildThreadExited(stoppedPid);
			continue;
		}

		int sendSig = 0;

		// check if the child is stopped
		if (WIFSTOPPED(status)) {
			int stopSig = WSTOPSIG(status);
			bool sysGood = stopSig & 0x80;
			stopSig = stopSig & 0x7F;

			LDEBUG("Stopped with signal %s and sysGood %i",
					strsignal(stopSig), (int) sysGood)

			// check if the child is known
			if (childStates.count(stoppedPid) == 0) {
				LDEBUG("new child observed: %i", stoppedPid)
				// child is not known, add as running
				childStates[stoppedPid] = ChildState_Running;

				// and mark as not processing a syscall
				childInSyscall[stoppedPid] = false;

				// only accept the initial SigStop notification
				if (stopSig != SIGSTOP) {
					LERROR("new child was not stopped first by SIGSTOP, instead %s was used",strsignal(stopSig))
					exitStatus = 1;
					break;
				}

				if (notificationSystemReady) {
					LTRACE("notificationSystemReady\n");
					// setup the initilization notification
					setupChildNotification(stoppedPid,
							ChildNotification_ThreadStarted, 0);
				} else {
					queueNotification(stoppedPid, stoppedPid,
							ChildNotification_ThreadStarted, 0);
				}
			}
			// did we receive the entry or exit of a syscall?
			else if (stopSig == SIGTRAP && sysGood) {
				if (!childInSyscall[stoppedPid]) {
					LTRACE("Syscall enter")

					childInSyscall[stoppedPid] = true;
				} else {
					LTRACE("Syscall leave")
					childInSyscall[stoppedPid] = false;
				}

				// and continue
			}
			// check if we have a trap
			else if (stopSig == SIGTRAP) {
				// get the event which caused the trap
				int event = (status >> 16) & 0xFF;
				LDEBUG("got sigtrap, event: %i", event);
				if (event == PTRACE_EVENT_EXIT) {
					LDEBUG("SIGTRAP and EVENT_EXIT %i", stoppedPid);
					if (stoppedPid == mainChild) {
						// retrieve the exit status
						if (ptrace(PTRACE_GETEVENTMSG, stoppedPid, 0,
								&exitStatus) < 0) {
							perror("error on geteventmsg");
							exit(1);
						}
						exitStatus = WEXITSTATUS(exitStatus);

						LDEBUG("Exit Status: %i", exitStatus);
						break;
					}

					handleChildThreadExited(stoppedPid);
				}else if (event == PTRACE_EVENT_CLONE) {
					LTRACE("SIGTRAP and EVENT_CLONE")
					// The child cloned another thread.
					// We don't have anything to do now, since the
					// child will be stopped with SIG_STOP, which we will detect.
				} else if (event == 0) {
					handleTrapOccured(stoppedPid);
				} else
					LWARNING("unknown event %i\n", event)

			} else {
				LDEBUG("forwarding signal to child")
				// forward the signal to the child
				sendSig = stopSig;
			}

			// continue the child
			//printf("continue %i\n",stoppedPid);
			if (ptrace(PTRACE_SYSCALL, stoppedPid, 0, sendSig) < 0) {
				LERROR("error on PTRACE_SYSCALL")
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

	return exitStatus;

}

void ParentProcess::queueNotification(pid_t stoppedChild, pid_t receiver,
		ChildNotification event, long arg) {
	queue<pair<ChildNotification, long> >* queue;
	if (childNotificationQueue.count(receiver) > 0) {
		queue = childNotificationQueue[receiver];
	} else {
		queue = new ::queue<pair<ChildNotification, long> >();
		childNotificationQueue[receiver] = queue;
	}
	queue->push(make_pair(event, arg));

	if (stoppedChild != receiver) {
		switch (childStates[receiver]) {
		case ChildState_ProcessingNotification:
		case ChildState_Stopping:
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

