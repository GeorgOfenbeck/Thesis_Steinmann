/*
 * Mutex.cpp
 *
 *  Created on: Mar 15, 2012
 *      Author: ruedi
 */

#include "Mutex.h"
#include <cstdio>
#include <cstdlib>
#include <cstring>

Mutex::Mutex() {
	pthread_mutex_init(&mutex, NULL);
}

Mutex::~Mutex() {
	pthread_mutex_destroy(&mutex);
}

void Mutex::lock() {
	int rc = pthread_mutex_lock(&mutex);
	if (rc != 0) {
		printf("mutex_lock: %s", strerror(rc));
		exit(1);
	}
}

void Mutex::unLock() {
	int rc = pthread_mutex_unlock(&mutex);
	if (rc != 0) {
		printf("pthread_mutex_unlock: %s", strerror(rc));
		exit(1);
	}
}
