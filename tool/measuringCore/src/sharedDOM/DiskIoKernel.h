/*
 * DiskIoKernel.h
 *
 *  Created on: Feb 15, 2012
 *      Author: ruedi
 */

#ifndef DISKIOKERNEL_H_
#define DISKIOKERNEL_H_
#include "sharedDOM/DiskIoKernelData.h"
class DiskIoKernel : public DiskIoKernelData {
	int fd;
public:
	virtual ~DiskIoKernel();

	void initialize() ;

		void run();

		void dispose();
};

#endif /* DISKIOKERNEL_H_ */
