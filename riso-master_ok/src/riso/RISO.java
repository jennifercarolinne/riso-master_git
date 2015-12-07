package riso;

import java.util.Iterator;
import java.util.List;

import jena.VetorTematico;
import riso.builder.conceptNet5.URI.out.ConceitoExpandido;
import riso.builder.conceptNet5.URI.out.FiltroDeResultados;
import riso.builder.conceptNet5.URI.out.Topico;
import riso.builder.documents.Biblioteca;
import riso.builder.documents.Desambiguador;
import riso.builder.documents.Documento;
import riso.builder.topicMap.OntoMaker;
import riso.db.biblioteca.BibliotecaDAO;
import riso.db.vetoresTematicos.VetorTematicoDAO;

public class RISO {
	
	public static void main(String args[]){
		
		Biblioteca biblioteca = new Biblioteca();

		List<Documento> documentos = biblioteca.constroiDocumentos();
		
		BibliotecaDAO bibDAO = new BibliotecaDAO(documentos);
		Iterator<Documento> it = documentos.iterator();
		
		while(it.hasNext()){
			
			Documento doc = it.next();
			String palavrasMarcadas = doc.getPalavrasMarcadas();
			
			System.out.println(palavrasMarcadas.length());
			palavrasMarcadas = palavrasMarcadas.replaceAll("\\[", "");
			palavrasMarcadas = palavrasMarcadas.replaceAll("\\]", "");
			System.out.println(palavrasMarcadas.length());
			
			if(!palavrasMarcadas.isEmpty()){
				boolean esseDocumentoExiste = bibDAO.verificaSeDocumentoExiste(doc);
				if(!esseDocumentoExiste){
					bibDAO.salvaDocumento(doc);
				}
				String conceitos[] = palavrasMarcadas.split(",");
				System.out.println(palavrasMarcadas);
				int i = 0;
				for (String conceito : conceitos) {
					i++;
					System.out.println("Processando conceito ["+i+"] de ["+conceitos.length+"]");
					System.out.println("Conceito a ser processado: " + conceito);
					
					if (conceito.indexOf("[") < 0 && conceito.indexOf("]") < 0){
						VetorTematicoDAO vetorDAO = new VetorTematicoDAO();
						List<VetorTematico> vetores = vetorDAO.obtemVetoresTematicos(conceito);
						
						if(vetores.isEmpty()){
							System.out.println("Vetor vazio"); // george remover
							ConceitoExpandido conceitoExp = new ConceitoExpandido(conceito);
							System.out.println("Enriquece Conceito: " + conceito);
//							conceitoExp.enriqueceConceito(FiltroDeResultados.SEARCH_TEXT,"&"); georg remover
							conceitoExp.enriqueceConceito(FiltroDeResultados.SEARCH_TEXT,"?");
							System.out.println("Fim Enriquece Conceito: " + conceito);
							vetores = vetorDAO.obtemVetoresTematicos(conceito);
							System.out.println("Obteve vetor tematico Conceito: " + conceito);
						}else{
							System.out.println("Vetor nao vazio"); // george remover
						}
		 
						System.out.println("Desambiguador: " + conceito);
						Desambiguador desambiguador = new Desambiguador(doc, vetores);
						System.out.println("calculaVetorTermos: " + conceito);
						desambiguador.calculaVetorTermos(conceito);
						if (desambiguador.getVetorTematico().size() > 0){
							System.out.println("desambiguador.getVetorTematico().size() > 0: " + conceito);
							VetorTematico vetor = desambiguador.getVetorTematico().get(0);
							Topico topico = vetor.getVetor().get(0);

							OntoMaker onto = new OntoMaker();
							System.out.println("Indexa: " + conceito);
							onto.indexaDocumento(doc, conceito, topico);
							System.out.println("Fim: " + conceito);
							
						}
						
					}
					
				}	
			}
			
			
		}
		
	}

}