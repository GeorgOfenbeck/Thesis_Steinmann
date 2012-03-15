/*
 * Mutex.h
 *
 *  Created on: Mar 15, 2012
 *      Author: ruedi
 */

#ifndef MUTEX_H_
#define MUTEX_H_
#include <pthread.h>

class Mutex{
	pthread_mutex_t mutex;
public:
	Mutex();
	~Mutex();
	void lock();
	void unLock();

	pthread_mutex_t &getMutex() {
		return mutex;
	}

};

#endif /* MUTEX_H_ */
