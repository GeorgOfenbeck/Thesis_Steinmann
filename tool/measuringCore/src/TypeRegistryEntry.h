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
#include <vector>

/* base class of entries in the TypeRegistry. Represents an object type */
template <class TObjectBase>
class TypeRegistryEntryBase{
public:
	virtual ~TypeRegistryEntryBase(){}

	/* create an object form the given description */
	virtual TObjectBase *createObject(typename TObjectBase::tDescriptionBase *description, std::vector<PolymorphicBase*> &args)=0;

	/*
	 * return true if objects of the represented object type can be created from this
	 * description
	 */
	virtual bool match(typename TObjectBase::tDescriptionBase *description, std::vector<PolymorphicBase*> args)=0;

	/*
	 * get the name of the object type this entry represents.
	 */
	virtual const char* getTypeName()=0;
};



/* concrete type registry entries for objects only having a description and no other parameters*/
template <class TObject, typename ... TArgs>
class TypeRegistryEntry: public TypeRegistryEntryBase<typename TObject::tBase>{

	/*
	 * The instantiator is used to instantiate a TObject
	 */
	template<typename ... TProcessed>
	struct instantiator{
		typedef typename TObject::tDescription tDesc;

		// if all arguments are processed, the base cased is used, which instantiates
		// TObject
		template<int idx, typename ... DUMMY>
		struct inner{
			static TObject* func(tDesc *desc, std::vector<PolymorphicBase*> &argVec,TProcessed* ... args){
				return new TObject(desc, args...);
			}
		};

		// this specialization is used as long as there are unprocessed arguments
		template<int idx, typename argsHead, typename ... argsTail>
		struct inner<idx,argsHead,argsTail...>{
			static TObject* func(tDesc *desc,  std::vector<PolymorphicBase*> &argVec,TProcessed* ... args){
				return
					// append the first unprocessed argument to the end of the processed arguments
					instantiator<TProcessed...,argsHead>
					// only the tail of the unprocessed arguments remains
					::template inner<idx+1,argsTail ...>
					// do the call with the next index and append the head argument
					::func(desc,argVec,args...,(argsHead*)argVec[idx]);
			}
		};
	};

public:
	virtual ~TypeRegistryEntry(){
	}

	typename TObject::tBase *createObject(typename TObject::tBase::tDescriptionBase *description, std::vector<PolymorphicBase*> &args){
		if (!match(description,args)){
			throw Exception("arguments do not match the entry");
		}

		typename TObject::tDescription * castedDescription=
				dynamic_cast<typename TObject::tDescription *>(description);

		if (castedDescription==NULL){
			throw Exception("description could not be downcasted");
		}

		return
			// no arguments are processed
			instantiator<>
			//start with the first member of args
			// process all arguments
			::template inner<0,TArgs ...>
			// supply the data
			::func(castedDescription,args);
	}

	virtual bool match(typename TObject::tBase::tDescriptionBase *description, std::vector<PolymorphicBase*> args){
		if (description==NULL){
			throw Exception("description was null");
		}

		if (args.size()!=utils::args_size<TArgs...>()){
			return false;
		}

		if (!utils::can_cast_elements<std::vector<PolymorphicBase*>,TArgs...>(args)){
			return false;
		}

		return typeid(*description)==typeid(typename TObject::tDescription);
	}

	TObject *createObject(typename TObject::tDescription *description, TArgs* ... args){
		return new TObject(description,args ...);
		//return NULL;
	}

	const char* getTypeName(){
		return typeid(TObject).name();
	}
};

#endif /* KERNELREGISTRYENTRY_H_ */
