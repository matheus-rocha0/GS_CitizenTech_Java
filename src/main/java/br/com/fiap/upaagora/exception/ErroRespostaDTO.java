package br.com.fiap.upaagora.exception;

public class ErroRespostaDTO {

    private int status;
    private String erro;
    private String mensagem;
    private String timestamp;

    public ErroRespostaDTO() {
    }

    public ErroRespostaDTO(int status, String erro, String mensagem, String timestamp) {
        this.status = status;
        this.erro = erro;
        this.mensagem = mensagem;
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getErro() {
        return erro;
    }

    public void setErro(String erro) {
        this.erro = erro;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "ErroRespostaDTO{" +
                "status=" + status +
                ", erro='" + erro + '\'' +
                ", mensagem='" + mensagem + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
