/*
 * FileMeasurer.cpp
 *
 *  Created on: Jan 25, 2012
 *      Author: ruedi
 */

#include "FileMeasurer.h"
#include "typeRegistry/TypeRegisterer.h"
#include "utils.h"
#include <string>
#include <fstream>

using namespace std;

static TypeRegisterer<FileMeasurer> dummy;
FileMeasurer::~FileMeasurer() {
	// TODO Auto-generated destructor stub
}

void FileMeasurer::start(){
	output=new FileMeasurerOutput();
	foreach(string file, description->getFilesToRecord()){
		// create content
		FileContent *content=new FileContent();
		content->setFileName(file);

		// read file
		ifstream ifs(file);
		string s( (std::istreambuf_iterator<char>(ifs) ),
		                       (std::istreambuf_iterator<char>()    ) );
		content->setStartContent(s);

		output->getFileContentList().push_back(content);
	}

}

void FileMeasurer::stop(){
	foreach (FileContent *content, output->getFileContentList()){
		string file=content->getFileName();
		ifstream ifs(file);
		string s( (std::istreambuf_iterator<char>(ifs) ),
							   (std::istreambuf_iterator<char>()    ) );
		content->setStopContent(s);
	}
}

MeasurerOutputBase* FileMeasurer::read(){
	return output;
}
