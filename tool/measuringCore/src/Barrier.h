/*
 * Barrier.h
 *
 *  Created on: Mar 15, 2012
 *      Author: ruedi
 */

#ifndef BARRIER_H_
#define BARRIER_H_

#include <pthread.h>
#include "Mutex.h"

class Barrier {
	Mutex mutex;
	pthread_cond_t condvar;
	bool barrierClosed;
	void waitImp(bool waitAlways);
public:
	Barrier();
	~Barrier();
	void waitIfClosed();
	void waitAlways();
	void open();
};

#endif /* BARRIER_H_ */
