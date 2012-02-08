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

#define THREADCOUNT 2
using namespace std;

static void *ThreadStart(void *arg) {
	pid_t tid = syscall(__NR_gettid);
	printf("child: Thread Start %i\n", tid);
	// do some work
	long sum = 0;
	for (int i = 0; i < 100000; i++)
		for (int j = 0; j < 1000; j++)
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
}

