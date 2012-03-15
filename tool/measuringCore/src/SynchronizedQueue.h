/*
 * SynchronizedQueue.h
 *
 *  Created on: Mar 15, 2012
 *      Author: ruedi
 */

#ifndef SYNCHRONIZEDQUEUE_H_
#define SYNCHRONIZEDQUEUE_H_

#include <queue>
#include "Mutex.h"

template<typename T >
class SynchronizedQueue {
	std::queue<T> queue;
	Mutex mutex;
public:
	void push(const T &x){
		mutex.lock();
		queue.push(x);
		mutex.unLock();
	}

	bool pop(T& x){
		mutex.lock();

		// if the queue is empty, return false;
		if (queue.empty()){
			mutex.unLock();
			return false;
		}

		// pop the first element
		x=queue.front();
		queue.pop();

		// return true
		mutex.unLock();
		return true;
	}
};

#endif /* SYNCHRONIZEDQUEUE_H_ */
