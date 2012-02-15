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

using namespace std;
DiskIoKernel::~DiskIoKernel() {
	// TODO Auto-generated destructor stub
}

void DiskIoKernel::initialize()
{
	srand48(0);

	string fileName="diskIo";
	fileName+=getId();
	fileName+=".tmp";

	fd=open(fileName.c_str(),O_APPEND|O_CREAT|O_WRONLY|O_DIRECT,S_IRWXU);
	if (fd==-1){
		perror("open disk io temporary file");
	}

	// get length
	off_t length=lseek(fd,0,SEEK_CUR);
	if (length==-1){
		perror("error reading file length");
	}

	printf("file length: %li\n",length);

	// increase size of file
	char buffer[512];
	for (;length<getFileSize();length+=512){
		for (int i=0; i<512; i+=sizeof(long int)){
			*(long int*)(&buffer[i])=lrand48();
		}
		write(fd,buffer,512);
	}


}



void DiskIoKernel::run()
{
	while (isKeepRunning()){
		// seek to the start of the file
		if (lseek(fd,0,SEEK_SET)==-1){
			perror("seeking to the beginning of the file");
		}


		char buffer[512];

		for (off_t position=0;position<getFileSize(); position+=512){
			read(fd,buffer,512);
		}
	}
}



void DiskIoKernel::dispose()
{
	// close the file
	close(fd);
}



