library(stringr)
alfa <- c("1.0")
beta <- c("5.0")
q <- c("0.5")
ro <- c("0.2")
tamColonia <- c("58","29")
iteracoes <- c("1000")
selecao <- c("0")
problema <- c("brazil58","bays29")
pobSelecaoAleatoria <- c("0.0")
diversidade <- c("1")

for(i in alfa){
	for(j in beta){
		for(k in q){
			for(l in ro){
					for(div in diversidade){
						for(n in iteracoes){
							for(o in selecao){
								for(p in 1:length(problema)){
									for(ps in pobSelecaoAleatoria){
										Local <- str_c("C:\\Workspace\\ASRank\\src\\Testes\\Execs\\4_D_1000_29_57\\")
										Origem <- str_c("D_SaidaDiversidade_0execucao_",problema[p],"_saidaMelhorGlobal tamColonia-",tamColonia[p],"_iteracoes-",n,"_selecao-",o,"_alfa-",i,"_beta-",j,"_feromonio-",k,"_ro-",l, "_SA-",ps,"_del-6")
										Dados.brutos <- read.table(str_c(Local,Origem,".txt"), header = FALSE)
										Dados <- data.frame(Iteracoes = Dados.brutos$V1, Diversidade = Dados.brutos$V2)
										jpeg(filename = str_c(Local,"Imagem_",Origem,".jpg"),width = 960, height = 700,quality = 100, restoreConsole = TRUE)
										plot(Dados, type = 'l', col='red')
										dev.off()										
									}
								}
							}
						}		
					}
			}
		
		}
		
	}
}