/*
 * MultiLanguageSerializationService.h
 *
 *  Created on: Nov 11, 2011
 *      Author: ruedi
 */

#ifndef MULTILANGUAGESERIALIZATIONSERVICE_H_
#define MULTILANGUAGESERIALIZATIONSERVICE_H_

#include <istream>
#include <ostream>
#include "MultiLanguageObjectBase.h"

class MultiLanguageSerializationService {
public:
	MultiLanguageSerializationService();
	virtual ~MultiLanguageSerializationService();

	void Serialize(MultiLanguageObjectBase *o, std::ostream &output);
	MultiLanguageObjectBase * DeSerialize(std::istream &input);
};

#endif /* MULTILANGUAGESERIALIZATIONSERVICE_H_ */
