package SteinerTree.ASRank;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Scanner;

public class ASRank {

	private double[][] d;
	private double[][] feromonio;
	private double[][] dividendosProbabilidades;
	private double alfa, alfatemp, beta, betatemp, Qk, ro, probSelecaoAleatoria;
	private ArrayList<Formiga> colonia;
	private ArrayList<Integer> cidadesAVisitar;
	private Formiga melhorFormiga;
	private int numeroFormigas, numeroIteracoes, selecao, delt, diversidade, contadorEspeciais;
	boolean[] cidadesSelecionadasK, cidadesVisitadas;
	private FileWriter arqPopulacao, arqMelhorGlobal, arqSaidaDiversidade;
	private PrintWriter gravarArqPopulacao, gravarArqMelhorGlobal, gravarArqSaidaDiversidade;
	private Random random;
	public String saida;
	private int[] especiais;
	private ArrayList<int[]> arestas;

	public ASRank(double alfa, double beta, double qk, double ro, int numeroFormigas, int numeroIteracoes, int selecao,
			int diversidadae, int delt, double probSelecaoAleatoria, String entrada, String saidaPopulacao,
			String saidaMelhorGlobal, String saidaDiversidade) {
		// Abrir arquivo do problema
		this.iniciarAmbiente(entrada);
		
		this.alfa = alfa;
		this.alfatemp = alfa;
		this.beta = beta;
		this.betatemp = beta;
		this.Qk = qk;
		this.ro = ro;
		this.numeroFormigas = numeroFormigas;
		this.numeroIteracoes = numeroIteracoes;
		this.selecao = selecao;
		this.feromonio = new double[d.length][d.length];
		this.dividendosProbabilidades = new double[d.length][d.length];
		try {
			this.arqPopulacao = new FileWriter(saidaPopulacao);
			this.arqMelhorGlobal = new FileWriter(saidaMelhorGlobal);
			this.arqSaidaDiversidade = new FileWriter(saidaDiversidade);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.gravarArqPopulacao = new PrintWriter(arqPopulacao);
		this.gravarArqMelhorGlobal = new PrintWriter(arqMelhorGlobal);
		this.gravarArqSaidaDiversidade = new PrintWriter(arqSaidaDiversidade);
		random = new Random();// 12345);
		this.probSelecaoAleatoria = probSelecaoAleatoria;
		this.delt = delt;
		this.diversidade = diversidadae;
		this.cidadesVisitadas = new boolean[this.d.length];
	}

	private void iniciar() {
		melhorFormiga = new Formiga();
		for (int i = 0; i < feromonio.length; i++) {
			for (int j = 0; j < feromonio.length; j++) {
				feromonio[i][j] = 0.001;
			}
		}
		int iteracao = 0;
		String textoDiversidade[] = new String[numeroIteracoes];
		String textoMelhorGlobal[] = new String[numeroIteracoes];
		String textoMelhorFormigaPopulacao[] = new String[numeroIteracoes];
		String textoMediaPopulacao[] = new String[numeroIteracoes];
		String textoPiorFormigaPopulacao[] = new String[numeroIteracoes];
		double diversidades[] = new double[numeroIteracoes];

		while (iteracao < numeroIteracoes) {

			colonia = new ArrayList<Formiga>();
			for (int k = 0; k < numeroFormigas; k++) {

				Formiga formiga = new Formiga();

				if (iteracao < (int) (this.numeroIteracoes / 10)) {
					// Cria a rota aleatoria da formiga e atualiza a distancia
					this.criaRotaAleatoria(formiga);
				} else {
					// Cria a rota da formiga e atualiza a distancia
					this.criaRota(formiga);
				}
				
				formiga.setLk(this.calcularDistancia(formiga));

				colonia.add(formiga);
			}

			// Ranqueia a populaÃ§Ã£o
			this.rank();

			// Atualizar Feromonio
			this.atualizaFeromomio();

			System.out.println("MelhorIteração: "+melhorFormiga.getLk());
			
			colonia.clear();

			iteracao++;

		}
		
		System.out.println("\n\nMelhor Formiga Todas: "+melhorFormiga.getLk());
		
		for (int i = 0; i < melhorFormiga.getRota().size(); i++) {
			System.out.print(" "+melhorFormiga.getRota().get(i));
		}
		System.out.print("\n");
		
		try {
			gravarArqPopulacao.close();
			arqPopulacao.close();
			gravarArqMelhorGlobal.close();
			arqMelhorGlobal.close();
			gravarArqSaidaDiversidade.close();
			arqSaidaDiversidade.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private double desvioPadraoPopulacional(double[] variaveis, int iteracao) {
		int contador = 0;
		double soma = 0.0;
		for (int i = iteracao; i > iteracao - 20; i--) {
			if (variaveis[i] > 0.0) {
				contador++;
				soma += variaveis[i];
			}
		}

		double media = soma / contador;
		double soma2 = 0.0;
		for (int i = iteracao; i > iteracao - 20; i--) {
			soma2 += Math.pow(variaveis[i] - media, 2);
		}

		return Math.sqrt(soma2 / contador);

	}

	private double calculaDiversidadePelaSomaDistancias() {

		double diversidade = 0.0;

		for (int i = 0; i < this.colonia.size(); i++) {
			for (int j = 0; j < this.colonia.size(); j++) {
				if (i != j) {
					double distancia = this.colonia.get(i).getLk() - this.colonia.get(j).getLk();
					diversidade += (distancia < 0) ? -distancia : distancia;
				}
			}
		}

		return diversidade / (this.colonia.size() ^ 2);
	}

	private double calculaDiversidadePeloPheromoneRatio() {

		double numeroArestasComFeromonio = 0.0;

		double numeroArestas = 0.0;

		double[][] razao = new double[this.feromonio.length][this.feromonio[0].length];

		for (int i = 0; i < this.feromonio.length; i++) {
			double soma = 0.0;
			for (int j = 0; j < this.feromonio[0].length; j++) {
				if (this.getDistancia(i, j) < 1000000000) {
					numeroArestas++;
					soma += this.feromonio[i][j];
				}
			}

			for (int j = 0; j < this.feromonio[0].length; j++) {
				if (this.getDistancia(i, j) < 1000000000) {
					razao[i][j] = 100 * (this.feromonio[i][j] / soma);
					double r = 100 * (1 / (double) this.feromonio.length);
					if (razao[i][j] > r) {
						numeroArestasComFeromonio++;
					}
				}
			}

		}

		return 100 * (numeroArestasComFeromonio / numeroArestas);
	}

	private void criaRota(Formiga formiga) {
		//int cidadeInicio = this.especiais[random.nextInt(this.especiais.length)];
		int cidadeInicio = random.nextInt(this.d.length);
		
		No arvore = new No(cidadeInicio);
		//System.out.println("Inicio: " + cidadeInicio);
		formiga.setSk(arvore);
		formiga.insertVertices(arvore);

		this.cidadesVisitadas[cidadeInicio] = true;
		this.contadorEspeciais = 0;

		for (int i = 0; i < this.especiais.length; i++) {
			if(this.especiais[i] == cidadeInicio){
				this.contadorEspeciais = 1;
			}
		}

		ArrayList<No> vertices = formiga.getVertices();

		while (this.contadorEspeciais < this.especiais.length) {
			ArrayList<int[]> vizinhanca = new ArrayList<int[]>();

			for (int i = 0; i < this.arestas.size(); i++) {
				if (this.cidadesVisitadas[this.arestas.get(i)[0]] == true
						&& this.cidadesVisitadas[this.arestas.get(i)[1]] == false) {
					vizinhanca.add(this.arestas.get(i));
				}

			}

			if (vizinhanca.size() != 0) {

				// A aresta deve ser selecionada pela probabilidade
				int indiceAresta = this.arestaSelecionadaJRoleta(formiga, vizinhanca);

				int escolhido = vizinhanca.get(indiceAresta)[1];

				if (this.cidadesVisitadas[escolhido] == false) {
					if (this.d[vizinhanca.get(indiceAresta)[0]][escolhido] != 0) {
						this.cidadesVisitadas[escolhido] = true;
						vertices.add(new No(escolhido));
						int origemEscolhido = vizinhanca.get(indiceAresta)[0];
						formiga.insertAposCidade(origemEscolhido, vertices.get(vertices.size() - 1));
					}
				}

				for (int i = 0; i < this.especiais.length; i++) {
					if (escolhido == this.especiais[i]) {
						this.contadorEspeciais++;
					}
				}
			}
			else break;

		}

		cidadesVisitadas = new boolean[this.d.length];
	}

	private void criaRotaAleatoria(Formiga formiga) {
		//int cidadeInicio = this.especiais[random.nextInt(this.especiais.length)];
		int cidadeInicio = random.nextInt(this.d.length);
		No arvore = new No(cidadeInicio);
		//System.out.println("Inicio: " + cidadeInicio);
		formiga.setSk(arvore);
		formiga.insertVertices(arvore);

		this.cidadesVisitadas[cidadeInicio] = true;
		this.contadorEspeciais = 0;

		for (int i = 0; i < this.especiais.length; i++) {
			if(this.especiais[i] == cidadeInicio){
				this.contadorEspeciais = 1;
			}
		}
		
		ArrayList<No> vertices = formiga.getVertices();

		while (this.contadorEspeciais < this.especiais.length) {
			ArrayList<int[]> vizinhanca = new ArrayList<int[]>();

			for (int i = 0; i < this.arestas.size(); i++) {
				if (this.cidadesVisitadas[this.arestas.get(i)[0]] == true
						&& this.cidadesVisitadas[this.arestas.get(i)[1]] == false) {
					vizinhanca.add(this.arestas.get(i));
				}

			}

			if (vizinhanca.size() != 0) {

				int indiceAresta = random.nextInt(vizinhanca.size());

				int escolhido = vizinhanca.get(indiceAresta)[1];

				if (this.cidadesVisitadas[escolhido] == false) {
					if (this.d[vizinhanca.get(indiceAresta)[0]][escolhido] != 0) {
						this.cidadesVisitadas[escolhido] = true;
						vertices.add(new No(escolhido));
						int origemEscolhido = vizinhanca.get(indiceAresta)[0];
						formiga.insertAposCidade(origemEscolhido, vertices.get(vertices.size() - 1));
					}
				}

				for (int i = 0; i < this.especiais.length; i++) {
					if (escolhido == this.especiais[i]) {
						this.contadorEspeciais++;
					}
				}
			}
			else break;

		}

		cidadesVisitadas = new boolean[this.d.length];
	}

	private void atualizaFeromomio() {

		double[][] delta = new double[feromonio.length][feromonio[0].length];

		if(this.delt > colonia.size()){
			this.delt = colonia.size();
		}
		
		for (int i = 0; i < this.delt; i++) {
			Formiga formiga = colonia.get(i);
			deltaFeromomio(formiga, delta, formiga.getSk());

			deltaFeromomio(this.melhorFormiga, delta, formiga.getSk());
		}

		// double p = Math.random();
		double p = this.ro;

		for (int i = 0; i < feromonio.length; i++) {
			for (int j = 0; j < feromonio[i].length; j++) {
				feromonio[i][j] = ((1 - p) * feromonio[i][j]) + delta[i][j];
				feromonio[i][j] += 10e-100;
			}
		}
	}

	// Calcula o delta de feromonio
	private void deltaFeromomio(Formiga formiga, double[][] delta, No arvore) {
		double deltatIJk = (1/formiga.getVertices().size() * Qk) / formiga.getLk();

		if (arvore.getSubArvore().size() != 0) {

			for (int i = 0; i < arvore.getSubArvore().size(); i++) {
				int cidadeI = arvore.getVertice();
				int cidadeJ = arvore.getSubArvore().get(i).getVertice();

				delta[cidadeI][cidadeJ] = deltatIJk;
				delta[cidadeJ][cidadeI] = deltatIJk;

				deltaFeromomio(formiga, delta, arvore.getSubArvore().get(i));
			}
		}
	}

	// Roleta da escolha cidade
	/**
	 * @param formiga
	 * @param posicao
	 * @return
	 */
	private int arestaSelecionadaJRoleta(Formiga formiga,  ArrayList<int[]> vizinhanca) {
		double somatorio = 0;
		for (int i = 0; i < vizinhanca.size(); i++) {
			somatorio += atratividadeClassicaDaAresta(vizinhanca.get(i)[0], vizinhanca.get(i)[1]);
		}

		double indice = Math.random();
		double somatorioProbabilidades = 0;
		for (int i = 0; i < vizinhanca.size(); i++) {
			somatorioProbabilidades += atratividadeClassicaDaAresta(vizinhanca.get(i)[0], vizinhanca.get(i)[1])
					/ somatorio;
			if (indice <= somatorioProbabilidades) {
				return i;
			}
		}

		return -1;
	}

	// calcula os dividendos
	private double atratividadeClassicaDaAresta(int i, int j) {
		double dividendosProbabilidades = Math.pow(feromonio[i][j], this.alfa) * Math.pow(1 / d[i][j], this.beta);
		return dividendosProbabilidades;
	}

	// atualiza feromonio
	private void setaFeromonio(int i, int j, double novoFeromonio) {
		this.feromonio[i][j] = novoFeromonio;
	}

	// getdistancia
	private double getDistancia(int i, int j) {
		return this.d[i][j];
	}

	// getferomonio
	private double getFeromonio(int i, int j) {
		return this.feromonio[i][j];
	}

	// Ranqueamento da populaÃƒÂ§ÃƒÂ£o
	public void rank() {

		Collections.sort(this.colonia);

		if (this.melhorFormiga.getLk() > this.colonia.get(0).getLk()) {
			this.melhorFormiga = new Formiga(this.colonia.get(0).getLk(), this.colonia.get(0).getSk());
			this.melhorFormiga.setRota(colonia.get(0).getRota());
			this.melhorFormiga.setVertices(colonia.get(0).getVertices());
		}
	}
	
	private double calcularDistancia(Formiga formiga) {
		
		return this.percorrerArvore(formiga, formiga.getSk());
		
	}
	
	private double percorrerArvore(Formiga formiga, No arvore) {

		if (arvore.getSubArvore().size() != 0) {
			double distancia = 0;
			for (int i = 0; i < arvore.getSubArvore().size(); i++) {
				int cidadeI = arvore.getVertice();
				int cidadeJ = arvore.getSubArvore().get(i).getVertice();
				distancia += this.d[cidadeI][cidadeJ];

				formiga.insertRota("[" + cidadeI + ", " + cidadeJ + "]");
				
				distancia += percorrerArvore(formiga, arvore.getSubArvore().get(i));
			}

			return distancia;

		} else {
			return 0;
		}
	}

	// Realiza aleitura do arquivo do tsp com as distÃƒÂ¢ncias ou
	// coordenadas
	private void iniciarAmbiente(String path) {
		// TODO Auto-generated method stub

				this.arestas = new ArrayList<int[]>();

				Scanner f;
				try {
					f = new Scanner(new File(path));
					String[] s = f.nextLine().split(" ");
					int tamanho = Integer.parseInt(s[1]);

					double[][] E = new double[tamanho + 1][tamanho + 1];

					while (f.hasNext() && (s = f.nextLine().split(" ")).length > 2) {

						int i = Integer.parseInt(s[1]);
						int j = Integer.parseInt(s[2]);
						int d = Integer.parseInt(s[3]);

						int[] aresta1 = { i, j, d };
						int[] aresta2 = { j, i, d };

						arestas.add(aresta1);
						arestas.add(aresta2);

						E[i][j] = d;
						E[j][i] = d;
					}

					int nEspeciais = Integer.parseInt(s[1]);

					int[] especiais = new int[nEspeciais];

					int i = 0;
					while (f.hasNext()) {

						int no = f.nextInt();

						especiais[i] = no;

						i++;
					}

					f.close();

					this.d = E;
					this.especiais = especiais;

				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		FileWriter arqSaidas;
		try {

			double[] alfa = { 1 };
			double[] beta = { 5 };
			double[] q = { 0.5 };
			double[] ro = { 0.2 };
			int[] tamColonia = { 50 };
			int[] iteracoes = { 100 };
			// 0 - roleta, 1 - torneio
			int[] selecao = { 0 };
			String[] problema = { "steinb1" };
			// Probabilidade de selecionar uma cidade de forma aleatorioa
			double[] pobSelecaoAleatoria = { 0 };
			int[] del = { 6 };
			// Sem controle de diversidade = 0; com controle de diversidade = 1
			int[] diversidade = { 0 };

			int numeroExecucoes = 1;

			for (int div = 0; div < diversidade.length; div++) {
				for (int delt = 0; delt < del.length; delt++) {
					for (int sa = 0; sa < pobSelecaoAleatoria.length; sa++) {
						for (int pr = 0; pr < problema.length; pr++) {

							arqSaidas = new FileWriter("src\\Testes\\Execs\\ASRank\\SteinerTree\\saida_testes_" + problema[pr]
									+ "_diversidade-" + diversidade[div] + ".txt");
							PrintWriter saida = new PrintWriter(arqSaidas);

							for (int se = 0; se < selecao.length; se++) {
								for (int i = 0; i < alfa.length; i++) {
									for (int j = 0; j < beta.length; j++) {
										for (int j2 = 0; j2 < q.length; j2++) {
											for (int k = 0; k < ro.length; k++) {
												for (int k2 = 0; k2 < tamColonia.length; k2++) {
													for (int l = 0; l < iteracoes.length; l++) {
														// Numero de Execucoes
														for (int l2 = 0; l2 < numeroExecucoes; l2++) {

															String entrada = "src\\Testes\\SteinerTree\\" + problema[pr]
																	+ ".txt";
															String saidaPopulacao = "src\\Testes\\Execs\\ASRank\\SteinerTree\\Testes_"
																	+ l2 + "execucao_" + problema[pr]
																	+ "_saidaPopulacao tamColonia-" + tamColonia[k2]
																	+ "_iteracoes-" + iteracoes[l] + "_diversidade-" + diversidade[div] + "_selecao-"
																	+ selecao[se] 
																	+ "_alfa-" + alfa[i] + "_beta-" + beta[j]
																	+ "_feromonio-" + q[j2] + "_ro-" + ro[k] + "_SA-"
																	+ pobSelecaoAleatoria[sa] + "_del-" + del[delt]
																	+ ".txt";
															String saidaMelhorGlobal = "src\\Testes\\Execs\\ASRank\\SteinerTree\\Testes_"
																	+ l2 + "execucao_" + problema[pr]
																	+ "_saidaMelhorGlobal tamColonia-" + tamColonia[k2]
																	+ "_iteracoes-" + iteracoes[l] + "_diversidade-" + diversidade[div] + "_selecao-"
																	+ selecao[se]
																	+ "_alfa-" + alfa[i] + "_beta-" + beta[j]
																	+ "_feromonio-" + q[j2] + "_ro-" + ro[k] + "_SA-"
																	+ pobSelecaoAleatoria[sa] + "_del-" + del[delt]
																	+ ".txt";
															String saidaDiversidade = "src\\Testes\\Execs\\ASRank\\SteinerTree\\SaidaDiversidade_"
																	+ l2 + "execucao_" + problema[pr]
																	+ "_saidaMelhorGlobal tamColonia-" + tamColonia[k2]
																	+ "_iteracoes-" + iteracoes[l] + "_diversidade-" + diversidade[div] + "_selecao-"
																	+ selecao[se]
																	+ "_alfa-" + alfa[i] + "_beta-" + beta[j]
																	+ "_feromonio-" + q[j2] + "_ro-" + ro[k] + "_SA-"
																	+ pobSelecaoAleatoria[sa] + "_del-" + del[delt]
																	+ ".txt";

															ASRank aco = new ASRank(alfa[i], beta[j], q[j2], ro[k],
																	tamColonia[k2], iteracoes[l], selecao[se],
																	diversidade[div], del[delt],
																	pobSelecaoAleatoria[sa], entrada, saidaPopulacao,
																	saidaMelhorGlobal, saidaDiversidade);

															String texto = "Teste\t" + l2 + "\texecucao\t"
																	+ problema[pr] + "\ttamColonia\t" + tamColonia[k2]
																	+ "\titeracoes\t" + iteracoes[l] + "\tdiversidade\t" + diversidade[div] + "\tselecao\t"
																	+ selecao[se] 
																	+ "\talfa\t" + alfa[i] + "\tbeta\t" + beta[j]
																	+ "\tQ\t" + q[j2] + "\tro\t" + ro[k] + "\tSA\t"
																	+ pobSelecaoAleatoria[sa] + "_del-" + del[delt]
																	+ "\t";

															System.out.print(texto);

															saida.print(texto);

															aco.iniciar();

															saida.println(aco.saida);
														}
													}
												}
											}
										}
									}
								}
							}
							arqSaidas.close();
							saida.close();
						}
					}
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

class Formiga implements Comparable<Formiga> {

	private double Lk = Integer.MAX_VALUE;
	private No Sk;
	private ArrayList<No> vertices;
	private ArrayList<String> rota;

	public Formiga() {
		vertices = new ArrayList<No>();
		rota = new ArrayList<String>();
	}

	public Formiga(No Sk) {
		this.Lk = 0.0;
		this.Sk = Sk;
		vertices = new ArrayList<No>();
		rota = new ArrayList<String>();
	}

	public Formiga(double Lk, No Sk) {
		super();
		this.Lk = Lk;
		this.Sk = Sk;
		vertices = new ArrayList<No>();
		rota = new ArrayList<String>();
	}

	public double getLk() {
		return Lk;
	}

	public void setLk(double Lk) {
		this.Lk = Lk;
	}

	public No getSk() {
		return Sk;
	}

	public void insertAposCidade(int posicao, No novoNo) {

		for (int i = 0; i < this.vertices.size(); i++) {
			if (this.vertices.get(i).getVertice() == posicao) {
				this.vertices.get(i).getSubArvore().add(novoNo);
			}
		}
	}

	public void setSk(No Sk) {
		this.Sk = Sk;
	}

	public ArrayList<No> getVertices() {
		return vertices;
	}

	public void insertVertices(No vertice) {
		this.vertices.add(vertice);
	}
	
	@SuppressWarnings("unchecked")
	public void setVertices(ArrayList<No> vertices) {
		this.vertices = (ArrayList<No>)vertices.clone();
	}

	public ArrayList<String> getRota() {
		return rota;
	}

	public void insertRota(String rota) {
		this.rota.add(rota);
	}
	
	public void setRota(ArrayList<String> rota) {
		this.rota = (ArrayList<String>) rota.clone();
	}

	@Override
	public int compareTo(Formiga outraFormiga) {
		// TODO Auto-generated method stub
		if (this.Lk < outraFormiga.getLk()) {
			return -1;
		}
		if (this.Lk > outraFormiga.getLk()) {
			return 1;
		}
		return 0;
	}

}

class No {

	Integer vertice;
	ArrayList<No> subArvore;

	No(Integer vertice, ArrayList<No> vizinhos) {
		this.setVertice(vertice);
		this.setSubArvore(vizinhos);
	}

	No(Integer vertice) {
		this.setVertice(vertice);
		this.setSubArvore(new ArrayList<No>());
	}

	No() {
		vertice = -1;
		subArvore = null;
	}

	public Integer getVertice() {
		return vertice;
	}

	public void setVertice(Integer vertice) {
		this.vertice = vertice;
	}

	public ArrayList<No> getSubArvore() {
		return subArvore;
	}

	public void setSubArvore(ArrayList<No> vizinhanca) {
		this.subArvore = vizinhanca;
	}
}
