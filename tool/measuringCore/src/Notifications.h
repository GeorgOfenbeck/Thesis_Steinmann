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

enum ChildNotification{
	ChildNotification_Started,
	ChildNotification_ProcessActions,
	ChildNotification_ChildExited,
};



#endif /* NOTIFICATIONS_H_ */
