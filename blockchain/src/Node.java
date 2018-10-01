import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// classe que representa um no da blockchain
// junta todas as funcionalidades implementadas no projeto
public class Node extends Thread implements Constants {

	// objeto que ira minerar novos blocos
	private Mineradora mineradora;

	// lista de transacoes que ainda nao foram mineradas, ainda nao estao em nenhum
	// bloco
	private List<Transacao> transacoesNaoConfirmadas;

	private Blockchain blockchain;

	// forneca funcionalidades basicas de comunicacao
	private NetworkUDP redeUDP;

	// forneca funcionalidades basicas de comunicacao
	private NetworkTCP redeTCP;

	
	public Node() {
		mineradora = new Mineradora();
		transacoesNaoConfirmadas = Collections.synchronizedList(new ArrayList<Transacao>());

		// cria o bloco genesis da blockchain
		Block genesisBlock = new Block();
		genesisBlock.setTimeStamp(0);
		blockchain = new Blockchain(genesisBlock);
		redeUDP = new NetworkUDP(this);
		redeTCP = new NetworkTCP(this);
	}

	@Override
	public void run() {

		int cod = 0;

		try {
			// executa infinitamente...
			while (true) {
				
				// cria uma transacao de teste
				addTransacao(new Transacao(String.valueOf(cod)));

				// caso tenham n o mais transacoes para serem mineradas...
				if (transacoesNaoConfirmadas.size() >= QUANT_TRANSACAO_BLOCO) {
					
					// cria um bloco com n transacoes nao confirmadas
					Block bloco = new Block();
					fillBlock(bloco);
					
					// minera o bloco, ou para se receber o bloco minerado por outro no.
					// a condicao de parada esta implementada na propria mineradora
					
					Block blocoMinerado = mineradora.minerarBloco(bloco);

					// bloco minerado por outro no...
					if (blocoMinerado == null) {
						
						System.out.println("Bloco recebido de outro no");
						
						// reativa a mineradora
						mineradora.getMinerar().set(true);
					}
					else { // bloco minerado por esse no
						System.err.println("Bloco minerado com sucesso por este no" + blocoMinerado.getId());

						// envia o bloco para os outros nos da rede (broadcast)
						redeUDP.broadcast(blocoMinerado);
						
						// adiciona o bloco na blockchain
						blockchain.add(blocoMinerado);
					}

				}
				//break;
				//System.err.println("Reiniciando o processo...");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// add na lista de transacoes nao confirmadas e trata questoes de concorrencia e
	// condicao de corrida entre threads
	public void addTransacao(Transacao t) {

		synchronized (transacoesNaoConfirmadas) {
			transacoesNaoConfirmadas.add(t);
		}

	}

	// preenche o bloco com transacoes nao confirmadas e as outras informacoes uteis
	public void fillBlock(Block bloco) {

		List<Transacao> transacoesBloco = new ArrayList<>();

		synchronized (transacoesNaoConfirmadas) {
			// pega as transacoes da lista de transacoes nao confirmadas
			transacoesBloco.addAll(transacoesNaoConfirmadas.subList(0, QUANT_TRANSACAO_BLOCO));
			// remove as transacoes selecionadas da lista de transacoes nao confirmadas
			transacoesNaoConfirmadas.removeIf(t -> transacoesNaoConfirmadas.indexOf(t) <= QUANT_TRANSACAO_BLOCO);
		}
		// seta as transacoes do bloco
		bloco.setTransacoes(transacoesBloco);
		// seta o id deste bloco como idAnterior + 1
		bloco.setId(blockchain.getBlocoAtual().getId() + 1);
		bloco.setHashBlockAnterior(blockchain.getBlocoAtual().hash());

	}

	public List<Transacao> getTransacoesNaoConfirmadas() {
		return transacoesNaoConfirmadas;
	}

	public void setTransacoesNaoConfirmadas(List<Transacao> transacoesNaoConfirmadas) {
		this.transacoesNaoConfirmadas = transacoesNaoConfirmadas;
	}

	public Blockchain getBlockchain() {
		return blockchain;
	}

	public void setBlockchain(Blockchain blockchain) {
		this.blockchain = blockchain;
	}

	public NetworkUDP getRede() {
		return redeUDP;
	}

	public void setRede(NetworkUDP rede) {
		this.redeUDP = rede;
	}

	public Mineradora getMineradora() {
		return mineradora;
	}

	public void setMineradora(Mineradora mineradora) {
		this.mineradora = mineradora;
	}

}
