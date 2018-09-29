import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.codec.digest.DigestUtils;

// classe responsavel por minerar novos blocos
public class Mineradora implements Constants {

	private Block bloco;

	private AtomicBoolean minerar;

	public Mineradora() {
		this.bloco = new Block();
		minerar = new AtomicBoolean(true);
	}

	// minera um bloco recebido como parametro
	public Block minerarBloco(Block bloco) {

		String inicioHash;
		String blockHash;

		do {

			bloco.setNonce(bloco.getNonce() + 1);

			blockHash = DigestUtils.sha256Hex(bloco.toString());
			//System.out.println(bloco.getNonce() + " " + blockHash);
			inicioHash = blockHash.substring(0, DIFICULDADE);

		} while (!inicioHash.equals(condicao()) && minerar.get());

		// caso outro no ja tenha minarado o bloco
		if (!minerar.get()) {
			return null;

		} else { // caso este no tenha minerado o bloco
			return bloco;
		}
	}

	public Block getBloco() {
		return bloco;
	}

	public void setBloco(Block bloco) {
		this.bloco = bloco;
	}

	public AtomicBoolean getMinerar() {
		return minerar;
	}

	public String condicao() {
		String condicao = "";

		for (int i = 0; i < DIFICULDADE; i++) {
			condicao += "0";
		}

		return condicao;
	}

}
