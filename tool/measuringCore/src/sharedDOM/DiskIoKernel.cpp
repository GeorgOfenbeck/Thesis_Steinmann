/*
 * DiskIoKernel.cpp
 *
 *  Created on: Feb 15, 2012
 *      Author: ruedi
 */

#include "DiskIoKernel.h"
#include <string>
#include <fcntl.h>
#include <unistd.h>
#include <stdlib.h>
#include <sstream>

using namespace std;
DiskIoKernel::~DiskIoKernel() {
	// TODO Auto-generated destructor stub
}

void DiskIoKernel::initialize() {
	{
		if (posix_memalign((void**)&buffer, 4*1024, bufferSize)!=0)
		{
			perror("error allocating buffer");
		}
	}

	srand48(0);
	stringstream fileName;
	fileName << "diskIO" << getId() << ".tmp";

	// open file in direct mode
	fd = open(fileName.str().c_str(), O_APPEND | O_CREAT | O_RDWR | O_DIRECT,
			S_IRWXU);
	if (fd == -1) {
		perror("open disk io temporary file");
	}

	// get length
	off_t length = lseek(fd, 0, SEEK_END);
	if (length == -1) {
		perror("error reading file length");
	}

	printf("file length: %li\n", length);

	int writeSize=1024*4;
	// increase size of file
	for (; length < getFileSize(); length += writeSize) {
		for (int i = 0; i < writeSize; i += sizeof(long int)) {
			*(long int*) (&buffer[i]) = lrand48();
		}

		long written = 0;

		while (written < writeSize) {
			long ret = write(fd, &(buffer[written]), writeSize - written);
			if (ret == -1) {
				printf("written: %li %i\n",written, writeSize);
				perror("writing to diskIo file");
				exit(1);
			} else {
				written += ret;
			}
		}
	}

	// sync to disk
	fsync(fd);

}

void DiskIoKernel::readFileOnce()
{
    // seek to the start of the file
    if (lseek(fd, 0, SEEK_SET) == -1) {
			perror("seeking to the beginning of the file");
		}
    // read the whole file
    for(off_t position = 0;position < getFileSize();){
        int ret = read(fd, buffer, bufferSize);
        if(ret == -1){
            printf("position: %li\n", position);
            perror("Error while reading");
            exit(1);
        }else{
            position += ret;
        }
    }

}

void DiskIoKernel::run() {
	long iterations = 0;
	while (
	// if infinite iterations are specified, repeat while keepRunning is true
	(getIterations() == -1 && isKeepRunning()) ||
	// if the iterations are bounded, repeat until enough iterations are made
			(getIterations() > 0 && iterations < getIterations())) {

		iterations++;

		readFileOnce();
	}
}

void DiskIoKernel::dispose() {
	// close the file
	close(fd);
}

std::vector<std::pair<void*,long > > DiskIoKernel::getBuffers()
{
	return std::vector<std::pair<void*,long > >();
}

