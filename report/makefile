ARGOUML_JAR=~/argouml-0.32.2/argouml.jar
.PHONY: report clean
report: out/report.pdf

clean:
	rm -rf out

out/uml/timestamp: ../masterthesis.uml
	rm -rf out/uml
	mkdir -p out/uml
	java -jar $(ARGOUML_JAR) -batch \
	-command "org.argouml.uml.ui.ActionOpenProject=../masterthesis.uml" \
	-command "org.argouml.uml.ui.ActionSaveAllGraphics=out/uml"
	touch out/uml/timestamp

out/report.pdf: report.tex report.bib out/uml/timestamp
	cd out &&\
	pdflatex ../report.tex &&\
	bibtex report &&\
	pdflatex ../report.tex &&\
	pdflatex ../report.tex