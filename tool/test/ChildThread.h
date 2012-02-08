/*
 * ChildThread.h
 *
 *  Created on: Feb 8, 2012
 *      Author: ruedi
 */

#ifndef CHILDTHREAD_H_
#define CHILDTHREAD_H_
#include <map>
#include <sys/types.h>

using namespace std;
class ChildThread {
public:
	static map<pid_t, ChildThread> threadMap;
	static void process();
};

#endif /* CHILDTHREAD_H_ */
