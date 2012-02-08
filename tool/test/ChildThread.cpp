/*
 * ChildThread.cpp
 *
 *  Created on: Feb 8, 2012
 *      Author: ruedi
 */

#include "ChildThread.h"

#include <cstdio>

map<pid_t, ChildThread> ChildThread::threadMap;

void ChildThread::process()
{
	printf("childThread:Process\n");
	asm("int3");
}


