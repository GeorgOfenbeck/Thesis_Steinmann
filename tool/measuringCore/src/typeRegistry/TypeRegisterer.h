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
#include "concepts.h"

/*
 * When instantiated, registers the type given as template parameter with the type registry
 * corresponding to TObject::tBase. Used in the .cpp file of a class as static variable:
 *
 *  static TypeRegisterer<MemoryLoadKernel> dummy;
 *
 *  The variable itself is never used.
 */
template <typename TObject, typename ... TArgs>
class TypeRegisterer {
	BOOST_CONCEPT_ASSERT((concepts::Object_concept<TObject>));
	BOOST_CONCEPT_ASSERT((concepts::constructible_concept<TObject,typename TObject::tDescription,TArgs ...>));
public:
	TypeRegisterer(){
		TypeRegistryEntry<TObject, TArgs...> *entry=new TypeRegistryEntry<TObject,TArgs ...>();
		TypeRegistry<typename TObject::tBase>::add_entry(entry);
	}
};

#endif /* TYPEREGISTERER_H_ */
