package LerArquivo;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Leitura {

	static double E[][];
	static int especiais[], contadorEspeciais=0;
	static boolean[] visitados;
	static ArrayList<int[]> arestas;
	static ArrayList<No> vertices;

	public Leitura() {

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		arestas = new ArrayList<int[]>();
		vertices = new ArrayList<No>();

		Scanner f;
		try {
			f = new Scanner(new File("src\\LerArquivo\\steinfa.txt"));
			String[] s = f.nextLine().split("\t");
			int tamanho = Integer.parseInt(s[1]);

			Leitura.E = new double[tamanho + 1][tamanho + 1];

			while (f.hasNext() && (s = f.nextLine().split("\t")).length > 2) {

				int i = Integer.parseInt(s[1]);
				int j = Integer.parseInt(s[2]);
				int d = Integer.parseInt(s[3]);

				int[] aresta1 = { i, j, d };
				int[] aresta2 = { j, i, d };

				arestas.add(aresta1);
				arestas.add(aresta2);

				Leitura.E[i][j] = d;
				Leitura.E[j][i] = d;
			}

			int nEspeciais = Integer.parseInt(s[1]);

			Leitura.especiais = new int[nEspeciais];

			int i = 0;
			while (f.hasNext()) {

				int no = f.nextInt();

				Leitura.especiais[i] = no;

				i++;
			}

			f.close();

			for (int i1 = 0; i1 < Leitura.E.length; i1++) {
				for (int j = 0; j < Leitura.E[i1].length; j++) {
					System.out.print(Leitura.E[i1][j] + "\t");
				}
				System.out.print("\n");
			}

			System.out.print("\n");

			for (int i1 = 0; i1 < Leitura.especiais.length; i1++) {
				System.out.print(Leitura.especiais[i1] + "\t");
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Leitura.visitados = new boolean[Leitura.E.length];

		No arvore = new No(3);
		Leitura.vertices.add(arvore);

		Leitura.visitados[arvore.getVertice()] = true;
		
		for (int i = 0; i < Leitura.especiais.length; i++) {
			if(arvore.getVertice() == Leitura.especiais[i]) {
				Leitura.contadorEspeciais++;
			}
		}

		Leitura l = new Leitura();

		l.formarCaminho();
		
		System.out.print("\n");

		double distancia = l.percorrerArvore(arvore);

		System.out.println("\n d: " + distancia);

	}

	private double percorrerArvore(No arvore) {

		if (arvore.getSubArvore().size() != 0) {
			double distancia = 0;
			for (int i = 0; i < arvore.getSubArvore().size(); i++) {
				distancia += Leitura.E[arvore.getVertice()][arvore.getSubArvore().get(i).getVertice()];

				System.out.print("[" + arvore.getVertice() + ", " + arvore.getSubArvore().get(i).getVertice() + "]");
				System.out.print(" D: " + distancia);

				distancia += percorrerArvore(arvore.getSubArvore().get(i));
			}

			System.out.print("\n");

			return distancia;

		} else {
			return 0;
		}

	}

	private void formarCaminho() {

		while(Leitura.contadorEspeciais < Leitura.especiais.length){
			
			ArrayList<int[]> visinhaca = new ArrayList<int[]>();
			
			for (int i = 0; i < Leitura.arestas.size(); i++) {
				if (Leitura.visitados[Leitura.arestas.get(i)[0]] == true
						&& Leitura.visitados[Leitura.arestas.get(i)[1]] == false) {
					visinhaca.add(Leitura.arestas.get(i));
				}

			}

			if (visinhaca.size() != 0) {
				
				int escolhido = visinhaca.get(0)[1];
				System.out.println(" E: "+escolhido);
				
				if (Leitura.visitados[escolhido] == false) {
					if (E[visinhaca.get(0)[0]][escolhido] != 0) {
						Leitura.visitados[escolhido] = true;
						Leitura.vertices.add(new No(escolhido));
					}
				}
				
				int origemEscolhido = visinhaca.get(0)[0];
				
				for (int i = 0; i < Leitura.vertices.size(); i++) {
					if (Leitura.vertices.get(i).getVertice() == origemEscolhido) {
						Leitura.vertices.get(i).getSubArvore().add(Leitura.vertices.get(Leitura.vertices.size() - 1));
					}
				}
				
				for (int i = 0; i < Leitura.especiais.length; i++) {
					if(escolhido == Leitura.especiais[i]) {
						Leitura.contadorEspeciais++;
					}
				}
			}
			
		}

	}

}

class No {

	Integer vertice;
	ArrayList<No> vizinhanca;

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
		vizinhanca = null;
	}

	public Integer getVertice() {
		return vertice;
	}

	public void setVertice(Integer vertice) {
		this.vertice = vertice;
	}

	public ArrayList<No> getSubArvore() {
		return vizinhanca;
	}

	public void setSubArvore(ArrayList<No> vizinhanca) {
		this.vizinhanca = vizinhanca;
	}
}
