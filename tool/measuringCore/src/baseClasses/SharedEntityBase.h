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
#include <map>

class SharedEntityBase: public PolymorphicBase {
public:
	virtual ~SharedEntityBase();
	virtual void cloneFrom(SharedEntityBase *c, std::map<PolymorphicBase*,PolymorphicBase*> &map){}
	virtual SharedEntityBase * clone(std::map<PolymorphicBase*,PolymorphicBase*> &map)=0;
	virtual void addAll(std::set<SharedEntityBase*> &result);
};

#endif /* SHAREDENTITYBASE_H_ */
