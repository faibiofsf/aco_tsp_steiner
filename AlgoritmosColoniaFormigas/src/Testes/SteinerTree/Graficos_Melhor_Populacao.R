library(stringr)
alfa <- c("1.0")
beta <- c("5.0")
q <- c("0.5")
ro <- c("0.2")
tamColonia <- c("29")
iteracoes <- c("1000")
selecao <- c("0")
problema <- c("bays29")
pobSelecaoAleatoria <- c("0.0")

for(i in alfa){
	for(j in beta){
		for(k in q){
			for(l in ro){
				for(m in tamColonia){
					for(n in iteracoes){
						for(o in selecao){
							for(p in problema){
								for(ps in pobSelecaoAleatoria){
									Origem1 <- str_c("C:\\Workspace\\ACO_NOVO\\src\\Testes\\Execs\\10_1000\\Testes_0execucao_",p)
									Origem2 <- str_c("_saidaPopulacao tamColonia-")
									Origem3 <- str_c(m,"_iteracoes-",n,"_selecao-",o,"_alfa-",i,"_beta-",j,"_feromonio-",k,"_ro-",l, "_SA-",ps)
									Dados.brutos <- read.table(str_c(Origem1,Origem2,Origem3,".txt"), header = FALSE)
									Dados1 <- data.frame(Iteracoes = Dados.brutos$V1, Distancia_Melhor = Dados.brutos$V2)
									jpeg(filename = str_c(Origem1,str_c("_saidaMelhorPopulacao tamColonia-"),Origem3,".jpg"),width = 960, height = 700,quality = 100, restoreConsole = TRUE)
									plot(Dados1, type = 'l', col='red')
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