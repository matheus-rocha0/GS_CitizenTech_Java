package br.com.fiap.upaagora.dao;

import br.com.fiap.upaagora.exception.DatabaseException;
import br.com.fiap.upaagora.factory.ConnectionFactory;
import br.com.fiap.upaagora.model.UnidadeUpa;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

@Repository
public class UnidadeUpaDAO {

    private static final String COLUNAS_SELECT =
            "id_unidade, codigo_slug, nome, tipo, endereco, bairro, cidade, latitude, longitude, " +
            "descricao, horario_funcionamento, distancia_km, tempo_deslocamento_min, " +
            "limiar_moderada_min, limiar_cheia_min, status_integracao, data_ultima_atualizacao";

    public UnidadeUpaDAO() {
    }

    public List<UnidadeUpa> listarTodas() {
        String sql = "SELECT " + COLUNAS_SELECT + " FROM T_UPA_UNIDADE ORDER BY id_unidade";
        List<UnidadeUpa> lista = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getInstancia().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(extrairUnidade(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao listar unidades UPA: " + e.getMessage(), e);
        }
    }

    public UnidadeUpa buscarPorId(Long id) {
        String sql = "SELECT " + COLUNAS_SELECT + " FROM T_UPA_UNIDADE WHERE id_unidade = ?";

        try (Connection conn = ConnectionFactory.getInstancia().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extrairUnidade(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao buscar UPA por ID (" + id + "): " + e.getMessage(), e);
        }
    }

    public UnidadeUpa inserir(UnidadeUpa u) {
        String sqlSeq = "SELECT SEQ_UPA_UNIDADE.NEXTVAL FROM DUAL";
        String sqlInsert = "INSERT INTO T_UPA_UNIDADE (" + COLUNAS_SELECT + ") " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getInstancia().getConnection()) {
            Long novoId;
            try (PreparedStatement psSeq = conn.prepareStatement(sqlSeq);
                 ResultSet rsSeq = psSeq.executeQuery()) {
                if (rsSeq.next()) {
                    novoId = rsSeq.getLong(1);
                } else {
                    throw new DatabaseException("Não foi possível gerar novo ID pela sequência SEQ_UPA_UNIDADE.");
                }
            }

            u.setIdUnidade(novoId);

            try (PreparedStatement ps = conn.prepareStatement(sqlInsert)) {
                ps.setLong(1, u.getIdUnidade());
                preencherDadosUnidade(ps, u, 2);
                ps.executeUpdate();
            }

            return u;
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao inserir UPA: " + e.getMessage(), e);
        }
    }

    public UnidadeUpa atualizar(Long id, UnidadeUpa u) {
        String sql = "UPDATE T_UPA_UNIDADE SET codigo_slug = ?, nome = ?, tipo = ?, endereco = ?, " +
                     "bairro = ?, cidade = ?, latitude = ?, longitude = ?, descricao = ?, " +
                     "horario_funcionamento = ?, distancia_km = ?, tempo_deslocamento_min = ?, " +
                     "limiar_moderada_min = ?, limiar_cheia_min = ?, status_integracao = ?, " +
                     "data_ultima_atualizacao = ? WHERE id_unidade = ?";

        try (Connection conn = ConnectionFactory.getInstancia().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int nextIdx = preencherDadosUnidade(ps, u, 1);
            ps.setLong(nextIdx, id);

            ps.executeUpdate();
            u.setIdUnidade(id);
            return u;
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao atualizar UPA ID (" + id + "): " + e.getMessage(), e);
        }
    }

    private int preencherDadosUnidade(PreparedStatement ps, UnidadeUpa u, int idx) throws SQLException {
        ps.setString(idx++, u.getCodigoSlug());
        ps.setString(idx++, u.getNome());
        ps.setString(idx++, u.getTipo() != null ? u.getTipo() : "UPA");
        ps.setString(idx++, u.getEndereco());
        ps.setString(idx++, u.getBairro());
        ps.setString(idx++, u.getCidade() != null ? u.getCidade() : "São Paulo");

        if (u.getLatitude() != null) {
            ps.setDouble(idx++, u.getLatitude());
        } else {
            ps.setNull(idx++, Types.NUMERIC);
        }

        if (u.getLongitude() != null) {
            ps.setDouble(idx++, u.getLongitude());
        } else {
            ps.setNull(idx++, Types.NUMERIC);
        }

        ps.setString(idx++, u.getDescricao());
        ps.setString(idx++, u.getHorarioFuncionamento() != null ? u.getHorarioFuncionamento() : "24 horas");

        if (u.getDistanciaKm() != null) {
            ps.setDouble(idx++, u.getDistanciaKm());
        } else {
            ps.setNull(idx++, Types.NUMERIC);
        }

        if (u.getTempoDeslocamentoMin() != null) {
            ps.setInt(idx++, u.getTempoDeslocamentoMin());
        } else {
            ps.setNull(idx++, Types.NUMERIC);
        }

        ps.setInt(idx++, u.getLimiarModeradaMin() != null ? u.getLimiarModeradaMin() : 15);
        ps.setInt(idx++, u.getLimiarCheiaMin() != null ? u.getLimiarCheiaMin() : 45);
        ps.setString(idx++, u.getStatusIntegracao() != null ? u.getStatusIntegracao() : "ONLINE");
        ps.setString(idx++, u.getDataUltimaAtualizacao());
        return idx;
    }

    public boolean deletar(Long id) {
        String sqlDeleteEventos = "DELETE FROM T_UPA_EVENTO_SENHA WHERE id_unidade = ?";
        String sqlDeleteUnidade = "DELETE FROM T_UPA_UNIDADE WHERE id_unidade = ?";

        try (Connection conn = ConnectionFactory.getInstancia().getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement psEventos = conn.prepareStatement(sqlDeleteEventos)) {
                    psEventos.setLong(1, id);
                    psEventos.executeUpdate();
                }

                int linhasAfetadas;
                try (PreparedStatement psUnidade = conn.prepareStatement(sqlDeleteUnidade)) {
                    psUnidade.setLong(1, id);
                    linhasAfetadas = psUnidade.executeUpdate();
                }

                conn.commit();
                return linhasAfetadas > 0;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao deletar UPA ID (" + id + "): " + e.getMessage(), e);
        }
    }

    private UnidadeUpa extrairUnidade(ResultSet rs) throws SQLException {
        UnidadeUpa u = new UnidadeUpa();
        u.setIdUnidade(rs.getLong("id_unidade"));
        u.setCodigoSlug(rs.getString("codigo_slug"));
        u.setNome(rs.getString("nome"));

        String tipo = rs.getString("tipo");
        u.setTipo(tipo != null ? tipo : "UPA");

        u.setEndereco(rs.getString("endereco"));
        u.setBairro(rs.getString("bairro"));

        String cidade = rs.getString("cidade");
        u.setCidade(cidade != null ? cidade : "São Paulo");

        double lat = rs.getDouble("latitude");
        u.setLatitude(rs.wasNull() ? null : lat);

        double lng = rs.getDouble("longitude");
        u.setLongitude(rs.wasNull() ? null : lng);

        u.setDescricao(rs.getString("descricao"));

        String horario = rs.getString("horario_funcionamento");
        u.setHorarioFuncionamento(horario != null ? horario : "24 horas");

        double dist = rs.getDouble("distancia_km");
        u.setDistanciaKm(rs.wasNull() ? null : dist);

        int tempo = rs.getInt("tempo_deslocamento_min");
        u.setTempoDeslocamentoMin(rs.wasNull() ? null : tempo);

        int mod = rs.getInt("limiar_moderada_min");
        u.setLimiarModeradaMin(rs.wasNull() ? null : mod);

        int cheia = rs.getInt("limiar_cheia_min");
        u.setLimiarCheiaMin(rs.wasNull() ? null : cheia);

        u.setStatusIntegracao(rs.getString("status_integracao"));
        u.setDataUltimaAtualizacao(rs.getString("data_ultima_atualizacao"));
        return u;
    }
}
