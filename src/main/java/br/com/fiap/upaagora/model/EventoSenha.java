package br.com.fiap.upaagora.model;

public class EventoSenha {

    private Long idEvento;
    private Long idUnidade;
    private String etapa;
    private String tipoEvento;
    private String prioridade;
    private String classificacaoRisco;
    private String refSenha;
    private String dataHoraOcorrido;

    public EventoSenha() {
    }

    public EventoSenha(Long idEvento, Long idUnidade, String etapa, String tipoEvento,
                       String prioridade, String classificacaoRisco, String refSenha,
                       String dataHoraOcorrido) {
        this.idEvento = idEvento;
        this.idUnidade = idUnidade;
        this.etapa = etapa;
        this.tipoEvento = tipoEvento;
        this.prioridade = prioridade;
        this.classificacaoRisco = classificacaoRisco;
        this.refSenha = refSenha;
        this.dataHoraOcorrido = dataHoraOcorrido;
    }

    public Long getIdEvento() {
        return idEvento;
    }

    public void setIdEvento(Long idEvento) {
        this.idEvento = idEvento;
    }

    public Long getIdUnidade() {
        return idUnidade;
    }

    public void setIdUnidade(Long idUnidade) {
        this.idUnidade = idUnidade;
    }

    public String getEtapa() {
        return etapa;
    }

    public void setEtapa(String etapa) {
        this.etapa = etapa;
    }

    public String getTipoEvento() {
        return tipoEvento;
    }

    public void setTipoEvento(String tipoEvento) {
        this.tipoEvento = tipoEvento;
    }

    public String getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(String prioridade) {
        this.prioridade = prioridade;
    }

    public String getClassificacaoRisco() {
        return classificacaoRisco;
    }

    public void setClassificacaoRisco(String classificacaoRisco) {
        this.classificacaoRisco = classificacaoRisco;
    }

    public String getRefSenha() {
        return refSenha;
    }

    public void setRefSenha(String refSenha) {
        this.refSenha = refSenha;
    }

    public String getDataHoraOcorrido() {
        return dataHoraOcorrido;
    }

    public void setDataHoraOcorrido(String dataHoraOcorrido) {
        this.dataHoraOcorrido = dataHoraOcorrido;
    }

    @Override
    public String toString() {
        return "EventoSenha{" +
                "idEvento=" + idEvento +
                ", idUnidade=" + idUnidade +
                ", etapa='" + etapa + '\'' +
                ", tipoEvento='" + tipoEvento + '\'' +
                ", prioridade='" + prioridade + '\'' +
                ", classificacaoRisco='" + classificacaoRisco + '\'' +
                ", refSenha='" + refSenha + '\'' +
                ", dataHoraOcorrido='" + dataHoraOcorrido + '\'' +
                '}';
    }
}
