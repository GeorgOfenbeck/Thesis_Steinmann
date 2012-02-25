/*
 * Serializer.h
 *
 *  Created on: Feb 24, 2012
 *      Author: ruedi
 */

#ifndef SERIALIZER_H_
#define SERIALIZER_H_

#include "baseClasses/SharedEntityBase.h"

#include <string>
#include <cstdio>
#include <vector>

struct SerializationContext;
struct DeSerializationContext;

using namespace std;
class Serializer {
protected:
	string deEscapeString(string s);
	string escapeString(string s);

	// only allow derived classes to be instantiated
	Serializer() {
		// register the system initializer instance
		getSerializers().push_back(this);
	}

public:
	// returns the singleton system initializer list
	static std::vector<Serializer*> &getSerializers() {
		// initialized during the first call to the method
		// remains the same afterwards

		// can't use a static class variable, since the order the
		// variables are initialized is not defined
		static std::vector<Serializer*> serializers;
		return serializers;
	}

	virtual ~Serializer();
	virtual bool canSerialize(SharedEntityBase *obj)=0;
	virtual void serialize(SharedEntityBase *o, ostream &output, SerializationContext *ctx)=0;

	virtual bool canDeSerialize(string className)=0;
	virtual SharedEntityBase *deSerialize(istream &input, DeSerializationContext *ctx)=0;

};

#endif /* SERIALIZER_H_ */
