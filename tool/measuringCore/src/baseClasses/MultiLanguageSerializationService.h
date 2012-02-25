/*
 * MultiLanguageSerializationService.h
 * header file of the serialization service. The methods are implemented by generated
 * and non-generated code.
 */

#ifndef MULTILANGUAGESERIALIZATIONSERVICE_H_
#define MULTILANGUAGESERIALIZATIONSERVICE_H_

#include <istream>
#include <ostream>
#include "SharedEntityBase.h"

class SerializationContext {
public:
	void Serialize(SharedEntityBase *o, std::ostream &output);
};

class DeSerializationContext {
public:
	SharedEntityBase * DeSerialize(std::istream &input);
};

class MultiLanguageSerializationService {
public:

	MultiLanguageSerializationService();
	virtual ~MultiLanguageSerializationService();

	void Serialize(SharedEntityBase *o, std::ostream &output);
	SharedEntityBase * DeSerialize(std::istream &input);


};

#endif /* MULTILANGUAGESERIALIZATIONSERVICE_H_ */
