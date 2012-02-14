/*
 * ChildProcess.h
 *
 *  Created on: Feb 8, 2012
 *      Author: ruedi
 */

#ifndef CHILDPROCESS_H_
#define CHILDPROCESS_H_
#include "Notifications.h"
#include "stdint.h"

class ChildProcess {
public:
	int main(int argc, char* argv[]);
	static void notifyParent(ParentNotification event, uint32_t arg);
};

#endif /* CHILDPROCESS_H_ */
