
// teste basico sobre cast de objetos
public class TesteInstanceof {

	public static void main(String[] args) {

		Block novoBloco = new Block();
		Transacao novaTransacao = new Transacao("");
		
		Object objBloco = novoBloco;
		Object objTransacao = novaTransacao;
		
		if(objBloco instanceof Block) {
			System.out.println("Classe " + objBloco.getClass().getName());
		}
		
		if(objTransacao instanceof Transacao) {
			System.out.println("Classe " + objTransacao.getClass().getName());
		}
		
		if(objBloco instanceof Object) {
			System.out.println("Object");
		}
		
		if(objTransacao instanceof Object) {
			System.out.println("Object");
		}
	}

}
