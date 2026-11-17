package br.com.fiap.upaagora.model;

public class UnidadeUpa {

    private Long idUnidade;
    private String codigoSlug;
    private String nome;
    private String tipo;
    private String endereco;
    private String bairro;
    private String cidade;
    private Double latitude;
    private Double longitude;
    private String descricao;
    private String horarioFuncionamento;
    private Double distanciaKm;
    private Integer tempoDeslocamentoMin;
    private Integer limiarModeradaMin;
    private Integer limiarCheiaMin;
    private String statusIntegracao;
    private String dataUltimaAtualizacao;

    public UnidadeUpa() {
    }

    public UnidadeUpa(Long idUnidade, String nome, String endereco, String bairro, Double distanciaKm,
                      Integer tempoDeslocamentoMin, Integer limiarModeradaMin, Integer limiarCheiaMin,
                      String statusIntegracao, String dataUltimaAtualizacao) {
        this.idUnidade = idUnidade;
        this.nome = nome;
        this.endereco = endereco;
        this.bairro = bairro;
        this.distanciaKm = distanciaKm;
        this.tempoDeslocamentoMin = tempoDeslocamentoMin;
        this.limiarModeradaMin = limiarModeradaMin != null ? limiarModeradaMin : 15;
        this.limiarCheiaMin = limiarCheiaMin != null ? limiarCheiaMin : 45;
        this.statusIntegracao = statusIntegracao;
        this.dataUltimaAtualizacao = dataUltimaAtualizacao;
    }

    public UnidadeUpa(Long idUnidade, String codigoSlug, String nome, String tipo, String endereco, String bairro,
                      String cidade, Double latitude, Double longitude, String descricao, String horarioFuncionamento,
                      Double distanciaKm, Integer tempoDeslocamentoMin, Integer limiarModeradaMin,
                      Integer limiarCheiaMin, String statusIntegracao, String dataUltimaAtualizacao) {
        this.idUnidade = idUnidade;
        this.codigoSlug = codigoSlug;
        this.nome = nome;
        this.tipo = tipo != null ? tipo : "UPA";
        this.endereco = endereco;
        this.bairro = bairro;
        this.cidade = cidade != null ? cidade : "São Paulo";
        this.latitude = latitude;
        this.longitude = longitude;
        this.descricao = descricao;
        this.horarioFuncionamento = horarioFuncionamento != null ? horarioFuncionamento : "24 horas";
        this.distanciaKm = distanciaKm;
        this.tempoDeslocamentoMin = tempoDeslocamentoMin;
        this.limiarModeradaMin = limiarModeradaMin != null ? limiarModeradaMin : 15;
        this.limiarCheiaMin = limiarCheiaMin != null ? limiarCheiaMin : 45;
        this.statusIntegracao = statusIntegracao;
        this.dataUltimaAtualizacao = dataUltimaAtualizacao;
    }

    public Long getIdUnidade() {
        return idUnidade;
    }

    public void setIdUnidade(Long idUnidade) {
        this.idUnidade = idUnidade;
    }

    public String getCodigoSlug() {
        return codigoSlug;
    }

    public void setCodigoSlug(String codigoSlug) {
        this.codigoSlug = codigoSlug;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getHorarioFuncionamento() {
        return horarioFuncionamento;
    }

    public void setHorarioFuncionamento(String horarioFuncionamento) {
        this.horarioFuncionamento = horarioFuncionamento;
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

    public Integer getLimiarModeradaMin() {
        return limiarModeradaMin;
    }

    public void setLimiarModeradaMin(Integer limiarModeradaMin) {
        this.limiarModeradaMin = limiarModeradaMin;
    }

    public Integer getLimiarCheiaMin() {
        return limiarCheiaMin;
    }

    public void setLimiarCheiaMin(Integer limiarCheiaMin) {
        this.limiarCheiaMin = limiarCheiaMin;
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

    @Override
    public String toString() {
        return "UnidadeUpa{" +
                "idUnidade=" + idUnidade +
                ", codigoSlug='" + codigoSlug + '\'' +
                ", nome='" + nome + '\'' +
                ", tipo='" + tipo + '\'' +
                ", endereco='" + endereco + '\'' +
                ", bairro='" + bairro + '\'' +
                ", cidade='" + cidade + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", descricao='" + descricao + '\'' +
                ", horarioFuncionamento='" + horarioFuncionamento + '\'' +
                ", distanciaKm=" + distanciaKm +
                ", tempoDeslocamentoMin=" + tempoDeslocamentoMin +
                ", limiarModeradaMin=" + limiarModeradaMin +
                ", limiarCheiaMin=" + limiarCheiaMin +
                ", statusIntegracao='" + statusIntegracao + '\'' +
                ", dataUltimaAtualizacao='" + dataUltimaAtualizacao + '\'' +
                '}';
    }
}
