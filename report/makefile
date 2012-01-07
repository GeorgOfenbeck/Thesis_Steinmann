ARGOUML_JAR=~/argouml-0.34/argouml.jar
.PHONY: report clean quick
report: out/report.pdf

clean:
	rm -rf out

quick: 
	cd out &&\
	pdflatex ../report.tex

out/uml/timestamp: ../masterthesis.uml
	rm -rf out/uml
	mkdir -p out/uml
	java -jar $(ARGOUML_JAR) -batch \
	-command "org.argouml.uml.ui.ActionOpenProject=../masterthesis.uml" \
	-command "org.argouml.uml.ui.ActionSaveAllGraphics=out/uml"
	touch out/uml/timestamp
out:
	mkdir out
	mkdir out/uml

out/report.pdf: report.tex report.bib out # out/uml/timestamp
	cd out &&\
	pdflatex ../report.tex &&\
	bibtex report &&\
	makeindex referencedFiles &&\
	pdflatex ../report.tex &&\
	pdflatex ../report.tex