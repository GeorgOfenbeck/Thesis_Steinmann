/*
 * MultiLanguageObjectBase.h
 *
 *  Created on: Nov 11, 2011
 *      Author: ruedi
 */

#ifndef MULTILANGUAGEOBJECTBASE_H_
#define MULTILANGUAGEOBJECTBASE_H_

#include <string>
#include "baseClasses/PolymorphicBase.h"

class MultiLanguageObjectBase: public PolymorphicBase {
public:
	virtual ~MultiLanguageObjectBase();
	virtual void cloneFrom(MultiLanguageObjectBase *c){}
	virtual MultiLanguageObjectBase * clone()=0;
};

#endif /* MULTILANGUAGEOBJECTBASE_H_ */
