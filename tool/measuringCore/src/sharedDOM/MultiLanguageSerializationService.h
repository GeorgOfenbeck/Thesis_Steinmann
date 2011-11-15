/*
 * MultiLanguageSerializationService.h
 * header file of the serialization service. The methods are implemented by generated
 * and non-generated code.
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
