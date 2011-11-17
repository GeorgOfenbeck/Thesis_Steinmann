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

/*
template<typename T, typename ... TArgs>
struct bar{

	template<typename THead, typename ... TTail>
	static T* foo(int idx, std::vector<PolymorphicBase*> &argVec, TArgs ... args){
		//bar<T,TArgs ...,THead>::foo<idx+1,TTail ... >(argVec,args...,argVec[idx]);
		bar<T,TArgs ...>::foo<TTail ... >(idx+1,argVec,args...,argVec[idx]);

	}

	static T* foo(std::vector<PolymorphicBase*> &argVec,TArgs... args){
		return new T(args...);
	}
};
*/

template<typename T, typename TDesc, typename ... TProcessed>
struct bar{
	template<typename ... TArgs>
	struct foo{
		static T* func(int idx,  TDesc *desc, std::vector<PolymorphicBase*> &argVec,TProcessed ... args){
					return new T(desc, args...);
				}
	};

	template<typename head, typename ... tail>
	struct foo<head,tail...>{
		typedef bar<T,TDesc,TProcessed...,head> t1;
		typedef typename t1::template foo<tail ...> t2;

		static T* func(int idx, TDesc *desc,  std::vector<PolymorphicBase*> &argVec,TProcessed ... args){
			return t2::func(idx+1,desc,argVec,args...,(head)argVec[idx]);
		}
	};

};

/* concrete type registry entries for objects only having a description and no other parameters*/
template <class TObject, typename ... TArgs>
class TypeRegistryEntry: public TypeRegistryEntryBase<typename TObject::tBase>{

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

		typedef bar<TObject,typename TObject::tDescription> t1;
		typedef typename t1::template foo<TArgs* ...> t2;
		return t2::func(0,castedDescription,args);
		//return createObject(castedDescription);
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
