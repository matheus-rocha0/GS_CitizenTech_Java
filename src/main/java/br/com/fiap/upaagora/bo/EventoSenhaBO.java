package br.com.fiap.upaagora.bo;

import br.com.fiap.upaagora.dao.EventoSenhaDAO;
import br.com.fiap.upaagora.dao.UnidadeUpaDAO;
import br.com.fiap.upaagora.exception.RecursoNaoEncontradoException;
import br.com.fiap.upaagora.exception.RegraNegocioException;
import br.com.fiap.upaagora.model.EventoSenha;
import br.com.fiap.upaagora.model.UnidadeUpa;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class EventoSenhaBO {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final Pattern CPF_FORMATADO_PATTERN = Pattern.compile(".*\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}.*");
    private static final Pattern CPF_DIGITOS_PATTERN = Pattern.compile(".*\\d{11}.*");

    private final EventoSenhaDAO eventoSenhaDAO;
    private final UnidadeUpaDAO unidadeUpaDAO;

    public EventoSenhaBO(EventoSenhaDAO eventoSenhaDAO, UnidadeUpaDAO unidadeUpaDAO) {
        this.eventoSenhaDAO = eventoSenhaDAO;
        this.unidadeUpaDAO = unidadeUpaDAO;
    }

    public List<EventoSenha> listarTodos() {
        List<EventoSenha> lista = eventoSenhaDAO.listarTodos();
        return lista != null ? lista : new ArrayList<>();
    }

    public List<EventoSenha> listarPorUnidade(Long idUnidade) {
        if (idUnidade == null) {
            throw new RegraNegocioException("ID da unidade UPA é obrigatório.");
        }
        List<EventoSenha> lista = eventoSenhaDAO.listarPorUnidade(idUnidade);
        return lista != null ? lista : new ArrayList<>();
    }

    public EventoSenha buscarPorId(Long id) {
        if (id == null) {
            throw new RegraNegocioException("ID do evento é obrigatório.");
        }
        EventoSenha ev = eventoSenhaDAO.buscarPorId(id);
        if (ev == null) {
            throw new RecursoNaoEncontradoException("Evento de senha não encontrado com ID: " + id);
        }
        return ev;
    }

    public EventoSenha registrarEvento(EventoSenha e) {
        if (e == null) {
            throw new RegraNegocioException("Dados do evento de senha são obrigatórios.");
        }
        validarEvento(e);
        aplicarValoresPadrao(e);
        return eventoSenhaDAO.inserir(e);
    }

    public EventoSenha atualizarEvento(Long id, EventoSenha e) {
        if (id == null) {
            throw new RegraNegocioException("ID do evento é obrigatório para atualização.");
        }
        buscarPorId(id);
        if (e == null) {
            throw new RegraNegocioException("Dados do evento de senha são obrigatórios.");
        }
        validarEvento(e);
        aplicarValoresPadrao(e);
        return eventoSenhaDAO.atualizar(id, e);
    }

    public void deletarEvento(Long id) {
        buscarPorId(id);
        eventoSenhaDAO.deletar(id);
    }

    private void validarEvento(EventoSenha e) {
        if (e.getIdUnidade() == null) {
            throw new RegraNegocioException("ID da unidade UPA associada é obrigatório.");
        }

        UnidadeUpa upa = unidadeUpaDAO.buscarPorId(e.getIdUnidade());
        if (upa == null) {
            throw new RecursoNaoEncontradoException("Unidade UPA associada não encontrada com ID: " + e.getIdUnidade());
        }

        if (e.getTipoEvento() == null || e.getTipoEvento().trim().isEmpty()) {
            throw new RegraNegocioException("Tipo de evento é obrigatório.");
        }
        String tipo = e.getTipoEvento().trim().toUpperCase();
        if (!"SENHA_EMITIDA".equals(tipo) && !"SENHA_CHAMADA".equals(tipo) && !"SENHA_CANCELADA".equals(tipo)) {
            throw new RegraNegocioException("Tipo de evento inválido: " + e.getTipoEvento()
                    + ". Permitidos: SENHA_EMITIDA, SENHA_CHAMADA, SENHA_CANCELADA.");
        }
        e.setTipoEvento(tipo);

        if (e.getRefSenha() == null || e.getRefSenha().trim().isEmpty()) {
            throw new RegraNegocioException("Referência de senha (refSenha) é obrigatória.");
        }
        String ref = e.getRefSenha().trim();
        if (CPF_FORMATADO_PATTERN.matcher(ref).matches() || CPF_DIGITOS_PATTERN.matcher(ref).matches()) {
            throw new RegraNegocioException("Violação LGPD: Nenhum dado pessoal (CPF) pode trafegar no evento. Utilize uma refSenha anônima.");
        }
    }

    private void aplicarValoresPadrao(EventoSenha e) {
        if (e.getEtapa() == null || e.getEtapa().trim().isEmpty()) {
            e.setEtapa("RECEPCAO");
        } else {
            e.setEtapa(e.getEtapa().trim().toUpperCase());
        }

        if (e.getPrioridade() == null || e.getPrioridade().trim().isEmpty()) {
            e.setPrioridade("NORMAL");
        } else {
            e.setPrioridade(e.getPrioridade().trim().toUpperCase());
        }

        if (e.getClassificacaoRisco() == null || e.getClassificacaoRisco().trim().isEmpty()) {
            e.setClassificacaoRisco("VERDE");
        } else {
            e.setClassificacaoRisco(e.getClassificacaoRisco().trim().toUpperCase());
        }

        if (e.getDataHoraOcorrido() == null || e.getDataHoraOcorrido().trim().isEmpty()) {
            e.setDataHoraOcorrido(LocalDateTime.now().format(FORMATTER));
        }
    }
}
