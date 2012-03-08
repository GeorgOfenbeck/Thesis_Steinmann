/*
 * SharedEntityBase.h
 *
 *  Created on: Nov 11, 2011
 *      Author: ruedi
 */

#ifndef SHAREDENTITYBASE_H_
#define SHAREDENTITYBASE_H_

#include <string>
#include "baseClasses/PolymorphicBase.h"
#include <set>

class SharedEntityBase: public PolymorphicBase {
public:
	virtual ~SharedEntityBase();
	virtual void cloneFrom(SharedEntityBase *c){}
	virtual SharedEntityBase * clone()=0;
	virtual void addAll(std::set<SharedEntityBase*> &result);
};

#endif /* SHAREDENTITYBASE_H_ */
