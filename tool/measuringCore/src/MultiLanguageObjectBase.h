/*
 * MultiLanguageObjectBase.h
 *
 *  Created on: Nov 11, 2011
 *      Author: ruedi
 */

#ifndef MULTILANGUAGEOBJECTBASE_H_
#define MULTILANGUAGEOBJECTBASE_H_

#include <string>
#include "generatedC/MultiLanguageTypeEnum.h"
class MultiLanguageObjectBase {
public:
	virtual MultiLanguageTypeEnum getType()=0;
	//MultiLanguageObjectBase();
	virtual ~MultiLanguageObjectBase();
};

#endif /* MULTILANGUAGEOBJECTBASE_H_ */
