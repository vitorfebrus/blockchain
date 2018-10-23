import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

// classe responsavel por fornecer comunicacao pela rede
public class NetworkUDP implements Constants {

	private DatagramSocket socketUDP;

	private ByteArrayOutputStream btOutput;
	private ObjectOutputStream objOutput;

	private ReceiverUDP input;

	// cria um relacionamento bidirecional entre o no e a network
	private Node no;

	public NetworkUDP() {
		input = new ReceiverUDP(no);
		input.start();
	}

	public NetworkUDP(Node no) {
		this.no = no;
		input = new ReceiverUDP(no);
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
			DatagramPacket pacote = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(BROADCAST_ADDR),
					PORT);

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

}

// thread resposavel por receber as conexoes a passar para outra classe
// trata-las
class ReceiverUDP extends Thread implements Constants {

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

			no.tratarApdu(objetoRecebio);

			objInput.close();
			btInput.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}