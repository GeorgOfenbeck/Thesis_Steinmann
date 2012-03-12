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
#include <map>
#include <stdint.h>

class SerializationContext {
	std::map<SharedEntityBase*, int64_t> map;
	int64_t nextIdx;
public:
	SerializationContext();
	void Serialize(SharedEntityBase *o, std::ostream &output);

	bool contains(SharedEntityBase* obj) {
		return map.count(obj) > 0;
	}

	int64_t getIdx(SharedEntityBase* obj) {
		return map[obj];
	}

	int64_t addObject(SharedEntityBase *obj) {
		map[obj] = nextIdx;
		nextIdx++;
		return nextIdx;
	}
};

class DeSerializationContext {
	std::map<int64_t, SharedEntityBase*> map;
	int64_t nextIdx;

public:
	DeSerializationContext();
	SharedEntityBase * DeSerialize(std::istream &input);

	bool contains(int64_t idx) {
		return map.count(idx) > 0;
	}

	SharedEntityBase* getObj(int64_t idx) {
		return map[idx];
	}

	int64_t addObject(SharedEntityBase* obj);
};

class MultiLanguageSerializationService {
public:

	MultiLanguageSerializationService();
	virtual ~MultiLanguageSerializationService();

	void Serialize(SharedEntityBase *o, std::ostream &output);
	SharedEntityBase * DeSerialize(std::istream &input);

};

#endif /* MULTILANGUAGESERIALIZATIONSERVICE_H_ */
