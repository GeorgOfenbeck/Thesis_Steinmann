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
	ChildNotification_Started,
	ChildNotification_ProcessActions,
	ChildNotification_ChildExited,
};

#ifndef NOTIFICATIONS_CPP_
extern
#endif
const char* ChildNotificationNames[]
#ifdef NOTIFICATIONS_CPP_
                                    ={
		"Started",
		"ProcessActions",
		"ChildExited",
		0
}
#endif
;


#endif /* NOTIFICATIONS_H_ */
