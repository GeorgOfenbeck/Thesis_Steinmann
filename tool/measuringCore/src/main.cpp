/*
 * main.cpp
 *
 *  Created on: Feb 6, 2012
 *      Author: ruedi
 */

#include <cstdio>
#include <signal.h>
#include <cstring>
#include <unistd.h>
#include <cstdlib>
#include <sys/types.h>
#include <sys/wait.h>
#include <sys/ptrace.h>
#include <sys/reg.h>
#include <sys/user.h>
#include <sys/syscall.h>
#include <boost/regex.hpp>
#include <string>
#include <vector>
#include <fstream>
#include <iostream>
#include <streambuf>
#include <sstream>
#include <pthread.h>
#include "ChildProcess.h"
#include "ParentProcess.h"

using namespace std;
using namespace boost;





pid_t startChildProcess(int argc, char* argv[]) {
	pid_t childPid = vfork();

	if (childPid == 0) {
		// start the ptrace session
		ptrace(PTRACE_TRACEME);

		// we are in the child process
		execv("childProcess",argv);

		perror("failed to start child process");
	}

	if (childPid == -1) {
		perror("fork failed\n");
		exit(1);
	}

	printf("ChildPid: %i\n",childPid);

	// wait for the child, will send SIGSTOP when started (due to ptrace)
	if (waitpid(childPid, NULL, 0) < 0) {
		perror("error on wait");
		exit(1);
	}

	// set ptrace options
	if (ptrace(PTRACE_SETOPTIONS, childPid, 0,
			PTRACE_O_TRACEEXIT | PTRACE_O_TRACECLONE |PTRACE_O_TRACESYSGOOD) < 0) {
		perror("error on ptrace set options");
		exit(1);
	}

	// restart child
	if (ptrace(PTRACE_CONT, childPid, 0, 0) < 0) {
		perror("error on ptrace syscall");
		exit(1);
	}

	printf("restarted\n");

/*
	int status;
	// the child will first call notify, so we know where the notify instruction
	// and the entrance to the child notification code lies
	if (waitpid(childPid, &status, 0) < 0) {
		perror("error on wait");
		exit(1);
	}

	if (WIFSTOPPED(status)){
		int stopSig = WSTOPSIG(status);
		printf("stopped signal: %i\n",stopSig);

		if (stopSig==SIGTRAP){
			int event = (status >> 16) & 0xFF;
			printf("event: %i\n",event);
		}

	}
	else{
		printf("not stopped\n");
	}

	// read the registers
	user_regs_struct regs;
	if (ptrace(PTRACE_GETREGS, childPid, NULL, &regs) < 0) {
		perror("error reading child regs");
		exit(1);
	}

	// store the address of the notify instruction
	ParentProcess::notifyAddress=regs.eip;
	ParentProcess::notificationProcedureEntry=regs.edx;

	printf("parent: notificationProcedureEntry %x\n",ParentProcess::notificationProcedureEntry);

	// restart child, we will wait for it again in the trace loop
	if (ptrace(PTRACE_CONT, childPid, 0, 0) < 0) {
		perror("error on ptrace syscall");
		exit(1);
	}*/

	return childPid;
}


int main(int argc, char* argv[]) {
	//ChildProcess child;
	//child.main(argc,argv);
	//return 0;

	printf("Hello World\n");

	// start the child process
	pid_t childPid = startChildProcess(argc,argv);

	// start the parent process
	ParentProcess parent(childPid);
	parent.traceLoop();

	printf("return from main\n");
	return 0;
}
