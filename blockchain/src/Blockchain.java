
// representa a blockchain em si
public class Blockchain implements Constants {

	// bloco de origem da blockchain
	private Block genesisBlock;

	// guarda o bloco mais recente
	private Block blocoAtual;

	public Blockchain(Block genesisBlock) {
		this.genesisBlock = genesisBlock;
		// no inicio, o bloco mais recente e' o genesis
		blocoAtual = genesisBlock;
		genesisBlock.setBlocoAnterior(null); // o bloco genesis nao tem bloco anterior
	}

	// adiciona um novo bloco na blockchain
	public synchronized boolean add(Block blocoNovo) {
		
		// 1 - verificar se o hash do bloco recebido esta calculado corretamente
		boolean dificuldadeSatisfeita = blocoNovo.hash().substring(0, DIFICULDADE).equals(condicao());
		
		
		// 2 - verificar se o hash do bloco anterior esta correto
		boolean hashBlocoAnteriorSatisfeito = blocoAtual.hash().equals(blocoNovo.getHashBlockAnterior());
		
		// bloco valido, insere ele na blockchain
		if(dificuldadeSatisfeita && hashBlocoAnteriorSatisfeito) {
			
			// seta o id do novo bloco
			blocoNovo.setId(blocoAtual.getId() + 1);
			
			blocoNovo.setBlocoAnterior(blocoAtual);
			blocoAtual = blocoNovo;
			
			System.out.print("Block adicionado com sucesso!");
			return true;
		}
		// bloco invalido
		else {
			System.out.println("Block invalido nao adicionado!");
			// notificar a rede sobre um no suspeito
			// pedir o reenvio do block
			return false;
		}
		
	}
	
	// cria uma string com a quantidade de 0 referente a dificuldade
	public String condicao() {
		String condicao = "";

		for (int i = 0; i < DIFICULDADE; i++) {
			condicao += "0";
		}

		return condicao;
	}

	public Block getGenesisBlock() {
		return genesisBlock;
	}

	public void setGenesisBlock(Block genesisBlock) {
		this.genesisBlock = genesisBlock;
	}

	public Block getBlocoAtual() {
		return blocoAtual;
	}

	public void setBlocoAtual(Block blocoAtual) {
		this.blocoAtual = blocoAtual;
	}

}
