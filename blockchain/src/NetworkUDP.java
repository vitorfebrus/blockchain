import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

// classe responsavel por fornecer comunicacao pela rede
public class NetworkUDP implements Constants {

	private DatagramSocket socketUDP;

	private ByteArrayOutputStream btOutput;
	private ByteArrayInputStream btInput;

	private ObjectOutputStream objOutput;
	private ObjectInputStream objInput;

	private Receiver input;

	// cria um relacionamento bidirecional entre o no e a network
	private Node no;

	public NetworkUDP() {
		input = new Receiver(no);
		input.start();
	}

	public NetworkUDP(Node no) {
		this.no = no;
		input = new Receiver(no);
		input.start();
	}

	// envia um objeto para um determinado ip
	public void send(Object obj, String ip) {
		try {

			// inicializa o socketUDP
			socketUDP = new DatagramSocket();

			// serializando o objeto
			btOutput = new ByteArrayOutputStream();
			objOutput = new ObjectOutputStream(btOutput);
			objOutput.writeObject(obj);

			byte[] buffer = btOutput.toByteArray();

			// cria o pacote que sera enviado
			DatagramPacket pacote = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(ip), PORT);

			// envia o pacote
			socketUDP.send(pacote);

			// fecha as streams
			btOutput.close();
			objOutput.close();
			socketUDP.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// envia um objeto para um determinado ip
	public void send(Object obj, InetAddress ip) {
		try {

			// inicializa o socketUDP
			socketUDP = new DatagramSocket();

			// serializando o objeto
			btOutput = new ByteArrayOutputStream();
			objOutput = new ObjectOutputStream(btOutput);
			objOutput.writeObject(obj);

			byte[] buffer = btOutput.toByteArray();

			// cria o pacote que sera enviado
			DatagramPacket pacote = new DatagramPacket(buffer, buffer.length, ip, PORT);

			// envia o pacote
			socketUDP.send(pacote);

			// fecha as streams
			btOutput.close();
			objOutput.close();
			socketUDP.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void broadcast(Object obj) {
		try {

			// inicializa o socketUDP
			socketUDP = new DatagramSocket();

			// serializando o objeto
			btOutput = new ByteArrayOutputStream();
			objOutput = new ObjectOutputStream(btOutput);
			objOutput.writeObject(obj);

			byte[] buffer = btOutput.toByteArray();

			// cria o pacote que sera enviado
			DatagramPacket pacote = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(BROADCAST_ADDR), PORT);

			// envia o pacote
			socketUDP.send(pacote);

			// fecha as streams
			btOutput.close();
			objOutput.close();
			socketUDP.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public Node getNo() {
		return no;
	}

	public void setNo(Node no) {
		this.no = no;
	}

	public Receiver getInput() {
		return input;
	}

	public void setInput(Receiver input) {
		this.input = input;
	}

}

// thread resposavel por receber as conexoes a passar para outra classe
// trata-las
class ReceiverUDP extends Thread implements Constants {

	// socket servidor
	private ServerSocket servidor;

	private DatagramSocket receptor;

	private Node no;

	public ReceiverUDP() {

	}

	public ReceiverUDP(Node no) {
		this.no = no;
	}

	// recebe objetos da rede
	@Override
	public void run() {

		try {

			// inicializa o servidor
			receptor = new DatagramSocket(PORT);

			// o socket servidor recebe conexoes infinitamente
			while (true) {

				byte[] buffer = new byte[TAM_MAX_PACOTE];
				DatagramPacket pacoteRecebido = new DatagramPacket(buffer, buffer.length);
				receptor.receive(pacoteRecebido);
				new ConectionManagerUDP(buffer, no).start(); // passa a conexao para a classe responsavel tratar
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

// thread que trata as conexoes recebidas pelo socket servidor
class ConectionManagerUDP extends Thread {

	private ByteArrayInputStream btInput;

	private ObjectInputStream objInput;

	private Node no;

	private byte[] buffer;

	public ConectionManagerUDP(byte[] buffer) {
		this.buffer = buffer;
	}

	public ConectionManagerUDP(byte[] buffer, Node no) {
		this.buffer = buffer;
		this.no = no;
	}

	@Override
	public void run() {
		try {

			// deserializando o objeto
			btInput = new ByteArrayInputStream(buffer);
			objInput = new ObjectInputStream(btInput);

			Object objetoRecebio = objInput.readObject();

			tratarApdu(objetoRecebio);

			objInput.close();
			btInput.close();

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