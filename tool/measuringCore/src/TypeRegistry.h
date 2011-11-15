/*
 * KernelRegistry.h
 *
 *  Created on: Nov 15, 2011
 *      Author: ruedi
 */

#ifndef KERNELREGISTRY_H_
#define KERNELREGISTRY_H_

#include "generatedC/KernelDescriptionBase.h"
#include <vector>
#include <cstdio>

template <class TObjectBase>
class TypeRegistryEntryBase;

template <class TObjectBase>
class TypeRegistry {
	static std::vector<TypeRegistryEntryBase<TObjectBase>*> &get_entries(){
		static std::vector<TypeRegistryEntryBase<TObjectBase>*> entries;
		return entries;
	}
public:
	virtual ~TypeRegistry();

	static void add_entry(TypeRegistryEntryBase<TObjectBase> *entry){
		get_entries().push_back(entry);
	}

	static void print(){
		typename std::vector<TypeRegistryEntryBase<TObjectBase>*>::iterator it=TypeRegistry::get_entries().begin();
		for (;it!=get_entries().end();it++){
			const char *name=(*it)->getTypeName();
			printf("%s\n",name);
		}
	}

	static TypeRegistryEntryBase<TObjectBase>* find(typename TObjectBase::tDescriptionBase *desc){
		// iterate over all entries and return the first that matches
		typename std::vector<TypeRegistryEntryBase<TObjectBase>*>::iterator it=TypeRegistry::get_entries().begin();
		for (;it!=get_entries().end();it++){
			if ((*it)->match(desc)){
				return *it;
			}
		}

		return NULL;
	}

	static TObjectBase *createObject(typename TObjectBase::tDescriptionBase *desc){
		// find an entry
		TypeRegistryEntryBase<TObjectBase>* entry=TypeRegistry<TObjectBase>::find(desc);

		// check if an entry was found
		if (entry==NULL){
			return NULL;
		}

		// return the kernel
		return entry->createKernel(desc);

	}

};

#endif /* KERNELREGISTRY_H_ */
