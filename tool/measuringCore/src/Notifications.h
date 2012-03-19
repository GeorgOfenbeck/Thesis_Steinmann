/*
 * Notifications.h
 *
 *  Created on: Feb 14, 2012
 *      Author: ruedi
 */

#ifndef NOTIFICATIONS_H_
#define NOTIFICATIONS_H_

enum ParentNotification{
	ParentNotification_Startup,
	ParentNotification_ProcessingDone,
	ParentNotification_QueueProcessActions,
};

#ifndef NOTIFICATIONS_CPP_
extern
#endif
const char* ParentNotificationNames[]
#ifdef NOTIFICATIONS_CPP_
                                    ={
		"Startup",
		"ProcessingDone",
		"QueueProcessActions",
		0
}
#endif
;

enum ChildNotification{
	ChildNotification_ThreadStarted,
	ChildNotification_ProcessActions,
	ChildNotification_ThreadExited,
	ChildNotification_ThreadExiting,
};

#ifndef NOTIFICATIONS_CPP_
extern
#endif
const char* ChildNotificationNames[]
#ifdef NOTIFICATIONS_CPP_
                                    ={
		"Started",
		"ProcessActions",
		"ThreadExited",
		"ThreadExiting",
		0
}
#endif
;


#endif /* NOTIFICATIONS_H_ */
