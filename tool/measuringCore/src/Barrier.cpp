/*
 * Barrier.cpp
 *
 *  Created on: Mar 15, 2012
 *      Author: ruedi
 */

#include "Barrier.h"
#include <cstdio>
#include <cstdlib>
#include <cstring>

Barrier::Barrier() {

	pthread_cond_init(&condvar, NULL);
	barrierClosed = true;
}

Barrier::~Barrier() {

	pthread_cond_destroy(&condvar);
}

void Barrier::waitIfClosed() {
	waitImp(false);
}

void Barrier::waitAlways() {
	waitImp(true);
}

void Barrier::open() {
	int rc;

	mutex.lock();

	barrierClosed = false;

	rc = pthread_cond_broadcast(&condvar);
	if (rc != 0) {
		printf("pthread_cond_signal: %s", strerror(rc));
		exit(1);
	}

	mutex.unLock();
}

void Barrier::waitImp(bool waitAlways) {
	int rc;
	mutex.lock();

	if (barrierClosed || waitAlways) {
		rc = pthread_cond_wait(&condvar, &mutex.getMutex());
		if (rc != 0) {
			printf("pthread_cond_wait: %s", strerror(rc));
			exit(1);
		}
	}

	mutex.unLock();
}


