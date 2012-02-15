/*
 * DiskIoKernel.h
 *
 *  Created on: Feb 15, 2012
 *      Author: ruedi
 */

#ifndef DISKIOKERNEL_H_
#define DISKIOKERNEL_H_
#include "sharedDOM/DiskIoKernelData.h"
class DiskIoKernel: public DiskIoKernelData {
	int fd;
	char *buffer;
	static const int bufferSize = 1024 * 512;
public:
	virtual ~DiskIoKernel();

	void initialize();
	void readFileOnce();
	void run();

	void dispose();
	virtual void warmCaches();
};

#endif /* DISKIOKERNEL_H_ */
