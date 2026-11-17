package br.com.fiap.upaagora.bo;

import br.com.fiap.upaagora.dao.EventoSenhaDAO;
import br.com.fiap.upaagora.dao.UnidadeUpaDAO;
import br.com.fiap.upaagora.exception.RecursoNaoEncontradoException;
import br.com.fiap.upaagora.exception.RegraNegocioException;
import br.com.fiap.upaagora.model.EventoSenha;
import br.com.fiap.upaagora.model.StatusLotacaoDTO;
import br.com.fiap.upaagora.model.UnidadeUpa;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UnidadeUpaBO {

    private static final ZoneOffset ZONA_BRASILIA = ZoneOffset.of("-03:00");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
    private static final String AVISO_EMERGENCIA_PADRAO = "Emergência? Ligue 192 (SAMU) - Estimativa válida para baixa complexidade (verde/azul)";
    private static final String DESCRICAO_PADRAO = "Unidade de Pronto Atendimento 24 horas (SMS-SP) integrada via API Java Spring Boot.";
    private static final String FONTE_CATALOGO = "API Java Spring Boot / Oracle FIAP";

    private final UnidadeUpaDAO unidadeUpaDAO;
    private final EventoSenhaDAO eventoSenhaDAO;

    public UnidadeUpaBO(UnidadeUpaDAO unidadeUpaDAO, EventoSenhaDAO eventoSenhaDAO) {
        this.unidadeUpaDAO = unidadeUpaDAO;
        this.eventoSenhaDAO = eventoSenhaDAO;
    }

    public List<UnidadeUpa> listarTodas() {
        List<UnidadeUpa> lista = unidadeUpaDAO.listarTodas();
        return lista != null ? lista : new ArrayList<>();
    }

    public UnidadeUpa buscarPorId(Long id) {
        if (id == null) {
            throw new RegraNegocioException("ID da unidade UPA é obrigatório.");
        }
        UnidadeUpa u = unidadeUpaDAO.buscarPorId(id);
        if (u == null) {
            throw new RecursoNaoEncontradoException("Unidade UPA não encontrada com ID: " + id);
        }
        return u;
    }

    public UnidadeUpa criar(UnidadeUpa u) {
        if (u == null) {
            throw new RegraNegocioException("Dados da unidade UPA são obrigatórios.");
        }
        validarCamposObrigatorios(u);
        aplicarValoresPadrao(u);
        return unidadeUpaDAO.inserir(u);
    }

    public UnidadeUpa atualizar(Long id, UnidadeUpa u) {
        if (id == null) {
            throw new RegraNegocioException("ID da unidade UPA é obrigatório para atualização.");
        }
        UnidadeUpa existente = buscarPorId(id);
        if (u == null) {
            throw new RegraNegocioException("Dados da unidade UPA são obrigatórios.");
        }
        preservarCamposOpcionais(u, existente);
        validarCamposObrigatorios(u);
        aplicarValoresPadrao(u);
        if (!"SEM_DADOS".equals(u.getStatusIntegracao()) && !"OFFLINE".equals(u.getStatusIntegracao())) {
            u.setDataUltimaAtualizacao(agoraIsoBrasilia());
        }
        return unidadeUpaDAO.atualizar(id, u);
    }

    public void deletar(Long id) {
        buscarPorId(id);
        unidadeUpaDAO.deletar(id);
    }

    public StatusLotacaoDTO calcularStatusLotacao(Long idUnidade) {
        UnidadeUpa u = buscarPorId(idUnidade);
        return calcularStatusLotacao(u);
    }

    public StatusLotacaoDTO calcularStatusLotacao(UnidadeUpa u) {
        List<EventoSenha> eventos = eventoSenhaDAO.listarPorUnidade(u.getIdUnidade());
        return calcularStatusLotacaoComEventos(u, eventos);
    }

    private StatusLotacaoDTO calcularStatusLotacaoComEventos(UnidadeUpa u, List<EventoSenha> eventos) {
        if (eventos == null) {
            eventos = new ArrayList<>();
        }

        long emitidas = 0;
        long chamadas = 0;
        long canceladas = 0;

        for (EventoSenha ev : eventos) {
            if (ev.getTipoEvento() != null) {
                String tipo = ev.getTipoEvento().trim().toUpperCase();
                switch (tipo) {
                    case "SENHA_EMITIDA":
                        emitidas++;
                        break;
                    case "SENHA_CHAMADA":
                        chamadas++;
                        break;
                    case "SENHA_CANCELADA":
                        canceladas++;
                        break;
                    default:
                        break;
                }
            }
        }

        int pessoasAguardando = (int) Math.max(0, emitidas - chamadas - canceladas);

        double vazaoPorMinuto = chamadas > 0
                ? Math.max(0.15, Math.round((chamadas / 60.0) * 100.0) / 100.0)
                : 0.20;

        int tempoEsperaEstimadoMin = (int) Math.round(pessoasAguardando / vazaoPorMinuto);

        String statusIntegracao = u.getStatusIntegracao() != null
                ? u.getStatusIntegracao().trim().toUpperCase()
                : "ONLINE";

        boolean semDados = "OFFLINE".equals(statusIntegracao)
                || "SEM_DADOS".equals(statusIntegracao)
                || "STALE".equals(statusIntegracao);

        String nivelLotacao;
        String faixaEspera;
        String tendencia;
        int tempoTotalEstimadoMin;

        if (semDados) {
            nivelLotacao = "STALE".equals(statusIntegracao) ? "DESATUALIZADO" : "SEM_DADOS";
            faixaEspera = "STALE".equals(statusIntegracao) ? "Dados desatualizados" : "Sem dados";
            tendencia = "SEM_DADOS";
            tempoTotalEstimadoMin = 9999;
        } else {
            int limiarMod = (u.getLimiarModeradaMin() != null && u.getLimiarModeradaMin() > 0)
                    ? u.getLimiarModeradaMin() : 15;
            int limiarCheia = (u.getLimiarCheiaMin() != null && u.getLimiarCheiaMin() > 0)
                    ? u.getLimiarCheiaMin() : 45;

            if (tempoEsperaEstimadoMin <= limiarMod) {
                nivelLotacao = "TRANQUILA";
            } else if (tempoEsperaEstimadoMin <= limiarCheia) {
                nivelLotacao = "MODERADA";
            } else {
                nivelLotacao = "CHEIA";
            }

            if (tempoEsperaEstimadoMin <= 10) {
                faixaEspera = "0-10 min";
            } else if (tempoEsperaEstimadoMin > 240) {
                faixaEspera = "mais de 4 h";
            } else if (tempoEsperaEstimadoMin >= 120) {
                int horas = tempoEsperaEstimadoMin / 60;
                int minutosRestantes = tempoEsperaEstimadoMin % 60;
                if (minutosRestantes < 30) {
                    faixaEspera = horas + "h-" + horas + "h30";
                } else {
                    faixaEspera = horas + "h30-" + (horas + 1) + "h";
                }
            } else {
                int base = (tempoEsperaEstimadoMin / 10) * 10;
                faixaEspera = base + "-" + (base + 15) + " min";
            }

            if (pessoasAguardando >= 15 || emitidas > (chamadas + 5)) {
                tendencia = "AUMENTANDO";
            } else if (pessoasAguardando <= 5 || chamadas >= emitidas) {
                tendencia = "DIMINUINDO";
            } else {
                tendencia = "ESTAVEL";
            }

            int desloc = u.getTempoDeslocamentoMin() != null ? u.getTempoDeslocamentoMin() : 0;
            tempoTotalEstimadoMin = desloc + tempoEsperaEstimadoMin;
        }

        StatusLotacaoDTO dto = new StatusLotacaoDTO();
        dto.setIdUnidade(u.getIdUnidade());
        dto.setNomeUnidade(u.getNome());
        dto.setBairro(u.getBairro());
        dto.setDistanciaKm(u.getDistanciaKm());
        dto.setTempoDeslocamentoMin(u.getTempoDeslocamentoMin());
        dto.setPessoasAguardando(pessoasAguardando);
        dto.setVazaoPorMinuto(vazaoPorMinuto);
        dto.setTempoEsperaEstimadoMin(tempoEsperaEstimadoMin);
        dto.setFaixaEspera(faixaEspera);
        dto.setNivelLotacao(nivelLotacao);
        dto.setTendencia(tendencia);
        dto.setTempoTotalEstimadoMin(tempoTotalEstimadoMin);
        dto.setStatusIntegracao(u.getStatusIntegracao());
        dto.setDataUltimaAtualizacao(u.getDataUltimaAtualizacao());
        dto.setAvisoEmergencia(AVISO_EMERGENCIA_PADRAO);

        return dto;
    }

    private Map<Long, List<EventoSenha>> agruparEventosPorUnidade() {
        List<EventoSenha> todosEventos = eventoSenhaDAO.listarTodos();
        if (todosEventos == null) {
            return Collections.emptyMap();
        }
        return todosEventos.stream()
                .filter(e -> e.getIdUnidade() != null)
                .collect(Collectors.groupingBy(EventoSenha::getIdUnidade));
    }

    public List<StatusLotacaoDTO> listarComparativoUnidades() {
        List<UnidadeUpa> todas = unidadeUpaDAO.listarTodas();
        if (todas == null) {
            return new ArrayList<>();
        }
        Map<Long, List<EventoSenha>> porUnidade = agruparEventosPorUnidade();
        return todas.stream()
                .map(u -> calcularStatusLotacaoComEventos(u, porUnidade.get(u.getIdUnidade())))
                .sorted(Comparator.comparing(StatusLotacaoDTO::getTempoTotalEstimadoMin))
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> listarCatalogoFilaZero() {
        List<UnidadeUpa> todas = listarTodas();
        Map<Long, List<EventoSenha>> porUnidade = agruparEventosPorUnidade();
        List<Map<String, Object>> catalogo = new ArrayList<>();

        for (UnidadeUpa u : todas) {
            String statusIntegracao = u.getStatusIntegracao() != null
                    ? u.getStatusIntegracao().trim().toUpperCase(Locale.ROOT)
                    : "SEM_DADOS";

            String status;
            switch (statusIntegracao) {
                case "ONLINE":
                    status = "available";
                    break;
                case "STALE":
                    status = "stale";
                    break;
                default:
                    status = "unavailable";
                    break;
            }

            String queueLevel;
            Map<String, Integer> estimatedWait;

            if (!"available".equals(status)) {
                queueLevel = "unknown";
                estimatedWait = null;
            } else {
                StatusLotacaoDTO lotacao = calcularStatusLotacaoComEventos(u, porUnidade.get(u.getIdUnidade()));
                int tempoEsperaEstimadoMin = lotacao.getTempoEsperaEstimadoMin() != null
                        ? lotacao.getTempoEsperaEstimadoMin() : 0;
                int limiarMod = (u.getLimiarModeradaMin() != null && u.getLimiarModeradaMin() > 0)
                        ? u.getLimiarModeradaMin() : 15;
                int limiarCheia = (u.getLimiarCheiaMin() != null && u.getLimiarCheiaMin() > 0)
                        ? u.getLimiarCheiaMin() : 45;

                if (tempoEsperaEstimadoMin <= limiarMod) {
                    queueLevel = "low";
                } else if (tempoEsperaEstimadoMin <= limiarCheia) {
                    queueLevel = "moderate";
                } else {
                    queueLevel = "high";
                }

                estimatedWait = Map.of(
                        "min", Math.max(5, tempoEsperaEstimadoMin - 5),
                        "max", tempoEsperaEstimadoMin + 5
                );
            }

            Map<String, Object> location = new LinkedHashMap<>();
            location.put("lat", u.getLatitude() != null ? u.getLatitude() : -23.55052);
            location.put("lng", u.getLongitude() != null ? u.getLongitude() : -46.633308);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("status", status);
            data.put("queueLevel", queueLevel);
            data.put("estimatedWait", estimatedWait);
            data.put("updatedAt", formatarDataIso(u.getDataUltimaAtualizacao()));
            data.put("source", FONTE_CATALOGO);
            data.put("simulated", false);

            String slug = (u.getCodigoSlug() != null && !u.getCodigoSlug().trim().isEmpty())
                    ? u.getCodigoSlug().trim()
                    : gerarSlug(u.getNome());

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", slug);
            item.put("idUnidade", u.getIdUnidade());
            item.put("name", u.getNome());
            item.put("type", (u.getTipo() != null && !u.getTipo().trim().isEmpty()) ? u.getTipo() : "UPA");
            item.put("address", u.getEndereco());
            item.put("neighborhood", u.getBairro());
            item.put("city", (u.getCidade() != null && !u.getCidade().trim().isEmpty()) ? u.getCidade() : "São Paulo");
            item.put("location", location);
            item.put("description", (u.getDescricao() != null && !u.getDescricao().trim().isEmpty())
                    ? u.getDescricao() : DESCRICAO_PADRAO);
            item.put("openingHours", (u.getHorarioFuncionamento() != null && !u.getHorarioFuncionamento().trim().isEmpty())
                    ? u.getHorarioFuncionamento() : "24 horas");
            item.put("data", data);

            catalogo.add(item);
        }

        return catalogo;
    }

    private void preservarCamposOpcionais(UnidadeUpa destino, UnidadeUpa existente) {
        if (isVazio(destino.getCodigoSlug())) {
            destino.setCodigoSlug(existente.getCodigoSlug());
        }
        if (isVazio(destino.getTipo())) {
            destino.setTipo(existente.getTipo());
        }
        if (isVazio(destino.getEndereco())) {
            destino.setEndereco(existente.getEndereco());
        }
        if (isVazio(destino.getBairro())) {
            destino.setBairro(existente.getBairro());
        }
        if (isVazio(destino.getCidade())) {
            destino.setCidade(existente.getCidade());
        }
        if (destino.getLatitude() == null) {
            destino.setLatitude(existente.getLatitude());
        }
        if (destino.getLongitude() == null) {
            destino.setLongitude(existente.getLongitude());
        }
        if (isVazio(destino.getDescricao())) {
            destino.setDescricao(existente.getDescricao());
        }
        if (isVazio(destino.getHorarioFuncionamento())) {
            destino.setHorarioFuncionamento(existente.getHorarioFuncionamento());
        }
        if (destino.getLimiarModeradaMin() == null || destino.getLimiarModeradaMin() <= 0) {
            destino.setLimiarModeradaMin(existente.getLimiarModeradaMin());
        }
        if (destino.getLimiarCheiaMin() == null || destino.getLimiarCheiaMin() <= 0) {
            destino.setLimiarCheiaMin(existente.getLimiarCheiaMin());
        }
        if (isVazio(destino.getStatusIntegracao())) {
            destino.setStatusIntegracao(existente.getStatusIntegracao());
        }
    }

    private void validarCamposObrigatorios(UnidadeUpa u) {
        if (isVazio(u.getNome())) {
            throw new RegraNegocioException("Nome da unidade UPA é obrigatório.");
        }
        if (u.getDistanciaKm() == null || u.getDistanciaKm() < 0) {
            throw new RegraNegocioException("Distância em km é obrigatória e deve ser maior ou igual a zero.");
        }
        if (u.getTempoDeslocamentoMin() == null || u.getTempoDeslocamentoMin() < 0) {
            throw new RegraNegocioException("Tempo de deslocamento em minutos é obrigatório e deve ser maior ou igual a zero.");
        }
    }

    private void aplicarValoresPadrao(UnidadeUpa u) {
        if (isVazio(u.getCodigoSlug())) {
            u.setCodigoSlug(gerarSlug(u.getNome()));
        } else {
            u.setCodigoSlug(gerarSlug(u.getCodigoSlug()));
        }

        if (isVazio(u.getTipo())) {
            u.setTipo("UPA");
        } else {
            String tipoLimpo = u.getTipo().trim();
            if ("UPA".equalsIgnoreCase(tipoLimpo)) {
                u.setTipo("UPA");
            } else if ("UBS".equalsIgnoreCase(tipoLimpo)) {
                u.setTipo("UBS");
            } else if ("HOSPITAL".equalsIgnoreCase(tipoLimpo)) {
                u.setTipo("Hospital");
            } else {
                throw new RegraNegocioException("Tipo de unidade inválido: " + u.getTipo() + ". Permitidos: UPA, UBS, Hospital.");
            }
        }

        if (isVazio(u.getEndereco())) {
            u.setEndereco("Não informado");
        }
        if (isVazio(u.getBairro())) {
            u.setBairro("Não informado");
        }
        if (isVazio(u.getCidade())) {
            u.setCidade("São Paulo");
        }
        if (isVazio(u.getDescricao())) {
            u.setDescricao(DESCRICAO_PADRAO);
        }
        if (isVazio(u.getHorarioFuncionamento())) {
            u.setHorarioFuncionamento("24 horas");
        }
        if (u.getLimiarModeradaMin() == null || u.getLimiarModeradaMin() <= 0) {
            u.setLimiarModeradaMin(15);
        }
        if (u.getLimiarCheiaMin() == null || u.getLimiarCheiaMin() <= 0) {
            u.setLimiarCheiaMin(45);
        }

        if (isVazio(u.getStatusIntegracao())) {
            u.setStatusIntegracao("ONLINE");
        } else {
            String statusLimpo = u.getStatusIntegracao().trim().toUpperCase(Locale.ROOT);
            if (!"ONLINE".equals(statusLimpo) && !"STALE".equals(statusLimpo)
                    && !"OFFLINE".equals(statusLimpo) && !"SEM_DADOS".equals(statusLimpo)) {
                throw new RegraNegocioException("Status de integração inválido: " + u.getStatusIntegracao()
                        + ". Permitidos: ONLINE, STALE, OFFLINE, SEM_DADOS.");
            }
            u.setStatusIntegracao(statusLimpo);
        }

        if (isVazio(u.getDataUltimaAtualizacao())
                && !"SEM_DADOS".equals(u.getStatusIntegracao())
                && !"OFFLINE".equals(u.getStatusIntegracao())) {
            u.setDataUltimaAtualizacao(agoraIsoBrasilia());
        }
    }

    private boolean isVazio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }

    private String agoraIsoBrasilia() {
        return OffsetDateTime.now(ZONA_BRASILIA).format(FORMATTER);
    }

    private String formatarDataIso(String raw) {
        if (isVazio(raw)) {
            return null;
        }
        String limpa = raw.trim().replace(' ', 'T');
        if (limpa.endsWith("Z")) {
            return limpa;
        }
        if (limpa.length() > 19 && (limpa.charAt(19) == '+' || limpa.charAt(19) == '-')) {
            return limpa;
        }
        if (limpa.length() == 19) {
            return limpa + "-03:00";
        }
        return limpa;
    }

    private String gerarSlug(String texto) {
        if (isVazio(texto)) {
            return "unidade-saude";
        }
        String semAcento = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        String slug = semAcento.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
        return slug.isEmpty() ? "unidade-saude" : slug;
    }
}
