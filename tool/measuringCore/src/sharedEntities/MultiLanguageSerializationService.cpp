/*
 * MultiLanguageSerializationService.cpp
 *
 * Contains non-generated code of the serialization service
 *
 *  Created on: Nov 11, 2011
 *      Author: ruedi
 */

#include "MultiLanguageSerializationService.h"
#include "Exception.h"
#include <typeinfo>
#include <string>
#include <cstdio>
#include "baseClasses/Serializer.h"
#include "utils.h"
#include "Logger.h"

using namespace std;
MultiLanguageSerializationService::MultiLanguageSerializationService() {
	// TODO Auto-generated constructor stub

}

MultiLanguageSerializationService::~MultiLanguageSerializationService() {
	// TODO Auto-generated destructor stub
}
void MultiLanguageSerializationService::Serialize(MultiLanguageObjectBase *o,
		std::ostream & output) {
	SerializationContext ctx;
	ctx.Serialize(o, output);
}

void SerializationContext::Serialize(MultiLanguageObjectBase *o,
		std::ostream & output) {
	foreach (Serializer *serializer, Serializer::getSerializers())
			{
				if (serializer->canSerialize(o)) {
					return serializer->serialize(o, output, this);
				}
			}
	throw Exception((std::string) "Unknown class: " + typeid(o).name());
}

MultiLanguageObjectBase *MultiLanguageSerializationService::DeSerialize(
		std::istream & input) {
	DeSerializationContext ctx;
	return ctx.DeSerialize(input);
}

MultiLanguageObjectBase *DeSerializationContext::DeSerialize(
		std::istream & input) {
	string line;

	getline(input, line);

	LDEBUG("Deserializing %s",line.c_str())

	if (line.compare("<null>") == 0) {
		return NULL;
	}

	foreach (Serializer *serializer, Serializer::getSerializers())
			{
		LDEBUG()
				if (serializer->canDeSerialize(line)) {
					return serializer->deSerialize(input, this);
				}
		LDEBUG()
			}

	LERROR("Parse Error: class %s is not known",line.c_str())
	throw Exception("Parse Error: class " + line + " is not known");
}

