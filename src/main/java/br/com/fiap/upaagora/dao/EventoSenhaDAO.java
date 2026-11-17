package br.com.fiap.upaagora.dao;

import br.com.fiap.upaagora.exception.DatabaseException;
import br.com.fiap.upaagora.factory.ConnectionFactory;
import br.com.fiap.upaagora.model.EventoSenha;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Repository
public class EventoSenhaDAO {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public EventoSenhaDAO() {
    }

    public List<EventoSenha> listarTodos() {
        String sql = "SELECT id_evento, id_unidade, etapa, tipo_evento, prioridade, " +
                     "classificacao_risco, ref_senha, data_hora_ocorrido " +
                     "FROM T_UPA_EVENTO_SENHA ORDER BY id_evento";
        List<EventoSenha> lista = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getInstancia().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(extrairEvento(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao listar eventos de senha: " + e.getMessage(), e);
        }
    }

    public List<EventoSenha> listarPorUnidade(Long idUnidade) {
        String sql = "SELECT id_evento, id_unidade, etapa, tipo_evento, prioridade, " +
                     "classificacao_risco, ref_senha, data_hora_ocorrido " +
                     "FROM T_UPA_EVENTO_SENHA WHERE id_unidade = ? ORDER BY id_evento";
        List<EventoSenha> lista = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getInstancia().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, idUnidade);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(extrairEvento(rs));
                }
            }
            return lista;
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao listar eventos da unidade (" + idUnidade + "): " + e.getMessage(), e);
        }
    }

    public EventoSenha buscarPorId(Long id) {
        String sql = "SELECT id_evento, id_unidade, etapa, tipo_evento, prioridade, " +
                     "classificacao_risco, ref_senha, data_hora_ocorrido " +
                     "FROM T_UPA_EVENTO_SENHA WHERE id_evento = ?";

        try (Connection conn = ConnectionFactory.getInstancia().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extrairEvento(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao buscar evento por ID (" + id + "): " + e.getMessage(), e);
        }
    }

    public EventoSenha inserir(EventoSenha e) {
        String sqlSeq = "SELECT SEQ_UPA_EVENTO.NEXTVAL FROM DUAL";
        String sqlInsert = "INSERT INTO T_UPA_EVENTO_SENHA (id_evento, id_unidade, etapa, tipo_evento, " +
                           "prioridade, classificacao_risco, ref_senha, data_hora_ocorrido) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlUpdateUnidade = "UPDATE T_UPA_UNIDADE SET data_ultima_atualizacao = ? WHERE id_unidade = ?";

        try (Connection conn = ConnectionFactory.getInstancia().getConnection()) {
            conn.setAutoCommit(false);
            try {
                Long novoId;
                try (PreparedStatement psSeq = conn.prepareStatement(sqlSeq);
                     ResultSet rsSeq = psSeq.executeQuery()) {
                    if (rsSeq.next()) {
                        novoId = rsSeq.getLong(1);
                    } else {
                        throw new DatabaseException("Não foi possível gerar novo ID pela sequência SEQ_UPA_EVENTO.");
                    }
                }

                e.setIdEvento(novoId);

                if (e.getDataHoraOcorrido() == null || e.getDataHoraOcorrido().isBlank()) {
                    e.setDataHoraOcorrido(LocalDateTime.now().format(FORMATTER));
                }

                try (PreparedStatement ps = conn.prepareStatement(sqlInsert)) {
                    ps.setLong(1, e.getIdEvento());
                    ps.setLong(2, e.getIdUnidade());
                    ps.setString(3, e.getEtapa());
                    ps.setString(4, e.getTipoEvento());
                    ps.setString(5, e.getPrioridade());

                    if (e.getClassificacaoRisco() != null) {
                        ps.setString(6, e.getClassificacaoRisco());
                    } else {
                        ps.setNull(6, Types.VARCHAR);
                    }

                    ps.setString(7, e.getRefSenha());
                    ps.setString(8, e.getDataHoraOcorrido());

                    ps.executeUpdate();
                }

                try (PreparedStatement psUp = conn.prepareStatement(sqlUpdateUnidade)) {
                    psUp.setString(1, e.getDataHoraOcorrido());
                    psUp.setLong(2, e.getIdUnidade());
                    psUp.executeUpdate();
                }

                conn.commit();
                return e;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Erro ao inserir evento de senha: " + ex.getMessage(), ex);
        }
    }

    public EventoSenha atualizar(Long id, EventoSenha e) {
        String sql = "UPDATE T_UPA_EVENTO_SENHA SET id_unidade = ?, etapa = ?, tipo_evento = ?, " +
                     "prioridade = ?, classificacao_risco = ?, ref_senha = ?, data_hora_ocorrido = ? " +
                     "WHERE id_evento = ?";

        try (Connection conn = ConnectionFactory.getInstancia().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, e.getIdUnidade());
            ps.setString(2, e.getEtapa());
            ps.setString(3, e.getTipoEvento());
            ps.setString(4, e.getPrioridade());

            if (e.getClassificacaoRisco() != null) {
                ps.setString(5, e.getClassificacaoRisco());
            } else {
                ps.setNull(5, Types.VARCHAR);
            }

            ps.setString(6, e.getRefSenha());
            ps.setString(7, e.getDataHoraOcorrido());
            ps.setLong(8, id);

            ps.executeUpdate();
            e.setIdEvento(id);
            return e;
        } catch (SQLException ex) {
            throw new DatabaseException("Erro ao atualizar evento de senha ID (" + id + "): " + ex.getMessage(), ex);
        }
    }

    public boolean deletar(Long id) {
        String sql = "DELETE FROM T_UPA_EVENTO_SENHA WHERE id_evento = ?";

        try (Connection conn = ConnectionFactory.getInstancia().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            int linhasAfetadas = ps.executeUpdate();
            return linhasAfetadas > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao deletar evento de senha ID (" + id + "): " + e.getMessage(), e);
        }
    }

    private EventoSenha extrairEvento(ResultSet rs) throws SQLException {
        EventoSenha e = new EventoSenha();
        e.setIdEvento(rs.getLong("id_evento"));
        e.setIdUnidade(rs.getLong("id_unidade"));
        e.setEtapa(rs.getString("etapa"));
        e.setTipoEvento(rs.getString("tipo_evento"));
        e.setPrioridade(rs.getString("prioridade"));
        e.setClassificacaoRisco(rs.getString("classificacao_risco"));
        e.setRefSenha(rs.getString("ref_senha"));
        e.setDataHoraOcorrido(rs.getString("data_hora_ocorrido"));
        return e;
    }
}
