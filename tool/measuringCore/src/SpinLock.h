/*
 * SpinLock.h
 *
 *  Created on: Mar 30, 2012
 *      Author: ruedi
 */

#ifndef SPINLOCK_H_
#define SPINLOCK_H_
#include <pthread.h>

class SpinLock {
	pthread_spinlock_t spinlock;
public:
	SpinLock();
	virtual ~SpinLock();

	void lock();
	void unLock();
};

#endif /* SPINLOCK_H_ */
