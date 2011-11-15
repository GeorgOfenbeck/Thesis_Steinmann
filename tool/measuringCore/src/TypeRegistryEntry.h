/*
 * KernelRegistryEntry.h
 *
 *  Created on: Nov 15, 2011
 *      Author: ruedi
 */

#ifndef KERNELREGISTRYENTRY_H_
#define KERNELREGISTRYENTRY_H_

#include "TypeRegistry.h"

#include <typeinfo>

template <class TObjectBase>
class TypeRegistryEntryBase{
public:
	virtual ~TypeRegistryEntryBase(){}
	virtual TObjectBase *createKernel(typename TObjectBase::tDescriptionBase *description)=0;
	virtual bool match(typename TObjectBase::tDescriptionBase *description)=0;
	virtual const char* getTypeName()=0;
};

template <class TObject>
class TypeRegistryEntry : public TypeRegistryEntryBase<typename TObject::tBase>{
public:
	virtual ~TypeRegistryEntry(){
	}

	typename TObject::tBase *createKernel(typename TObject::tBase::tDescriptionBase *description){
		return createKernel((typename TObject::tDescription *)description);
	}

	virtual bool match(typename TObject::tBase::tDescriptionBase *description){
		return typeid(*description)==typeid(typename TObject::tDescription);
	}

	TObject *createKernel(typename TObject::tDescription *description){
		return new TObject(description);
	}

	const char* getTypeName(){
		return typeid(TObject).name();
	}
};

#endif /* KERNELREGISTRYENTRY_H_ */
