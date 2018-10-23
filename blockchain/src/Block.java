import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;

// representa um bloco da blockchain
public class Block implements Serializable {

	private static final long serialVersionUID = -2992618043871207344L;

	private int id; // identificador de ordem dos blocos
	
	private List<Transacao> transacoes;

	private String hashBlockAnterior;

	private int nonce;

	private long timeStamp;

	private Block blocoAnterior; // referencia ao bloco anterior, nao entra no calculo do hash

	public Block() {
		transacoes = new ArrayList<Transacao>();
		hashBlockAnterior = "";
		nonce = 0;
		timeStamp = System.currentTimeMillis();
		id = 0;
	}

	// calcula e retorna o hash deste bloco
	public String hash() {
		return DigestUtils.sha256Hex(toString());
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public List<Transacao> getTransacoes() {
		return transacoes;
	}

	public void setTransacoes(List<Transacao> transacoes) {
		this.transacoes = transacoes;
	}

	public String getHashBlockAnterior() {
		return hashBlockAnterior;
	}

	public void setHashBlockAnterior(String hashBlockAnterior) {
		this.hashBlockAnterior = hashBlockAnterior;
	}

	public int getNonce() {
		return nonce;
	}

	public void setNonce(int nonce) {
		this.nonce = nonce;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}

	public Block getBlocoAnterior() {
		return blocoAnterior;
	}

	public void setBlocoAnterior(Block blocoAnterior) {
		this.blocoAnterior = blocoAnterior;
	}

	@Override
	public String toString() {
		return "Block [id=" + id + ", transacoes=" + transacoes + ", hashBlockAnterior=" + hashBlockAnterior
				+ ", nonce=" + nonce + "]";
	}

	
}