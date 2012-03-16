/*
 * Logger.h
 *
 *  Created on: Feb 16, 2012
 *      Author: ruedi
 */

#ifndef LOGGER_H_
#define LOGGER_H_

#define LOGLEVEL_ERROR 1
#define LOGLEVEL_WARNING 2
#define LOGLEVEL_INFO 3
#define LOGLEVEL_DEBUG 4
#define LOGLEVEL_TRACE 5

#ifndef LOGLEVEL
#include "LogLevel.h"
#endif

#ifndef LOGLEVEL
#define LOGLEVEL LOGLEVEL_TRACE
#endif

#ifndef LOG_ADDITIONAL
#define LOG_ADDITIONAL ""
#endif

#include <cstdio>
#include <unistd.h>
#include <sys/syscall.h>
#include <typeinfo>

#define LOG_LOG(name,level,msg,...) { \
	int tid=syscall(__NR_gettid);\
	if (level<=LOGLEVEL_WARNING){\
			fprintf(stderr,"%s %i %s:%s():%i",name, tid, __FILE__,__FUNCTION__,__LINE__);\
			fprintf(stderr," " LOG_ADDITIONAL);\
			fprintf(stderr,"--> " msg, ##__VA_ARGS__);\
			fprintf(stderr,"\n");\
		}else{\
			printf("%s %i %s:%s():%i",name, tid, __FILE__,__FUNCTION__,__LINE__);\
			printf(" " LOG_ADDITIONAL);\
			printf("--> " msg, ##__VA_ARGS__);\
			printf("\n");\
		}\
}

#if LOGLEVEL>=LOGLEVEL_ERROR
#define LERROR(msg, ...) LOG_LOG("Error",1,msg,##__VA_ARGS__)
#else
#define LERROR(msg, ...) {}
#endif

#if LOGLEVEL>=LOGLEVEL_WARNING
#define LWARNING(msg,...) LOG_LOG("Warning",2,msg,##__VA_ARGS__)
#else
#define LWARNING(msg, ...) {}
#endif

#if LOGLEVEL>=LOGLEVEL_INFO
#define LINFO(msg,...) LOG_LOG("Info",3,msg,##__VA_ARGS__)
#else
#define LINFO(msg, ...) {}
#endif

#if LOGLEVEL>=LOGLEVEL_DEBUG
#define LDEBUG(msg,...) LOG_LOG("Debug",4,msg,##__VA_ARGS__)
#else
#define LDEBUG(msg, ...) {}
#endif

#if LOGLEVEL>=LOGLEVEL_TRACE
#define LTRACE(msg,...) LOG_LOG("Trace",5,msg,##__VA_ARGS__)
#else
#define LTRACE(msg, ...) {}
#endif

#define LENTER LTRACE("entering method")
#define LLEAVE LTRACE("leaving method")

#define LENTERT LTRACE("entering method %p->%s", this, typeid(*this).name())
#define LLEAVET LTRACE("leaving method %p->%s", this, typeid(*this).name())

#endif /* LOGGER_H_ */
