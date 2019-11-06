package LerArquivo;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Leitura {

	static double E[][];
	static int especiais[];
	static boolean[] visitados;
	
	public Leitura(){
		
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Scanner f;
		try {
			f = new Scanner(new File("src\\LerArquivo\\steinfa.txt"));
			String[] s = f.nextLine().split("\t");
			int tamanho = Integer.parseInt(s[1]);
			
			Leitura.E = new double[tamanho+1][tamanho+1];
			
			while(f.hasNext() && (s = f.nextLine().split("\t")).length > 2) {
				
				int i = Integer.parseInt(s[1]);
				int j = Integer.parseInt(s[2]);
				int d = Integer.parseInt(s[3]);
				
				Leitura.E[i][j] = d;
				Leitura.E[j][i] = d;
			}
			
			int nEspeciais = Integer.parseInt(s[1]);
			
			Leitura.especiais = new int[nEspeciais];
			
			int i = 0;
			while(f.hasNext()) {
				
				int no = f.nextInt();
				
				Leitura.especiais[i]=no;
				
				i++;
			}
			
			f.close();
			
			for (int i1 = 0; i1 < Leitura.E.length; i1++) {
				for (int j = 0; j < Leitura.E[i1].length; j++) {
					System.out.print(Leitura.E[i1][j]+"\t");
				}
				System.out.print("\n");
			}
			
			System.out.print("\n");
			
			for (int i1 = 0; i1 < Leitura.especiais.length; i1++) {
				System.out.print(Leitura.especiais[i1]+"\t");
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Leitura.visitados = new boolean[Leitura.E.length];
		
		No arvore = new No(3, null);
		
		Leitura.visitados[arvore.getVertice()] = true;
		
		Leitura l = new Leitura();
		
		l.formarCaminho(arvore);
		System.out.print("\n");
		for (int i = 0; i < Leitura.visitados.length; i++) {
			System.out.print(" "+Leitura.visitados[i]);
		}
		
		System.out.print("\n");
		
		double distancia = l.percorrerArvore(arvore);
		
		System.out.println("\n d: "+distancia);
			
	}
	
	
	private double percorrerArvore(No arvore) {
		
		if(arvore.getVizinhanca()!=null) {
			double distancia = 0;
			for (int i = 0; i < arvore.getVizinhanca().size(); i++) {
				distancia+=Leitura.E[arvore.getVertice()][arvore.getVizinhanca().get(i).getVertice()];
				
				System.out.print("["+arvore.getVertice()+", "+arvore.getVizinhanca().get(i).getVertice() +"]");
				System.out.print(" D: "+distancia);
				
				distancia += percorrerArvore(arvore.getVizinhanca().get(i));
			}
			
			System.out.print("\n");
			
			return distancia;
			
		}		
		else {
			return 0;
		}
		
	}
	
	
	private void formarCaminho(No arvore) {
		
		ArrayList<No> vizinhanca = new ArrayList<No>();
		
		for (int i = 0; i < E.length; i++) {
			if ( Leitura.visitados[i] == false) {
				if(E[arvore.getVertice()][i] != 0) {
					vizinhanca.add(new No(i, null));
					Leitura.visitados[i] = true;
				}
			}
		}
		
		if(!vizinhanca.isEmpty()) {
			arvore.setVizinhanca(vizinhanca);
			for (int i = 0; i < arvore.getVizinhanca().size(); i++) {
				formarCaminho(arvore.getVizinhanca().get(i));
			}
		}
		
	}
	
	
}
	
class No {
	 
	Integer vertice;
	ArrayList<No> vizinhanca;
	
	No(Integer vertice, ArrayList<No> vizinhos){
		this.setVertice(vertice);
		this.setVizinhanca(vizinhos);
	}
	
	No(){
		vertice = -1;
		vizinhanca = null;
	}
	
	public Integer getVertice() {
		return vertice;
	}

	public void setVertice(Integer vertice) {
		this.vertice = vertice;
	}

	public ArrayList<No> getVizinhanca() {
		return vizinhanca;
	}

	public void setVizinhanca(ArrayList<No> vizinhanca) {
		this.vizinhanca = vizinhanca;
	}
}
