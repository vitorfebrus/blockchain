import java.io.Serializable;

// representa a transacao (carga util) que vai dentro de cada bloco
public class Transacao implements Serializable {

	private static final long serialVersionUID = -793162072288896910L;

	public Transacao(String value) {
		this.value = value;
	}

	private String value;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "Transacao [value=" + value + "]";
	}

}
