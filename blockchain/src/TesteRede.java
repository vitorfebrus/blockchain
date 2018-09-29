import java.net.InetAddress;
import java.util.Scanner;

// testa o envio simples de mensagens pela rede
public class TesteRede {

	public static void main(String[] args) {
		
		try {
			
			// cria uma instancia da classe Network que fornece as funcionalidade de comunicacao
			NetworkTCP rede = new NetworkTCP();
			rede.getInput().start();
			
			System.out.println("Digite a mensagem: ");
			Scanner leitor = new Scanner(System.in);
			
			// recebe uma mensagem digitada pelo usuario e envia para o endereco
			// recebido como parametro, faz isso infinitamente
			while (true) {
				String mensagem = leitor.nextLine();
				rede.send(mensagem, args[0]);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		
		}
		
	}
	
}
