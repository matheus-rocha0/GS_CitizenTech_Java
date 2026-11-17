package br.com.fiap.upaagora.model;

public class StatusLotacaoDTO {

    private Long idUnidade;
    private String nomeUnidade;
    private String bairro;
    private Double distanciaKm;
    private Integer tempoDeslocamentoMin;
    private Integer pessoasAguardando;
    private Double vazaoPorMinuto;
    private Integer tempoEsperaEstimadoMin;
    private String faixaEspera;
    private String nivelLotacao;
    private String tendencia;
    private Integer tempoTotalEstimadoMin;
    private String statusIntegracao;
    private String dataUltimaAtualizacao;
    private String avisoEmergencia;

    public StatusLotacaoDTO() {
    }

    public StatusLotacaoDTO(Long idUnidade, String nomeUnidade, String bairro, Double distanciaKm,
                            Integer tempoDeslocamentoMin, Integer pessoasAguardando, Double vazaoPorMinuto,
                            Integer tempoEsperaEstimadoMin, String faixaEspera, String nivelLotacao,
                            String tendencia, Integer tempoTotalEstimadoMin, String statusIntegracao,
                            String dataUltimaAtualizacao, String avisoEmergencia) {
        this.idUnidade = idUnidade;
        this.nomeUnidade = nomeUnidade;
        this.bairro = bairro;
        this.distanciaKm = distanciaKm;
        this.tempoDeslocamentoMin = tempoDeslocamentoMin;
        this.pessoasAguardando = pessoasAguardando;
        this.vazaoPorMinuto = vazaoPorMinuto;
        this.tempoEsperaEstimadoMin = tempoEsperaEstimadoMin;
        this.faixaEspera = faixaEspera;
        this.nivelLotacao = nivelLotacao;
        this.tendencia = tendencia;
        this.tempoTotalEstimadoMin = tempoTotalEstimadoMin;
        this.statusIntegracao = statusIntegracao;
        this.dataUltimaAtualizacao = dataUltimaAtualizacao;
        this.avisoEmergencia = avisoEmergencia;
    }

    public Long getIdUnidade() {
        return idUnidade;
    }

    public void setIdUnidade(Long idUnidade) {
        this.idUnidade = idUnidade;
    }

    public String getNomeUnidade() {
        return nomeUnidade;
    }

    public void setNomeUnidade(String nomeUnidade) {
        this.nomeUnidade = nomeUnidade;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public Double getDistanciaKm() {
        return distanciaKm;
    }

    public void setDistanciaKm(Double distanciaKm) {
        this.distanciaKm = distanciaKm;
    }

    public Integer getTempoDeslocamentoMin() {
        return tempoDeslocamentoMin;
    }

    public void setTempoDeslocamentoMin(Integer tempoDeslocamentoMin) {
        this.tempoDeslocamentoMin = tempoDeslocamentoMin;
    }

    public Integer getPessoasAguardando() {
        return pessoasAguardando;
    }

    public void setPessoasAguardando(Integer pessoasAguardando) {
        this.pessoasAguardando = pessoasAguardando;
    }

    public Double getVazaoPorMinuto() {
        return vazaoPorMinuto;
    }

    public void setVazaoPorMinuto(Double vazaoPorMinuto) {
        this.vazaoPorMinuto = vazaoPorMinuto;
    }

    public Integer getTempoEsperaEstimadoMin() {
        return tempoEsperaEstimadoMin;
    }

    public void setTempoEsperaEstimadoMin(Integer tempoEsperaEstimadoMin) {
        this.tempoEsperaEstimadoMin = tempoEsperaEstimadoMin;
    }

    public String getFaixaEspera() {
        return faixaEspera;
    }

    public void setFaixaEspera(String faixaEspera) {
        this.faixaEspera = faixaEspera;
    }

    public String getNivelLotacao() {
        return nivelLotacao;
    }

    public void setNivelLotacao(String nivelLotacao) {
        this.nivelLotacao = nivelLotacao;
    }

    public String getTendencia() {
        return tendencia;
    }

    public void setTendencia(String tendencia) {
        this.tendencia = tendencia;
    }

    public Integer getTempoTotalEstimadoMin() {
        return tempoTotalEstimadoMin;
    }

    public void setTempoTotalEstimadoMin(Integer tempoTotalEstimadoMin) {
        this.tempoTotalEstimadoMin = tempoTotalEstimadoMin;
    }

    public String getStatusIntegracao() {
        return statusIntegracao;
    }

    public void setStatusIntegracao(String statusIntegracao) {
        this.statusIntegracao = statusIntegracao;
    }

    public String getDataUltimaAtualizacao() {
        return dataUltimaAtualizacao;
    }

    public void setDataUltimaAtualizacao(String dataUltimaAtualizacao) {
        this.dataUltimaAtualizacao = dataUltimaAtualizacao;
    }

    public String getAvisoEmergencia() {
        return avisoEmergencia;
    }

    public void setAvisoEmergencia(String avisoEmergencia) {
        this.avisoEmergencia = avisoEmergencia;
    }

    @Override
    public String toString() {
        return "StatusLotacaoDTO{" +
                "idUnidade=" + idUnidade +
                ", nomeUnidade='" + nomeUnidade + '\'' +
                ", bairro='" + bairro + '\'' +
                ", distanciaKm=" + distanciaKm +
                ", tempoDeslocamentoMin=" + tempoDeslocamentoMin +
                ", pessoasAguardando=" + pessoasAguardando +
                ", vazaoPorMinuto=" + vazaoPorMinuto +
                ", tempoEsperaEstimadoMin=" + tempoEsperaEstimadoMin +
                ", faixaEspera='" + faixaEspera + '\'' +
                ", nivelLotacao='" + nivelLotacao + '\'' +
                ", tendencia='" + tendencia + '\'' +
                ", tempoTotalEstimadoMin=" + tempoTotalEstimadoMin +
                ", statusIntegracao='" + statusIntegracao + '\'' +
                ", dataUltimaAtualizacao='" + dataUltimaAtualizacao + '\'' +
                ", avisoEmergencia='" + avisoEmergencia + '\'' +
                '}';
    }
}
