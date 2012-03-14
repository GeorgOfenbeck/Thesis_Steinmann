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
#include "Logger.h"

using namespace std;
using namespace boost;


pid_t startChildProcess(int argc, char* argv[]) {
	LENTER
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

	LDEBUG("Child process pid: %i",childPid)

	// wait for the child, will send SIGSTOP when started (due to ptrace)
	if (waitpid(childPid, NULL, 0) < 0) {
		perror("error on wait");
		exit(1);
	}

	// set ptrace options
	if (ptrace(PTRACE_SETOPTIONS, childPid, 0,
			PTRACE_O_TRACEEXIT |PTRACE_O_TRACESYSGOOD) < 0) {
		perror("error on ptrace set options");
		exit(1);
	}

	// restart child
	if (ptrace(PTRACE_CONT, childPid, 0, 0) < 0) {
		perror("error on ptrace syscall");
		exit(1);
	}

	LLEAVE
	return childPid;
}


int main(int argc, char* argv[]) {
	LENTER

	// start the child process
	pid_t childPid = startChildProcess(argc,argv);

	// start the parent process
	ParentProcess parent(childPid);
	int exitStatus=parent.traceLoop();

	LDEBUG("exit: %i",exitStatus)
	LLEAVE
	return exitStatus;
}
