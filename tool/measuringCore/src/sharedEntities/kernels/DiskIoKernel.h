/*
 * DiskIoKernel.h
 *
 *  Created on: Feb 15, 2012
 *      Author: ruedi
 */

#ifndef DISKIOKERNEL_H_
#define DISKIOKERNEL_H_
#include "sharedEntities/kernels/DiskIoKernelData.h"
class DiskIoKernel: public DiskIoKernelData {
	int fd;
	char *buffer;
	static const int bufferSize = 1024 * 512;
protected:
	std::vector<std::pair<void*,long> > getBuffers();
public:
	virtual ~DiskIoKernel();

	void initialize();
	void readFileOnce();
	void run();

	void dispose();
};

#endif /* DISKIOKERNEL_H_ */
