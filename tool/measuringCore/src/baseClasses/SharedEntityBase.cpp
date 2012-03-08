/*
 * SharedEntityBase.cpp
 *
 *  Created on: Nov 11, 2011
 *      Author: ruedi
 */

#include "SharedEntityBase.h"

SharedEntityBase::~SharedEntityBase() {
	// TODO Auto-generated destructor stub
}

void SharedEntityBase::addAll(std::set<SharedEntityBase*> & result)
{
	result.insert(this);
}



