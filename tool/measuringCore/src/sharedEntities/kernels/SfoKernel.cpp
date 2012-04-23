/*
 * SfoKernel.cpp
 *
 *  Created on: Apr 23, 2012
 *      Author: ruedi
 */

#include "SfoKernel.h"

extern void bench_graph_cut_minimize(char *filename);
extern void setupSatoru();
extern void setupOther();

SfoKernel::~SfoKernel() {
	// TODO Auto-generated destructor stub
}

std::vector<std::pair<void*, long> > SfoKernel::getBuffers() {
	std::vector<std::pair<void*, long> > result;
	return result;
}

void SfoKernel::initialize() {
}

void SfoKernel::run() {
	if (getUseSatoru()){
		setupSatoru();
	}
	else{
		setupOther();
	}

	bench_graph_cut_minimize((char*) getControlFile().c_str());

}

void SfoKernel::dispose() {
}


