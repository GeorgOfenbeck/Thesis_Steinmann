/*
 * ParentProcess.cpp
 *
 *  Created on: Feb 8, 2012
 *      Author: ruedi
 */

#include "ParentProcess.h"
#include "ChildThread.h"

#include <sys/wait.h>
#include <sys/ptrace.h>
#include <cstdio>
#include <cstdlib>
#include <sys/syscall.h>
#include <unistd.h>
#include <errno.h>

int32_t ParentProcess::notifyAddress;

void ParentProcess::handleChildExited(pid_t stoppedChild) {

	switch (childStates[stoppedChild]) {
	case ChildState_Stopping:
	case ChildState_Running:
		childStates.erase(stoppedChild);
		childNotificationQueue.erase(stoppedChild);
		childRegs.erase(stoppedChild);
		queueNotification(stoppedChild, mainChild,
				ChildNotification_ChildExited, stoppedChild);
		return;
	default:
		break;
	}
	printf("handleChildExited: transition not supported");
	exit(0);

}

void ParentProcess::handleChildCloned(pid_t clonePid, pid_t stoppedChild) {
	switch (childStates[stoppedChild]) {
	case ChildState_Running:
		childStates[clonePid] = ChildState_New;
		return;
	default:
		break;
	}
	printf("transition not supported");
	exit(0);
}

int ParentProcess::handleSignalReceived(pid_t stoppedChild, int signal) {
	switch (childStates[stoppedChild]) {
	case ChildState_New:
		// only accept the initial SigStop notification
		if (signal == SIGSTOP) {
			// setup the initilization notification
			setupChildNotification(stoppedChild, ChildNotification_Started, 0);
			return 0;
		}
		break;
	case ChildState_Running:
	case ChildState_Stopping:
	case ChildState_ProcessingNotification:
		// forward the signal to the child
		return signal;
	}

	printf("transition not supported");
	exit(0);
	return 0;
}

void ParentProcess::handleTrapOccured(pid_t stoppedChild) {
	// read the child registers
	user_regs_struct regs;
	if (ptrace(PTRACE_GETREGS, stoppedChild, NULL, &regs) < 0) {
		perror("trapOccured:error reading child regs");
		exit(1);
	}

	ParentNotification event = (ParentNotification) regs.ecx;
	uint32_t arg = regs.edx;

	switch (childStates[stoppedChild]) {
	case ChildState_Stopping:
	case ChildState_Running:

		// check if a notification was sent
		if (regs.eip == notifyAddress) {
			if (!handleNotification(stoppedChild, event, arg)) {
				printf("signal not handled");
				exit(1);
			}
		}

		// wether there was a notification or this was a wakeup signal, check the notification queue
		if (hasPendingNotification(stoppedChild)) {
			setupChildNotification(stoppedChild);
		}

		return;
	case ChildState_ProcessingNotification:
		// check if a processing done notification was sent
		if (regs.eip == notifyAddress) {

			if (event == ParentNotification_ProcessingDone) {
				printf("processingDone %i\n",stoppedChild);
				// we are done with processing, check the queue
				if (hasPendingNotification(stoppedChild)) {
					setupChildNotification(stoppedChild);
				} else {
					// there are no more notifications

					// set the new state
					childStates[stoppedChild] = ChildState_Running;

					// restore previous registers
					ptrace(PTRACE_SETREGS, stoppedChild, NULL,
							&childRegs[stoppedChild]);

					// remove previous state from store
					childRegs.erase(stoppedChild);
				}
				return;
			} else {

				// handle normal notifications
				if (handleNotification(stoppedChild, event, arg))
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

	printf("transition not supported");
	exit(0);
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

			if (childStates.count(word) > 0
					&& childStates[word] == ChildState_Running) {
				// send a signal to make it stop eventually
				syscall(__NR_tgkill, mainChild, word, SIGTRAP);
			}

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

	printf("setupNotification: receiver: %i notification: %i arg: %i\n",stoppedChild,event,arg);
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
	// push the return address
	regs.esp -= 4;
	ptrace(PTRACE_POKEDATA, stoppedChild, regs.esp, regs.eip);
	// set the target address
	regs.eip = (long) (ChildThread::processNotification);
	regs.eax = stoppedChild;
	regs.ebx = event;
	regs.ecx = arg;
	// set the modified
	ptrace(PTRACE_SETREGS, stoppedChild, NULL, &regs);
}

void ParentProcess::setupChildNotification(pid_t stoppedChild) {
	ChildNotification event;
	uint32_t arg;

	// check if there is a notification
	if (!hasPendingNotification(stoppedChild)) {
		printf("queue was empty\n");
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
// add the child to the list of know processes
	childStates[mainChild] = ChildState_Running;

	while (1) {
		int status;
		pid_t stoppedPid;

		// wait for the child
		printf("Parent: waitForChild\n");
		stoppedPid = waitpid(-1, &status, __WALL);
		if (stoppedPid < 0) {
			perror("mainloop: error on wait");
			exit(1);
		}

		// check if the child exited
		if (WIFEXITED(status)) {
			// the child exited
			if (stoppedPid == mainChild)
				break;

			printf("WIFEXITED\n");
			// check if the child has been exited already, if not, do it
			if (childStates.count(stoppedPid) > 0)
				handleChildExited(stoppedPid);
			continue;
		}

		// check if the child is known
		if (childStates.count(stoppedPid) == 0) {
			printf("unknown child stopped %i", stoppedPid);
			if (WIFSTOPPED(status)){
				if (ptrace(PTRACE_CONT, stoppedPid, 0, 0) < 0) {
					perror("cont: error on ptrace syscall");
					exit(1);
				}
			}
			continue;
		}

		// check if the child is stopped
		if (WIFSTOPPED(status)) {
			int stopSig = WSTOPSIG(status);
			int sendSig = 0;
			// check if we have a trap
			if (stopSig == SIGTRAP) {
				// get the event which caused the trap
				int event = (status >> 16) & 0xFF;
				//printf("got sigtrap, event: %i\n", event);
				if (event == PTRACE_EVENT_EXIT) {
					printf("SIGTRAP | EVENT_EXIT<<16 %i\n",stoppedPid);
					if (stoppedPid == mainChild)
						break;
					handleChildExited(stoppedPid);
				} else if (event == PTRACE_EVENT_CLONE) {
					uint32_t newChildPid;
					ptrace(PTRACE_GETEVENTMSG, stoppedPid, 0, &newChildPid);
					handleChildCloned(newChildPid, stoppedPid);
				} else if (event == 0) {
					handleTrapOccured(stoppedPid);
				} else
					printf("unknown event %i\n", event);

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

void ParentProcess::notifyParent(ParentNotification event, uint32_t arg) {
	asm("int3": : "c" (event), "d" (arg));
}

