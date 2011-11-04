report: report.dvi

clean:
	rm -f out/*

report.dvi: report.tex report.bib
	cd out &&\
	pdflatex ../report.tex &&\
	bibtex report &&\
	pdflatex ../report.tex &&\
	pdflatex ../report.tex