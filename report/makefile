report: report.dvi

clean:
	rm -f out/*

report.dvi: report.tex report.bib
	cd out &&\
	latex ../report.tex &&\
	bibtex report &&\
	latex ../report.tex &&\
	latex ../report.tex