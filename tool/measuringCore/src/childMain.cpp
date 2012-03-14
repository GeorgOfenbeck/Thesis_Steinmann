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
#include "Exception.h"
#include <string>

using namespace std;

int main(int argc, char* argv[]) {
	LDEBUG("address of notification procedure entry: %x",
			(uint32_t) ChildThread::processNotification)

	// send a notification to let the parent know where the notification instruction is
	ChildProcess::notifyParent(ParentNotification_Startup,
			(long) ChildThread::processNotification);

	// start the child process
	ChildProcess child;
	int ret;
	ret = child.main(argc, argv);
	try {
		ret = child.main(argc, argv);
	} catch (Exception &e) {
		fprintf(stderr, "Exception occurred: %s\n", e.get_message().c_str());
		e.print(2);
		exit(1);
	} catch (Exception *e) {
		fprintf(stderr, "Exception occurred: %s\n", e->get_message().c_str());
		e->print(2);
		exit(1);
	} catch (string &s) {
		fprintf(stderr, "Exception occurred: %s\n", s.c_str());
		exit(1);
	} catch (const char* str) {
		fprintf(stderr, "Exception occurred: %s\n", str);
		exit(1);
	} catch (...) {
		fprintf(stderr, "Exception occurred \n");
		exit(1);
	}

	LDEBUG("Exit value: %i", ret)
	exit(ret);
}

