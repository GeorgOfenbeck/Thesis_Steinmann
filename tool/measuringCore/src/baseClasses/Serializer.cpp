/*
 * Serializer.cpp
 *
 *  Created on: Feb 24, 2012
 *      Author: ruedi
 */

#include "Serializer.h"

Serializer::~Serializer() {
	// TODO Auto-generated destructor stub
}

/**
 * escapes a string such that it can be serialized
 */
string Serializer::escapeString(string s) {
	string result;

	// iterate over the input
	for (unsigned int i = 0; i < s.length(); i++) {

		// escape newlines
		if (s[i] == '\n') {
			result.append("\\n");
		}
		// escape backslashes
		else if (s[i] == '\\') {
			result.append("\\\\");
		} else {
			// it's a normal char, just append it
			result.push_back(s[i]);
		}
	}
	return result;
}

/**
 * revert the escaping
 */
string Serializer::deEscapeString(string s) {
	string result;

	// iterate over the input
	for (unsigned int i = 0; i < s.length(); i++) {
		// is the current character the beginning of an escape sequence?
		if (s[i] == '\\') {
			// go to next char
			i++;

			// the sequence may not stop now
			if (i >= s.length()) {
				throw "invalid string";
			}

			// append the escaped character to the result
			if (s[i] == '\\') {
				result.push_back('\\');
			} else if (s[i] == 'n') {
				result.push_back('\n');
			} else {
				throw "invalid character sequence \\" + s[i];
			}
		} else {
			// it's a normal char, just append it
			result.push_back(s[i]);
		}
	}
	return result;
}
