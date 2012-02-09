/*
 * TriadKernel.cpp
 *
 *  Created on: Dec 19, 2011
 *      Author: ruedi
 */

#include "TriadKernel.h"

#include <cstring>



void TriadKernel::initialize(){
	size_t size=getBufferSize()*sizeof(double);
	a=(double*)malloc(size);
	b=(double*)malloc(size);
	c=(double*)malloc(size);

	memset(a,0,size);
	memset(b,0,size);
	memset(c,0,size);
}

void TriadKernel::dispose() {
	free(a);
	free(b);
	free(c);
}
