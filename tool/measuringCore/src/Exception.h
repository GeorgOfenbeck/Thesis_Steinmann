/*
 * Exception.h
 *
 *  Created on: Nov 16, 2011
 *      Author: ruedi
 */

#ifndef EXCEPTION_H_
#define EXCEPTION_H_
#include "execinfo.h"
#include <string>

class Exception{
	static const int bufSize=100;

	std::string message;
	void *stackTrace[bufSize];
	int stackTraceCount;
public:
	Exception(std::string msg){
		message=msg;
		stackTraceCount=backtrace(stackTrace,bufSize);
	}

	void print(int fd){
		backtrace_symbols_fd(stackTrace,stackTraceCount,fd);
	}

	std::string get_message(){return message;}
};

#endif /* EXCEPTION_H_ */
