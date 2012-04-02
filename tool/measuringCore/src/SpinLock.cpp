/*
 * SpinLock.cpp
 *
 *  Created on: Mar 30, 2012
 *      Author: ruedi
 */

#include "SpinLock.h"
#include <cstdio>
#include <cstdlib>
#include <cstring>

SpinLock::~SpinLock() {
	pthread_spin_destroy(&spinlock);
}

void SpinLock::lock() {
	int rc = pthread_spin_lock(&spinlock);
	if (rc != 0) {
		printf("spinlock_lock: %s", strerror(rc));
		exit(1);
	}
}

SpinLock::SpinLock() {
	pthread_spin_init(&spinlock, 0);
}

void SpinLock::unLock() {
	int rc = pthread_spin_unlock(&spinlock);
	if (rc != 0) {
		printf("pthread_spin_unlock: %s", strerror(rc));
		exit(1);
	}
}

