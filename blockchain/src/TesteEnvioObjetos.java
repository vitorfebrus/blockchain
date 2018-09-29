import java.net.InetAddress;
import java.util.Scanner;

//testa se o envio de objeto serializados pela rede esta funcionando corretamente
public class TesteEnvioObjetos {

	public static void main(String[] args) {

		try {
			
			// cria uma instancia da rede para poder ter comunicacao
			NetworkTCP rede = new NetworkTCP();
			rede.getInput().start(); // inicia a espera por conexoes

			System.out.println("Digite a mensagem: ");
			Scanner leitor = new Scanner(System.in);

			// criando e enviando transacoes e blocos enquando o usuario digita novas coisas
			while (true) {
				String mensagem = leitor.nextLine();
				System.out.println("Enviando nova Transacao...");
				// cria uma transacao com o texto digitado pela usuario
				Transacao t = new Transacao(mensagem);
				// envia a transacao para o ip recebido como parametro
				rede.send(t, args[0]);

				// cria um bloco com a transacao criada anteriormente e envia para o mesmo ip
				System.out.println("Enviando novo bloco com a transacao anterior...");
				Block b = new Block();
				b.getTransacoes().add(t);
				rede.send(b, args[0]);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
