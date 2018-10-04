import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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

	private HashMap<String, Block> backupBlocos;

	// forneca funcionalidades basicas de comunicacao
	private NetworkUDP redeUDP;

	public Node() {
		mineradora = new Mineradora();
		transacoesNaoConfirmadas = Collections.synchronizedList(new ArrayList<Transacao>());

		// cria o bloco genesis da blockchain
		Block genesisBlock = new Block();
		genesisBlock.setTimeStamp(0);
		blockchain = new Blockchain(genesisBlock);
		redeUDP = new NetworkUDP(this);

		backupBlocos = new HashMap<>();
	}

	@Override
	public void run() {

		int cod = 0;

		try {
			// executa infinitamente...
			while (true) {

				// cria uma transacao de teste
				addTransacao(new Transacao(String.valueOf(cod)));

				// caso tenham n ou mais transacoes para serem mineradas...
				if (transacoesNaoConfirmadas.size() >= QUANT_TRANSACAO_BLOCO) {

					// cria um bloco com n transacoes nao confirmadas
					Block bloco = new Block();
					fillBlock(bloco);

					// minera o bloco, ou para se receber o bloco minerado por outro no.
					// a condicao de parada esta implementada na propria mineradora

					Block blocoMinerado = mineradora.minerarBloco(bloco);

					// bloco minerado por outro no...
					if (blocoMinerado == null) {

						// System.out.println("Bloco recebido de outro no");

						// reativa a mineradora
						mineradora.getMinerar().set(true);
					} else { // bloco minerado por esse no
						System.err.println("Bloco " + blocoMinerado.getId() + " minerado com sucesso por este no! "
								+ blocoMinerado);

						// envia o bloco para os outros nos da rede (broadcast)
						redeUDP.broadcast(blocoMinerado);

						// adiciona o bloco na blockchain
						blockchain.add(blocoMinerado);
					}

				}
				// break;
				// System.err.println("Reiniciando o processo...");
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

	public void tratarApdu(Object apdu) {

		try {

			// caso a apdu recebida tenha uma transacao...
			if (apdu instanceof Transacao) {
				// adiciona a transacao recebida na lista de transacoes nao confirmadas
				addTransacao((Transacao) apdu);
			}
			// caso a apdu recebida tenha um bloco...
			else if (apdu instanceof Block) {

				// trata o bloco de acordo com o id dele
				tratarBloco((Block) apdu);

			}
			// caso a apdu recebida seja uma requisicao da blockchain...
			else if (apdu instanceof BlockchainRequest) {
				// envia toda a blockchain para o ip que a solcitou
				redeUDP.send(blockchain, "");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void tratarBloco(Block blocoRecebido) {

		// pega o id do bloco mais recente da blochain
		int idAtual = blockchain.getBlocoAtual().getId();

		// proximo bloco
		if (idAtual == blocoRecebido.getId() + 1) {

			if (blockchain.add(blocoRecebido)) { // adiciona o bloco na blockchain
				mineradora.getMinerar().set(false);
				/*
				 * se a adicao for um sucesso, indica para a mineradora que este bloco ja foi
				 * minerado remove todas as transacoes que ja foram mineradas por outro no da
				 * lista de transacoes nao confirmadas
				 */
				getTransacoesNaoConfirmadas().removeAll(blocoRecebido.getTransacoes());
			} else {
				
				// verifica se este no tem o bloco anterior ao bloco nao adicionado
				Block anterior = backupBlocos.get(blocoRecebido.getHashBlockAnterior());
				
				// algum outro no tem uma blockchain maior do que a deste no... reestrutura a blockchain
				if(anterior != null) {
					// remove o mais recente
					blockchain.removerMaisRecente();
					
					// seta o anterior como o mais recente
					blockchain.add(anterior);
					
					// seta o blocoRecebido como o mais recente
					blockchain.add(blocoRecebido);
					
				}
				
			}

		}

		// blocos simultaneos
		if (idAtual == blocoRecebido.getId()) {
			backupBlocos.put(blocoRecebido.hash(), blocoRecebido); // guarda o bloco
		}

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
