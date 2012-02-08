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

int32_t ParentProcess::notifyAddress;

void ParentProcess::childExited(pid_t child) {
	childStates.erase(child);
}

void ParentProcess::childCloned(pid_t clonePid) {
	childStates[clonePid] = ChildState_New;
}

void ParentProcess::sendProcessCommandToChild(pid_t child) {
	printf("setting up %i for process\n", child);

	childStates[child] = ChildState_Processing;

	// read the child registers
	user_regs_struct regs;
	if (ptrace(PTRACE_GETREGS, child, NULL, &regs) < 0) {
		perror("error reading child regs");
		exit(1);
	}

	childRegs[child] = regs;

	// push the return address
	regs.esp -= 4;
	ptrace(PTRACE_POKEDATA, child, regs.esp, regs.eip);

	// set the target address
	regs.eip = (long) ChildThread::process;

	// set the modified
	ptrace(PTRACE_SETREGS, child, NULL, &regs);
}

void ParentProcess::trapOccured(pid_t child)
{
	// read the child registers
	user_regs_struct regs;
	if (ptrace(PTRACE_GETREGS, child, NULL, &regs) < 0) {
		perror("error reading child regs");
		exit(1);
	}

	// chek if a notification was sent
	if (regs.eip==notifyAddress){
		uint32_t event=regs.ecx;
		uint32_t arg=regs.edx;
	}
	else{
		printf("traps not supported yet");
	}
}

void ParentProcess::processingDone(pid_t child) {
	printf("setting up child %i for continuing\n", child);
	childStates[child] = ChildState_Running;

	// set the modified
	ptrace(PTRACE_SETREGS, child, NULL, &childRegs[child]);

	childRegs.erase(child);
}

void ParentProcess::traceLoop(pid_t childPid) {
	// add the child to the list of know processes
	childStates[childPid] = ChildState_Running;

	while (1) {
		int status;
		pid_t stoppedPid;
		// wait for the child
		//printf("waiting for child\n");

		stoppedPid = waitpid(-1, &status, __WALL);
		if (stoppedPid < 0) {
			printf("==>err\n");
			perror("mainloop: error on wait");
			exit(1);
		}

		// check if the child exited
		if (WIFEXITED(status)) {
			printf("child %i exited\n", stoppedPid);
			// the child exited
			if (stoppedPid == childPid)
				break;

			// check if the child has been exited already, if not, do it
			if (childStates.count(stoppedPid) > 0)
				childExited(stoppedPid);
			continue;
		}

		// check if the child is known
		if (childStates.count(stoppedPid) == 0) {
			printf("received signal from unknow child %i", stoppedPid);
			exit(1);
		}

		printf("stoppedPid: %i\n", stoppedPid);

		// check if the child is stopped
		if (WIFSTOPPED(status)) {
			int stopSig = WSTOPSIG(status);
			// check if we have a trap
			if (stopSig == SIGTRAP) {
				// get the event which caused the trap
				int event = (status >> 16) & 0xFF;
				//printf("got sigtrap, event: %i\n", event);
				if (event == PTRACE_EVENT_EXIT) {
					printf("SIGTRAP | EVENT_EXIT<<16\n");
					childExited(stoppedPid);
				} else if (event == PTRACE_EVENT_CLONE) {
					uint32_t newChildPid;
					ptrace(PTRACE_GETEVENTMSG, stoppedPid, 0, &newChildPid);
					printf("got clone with child %i \n", newChildPid);
					childCloned(newChildPid);
				} else if (event == 0
						&& childStates[stoppedPid] == ChildState_Processing) {
					printf("received done\n");
					processingDone(stoppedPid);
				}else if (event==0 && childStates[stoppedPid]==ChildState_Running){
					trapOccured(childPid);
				} else
					printf("unknown event %i\n", event);

				// continue the child
				//printf("continue %i\n",stoppedPid);
				if (ptrace(PTRACE_CONT, stoppedPid, 0, 0) < 0) {
					perror("cont trap: error on ptrace syscall");
					exit(1);
				}
			} else {
				printf("received signal %i from process %i\n", stopSig,
						stoppedPid);
				int sendSig = stopSig;
				// check if this is the stop signal sent when a thread is started
				if (childStates[stoppedPid]
						== ChildState_New && stopSig==SIGSTOP) {
					sendProcessCommandToChild(stoppedPid);
					sendSig = 0;
				}
				// forward the signal to the child
				if (ptrace(PTRACE_CONT, stoppedPid, 0, sendSig) < 0) {
					perror("forwardSignal: error on ptrace syscall");
					exit(1);
				}
			}
		}
	}
}

void ParentProcess::notify(NotifyEvent event, uint32_t arg)
{
	asm("int3": : "c" (event), "d" (arg));
}



