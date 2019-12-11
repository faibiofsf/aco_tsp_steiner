library(stringr)

sink("C:\\Workspace\\Scripts R\\P-Testes_Steiner.txt")

ASR_steinb1_1000 <- c(95,100,101,101,106,107,107,107,108,109,111,111,112,112,112,112,112,113,114,115,115,116,116,116,117,118,118,119,121,121)

AS_steinb1_1000 <- c(101,101,105,105,106,106,106,106,106,107,107,108,110,110,111,111,111,112,112,112,112,113,113,114,114,116,116,117,118,118)


cat("Mann-Whitney U-Test Steinb1")
cat("\n")
for(x in 1:2){
	str <- ""
	for(y in 1:2){
		str2 <- ""
		X <- switch(x,ASR_steinb1_1000, AS_steinb1_1000)
		Y <- switch(y,ASR_steinb1_1000, AS_steinb1_1000)

		if(x==y){
			str2 <- "0.0000"
		}
		else {
			test <- wilcox.test(X,Y)
			str2 <- formatC(test$p.value, digits = 4)
		}
		str <- str_c(str,"	",str2)
	}
	cat(str,sep="\n")
}
cat("\n\n")
cat("para o Steinb1 \t Media \t Max \t Min")
cat("\n")
for(x in 1:2){
	X <- switch(x,ASR_steinb1_1000, AS_steinb1_1000)
	Y <- switch(x,"ASR_steinb1_1000", "AS_steinb1_1000")
	media <- formatC(mean(X), digits = 10)
	max <- formatC(max(X), digits = 10)
	min <- formatC(min(X), digits = 10)
	str2 <- str_c(Y,"\t",media,"\t",max,"\t",min)

	cat(str2,sep="\n")
}