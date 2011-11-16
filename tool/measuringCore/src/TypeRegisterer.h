/*
 * TypeRegisterer.h
 *
 *  Created on: Nov 15, 2011
 *      Author: ruedi
 */

#ifndef TYPEREGISTERER_H_
#define TYPEREGISTERER_H_

#include "TypeRegistry.h"
#include "TypeRegistryEntry.h"

/*
 * When instantiated, registers the type given as template parameter with the type registry
 * corresponding to TObject::tBase. Used in the .cpp file of a class as static variable:
 *
 *  static TypeRegisterer<MemoryLoadKernel> dummy;
 *
 *  The variable itself is never used.
 */
template <class TObject>
class TypeRegisterer {
public:
	TypeRegisterer(){
		TypeRegistryEntry<TObject> *entry=new TypeRegistryEntry<TObject>();
		TypeRegistry<typename TObject::tBase>::add_entry(entry);
	}
};

#endif /* TYPEREGISTERER_H_ */
