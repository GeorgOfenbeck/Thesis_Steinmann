/*
 * WhtKernel.cpp
 *
 *  Created on: Feb 22, 2012
 *      Author: ruedi
 */

#include "WhtKernel.h"

std::vector<std::pair<void*, long> > WhtKernel::getBuffers() {
	long size = 1 << getBufferSizeExp();
	std::vector<std::pair<void*, long> > result;
	result.push_back(std::make_pair(buffer,size*sizeof(wht_value)));
	result.push_back(std::make_pair(tree,sizeof(Wht)));
	return result;
}

WhtKernel::~WhtKernel() {
	// TODO Auto-generated destructor stub
}

static Wht * foo(int size){
	return wht_get_tree(size);
}

void WhtKernel::initialize() {
	// seed random number generator for reproduceability
	srand48(0);

	long size = 1 << getBufferSizeExp();

	// allocate memory
	if (posix_memalign((void**) (&buffer), 16, size * sizeof(wht_value)) != 0) {
		throw "could not allocate memory";
	}

	// initialize vectors
	for (long i = 0; i < size; i++) {
		buffer[i] = drand48();
	}

	// initialize the tree
	tree=new Wht();
	tree=foo(getBufferSizeExp());
	//tree = wht_get_tree(getBufferSizeExp());
}

void WhtKernel::run() {
	wht_apply(tree, 1, buffer);
}

void WhtKernel::dispose() {
	free(buffer);
	free(tree);
}

