/*
 * MultiLanguageSerializationService.cpp
 *
 * Contains non-generated code of the serialization service
 *
 *  Created on: Nov 11, 2011
 *      Author: ruedi
 */
#define __STDC_FORMAT_MACROS 1
#include <inttypes.h>
#include "MultiLanguageSerializationService.h"
#include "Exception.h"
#include <typeinfo>
#include <string>
#include <cstdio>
#include "baseClasses/Serializer.h"
#include "utils.h"
#include "Logger.h"
#include <stdint.h>

using namespace std;
MultiLanguageSerializationService::MultiLanguageSerializationService() {
	// TODO Auto-generated constructor stub

}

MultiLanguageSerializationService::~MultiLanguageSerializationService() {
	// TODO Auto-generated destructor stub
}
void MultiLanguageSerializationService::Serialize(SharedEntityBase *o,
		std::ostream & output) {
	SerializationContext ctx;
	ctx.Serialize(o, output);
}

SerializationContext::SerializationContext()
{
	nextIdx=0;
}

void SerializationContext::Serialize(SharedEntityBase *o,
		std::ostream & output) {

	// handle o==null
	if (o == NULL) {
		output << "<null>\n";
		return;
	}

	// check if the object is already serialized
	if (contains(o)) {
		output << "=> " << getIdx(o) << "\n";
		return;
	}

	// add the object to the context
	addObject(o);

	foreach (Serializer *serializer, Serializer::getSerializers())
			{
				if (serializer->canSerialize(o)) {
					return serializer->serialize(o, output, this);
				}
			}
	throw Exception((std::string) "Unknown class: " + typeid(o).name());
}

SharedEntityBase *MultiLanguageSerializationService::DeSerialize(
		std::istream & input) {
	DeSerializationContext ctx;
	return ctx.DeSerialize(input);
}

SharedEntityBase *DeSerializationContext::DeSerialize(std::istream & input) {
	string line;

	getline(input, line);

	LDEBUG("Deserializing %s", line.c_str())

	if (line.compare("<null>") == 0) {
		return NULL;
	}

	if (line.find("=> ") == 0) {
		int64_t idx;
		if (sscanf(line.substr(3).c_str(),"%"PRId64"",&idx)!=1) {
			fprintf(stderr, "Error interpreting %s as link to object",
					line.c_str());
		}
		return getObj(idx);
	}

	foreach (Serializer *serializer, Serializer::getSerializers())
			{
				if (serializer->canDeSerialize(line)) {
					return serializer->deSerialize(input, this);
				}
			}

	LERROR("Parse Error: class %s is not known", line.c_str())
	throw Exception("Parse Error: class " + line + " is not known");
}

DeSerializationContext::DeSerializationContext()
{
	nextIdx=0;
}

int64_t DeSerializationContext::addObject(SharedEntityBase *obj) {
	map[nextIdx] = obj;
	nextIdx++;
	return nextIdx;
}

