package TSP.AntSystem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Scanner;

public class AntSystem {

	private double[][] d;
	private double[][] feromonio;
	private double[][] dividendosProbabilidades;
	private double alfa, alfatemp, beta, betatemp, Qk, ro, probSelecaoAleatoria;
	private ArrayList<Formiga> colonia;
	private ArrayList<Integer> cidadesAVisitar;
	private Formiga melhorFormiga;
	private int numeroFormigas, numeroIteracoes, selecao, diversidade;
	boolean[] cidadesSelecionadasK;
	private FileWriter arqPopulacao, arqMelhorGlobal, arqSaidaDiversidade;
	private PrintWriter gravarArqPopulacao, gravarArqMelhorGlobal, gravarArqSaidaDiversidade;
	private Random random;
	public String saida;

	public AntSystem(double alfa, double beta, double qk, double ro, int numeroFormigas, int numeroIteracoes,
			int selecao, int diversidade, double probSelecaoAleatoria, String entrada, String saidaPopulacao,
			String saidaMelhorGlobal, String saidaDiversidade) {
		if (entrada.contains("brazil27") || entrada.contains("bays29")) {
			this.iniciarAmbienteFullMatrix(entrada);
		} else if (entrada.contains("att532")) {
			this.iniciarAmbienteCoordAtt(entrada);
		} else if (entrada.contains("pcb1173") || entrada.contains("eil76") || entrada.contains("kroA100")) {
			this.iniciarAmbienteCoordEuc_2d(entrada);
		} else if (entrada.contains("pa561")) {
			this.iniciarAmbienteLowerDiagRow(entrada);
		} else
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
		this.diversidade = diversidade;
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

		while (iteracao < numeroIteracoes) {

			colonia = new ArrayList<Formiga>();
			for (int k = 0; k < numeroFormigas; k++) {
				int[] caminhoFormigak = new int[d.length + 1];
				for (int j = 0; j < caminhoFormigak.length; j++) {
					caminhoFormigak[j] = -1;
				}

				this.cidadesAVisitar = new ArrayList<Integer>();
				for (int i = 0; i < this.d.length; i++) {
					this.cidadesAVisitar.add(new Integer(i));
				}

				cidadesSelecionadasK = new boolean[d.length];

				Formiga formiga = new Formiga(caminhoFormigak);

				if (iteracao < (int) (this.numeroIteracoes / 10)) {
					// Cria a rota aleatoria da formiga e atualiza a distancia
					this.criaRotaAleatoria(formiga);
				} else {
					// Cria a rota da formiga e atualiza a distancia
					this.criaRota(formiga, k);
				}

				colonia.add(formiga);
			}

			// Ranqueia a população
			this.rank();

			// Atualizar Feromonio
			this.atualizaFeromomio();

			textoMelhorGlobal[iteracao] = iteracao + "\t" + melhorFormiga.getLk();

			// Media de fitness da colonia
			double fitnessMedio = 0.0;
			for (Formiga formiga : colonia) {
				fitnessMedio += formiga.getLk();
			}
			fitnessMedio = fitnessMedio / colonia.size();
			textoMediaPopulacao[iteracao] = String.format("%.6f", fitnessMedio);

			// Melhor Formiga colonia
			textoMelhorFormigaPopulacao[iteracao] = colonia.get(0).getLk() + "";

			// Pior Formiga colonia
			textoPiorFormigaPopulacao[iteracao] = String.format("%.5f", colonia.get(colonia.size() - 1).getLk());

			double diversidaded = this.calculaDiversidadePeloPheromoneRatio();

			if (this.diversidade == 1) {
				if (diversidaded <= 4) {
					beta = 0;
				} else if (diversidaded >= 17) {
					beta = betatemp;
				}
			}

			textoDiversidade[iteracao] = iteracao + "\t" + diversidaded;// this.calculaDiversidadePelaSomaDistancias();

			colonia.clear();

			iteracao++;

		}
		/*
		 * int ci = 0; ArrayList<Integer> cidades = new ArrayList();
		 * cidades.add(0); System.out.print(ci+","); for (int i = 0; i <
		 * this.feromonio.length; i++) { int cj = -1; double maxefero = 0.0; for
		 * (int j = 0; j < this.feromonio.length; j++) {
		 * if(this.feromonio[ci][j] > maxefero && !cidades.contains(j)){
		 * maxefero = this.feromonio[ci][j]; cj = j; } }
		 * 
		 * ci = cj; cidades.add(ci); System.out.print(cj+",");
		 * 
		 * }
		 */

		// System.out.print("\t");

		saida = textoMelhorGlobal[textoMelhorGlobal.length - 1] + "\t";

		for (int cid : melhorFormiga.getSk()) {
			saida += cid + ",";
		}

		System.out.println(saida);

		for (String mFormiga : textoMelhorGlobal) {
			gravarArqMelhorGlobal.println(mFormiga);
		}

		for (int i = 0; i < textoMelhorFormigaPopulacao.length; i++) {
			gravarArqPopulacao.println(i + "\t" + textoMelhorFormigaPopulacao[i] + "\t" + textoMediaPopulacao[i] + "\t"
					+ textoPiorFormigaPopulacao[i]);
		}

		for (String txDiversidade : textoDiversidade) {
			gravarArqSaidaDiversidade.println(txDiversidade);
		}

		/*
		 * for (int i = 0; i < feromonio.length; i++) { for (int j = 0; j <
		 * feromonio.length; j++) { gravarArqPopulacao.printf(feromonio[i][j] +
		 * "\t"); } gravarArqPopulacao.printf("\n"); }
		 */

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

	private void criaRota(Formiga formiga, int k) {
		for (int posicao = 0; posicao < formiga.getSk().length - 1; posicao++) {

			int cidadeJ = -1;

			if (posicao == 0) {
				cidadeJ = this.cidadesAVisitar.get(k);// random.nextInt(this.cidadesAVisitar.size()));
				formiga.setCidade(posicao, cidadeJ);
				cidadesSelecionadasK[cidadeJ] = true;
				this.cidadesAVisitar.remove(new Integer(cidadeJ));
			} else {
				// Probabilidade de selecionar a cidade de forma aleatoria
				if (this.probSelecaoAleatoria > 0) {
					if (random.nextDouble() < this.probSelecaoAleatoria) {
						cidadeJ = this.cidadesAVisitar.get(random.nextInt(this.cidadesAVisitar.size()));
					} else {
						if (this.selecao == 0) {
							cidadeJ = this.selecionaCidadeJRoleta(formiga, posicao);
						} else if (this.selecao == 1) {
							cidadeJ = this.selecionaCidadeJTorneio(formiga, posicao);
						}
					}
				} else {
					if (this.selecao == 0) {
						cidadeJ = this.selecionaCidadeJRoleta(formiga, posicao);
					} else if (this.selecao == 1) {
						cidadeJ = this.selecionaCidadeJTorneio(formiga, posicao);
					}
				}

				formiga.setCidade(posicao, cidadeJ);
				cidadesSelecionadasK[cidadeJ] = true;
				this.cidadesAVisitar.remove(new Integer(cidadeJ));
				// Calcular a distancia entre o elemento na posição
				// anterior e o
				// elemento inserido na posição atual
				formiga.setLk(formiga.getLk() + d[formiga.getSk()[posicao - 1]][cidadeJ]);
			}
		}

		formiga.setCidade(formiga.getSk().length - 1, formiga.getSk()[0]);
		int ultima = formiga.getSk()[formiga.getSk().length - 2];
		int primeira = formiga.getSk()[formiga.getSk().length - 1];
		double distancia_ultima_primeira = d[ultima][primeira];
		formiga.setLk(formiga.getLk() + distancia_ultima_primeira);
	}

	private void criaRotaAleatoria(Formiga formiga) {
		for (int posicao = 0; posicao < formiga.getSk().length - 1; posicao++) {

			int cidadeJ = -1;
			cidadeJ = this.cidadesAVisitar.get(random.nextInt(this.cidadesAVisitar.size()));
			formiga.setCidade(posicao, cidadeJ);
			cidadesSelecionadasK[cidadeJ] = true;
			this.cidadesAVisitar.remove(new Integer(cidadeJ));
			if (posicao > 0) {
				formiga.setLk(formiga.getLk() + d[formiga.getSk()[posicao - 1]][posicao]);
			}
		}

		formiga.setCidade(formiga.getSk().length - 1, formiga.getSk()[0]);
		int ultima = formiga.getSk()[formiga.getSk().length - 2];
		int primeira = formiga.getSk()[formiga.getSk().length - 1];
		double distancia_ultima_primeira = d[ultima][primeira];
		formiga.setLk(formiga.getLk() + distancia_ultima_primeira);
	}

	private void atualizaFeromomio() {

		double[][] delta = new double[feromonio.length][feromonio[0].length];

		for (Formiga formiga : colonia) {
			deltaFeromomio(formiga, delta);
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
	private void deltaFeromomio(Formiga formiga, double[][] delta) {

		double deltatIJk = (formiga.getSk().length * Qk) / formiga.getLk();

		for (int i = 0; i < formiga.getSk().length; i++) {

			int cidadeI = -1;
			int cidadeJ = -1;

			if (i == formiga.getSk().length - 1) {
				cidadeI = formiga.getSk()[i];
				cidadeJ = formiga.getSk()[0];
			} else {
				cidadeI = formiga.getSk()[i];
				cidadeJ = formiga.getSk()[i + 1];
			}

			delta[cidadeI][cidadeJ] = deltatIJk;
			delta[cidadeJ][cidadeI] = deltatIJk;
		}
	}

	// Roleta da escolha cidade
	/**
	 * @param formiga
	 * @param posicao
	 * @return
	 */
	private int selecionaCidadeJRoleta(Formiga formiga, int posicao) {
		int i = formiga.getSk()[posicao - 1];
		double aleatorio = random.nextDouble();
		this.atualizaSomatorio(i);

		int escolhida = -1;

		// Seleciona somente as que nao foram escolhidas

		double somatorioProbabilidades = 0.0;
		ArrayList<Integer> aVisitar = (ArrayList<Integer>) this.cidadesAVisitar.clone();
		while (!aVisitar.isEmpty()) {
			int j = (int) aVisitar.remove(0);
			double prob = getProbabilidade(i, j);
			somatorioProbabilidades += prob;
			if (aleatorio < somatorioProbabilidades) {
				escolhida = j;
				break;
			}

		}

		if (escolhida == -1) {
			System.out.println(escolhida);
		}

		return escolhida;
	}

	private int selecionaCidadeJTorneio(Formiga formiga, int posicao) {
		int i = formiga.getSk()[posicao - 1];
		// double aleatorio = random.nextDouble();
		// this.atualizaSomatorio(i);

		int escolhida = -1;

		// Seleciona somente as que nao foram escolhidas

		double somatorioProbabilidades = 0.0;
		ArrayList<Integer> aVisitar = (ArrayList<Integer>) this.cidadesAVisitar.clone();
		ArrayList<Integer> aEscolherAleatorio = new ArrayList<Integer>();
		int tamanhoAVisitar = aVisitar.size();
		int tamanhoTorneio = (aVisitar.size() * 0.1) < 4 ? 4 : (int) (aVisitar.size() * 0.1);
		// System.out.println(tamanhoAVisitar);
		while (aEscolherAleatorio.size() < tamanhoTorneio && aEscolherAleatorio.size() < tamanhoAVisitar) {
			int j = (int) aVisitar.remove(random.nextInt(aVisitar.size()));
			aEscolherAleatorio.add(j);
		}

		// double feromonioC = this.getFeromonio(i, aEscolherAleatorio.get(0));
		double dividendo = this.atratividadeClassicaDaAresta(i, aEscolherAleatorio.get(0));

		escolhida = aEscolherAleatorio.get(0);

		for (int j = 0; j < aEscolherAleatorio.size(); j++) {
			// if(feromonioC > this.getFeromonio(i, aEscolherAleatorio.get(j)))
			// {
			if (dividendo > this.atratividadeClassicaDaAresta(i, aEscolherAleatorio.get(j))) {
				// feromonioC = this.getFeromonio(i, aEscolherAleatorio.get(j));
				dividendo = this.atratividadeClassicaDaAresta(i, aEscolherAleatorio.get(j));
				escolhida = aEscolherAleatorio.get(j);
			}
		}

		if (escolhida == -1) {
			System.out.println(escolhida);
		}

		return escolhida;
	}

	// probabilidade de estar na cidade i e ir para j
	private double getProbabilidade(int i, int j) {
		return this.dividendosProbabilidades[i][j] / somatorio;
	}

	// somatorio dos dividendos
	private double somatorio;

	private void atualizaSomatorio(int i) {
		somatorio = 0;

		ArrayList<Integer> aVisitar = (ArrayList<Integer>) this.cidadesAVisitar.clone();
		while (!aVisitar.isEmpty()) {
			int j = (int) aVisitar.remove(0);
			somatorio += atratividadeClassicaDaAresta(i, j);
		}

	}

	// calcula os dividendos
	private double atratividadeClassicaDaAresta(int i, int j) {
		dividendosProbabilidades[i][j] = Math.pow(feromonio[i][j], this.alfa) * Math.pow(1 / d[i][j], this.beta);
		return dividendosProbabilidades[i][j];
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

	// Ranqueamento da populaÃ§Ã£o
	public void rank() {

		Collections.sort(this.colonia);

		if (this.melhorFormiga.getLk() > this.colonia.get(0).getLk()) {
			this.melhorFormiga = new Formiga(this.colonia.get(0).getLk(), this.colonia.get(0).getSk());
		}
	}

	// Realiza aleitura do arquivo do tsp com as distÃ¢ncias ou
	// coordenadas
	private void iniciarAmbiente(String path) {
		try {

			double[][] E;

			int dimensao = 0;
			Scanner f = new Scanner(new File(path));
			String s = f.nextLine();

			while (!s.contains("DIMENSION: ")) {
				s = f.nextLine();
			}
			dimensao = Integer.parseInt(s.split(" ")[1]);

			E = new double[dimensao][dimensao];

			while (!s.contains("EDGE_WEIGHT_SECTION")) {
				s = f.nextLine();
			}

			int i = 0;
			s = f.nextLine();
			while (f.hasNext() && (!s.equals("EOF") || !s.equals("eof"))) {

				String[] linha = s.split(" ");
				int j = i + 1;
				for (int z = 0; z < linha.length; z++) {
					E[i][j] = Double.parseDouble(linha[z]);
					E[j][i] = Double.parseDouble(linha[z]);
					j++;
				}
				i++;
				s = f.nextLine();
			}

			f.close();

			this.d = E;

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void iniciarAmbienteCoordAtt(String path) {
		try {
			int dimensao = 0;
			Scanner f = new Scanner(new File(path));
			String s = f.nextLine();

			while (!s.contains("DIMENSION: ")) {
				s = f.nextLine();
			}
			dimensao = Integer.parseInt(s.split(" ")[1]);

			while (!s.contains("NODE_COORD_SECTION")) {
				s = f.nextLine();
			}
			ArrayList<double[]> lista = new ArrayList<double[]>();
			s = f.next();
			while (f.hasNext() && (!s.equals("EOF") && !s.equals("eof"))) {
				double x = f.nextDouble();
				double y = f.nextDouble();
				lista.add(new double[] { x, y });
				s = f.next();
			}

			double[][] E = new double[dimensao][dimensao];

			for (int i = 0; i < E.length; i++) {
				for (int j = 0; j < E.length; j++) {

					double xd = lista.get(i)[0] - lista.get(j)[0];
					double yd = lista.get(i)[1] - lista.get(j)[1];
					double rij = Math.sqrt(((xd * xd) + (yd * yd)) / 10);
					double tij = (int) Math.rint(rij);
					double dij = (tij < rij) ? tij + 1 : tij;

					E[i][j] = dij;
					E[j][i] = dij;

				}
			}

			f.close();

			this.d = E;

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void iniciarAmbienteCoordEuc_2d(String path) {
		try {

			int dimensao = 0;
			Scanner f = new Scanner(new File(path));
			String s = f.nextLine();

			while (!s.contains("DIMENSION: ")) {
				s = f.nextLine();
			}
			dimensao = Integer.parseInt(s.split(" ")[1]);

			while (!s.contains("NODE_COORD_SECTION")) {
				s = f.nextLine();
			}
			ArrayList<double[]> lista = new ArrayList<double[]>();
			s = f.next();
			while (f.hasNext() && (!s.equals("EOF") && !s.equals("eof"))) {
				double x = f.nextDouble();
				double y = f.nextDouble();
				lista.add(new double[] { x, y });
				s = f.next();
			}

			double[][] E = new double[dimensao][dimensao];

			for (int i = 0; i < E.length; i++) {
				for (int j = 0; j < i; j++) {

					double xd = Math.abs(lista.get(i)[0] - lista.get(j)[0]);
					double yd = Math.abs(lista.get(i)[1] - lista.get(j)[1]);
					double dij = (int) Math.rint((Math.sqrt(xd * xd + yd * yd)));

					E[i][j] = dij;
					E[j][i] = dij;

				}
			}

			f.close();

			this.d = E;

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// Realiza aleitura do arquivo do tsp com as distÃ¢ncias
	private void iniciarAmbienteFullMatrix(String path) {
		try {

			double[][] E;

			int dimensao = 0;
			Scanner f = new Scanner(new File(path));
			String s = f.nextLine();

			while (!s.contains("DIMENSION: ")) {
				s = f.nextLine();
			}
			dimensao = Integer.parseInt(s.split(" ")[1]);

			E = new double[dimensao][dimensao];

			while (!s.contains("EDGE_WEIGHT_SECTION")) {
				s = f.nextLine();
			}

			int i = 0;
			s = f.nextLine();
			while (f.hasNext() && (!s.equals("EOF") && !s.equals("eof"))) {

				String[] linha = s.split(" ");
				for (int z = 0; z < linha.length; z++) {
					if (linha[z].contentEquals("Inf")) {
						E[i][z] = 1000000000;
					} else
						E[i][z] = Double.parseDouble(linha[z]);

				}
				i++;
				s = f.nextLine();
			}

			f.close();

			this.d = E;

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void iniciarAmbienteLowerDiagRow(String path) {
		try {

			double[][] E;

			int dimensao = 0;
			Scanner f = new Scanner(new File(path));
			String s = f.nextLine();

			while (!s.contains("DIMENSION: ")) {
				s = f.nextLine();
			}
			dimensao = Integer.parseInt(s.split(" ")[1]);

			E = new double[dimensao][dimensao];

			while (!s.contains("EDGE_WEIGHT_SECTION")) {
				s = f.nextLine();
			}

			int i = 0;
			s = f.nextLine();
			while (f.hasNext() && (!s.equals("EOF") && !s.equals("eof"))) {

				String[] linha = s.split(" ");
				int j = 0;
				for (int z = 0; z < linha.length; z++) {
					E[i][z] = Double.parseDouble(linha[z]);
					E[z][i] = Double.parseDouble(linha[z]);
					j++;
				}
				i++;

				s = f.nextLine();
			}
			f.close();

			this.d = E;

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
			int[] tamColonia = { 58 };
			int[] iteracoes = { 1000 };
			// 0 - roleta, 1 - torneio
			int[] selecao = { 0 };
			String[] problema = { "bays29"};// , "brazil58",
												// "kroA100", "att48", "eil76",
												// "att532", "d1291" };
			// Probabilidade de selecionar uma cidade de forma aleatorioa
			double[] pobSelecaoAleatoria = { 0 };
			// Sem controle de diversidade = 0; com controle de diversidade = 1
			int[] diversidade = { 0, 1 };

			int numeroExecucoes = 50;

			for (int div = 0; div < diversidade.length; div++) {
				for (int sa = 0; sa < pobSelecaoAleatoria.length; sa++) {
					for (int pr = 0; pr < problema.length; pr++) {

						if (problema[pr].equals("att532"))
							tamColonia[0] = 532;
						else if (problema[pr].equals("d1291"))
							tamColonia[0] = 1291;
						else if (problema[pr].equals("eil76"))
							tamColonia[0] = 76;
						else if (problema[pr].equals("kroA100"))
							tamColonia[0] = 100;
						else if (problema[pr].equals("bays29"))
							tamColonia[0] = 29;
						else if (problema[pr].equals("brazil58"))
							tamColonia[0] = 58;
						else if (problema[pr].equals("pa561"))
							tamColonia[0] = 561;
						else if (problema[pr].equals("att48"))
							tamColonia[0] = 48;

						arqSaidas = new FileWriter("..\\src\\Testes\\Execs\\AntSystem\\TSP\\saida_testes_" + problema[pr]
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

														String entrada = "..\\src\\Testes\\TSP\\" + problema[pr]
																+ ".tsp";
														String saidaPopulacao = "..\\src\\Testes\\Execs\\AntSystem\\TSP\\Testes_"
																+ l2 + "execucao_" + problema[pr]
																+ "_saidaPopulacao tamColonia-" + tamColonia[k2]
																+ "_iteracoes-" + iteracoes[l]  + "_diversidade-" + diversidade[div] + "_selecao-"
																+ selecao[se]
																+ "_alfa-" + alfa[i] + "_beta-" + beta[j]
																+ "_feromonio-" + q[j2] + "_ro-" + ro[k] + "_SA-"
																+ pobSelecaoAleatoria[sa] + ".txt";
														String saidaMelhorGlobal = "..\\src\\Testes\\Execs\\AntSystem\\TSP\\Testes_"
																+ l2 + "execucao_" + problema[pr]
																+ "_saidaMelhorGlobal tamColonia-" + tamColonia[k2]
																+ "_iteracoes-" + iteracoes[l]  + "_diversidade-" + diversidade[div] + "_selecao-"
																+ selecao[se]
																+ "_alfa-" + alfa[i] + "_beta-" + beta[j]
																+ "_feromonio-" + q[j2] + "_ro-" + ro[k] + "_SA-"
																+ pobSelecaoAleatoria[sa] + ".txt";
														String saidaDiversidade = "..\\src\\Testes\\Execs\\AntSystem\\TSP\\SaidaDiversidade_"
																+ l2 + "execucao_" + problema[pr]
																+ "_saidaMelhorGlobal tamColonia-" + tamColonia[k2]
																+ "_iteracoes-" + iteracoes[l]  + "_diversidade-" + diversidade[div] + "_selecao-"
																+ selecao[se]
																+ "_alfa-" + alfa[i] + "_beta-" + beta[j]
																+ "_feromonio-" + q[j2] + "_ro-" + ro[k] + "_SA-"
																+ pobSelecaoAleatoria[sa] + ".txt";

														AntSystem aco = new AntSystem(alfa[i], beta[j], q[j2], ro[k],
																tamColonia[k2], iteracoes[l], selecao[se],
																diversidade[div], pobSelecaoAleatoria[sa], entrada,
																saidaPopulacao, saidaMelhorGlobal, saidaDiversidade);

														String texto = "Teste\t" + l2 + "\texecucao\t" + problema[pr]
																+ "\ttamColonia\t" + tamColonia[k2] + "\titeracoes\t"
																+ iteracoes[l] + "\tdiversidade\t" + diversidade[div] + "\tselecao\t" + selecao[se]
																 + "\talfa\t"
																+ alfa[i] + "\tbeta\t" + beta[j] + "\tQ\t" + q[j2]
																+ "\tro\t" + ro[k] + "\tSA\t" + pobSelecaoAleatoria[sa]
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

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

class Formiga implements Comparable<Formiga> {

	private double Lk = Integer.MAX_VALUE;
	private int[] Sk;

	public Formiga() {

	}

	public Formiga(int[] Sk) {
		this.Lk = 0.0;
		this.Sk = Sk;
	}

	public Formiga(double Lk, int[] Sk) {
		super();
		this.Lk = Lk;
		this.Sk = Sk;
	}

	public double getLk() {
		return Lk;
	}

	public void setLk(double Lk) {
		this.Lk = Lk;
	}

	public int[] getSk() {
		return Sk;
	}

	public void setCidade(int posicao, int cidade) {
		this.Sk[posicao] = cidade;
	}

	public void setSk(int[] Sk) {
		this.Sk = Sk;
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