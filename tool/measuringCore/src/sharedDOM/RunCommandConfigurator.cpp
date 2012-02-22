/*
 * SystemConfigurator.cpp
 *
 *  Created on: Feb 1, 2012
 *      Author: ruedi
 */

#include "RunCommandConfigurator.h"
#include "sharedDOM/RunCommand.h"
#include <cstdlib>
#include <sys/wait.h>
#include "utils.h"
#include <cstdio>

using namespace std;

RunCommandConfigurator::~RunCommandConfigurator() {
	// TODO Auto-generated destructor stub
}

void RunCommandConfigurator::beforeMeasurement(){
	runConfigurator(getBeforeMeasurementCommands());
}

void RunCommandConfigurator::afterMeasurement(){
	runConfigurator(getAfterMeasurementCommands());
}

void RunCommandConfigurator::beforeRun(){
	runConfigurator(getBeforeRunCommands());
}


void RunCommandConfigurator::afterRun(){
	runConfigurator(getAfterRunCommands());
}

void RunCommandConfigurator::runConfigurator(std::vector<RunCommand*> &commands)
{
	foreach(RunCommand *command, commands){
		pid_t pid=fork();

		// are we the child process?
		if (pid==0){
			const char * cmd[command->getArgs().size()+1];
			memset(cmd, 0, sizeof(char*) * (command->getArgs().size()+1));

			int idx=0;

			foreach(string arg, command->getArgs()){
				cmd[idx++]=arg.c_str();
			}
			cmd[idx++]=NULL;

			if (execvp(command->getExecutable().c_str(),(char*const*)cmd)<0){
				perror("failed to start system configurator");
				exit(1);
			}
		}

		// check if fork succeeded
		if (pid<0){
			throw "failed to fork";
		}

		int status;

		// wait for the child
		if (waitpid(pid,&status,0)<0){
			perror("waiting for child process failed");
			exit(1);
		}

		// check if the child exited
		if (!WIFEXITED(status)){
			perror("child dit not termiate");
			exit(1);
		}

		// chck if the child ran succefully
		if (WEXITSTATUS(status)!=0){
			throw "systemConfigurator encountered an error";
		}
	}
}
