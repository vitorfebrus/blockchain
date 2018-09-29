import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

// classe responsavel por fornecer comunicacao pela rede
public class NetworkTCP implements Constants {

	private ObjectOutputStream saida;

	private Socket socket;

	private ReceiverUDP input;

	// cria um relacionamento bidirecional entre o no e a network
	private Node no;

	public NetworkTCP() {
		input = new ReceiverUDP(no);
		input.start();
	}

	public NetworkTCP(Node no) {
		this.no = no;
		input = new ReceiverUDP(no);
		input.start();
	}

	// envia um objeto para um determinado ip
	public void send(Object obj, String ip) {
		try {
			// cria um socket e abre a conexao com o receptor
			socket = new Socket(ip, PORT);
			saida = new ObjectOutputStream(socket.getOutputStream());

			// envia o objeto
			saida.writeObject(obj);

			// fecha as conexoes
			saida.close();
			socket.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// envia um objeto para um determinado ip
	public void send(Object obj, InetAddress ip) {
		try {
			// cria um socket e abre a conexao com o receptor
			socket = new Socket(ip, PORT);
			saida = new ObjectOutputStream(socket.getOutputStream());

			// envia o objeto
			saida.writeObject(obj);

			// fecha as conexoes
			saida.close();
			socket.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void broadcast() {

	}

	public Node getNo() {
		return no;
	}

	public void setNo(Node no) {
		this.no = no;
	}

	public ReceiverUDP getInput() {
		return input;
	}

	public void setInput(ReceiverUDP input) {
		this.input = input;
	}

}

// thread resposavel por receber as conexoes a passar para outra classe
// trata-las
class Receiver extends Thread implements Constants {

	// socket servidor
	private ServerSocket servidor;

	private Node no;

	public Receiver() {

	}

	public Receiver(Node no) {
		this.no = no;
	}

	// recebe objetos da rede
	@Override
	public void run() {

		try {

			// inicializa o servidor
			servidor = new ServerSocket(PORT);

			// o socket servidor recebe conexoes infinitamente
			while (true) {

				Socket cliente = servidor.accept(); // aguarda a conexao
				new ConectionManager(cliente, no).start(); // passa a conexao para a classe responsavel tratar
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

// thread que trata as conexoes recebidas pelo socket servidor
class ConectionManager extends Thread {

	private Socket cliente;

	private ObjectInputStream entrada;

	private Node no;

	public ConectionManager(Socket cliente) {
		this.cliente = cliente;
	}

	public ConectionManager(Socket cliente, Node no) {
		this.no = no;
		this.cliente = cliente;
	}

	@Override
	public void run() {
		try {

			// cria a stream com o cliente (emissor)
			entrada = new ObjectInputStream(cliente.getInputStream());
			Object objetoRecebido = entrada.readObject(); // captura o pbjeto recebido

			tratarApdu(objetoRecebido);

			// encerra a conexao
			cliente.close();
			entrada.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void tratarApdu(Object apdu) {

		try {

			// caso a apdu recebida tenha uma transacao...
			if (apdu instanceof Transacao) {
				// adiciona a transacao recebida na lista de transacoes nao confirmadas
				no.addTransacao((Transacao) apdu);
			}
			// caso a apdu recebida tenha um bloco...
			else if (apdu instanceof Block) {

				// trata o bloco de acordo com o id dele
				tratarBloco((Block) apdu);

			}
			// caso a apdu recebida seja uma requisicao da blockchain...
			else if (apdu instanceof BlockchainRequest) {
				// envia toda a blockchain para o ip que a solcitou
				Blockchain blockchain = no.getBlockchain();
				no.getRede().send(blockchain, "");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void tratarBloco(Block bloco) {

		// pega o id do bloco mais recente da blochain

		Block atual = no.getBlockchain().getBlocoAtual();
		int idAtual = atual.getId();

		// caso este no ainda nao tenha esse bloco
		if (idAtual < bloco.getId()) {

			// caso seja o proximo bloco
			if (bloco.getId() == idAtual + 1) {
				if (no.getBlockchain().add(bloco)) { // adiciona o bloco na blockchain
					no.getMineradora().getMinerar().set(false);
					; // se a adicao for um sucesso, indica para a mineradora que
						// este bloco ja foi minerado
					// remove todas as transacoes que ja foram mineradas por outro no
					// da lista de transacoes nao confirmadas
					no.getTransacoesNaoConfirmadas().removeAll(bloco.getTransacoes());
				}
			} else {
				// muito improvavel em rede local com geracao de blocos a cada 1 minuto
				// blockchain muito antiga...
				// guarda o bloco e espera por novos blocos pra completar o gap
			}
		}
		// caso este bloco tenha o mesmo id do bloco atual (blocos minerados
		// "simultaneamente")
		else if (idAtual == bloco.getId()) {
			// guarda o bloco
		}
	}
}