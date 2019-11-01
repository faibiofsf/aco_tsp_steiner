import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Leitura {

	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {

			double[][] E;
			int[] nosEspeciais;

			int dimensao = 0;
			Scanner f = new Scanner(new File("src\\Testes\\SteinerTree\\steinb1.txt"));
			String s = f.nextLine();

			dimensao = Integer.parseInt(s.split(" ")[1])+1;

			E = new double[dimensao][dimensao];

			s = f.nextLine();
			String[] linha = s.split(" ");
			
			while ((!s.equals("EOF") || !s.equals("eof")) && linha.length > 3) {

				
				int i = Integer.parseInt(linha[1]);
				int j = Integer.parseInt(linha[2]);				
				
				double valor = Double.parseDouble(linha[3]);
				
				E[i][j] = valor;
				E[j][i] = valor;
				
				s = f.nextLine();
				linha = s.split(" ");
			}

			int numEspeciais = Integer.parseInt(linha[1]);
			
			nosEspeciais = new int[numEspeciais];
			int i = 0;
			while(f.hasNext()){
				
				nosEspeciais[i] = f.nextInt();
				i++;				
			}
			
			f.close();
			
			for (int j = 0; j < E.length; j++) {
				for (int j2 = 0; j2 < E[0].length; j2++) {
					System.out.print(" "+E[j][j2]);
				}
				System.out.print("\n");
			}
			

			for (int j = 0; j < nosEspeciais.length; j++) {
				System.out.print(" "+ nosEspeciais[j]);
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
