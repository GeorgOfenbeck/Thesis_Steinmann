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

using namespace std;
using namespace boost;

#if defined(__i386)
#define REGISTER_IP EIP
#define TRAP_LEN    1
#define TRAP_INST   0xCC
#define TRAP_MASK   0xFFFFFF00

#elif defined(__x86_64)
#define REGISTER_IP RIP
#define TRAP_LEN    1
#define TRAP_INST   0xCC
#define TRAP_MASK   0xFFFFFFFFFFFFFF00

#else
#error Unsupported architecture
#endif

void startSingleStepping() {
	asm volatile (
			"pushfw\n"
			"popw %%ax\n"
			"orw $0x0100, %%ax \n"
			"pushw %%ax \n"
			"popfw\n"
			: //out
			://int
			: "%eax"

	);
}

void stopSingleStepping() {
	asm volatile(
			"pushfw\n"
			"popw %%ax\n"
			"andw $0x0FEFF, %%ax \n"
			"pushw %%ax \n"
			"popfw\n"
			: //out
			://int
			: "%ax"

	);
}

struct fileLocation {
	bool valid;
	string fileName;
	uint32_t offset, start, stop;
	fileLocation() {
		valid = false;
	}
};

string readFile(string fileName) {
	std::ifstream t("file.txt");
	std::string str;

	t.seekg(0, std::ios::end);
	str.reserve(t.tellg());
	t.seekg(0, std::ios::beg);

	str.assign((std::istreambuf_iterator<char>(t)),
			std::istreambuf_iterator<char>());

	return str;
}

fileLocation getFileLocation(pid_t pid, uint32_t addr) {
	stringstream fileName;
	fileName << "/proc/" << pid << "/maps";
	//cout<<fileName.str()<<"\n";

	ifstream f(fileName.str().c_str());

	regex expression(
			"([0-9,a-f]*)\\-([0-9,a-f]*) .... ([0-9,a-f]*) [0-9,a-f][0-9,a-f]:[0-9,a-f][0-9,a-f] [0-9,a-f]* *(.*)?");

	while (f.good()) {
		string line;
		getline(f, line);
		//cout <<"<"<< line << ">"<<endl;
		if (line.empty())
			continue;
		cmatch what;
		if (!regex_match(line.c_str(), what, expression)) {
			cout << "failed to match line of map " << line.c_str() << "\n";
			exit(1);
		}
		/*cout<<"matched line ";
		 for (unsigned int i=1; i<what.size(); i++){
		 if (what[i].length()==0)
		 continue;
		 cout<<what[i].str()<<"|";
		 }
		 cout<<"\n";*/

		uint32_t start, stop;
		sscanf(what[1].str().c_str(), "%x", &start);
		sscanf(what[2].str().c_str(), "%x", &stop);

		if (addr >= start && addr < stop) {
			cout << "found desired address\n";
			if (what[4].length() == 0)
				continue;
			fileLocation result;
			sscanf(what[3].str().c_str(), "%x", &result.offset);
			// adjust the offset
			result.offset += addr - start;
			result.fileName = what[4].str();
			result.valid = true;
			result.start = start;
			result.stop = stop;
			return result;
		}

	}

	if (!f.eof()) {
		cerr << "error opening map file\n";
		exit(1);
	}
	f.close();
	return fileLocation();
}

#define THREADCOUNT 2

void *ThreadStart(void *arg) {
	pid_t tid=syscall(__NR_gettid);
	printf("child: Thread Start %li\n", tid);
	// do some work
	long sum = 0;
	for (int i = 0; i < 100000; i++)
		for (int j = 0; j < 1000; j++)
			sum++;
	printf("child: tid: %i sum: %li\n", tid, sum);

	return NULL;
}

void childMain() {
	// wait till the parent traces the child
	ptrace(PTRACE_TRACEME);
	printf("child: child stopping...\n");
	raise(SIGCHLD);
	printf("child: restarted\n");

	vector<pthread_t> threads;
	pthread_attr_t attr;
	pthread_attr_init(&attr);
	pthread_attr_setdetachstate(&attr, PTHREAD_CREATE_JOINABLE);

	for (int i = 0; i < THREADCOUNT; i++) {
		pthread_t thread;
		pthread_create(&thread, &attr, ThreadStart, NULL);
		threads.push_back(thread);
	}
	pthread_attr_destroy(&attr);

	// join all threads
	for (int i = 0; i < THREADCOUNT; i++) {
		void *ret = NULL;
		printf("child: joining thead %i\n",i);
		if (pthread_join(threads[i], NULL)!=0){
			printf("child: error on join\n");
			perror("child: join");
			exit(1);
		}
		printf("child: joined thead %i\n",i);
	}

	printf("child: exiting\n");

	exit(0);
}

pid_t startChildProcess() {
	pid_t childPid = fork();

	if (childPid == 0) {
		// we are in the child process
		childMain();
	}

	if (childPid == -1) {
		perror("fork failed\n");
		exit(1);
	}

	printf("wait for child\n");
	//wait for the child
	if (waitpid(childPid, NULL, 0) < 0) {
		perror("error on wait");
		exit(1);
	}

	printf("child stopped\n");

	return childPid;
}

int numInstr;

void trapHandler(int signum, siginfo_t *info, void* ctx) {
	ucontext_t *context = (ucontext_t*) ctx;

	numInstr++;
}

uint32_t getChildIp(pid_t pid) {
	uint32_t v = ptrace(PTRACE_PEEKUSER, pid, sizeof(long) * REGISTER_IP);
	return v;
}

int main(int argc, char* argv[]) {
	printf("Hello World\n");
	numInstr = 0;
	/*
	 struct sigaction newAction;

	 memset(&newAction,0,sizeof(newAction));
	 newAction.sa_sigaction=trapHandler;
	 sigemptyset(&newAction.sa_mask);
	 newAction.sa_flags=SA_SIGINFO;

	 sigaction(SIGTRAP,&newAction,NULL);*/

	pid_t childPid = startChildProcess();

	if (ptrace(PTRACE_SETOPTIONS, childPid, 0,
			PTRACE_O_TRACEEXIT | PTRACE_O_TRACESYSGOOD | PTRACE_O_TRACECLONE)
			< 0) {
		perror("error on ptrace set options");
		exit(1);
	}

	// restart child
	if (ptrace(PTRACE_SYSCALL, childPid, 0, 0) < 0) {
		perror("error on ptrace syscall");
		exit(1);
	}

	while (1) {
		int status;
		pid_t stoppedPid;
		// wait for the child
		//printf("waiting for child\n");

		stoppedPid=waitpid(-1, &status, __WALL);
		if (stoppedPid < 0) {
			printf("==>err\n");
			perror("mainloop: error on wait");
			exit(1);
		}

		// check if the child exited
		if (WIFEXITED(status)) {
			printf("child %i exited\n",stoppedPid);
			// the child exited
			if (stoppedPid==childPid)
				break;
		}

		//printf("stoppedPid: %i\n",stoppedPid);

		// check if the child is stopped
		if (WIFSTOPPED(status)) {
			int stopSig=WSTOPSIG(status);
			// check if we have a trap
			if ((stopSig & 0x3f) == SIGTRAP) {
				//printf("trap 0x%x\n",stopSig);
				// check if we have a trap on a syscall
				if ((stopSig  & 0x80) == 0x80) {
					struct user_regs_struct uregs;
					ptrace(PTRACE_GETREGS, childPid, 0, &uregs);

					//printf("got syscall 0x%lX %ld\n", uregs.orig_eax, uregs.orig_eax);
				} else {

					// get the event which caused the trap
					int event = (status  >> 16) & 0xFF;
					//printf("got sigtrap, event: %i\n", event);
					if (event == PTRACE_EVENT_EXIT) {
						printf("SIGTRAP | EVENT_EXIT<<16\n");
						// the child process exited
						//break;
					} else if (event == PTRACE_EVENT_CLONE) {
						uint32_t newChildPid;
						ptrace(PTRACE_GETEVENTMSG, stoppedPid, 0, &newChildPid);
						printf("got clone with child %i \n", newChildPid);

					} else{
						printf("unknown event\n");
					}
				}
				// continue the child
				//printf("continue %i\n",stoppedPid);
				if (ptrace(PTRACE_SYSCALL, stoppedPid, 0, 0) < 0) {
					perror("cont trap: error on ptrace syscall");
					exit(1);
				}
			} else {
				//printf("child received signal 0x%x, forwarding to child\n",stopSig);
				// continue the child
				if (ptrace(PTRACE_SYSCALL, stoppedPid, 0, stopSig) < 0) {
					perror("cont signal: error on ptrace syscall");
					exit(1);
				}
			}
		}
	}

	printf("return from main\n");
}
