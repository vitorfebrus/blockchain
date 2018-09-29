
// testa a corretude das funcionalidade da blockchain, ainda localmente
public class TesteBlockchain {

	public static void main(String[] args) {
		
		try {
			
			Mineradora  mineradora = new Mineradora(); // cria uma mineradora para minerar os blocos de teste
 			Block genesisBlock = new Block(); // cria um bloco genesis para dar inicio à blockchain
			Blockchain blockchain = new Blockchain(genesisBlock); // blochain local de teste
			
			// configura um novo bloco para ser adicionado à blockchain
			Block bloco1 = new Block();
			bloco1.setHashBlockAnterior(genesisBlock.hash());
			
			// tenta criar um novo bloco com um bloco anterior diferente
			//bloco1.setHashBlockAnterior("eec7cba1341a41bce2a605274530203e4a827a583ffa1fb8188810a288fb8fe8");
			
			// minera o novo block que sera adicionado
			mineradora.minerarBloco(bloco1);
			
			// tenta adicionar uma transacao depois que o bloco ja tenha sido minerado
			//bloco1.getTransacoes().add(new Transacao("Transacao maliciosa"));
			
			// adiciona o novo bloco, caso o bloco seja valido
			blockchain.add(bloco1);
			
		} catch (Exception e) {
			e.printStackTrace();
		
		}
		
	}
	
}
