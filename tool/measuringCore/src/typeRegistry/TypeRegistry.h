/*
 * KernelRegistry.h
 *
 *  Created on: Nov 15, 2011
 *      Author: ruedi
 */

#ifndef KERNELREGISTRY_H_
#define KERNELREGISTRY_H_

#include "sharedDOM/KernelDescriptionBase.h"
#include <vector>
#include <list>
#include <cstdio>
#include "utils.h"
#include "baseClasses/PolymorphicBase.h"
#include "concepts.h"
#include "Exception.h"

template <class TObjectBase>
class TypeRegistryEntryBase;

/*
 * Used to instantiate objects from their description. The object classes
 * are registered using the TypeRegisterer, which adds an entry to the entries
 * list.
 */
template <class TObjectBase>
class TypeRegistry {
	BOOST_CONCEPT_ASSERT((concepts::ObjectBase_concept<TObjectBase>));
	/*
	 * return the entry list for the TObjectBase
	 */
	static std::vector<TypeRegistryEntryBase<TObjectBase>*> &get_entries(){
		// initialized during the first call to the method
		// remains the same afterwards
		static std::vector<TypeRegistryEntryBase<TObjectBase>*> entries;
		return entries;
	}



public:
	virtual ~TypeRegistry();

	/* add an entry to the entry list */
	static void add_entry(TypeRegistryEntryBase<TObjectBase> *entry){
		get_entries().push_back(entry);
	}

	/* print the names of all registered object classes */
	static void print(){
		typename std::vector<TypeRegistryEntryBase<TObjectBase>*>::iterator it=TypeRegistry::get_entries().begin();
		for (;it!=get_entries().end();it++){
			const char *name=(*it)->getTypeName();
			printf("%s\n",name);
		}
	}



	/* find the entry for the given description and arguments. return null if no entry was found. throw exception if multiple entries are found */
	template<typename ...TArgs>
	static TypeRegistryEntryBase<TObjectBase> *find(typename TObjectBase::tDescriptionBase *desc, TArgs... args){
		// put all arguments in a vector
		std::vector<PolymorphicBase*> argVec;
		utils::push_all_back(argVec,args...);

		// iterate over all entries and return the one that matches
		TypeRegistryEntryBase<TObjectBase> *result=NULL;
		auto it=TypeRegistry::get_entries().begin();
		for (;it!=get_entries().end();it++){
			if ((*it)->match(desc,argVec)){
				if (result==NULL){
					result=*it;
				}
				else{
					std::string msg="Multiple matching TypeRegistryEntries found: ";
					msg+=result->getTypeName();
					msg+=", ";
					msg+=(*it)->getTypeName();
					throw Exception(msg);
				}
			}
		}

		return result;
	}

	template<typename ...TArgs>
	static TObjectBase *createObject(typename TObjectBase::tDescriptionBase *desc, TArgs... args){
		// find an entry
		TypeRegistryEntryBase<TObjectBase>* entry=TypeRegistry<TObjectBase>::find(desc,args...);

		// check if an entry was found
		if (entry==NULL){
			return NULL;
		}

		std::vector<PolymorphicBase*> argVec;
		utils::push_all_back(argVec,args...);

		// return the kernel
		return entry->createObject(desc,argVec);

	}

};

#endif /* KERNELREGISTRY_H_ */
