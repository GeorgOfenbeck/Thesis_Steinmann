/*
 * ChildProcess.cpp
 *
 *  Created on: Feb 8, 2012
 *      Author: ruedi
 */

#include "ChildProcess.h"
#include <vector>
#include <pthread.h>
#include <cstdio>
#include <cstdlib>
#include <unistd.h>
#include <syscall.h>
#include <stdint.h>
#include "ParentProcess.h"

#define THREADCOUNT 2
using namespace std;

void foo(uint32_t pid){
	uint32_t a[2];
	a[0]=pid;
	a[1]=0;
	uint32_t p=(uint32_t)a;
	printf("foo: %i\n",*(uint32_t*)p);
	ParentProcess::notifyParent(ParentNotification_QueueProcessActions,(uint32_t)a);
}

static void *ThreadStart(void *arg) {
	pid_t tid = syscall(__NR_gettid);
	printf("child: Thread Start %i\n", tid);
	foo(tid);
	// do some work
	long sum = 0;
	for (int i = 0; i < 100000; i++)
		for (int j = 0; j < 10000; j++)
			sum++;
	printf("child: tid: %i sum: %li\n", tid, sum);


	return NULL;
}


void ChildProcess::main()
{
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
		printf("child: joining thead %i\n", i);
		if (pthread_join(threads[i], NULL) != 0) {
			printf("child: error on join\n");
			perror("child: join");
			exit(1);
		}
		printf("child: joined thead %i\n", i);
	}

	ThreadStart(NULL);
}

