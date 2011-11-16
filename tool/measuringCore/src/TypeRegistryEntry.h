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
#include "Exception.h"

/* base class of entries in the TypeRegistry. Represents an object type */
template <class TObjectBase>
class TypeRegistryEntryBase{
public:
	virtual ~TypeRegistryEntryBase(){}

	/* create an object form the given description */
	virtual TObjectBase *createObject(typename TObjectBase::tDescriptionBase *description)=0;

	/*
	 * return true if objects of the represented object type can be created from this
	 * description
	 */
	virtual bool match(typename TObjectBase::tDescriptionBase *description)=0;

	/*
	 * get the name of the object type this entry represents.
	 */
	virtual const char* getTypeName()=0;
};

/* concrete type registry entries */
template <class TObject>
class TypeRegistryEntry : public TypeRegistryEntryBase<typename TObject::tBase>{
public:
	virtual ~TypeRegistryEntry(){
	}

	typename TObject::tBase *createObject(typename TObject::tBase::tDescriptionBase *description){
		if (description==NULL){
			throw Exception("description was null");
		}

		typename TObject::tDescription * castedDescription=
				dynamic_cast<typename TObject::tDescription *>(description);

		if (castedDescription==NULL){
			throw Exception("description could not be downcasted");
		}

		return createObject(castedDescription);
	}

	virtual bool match(typename TObject::tBase::tDescriptionBase *description){
		return typeid(*description)==typeid(typename TObject::tDescription);
	}

	TObject *createObject(typename TObject::tDescription *description){
		return new TObject(description);
	}

	const char* getTypeName(){
		return typeid(TObject).name();
	}
};

#endif /* KERNELREGISTRYENTRY_H_ */
