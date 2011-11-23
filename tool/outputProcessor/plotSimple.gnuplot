set log xy
plot 'plotSimple.data' index '[cps]' using 1:2:4:5:3 with candlesticks
pause mouse keypress
