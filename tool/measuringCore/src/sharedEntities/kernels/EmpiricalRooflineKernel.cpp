/*
 * IntroductionExample.cpp
 *
 *  Created on: Apr 19, 2012
 *      Author: ruedi
 */

#include "EmpiricalRooflineKernel.h"
#include <utility>
#include <stdlib.h>
#include <cmath>
#include <stdio.h>
#define BufferSize 1024L*256/sizeof(double) //2 MB Buffer

std::vector<std::pair<void*, long> > EmpiricalRooflineKernel::getBuffers() {
	std::vector<std::pair<void*, long> > result;
	result.push_back(std::make_pair(buffer,BufferSize*sizeof(double)));
	return result;
}

EmpiricalRooflineKernel::~EmpiricalRooflineKernel() {
	// TODO Auto-generated destructor stub
}



void EmpiricalRooflineKernel::initialize() {
	int tmp,j,i;
	// allocate buffer pointers
	if (posix_memalign((void**) (&buffer), CacheLineSize,
			BufferSize * sizeof(double)) != 0) {
		throw "could not allocate memory";
	}

	for (j = 0; j < BufferSize; j = j+16)
	{
		for (i = 0; i< 8; i++)
			buffer[j+i] = 1;
		for (i = 0; i< 8; i++)
			buffer[j+8+i] = -1;
	}
}

void EmpiricalRooflineKernel::run() {
	int64_t flops = getFlops();
	int64_t bytes = getTransfered_bytes();
	int dummy = getDummy();
	int i,j;

	volatile double *pointer;
	volatile double res[16];

	double m0 =0;double m1=0;double m2=0;double m3=0;double m4=0;double m5=0;double m6=0;double m7=0;
	double a0=0;double a1=0;double a2=0;double a3=0;double a4=0;double a5=0;double a6=0;double a7=0;
	double t0,t1,t2,t3,t4,t5,t6,t7;
	pointer = res + dummy;



	for (j = 0; j < flops; j++)
	for (i = 0; i < BufferSize; i=i+8)
	{

		t0 = buffer[i];
		t1 = buffer[i+1];
		t2 = buffer[i+2];
		t3 = buffer[i+3];
		t4 = buffer[i+4];
		t5 = buffer[i+5];
		t6 = buffer[i+6];
		t7 = buffer[i+7];


		m0 = m0*t0;
		a0 = a0+t0;
		m1 = m1*t1;
		a1 = a1+t1;
		m2 = m2*t2;
		a2 = a2*t2;
		m3 = m3*t3;
		a3 = a3+t3;
		m4 = m4*t4;
		a4 = a4+t4;
		m5 = m5*t5;
		a5 = a5+t5;
		m6 = m6*t6;
		a6 = a6+t6;
		m7 = m7*t7;
		a7 = a7+t7;
	}

	pointer[0] = a0;
	pointer[1] = a1;
	pointer[2] = a2;
	pointer[3] = a3;
	pointer[4] = a4;
	pointer[5] = a5;
	pointer[6] = a6;
	pointer[7] = a7;
	pointer[8+0] = m0;
	pointer[8+1] = m1;
	pointer[8+2] = m2;
	pointer[8+3] = m3;
	pointer[8+4] = m4;
	pointer[8+5] = m5;
	pointer[8+6] = m6;
	pointer[8+7] = m7;

	//printf("%lf",pointer[8+7]);
}



void EmpiricalRooflineKernel::dispose() {
	free(buffer);
}

