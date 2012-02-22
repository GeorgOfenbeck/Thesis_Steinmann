/*
 * childMain.cpp
 *
 *  Created on: Feb 14, 2012
 *      Author: ruedi
 */

#include "Logger.h"
#include "ChildProcess.h"
#include "Notifications.h"
#include "ChildThread.h"
#include <stdint.h>
#include <stdlib.h>

int main(int argc, char* argv[]) {
	LDEBUG("address of notification procedure entry: %x", (uint32_t) ChildThread::processNotification)

	// send a notification to let the parent know where the notification instruction is
	ChildProcess::notifyParent(ParentNotification_Startup,(uint32_t) ChildThread::processNotification);

	// start the child process
	ChildProcess child;
	int ret=child.main(argc,argv);

	LDEBUG("Exit value: %i",ret)
	exit(ret);
}

