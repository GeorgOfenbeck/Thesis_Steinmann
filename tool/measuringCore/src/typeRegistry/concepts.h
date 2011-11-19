/*
 * concepts.h
 *
 *  Created on: Nov 18, 2011
 *      Author: ruedi
 */

#ifndef CONCEPTS_H_
#define CONCEPTS_H_


#include <boost/concept_check.hpp>
#include "utils.h"
#include <typeinfo>

namespace concepts{

template <class X>
struct polymorphic_concept{
	struct derived: X { };
	BOOST_CONCEPT_USAGE(polymorphic_concept){
		X *x;
		derived *d=dynamic_cast<derived*>(x);
		boost::ignore_unused_variable_warning(d);
	}
};

template <typename X, typename ... TArgs>
struct constructible_concept{
	BOOST_CONCEPT_USAGE(constructible_concept){
		// no arguments are processed
		instantiator<>
		//start with the first member of args
		// process all arguments
		::template inner<TArgs ...>
		// supply the data
		::func();
	}
private:

	/*
	 * The instantiator is used to instantiate a TObject
	 */
	template<typename ... TProcessed>
	struct instantiator{
		// if all arguments are processed, the base cased is used, which instantiates
		// TObject
		template<typename ... DUMMY>
		struct inner{
			static void func(TProcessed* ... args){
				X(args...);
			}
		};

		// this specialization is used as long as there are unprocessed arguments
		template<typename argsHead, typename ... argsTail>
		struct inner<argsHead,argsTail...>{
			static void func(TProcessed* ... args){
				void *ptr;

				// append the first unprocessed argument to the end of the processed arguments
				instantiator<TProcessed...,argsHead>
				// only the tail of the unprocessed arguments remains
				::template inner<argsTail ...>
				// do the call with the next index and append the head argument
				::func(args...,(argsHead*)ptr);
			}
		};
	};
};

template <class X>
struct DescriptionBase_concept{
	BOOST_CONCEPT_ASSERT((polymorphic_concept<X>));
};

template <class X>
struct Description_concept{
	BOOST_CONCEPT_ASSERT((polymorphic_concept<X>));
};

template <class X>
struct ObjectBase_concept{
	typedef typename X::tDescriptionBase tDescriptionBase;
};

template <class X>
struct Object_concept{
	typedef typename X::tBase tBase;
	typedef typename X::tDescription tDescription;

	BOOST_CONCEPT_ASSERT((ObjectBase_concept<tBase>));
	BOOST_CONCEPT_ASSERT((Description_concept<tDescription>));
	BOOST_CONCEPT_ASSERT((DescriptionBase_concept<typename tBase::tDescriptionBase>));
	BOOST_CONCEPT_ASSERT((boost::Convertible<X*,tBase*>));
	BOOST_CONCEPT_ASSERT((boost::Convertible<tDescription*,typename tBase::tDescriptionBase *>));
private:

};


struct DescriptionBase_archetype{
	// make class polymorphic
	virtual ~DescriptionBase_archetype(){}
protected:
	DescriptionBase_archetype(){}
};

struct Description_archetype
	: DescriptionBase_archetype
{
private:
		Description_archetype(){}
};


struct ObjectBase_archetype{
	typedef DescriptionBase_archetype tDescriptionBase;
protected:
	ObjectBase_archetype(){}
};

struct Object_archetype
	: ObjectBase_archetype
{
	typedef Description_archetype tDescription;
	typedef ObjectBase_archetype tBase;

	Object_archetype(tDescription *desc){}
private:
	Object_archetype(){}
};



}
#endif /* CONCEPTS_H_ */
