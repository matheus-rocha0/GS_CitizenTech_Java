package br.com.fiap.upaagora.factory;

import br.com.fiap.upaagora.exception.DatabaseException;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

public class ConnectionFactory {

    private static ConnectionFactory instancia;
    private static volatile boolean bancoInicializado = false;

    private static final String URL_ORACLE = System.getenv("DB_URL") != null
            ? System.getenv("DB_URL")
            : "jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL";

    private static final String USUARIO_ORACLE = System.getenv("DB_USER") != null
            ? System.getenv("DB_USER")
            : "rm561689";

    private static final String SENHA_ORACLE = System.getenv("DB_PASSWORD") != null
            ? System.getenv("DB_PASSWORD")
            : "070706";

    private static final String URL_H2 = "jdbc:h2:./data/upa_agora_db;MODE=Oracle;DEFAULT_NULL_ORDERING=HIGH;AUTO_SERVER=TRUE";
    private static final String USUARIO_H2 = "sa";
    private static final String SENHA_H2 = "";

    private static final String SQL_INSERT_UNIDADE =
            "INSERT INTO T_UPA_UNIDADE (id_unidade, codigo_slug, nome, tipo, endereco, bairro, " +
            "cidade, latitude, longitude, descricao, horario_funcionamento, distancia_km, tempo_deslocamento_min, " +
            "limiar_moderada_min, limiar_cheia_min, status_integracao, data_ultima_atualizacao) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_INSERT_EVENTO =
            "INSERT INTO T_UPA_EVENTO_SENHA (id_evento, id_unidade, etapa, tipo_evento, " +
            "prioridade, classificacao_risco, ref_senha, data_hora_ocorrido) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    private ConnectionFactory() {
    }

    public static synchronized ConnectionFactory getInstancia() {
        if (instancia == null) {
            instancia = new ConnectionFactory();
        }
        return instancia;
    }

    public Connection getConnection() throws SQLException {
        boolean usarOracle = "true".equalsIgnoreCase(System.getenv("DB_USE_ORACLE"));

        if (usarOracle) {
            try {
                Class.forName("oracle.jdbc.OracleDriver");
                return DriverManager.getConnection(URL_ORACLE, USUARIO_ORACLE, SENHA_ORACLE);
            } catch (ClassNotFoundException e) {
                throw new DatabaseException("Driver Oracle JDBC não encontrado no classpath", e);
            }
        } else {
            try {
                File dir = new File("./data");
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                Class.forName("org.h2.Driver");
                Connection conn = DriverManager.getConnection(URL_H2, USUARIO_H2, SENHA_H2);
                if (!bancoInicializado) {
                    inicializarBancoLocal(conn);
                }
                return conn;
            } catch (ClassNotFoundException e) {
                throw new DatabaseException("Driver H2 não encontrado no classpath", e);
            }
        }
    }

    private synchronized void inicializarBancoLocal(Connection conn) {
        if (bancoInicializado) {
            return;
        }

        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE SEQUENCE IF NOT EXISTS SEQ_UPA_UNIDADE START WITH 1 INCREMENT BY 1");
            stmt.execute("CREATE SEQUENCE IF NOT EXISTS SEQ_UPA_EVENTO START WITH 1 INCREMENT BY 1");

            stmt.execute("CREATE TABLE IF NOT EXISTS T_UPA_UNIDADE (" +
                    "id_unidade NUMBER PRIMARY KEY, " +
                    "codigo_slug VARCHAR2(60), " +
                    "nome VARCHAR2(120) NOT NULL, " +
                    "tipo VARCHAR2(20) DEFAULT 'UPA', " +
                    "endereco VARCHAR2(200) NOT NULL, " +
                    "bairro VARCHAR2(80) NOT NULL, " +
                    "cidade VARCHAR2(60) DEFAULT 'São Paulo', " +
                    "latitude NUMBER(10,6), " +
                    "longitude NUMBER(10,6), " +
                    "descricao VARCHAR2(300), " +
                    "horario_funcionamento VARCHAR2(80) DEFAULT '24 horas', " +
                    "distancia_km NUMBER(6,2) NOT NULL, " +
                    "tempo_deslocamento_min NUMBER(5) NOT NULL, " +
                    "limiar_moderada_min NUMBER(5) DEFAULT 15, " +
                    "limiar_cheia_min NUMBER(5) DEFAULT 45, " +
                    "status_integracao VARCHAR2(20) DEFAULT 'ONLINE', " +
                    "data_ultima_atualizacao VARCHAR2(30)" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS T_UPA_EVENTO_SENHA (" +
                    "id_evento NUMBER PRIMARY KEY, " +
                    "id_unidade NUMBER NOT NULL, " +
                    "etapa VARCHAR2(30) NOT NULL, " +
                    "tipo_evento VARCHAR2(30) NOT NULL, " +
                    "prioridade VARCHAR2(20) NOT NULL, " +
                    "classificacao_risco VARCHAR2(20), " +
                    "ref_senha VARCHAR2(64) NOT NULL, " +
                    "data_hora_ocorrido VARCHAR2(30) NOT NULL, " +
                    "CONSTRAINT fk_evento_unidade FOREIGN KEY (id_unidade) REFERENCES T_UPA_UNIDADE(id_unidade)" +
                    ")");

            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM T_UPA_UNIDADE")) {
                if (rs.next() && rs.getInt(1) < 28) {
                    stmt.execute("DELETE FROM T_UPA_EVENTO_SENHA");
                    stmt.execute("DELETE FROM T_UPA_UNIDADE");
                    popularDadosIniciais(conn, stmt);
                }
            }

            bancoInicializado = true;
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao inicializar schema do banco local H2: " + e.getMessage(), e);
        }
    }

    private void popularDadosIniciais(Connection conn, Statement stmt) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SQL_INSERT_UNIDADE)) {
            inserirUnidadeInicial(ps, 1, "upa-vergueiro", "UPA Vergueiro", "UPA",
                    "Rua Vergueiro, 613", "Liberdade", -23.56743, -46.63965,
                    "Unidade de Pronto Atendimento 24 horas (SMS-SP) na região central de São Paulo.",
                    "24 horas", 2.1, 8, "ONLINE", "2026-11-15T21:00:00-03:00");

            inserirUnidadeInicial(ps, 2, "upa-lapa", "UPA Lapa (Prof. João Catarin Mezomo)", "UPA",
                    "Av. Queiroz Filho, 313", "Vila Hamburguesa", -23.53726, -46.72252,
                    "Unidade de Pronto Atendimento 24 horas (SMS-SP) na região da Lapa, Zona Oeste.",
                    "24 horas", 5.4, 15, "ONLINE", "2026-11-15T21:00:00-03:00");

            inserirUnidadeInicial(ps, 3, "upa-carrao", "UPA Carrão (Masataka Ota)", "UPA",
                    "Av. Conselheiro Carrão, 2893", "Carrão", -23.54992, -46.53398,
                    "Unidade de Pronto Atendimento 24 horas (SMS-SP) para urgências e emergências de média complexidade na Zona Leste.",
                    "24 horas", 6.8, 20, "ONLINE", "2026-11-15T21:00:00-03:00");

            inserirUnidadeInicial(ps, 4, "upa-itaquera", "UPA Itaquera (26 de Agosto)", "UPA",
                    "Av. Miguel Ignácio Curi, 44", "Itaquera", -23.54431, -46.46402,
                    "Unidade de Pronto Atendimento 24 horas (SMS-SP) para atendimento de urgências clínicas e pediátricas em Itaquera.",
                    "24 horas", 9.2, 25, "ONLINE", "2026-11-15T21:00:00-03:00");

            inserirUnidadeInicial(ps, 5, "upa-vera-cruz", "UPA Vera Cruz", "UPA",
                    "Av. dos Funcionários Públicos, 379", "Jardim Ângela", -23.73497, -46.7815,
                    "Unidade de Pronto Atendimento 24 horas (SMS-SP) na região do Jardim Ângela e M’Boi Mirim, Zona Sul.",
                    "24 horas", 11.0, 30, "SEM_DADOS", null);

            inserirUnidadeInicial(ps, 6, "upa-pirituba", "UPA Pirituba", "UPA",
                    "Av. Menotti Laudísio, 100", "Pirituba", -23.48453, -46.72752,
                    "Unidade de Pronto Atendimento 24 horas (SMS-SP) junto ao complexo hospitalar de Pirituba.",
                    "24 horas", 7.5, 18, "ONLINE", "2026-11-15T21:00:00-03:00");

            inserirUnidadeInicial(ps, 7, "upa-city-jaragua", "UPA City Jaraguá", "UPA",
                    "Estrada das Taipas, 1650", "Jaraguá", -23.44155, -46.73374,
                    "Unidade de Pronto Atendimento 24 horas (SMS-SP) na região do Jaraguá, Zona Norte.",
                    "24 horas", 10.2, 24, "ONLINE", "2026-11-15T21:00:00-03:00");

            inserirUnidadeInicial(ps, 8, "upa-jacana", "UPA Jaçanã", "UPA",
                    "Rua Ester Elisa, 229", "Jaçanã", -23.45936, -46.57979,
                    "Unidade de Pronto Atendimento 24 horas (SMS-SP) na região do Jaçanã, Zona Norte.",
                    "24 horas", 8.4, 22, "ONLINE", "2026-11-15T21:00:00-03:00");

            inserirUnidadeInicial(ps, 9, "upa-julio-tupy", "UPA Júlio Tupy", "UPA",
                    "Rua Serra da Queimada, 800", "Guaianases", -23.53019, -46.41269,
                    "Unidade de Pronto Atendimento 24 horas (SMS-SP) no extremo leste de São Paulo.",
                    "24 horas", 12.5, 32, "ONLINE", "2026-11-15T21:00:00-03:00");

            inserirUnidadeInicial(ps, 10, "upa-santo-amaro", "UPA Santo Amaro", "UPA",
                    "Rua Carlos Gomes, 661", "Santo Amaro", -23.65378, -46.70136,
                    "Unidade de Pronto Atendimento 24 horas (SMS-SP) na região de Santo Amaro, Zona Sul.",
                    "24 horas", 8.9, 21, "ONLINE", "2026-11-15T21:00:00-03:00");

            inserirUnidadeInicial(ps, 11, "conjunto-hospitalar-mandaqui", "Conjunto Hospitalar do Mandaqui", "Hospital",
                    "Rua Voluntários da Pátria, 4301", "Mandaqui", -23.48498, -46.63144,
                    "Hospital geral e de ensino da rede estadual (SES-SP), referência em urgência, emergência e trauma na Zona Norte.",
                    "Pronto-socorro 24 horas", 8.7, 30, "ONLINE", "2026-11-15T21:00:00-03:00");

            inserirUnidadeInicial(ps, 12, "hospital-das-clinicas", "Hospital das Clínicas da FMUSP", "Hospital",
                    "Av. Dr. Enéas Carvalho de Aguiar, 255", "Cerqueira César", -23.55707, -46.66885,
                    "Complexo hospitalar universitário estadual de alta complexidade; o pronto-socorro atende prioritariamente casos graves e referenciados.",
                    "24 horas (urgência referenciada)", 4.4, 19, "ONLINE", "2026-11-15T21:00:00-03:00");

            inserirUnidadeInicial(ps, 13, "hospital-vila-nova-cachoeirinha", "Hospital Geral de Vila Nova Cachoeirinha", "Hospital",
                    "Av. Deputado Emílio Carlos, 3000", "Vila Nova Cachoeirinha", -23.48135, -46.67179,
                    "Hospital geral estadual (SES-SP) com pronto-socorro 24 horas adulto e infantil na Zona Norte.",
                    "Pronto-socorro 24 horas", 10.4, 34, "ONLINE", "2026-11-15T21:00:00-03:00");

            inserirUnidadeInicial(ps, 14, "hospital-grajau", "Hospital Geral do Grajaú", "Hospital",
                    "Rua Francisco Octávio Pacca, 180", "Grajaú", -23.72903, -46.69085,
                    "Hospital geral estadual (SES-SP) com pronto-socorro 24 horas e maternidade no extremo sul de São Paulo.",
                    "Pronto-socorro 24 horas", 24.8, 70, "ONLINE", "2026-11-15T21:00:00-03:00");

            inserirUnidadeInicial(ps, 15, "hospital-jabaquara", "Hospital Municipal Dr. Arthur Ribeiro de Saboya (Jabaquara)", "Hospital",
                    "Av. Francisco de Paula Quintanilha Ribeiro, 860", "Jabaquara", -23.65334, -46.64568,
                    "Hospital geral municipal (SMS-SP) com pronto-socorro 24 horas na região do Jabaquara, Zona Sul.",
                    "Pronto-socorro 24 horas", 13.8, 43, "ONLINE", "2026-11-15T21:00:00-03:00");

            inserirUnidadeInicial(ps, 16, "hospital-tatuape", "Hospital Municipal Dr. Cármino Caricchio (Tatuapé)", "Hospital",
                    "Av. Celso Garcia, 4815", "Tatuapé", -23.53295, -46.56644,
                    "Hospital geral municipal (SMS-SP) com pronto-socorro 24 horas, referência em trauma, ortopedia e queimados na Zona Leste.",
                    "Pronto-socorro 24 horas", 8.5, 29, "ONLINE", "2026-11-15T21:00:00-03:00");

            inserirUnidadeInicial(ps, 17, "hospital-campo-limpo", "Hospital Municipal Dr. Fernando Mauro Pires da Rocha (Campo Limpo)", "Hospital",
                    "Estrada de Itapecerica da Serra, 1661", "Campo Limpo", -23.6491, -46.749,
                    "Hospital geral municipal (SMS-SP) com pronto-socorro 24 horas, maternidade e atendimento de urgência na Zona Sul.",
                    "Pronto-socorro 24 horas", 19.3, 56, "ONLINE", "2026-11-15T21:00:00-03:00");

            inserirUnidadeInicial(ps, 18, "hospital-mario-degni", "Hospital Municipal Prof. Mário Degni (Jardim Sarah)", "Hospital",
                    "Rua Lucas de Leyde, 257", "Rio Pequeno", -23.57775, -46.76484,
                    "Hospital geral e maternidade municipal (SMS-SP) com pronto-socorro 24 horas na Zona Oeste.",
                    "Pronto-socorro 24 horas", 16.5, 49, "ONLINE", "2026-11-15T21:00:00-03:00");

            inserirUnidadeInicial(ps, 19, "hospital-tide-setubal", "Hospital Municipal Tide Setúbal", "Hospital",
                    "Rua Dr. José Guilherme Eiras, 123", "São Miguel Paulista", -23.49718, -46.44008,
                    "Hospital geral municipal (SMS-SP) com pronto-socorro adulto e infantil 24 horas na Zona Leste.",
                    "Pronto-socorro 24 horas", 24.7, 70, "ONLINE", "2026-11-15T21:00:00-03:00");

            inserirUnidadeInicial(ps, 20, "hospital-sao-paulo", "Hospital São Paulo (UNIFESP)", "Hospital",
                    "Rua Napoleão de Barros, 715", "Vila Clementino", -23.59778, -46.64353,
                    "Hospital universitário com atendimento pelo SUS; a Unidade de Emergência Referenciada prioriza casos graves e encaminhamentos regulados.",
                    "24 horas (urgência referenciada)", 6.4, 24, "STALE", "2026-11-14T16:15:00-03:00");

            inserirUnidadeInicial(ps, 21, "ubs-butanta", "UBS Butantã (Vila Gomes)", "UBS",
                    "Rua Cabral de Menezes, 51", "Vila Gomes", -23.57705, -46.73399,
                    "Unidade Básica de Saúde da rede municipal (SMS-SP) com atenção primária, vacinação e acompanhamento clínico no Butantã.",
                    "Segunda a sexta-feira, 7h às 19h", 12.8, 40, "ONLINE", "2026-11-15T21:00:00-03:00");

            inserirUnidadeInicial(ps, 22, "ubs-casa-verde", "UBS Casa Verde", "UBS",
                    "Rua Vichy, 468", "Casa Verde", -23.50446, -46.65994,
                    "Unidade Básica de Saúde da rede municipal (SMS-SP) na Zona Norte, com consultas agendadas, vacinação e farmácia.",
                    "Segunda a sexta-feira, 7h às 19h", 7.0, 26, "ONLINE", "2026-11-15T21:00:00-03:00");

            inserirUnidadeInicial(ps, 23, "ubs-indianopolis", "UBS Indianópolis (Sigmund Freud)", "UBS",
                    "Av. dos Carinás, 525", "Indianópolis", -23.61406, -46.66526,
                    "Unidade Básica de Saúde da rede municipal (SMS-SP) na região de Moema e Indianópolis.",
                    "Segunda a sexta-feira, 7h às 19h", 9.3, 31, "ONLINE", "2026-11-15T21:00:00-03:00");

            inserirUnidadeInicial(ps, 24, "ubs-jardim-edite", "UBS Jardim Edite (Gerôncio Henrique Neto)", "UBS",
                    "Rua Charles Coulomb, 80", "Cidade Monções", -23.61322, -46.69424,
                    "Unidade Básica de Saúde da rede municipal (SMS-SP) na região do Brooklin e Itaim Bibi.",
                    "Segunda a sexta-feira, 7h às 19h", 11.2, 36, "SEM_DADOS", null);

            inserirUnidadeInicial(ps, 25, "ubs-jardim-sapopemba", "UBS Jardim Sapopemba", "UBS",
                    "Rua João Lopes de Lima, 1151", "Jardim Sapopemba", -23.61147, -46.49332,
                    "Unidade Básica de Saúde da rede municipal (SMS-SP) em complexo que também abriga a AMA Sapopemba 24h.",
                    "Segunda a sexta-feira, 7h às 19h (AMA 24h no complexo)", 19.0, 56, "ONLINE", "2026-11-15T21:00:00-03:00");

            inserirUnidadeInicial(ps, 26, "ubs-mooca", "UBS Mooca I", "UBS",
                    "Rua Taquari, 549", "Mooca", -23.55043, -46.59961,
                    "Unidade Básica de Saúde da rede municipal (SMS-SP) na Mooca, com atenção primária, vacinação e farmácia.",
                    "Segunda a sexta-feira, 7h às 19h", 4.1, 18, "ONLINE", "2026-11-15T21:00:00-03:00");

            inserirUnidadeInicial(ps, 27, "ubs-sao-vicente-paula", "UBS São Vicente de Paula (AMA/UBS Integrada)", "UBS",
                    "Rua Vicente da Costa, 289", "Ipiranga", -23.59009, -46.60929,
                    "Unidade integrada de atenção básica e pronto atendimento ambulatorial (AMA/UBS) da rede municipal no Ipiranga.",
                    "Segunda a sexta-feira, 7h às 19h (AMA até sábado)", 6.0, 23, "ONLINE", "2026-11-15T21:00:00-03:00");

            inserirUnidadeInicial(ps, 28, "ubs-se", "UBS Sé", "UBS",
                    "Rua Frederico Alvarenga, 259", "Sé", -23.55121, -46.62833,
                    "Unidade Básica de Saúde da rede municipal (SMS-SP) na região central, próxima ao Parque Dom Pedro II.",
                    "Segunda a sexta-feira, 7h às 19h", 0.6, 8, "ONLINE", "2026-11-15T21:00:00-03:00");

            inserirUnidadeInicial(ps, 29, "ubs-vila-guilherme", "UBS Vila Guilherme (AMA/UBS Integrada)", "UBS",
                    "Rua João Ventura Batista, 615", "Vila Guilherme", -23.51623, -46.60539,
                    "Unidade Básica de Saúde integrada à AMA da rede municipal (SMS-SP) na Vila Guilherme, Zona Norte.",
                    "Segunda a sábado, 7h às 19h", 5.7, 22, "ONLINE", "2026-11-15T21:00:00-03:00");

            inserirUnidadeInicial(ps, 30, "ubs-vila-prudente", "UBS Vila Prudente", "UBS",
                    "Praça Centenário de Vila Prudente, 108", "Vila Prudente", -23.58213, -46.58283,
                    "Unidade Básica de Saúde da rede municipal (SMS-SP) na Vila Prudente, Zona Leste.",
                    "Segunda a sexta-feira, 7h às 19h", 7.5, 27, "STALE", "2026-11-14T15:40:00-03:00");
        }

        stmt.execute("ALTER SEQUENCE SEQ_UPA_UNIDADE RESTART WITH 31");

        long eventoId = 1;
        try (PreparedStatement psEvento = conn.prepareStatement(SQL_INSERT_EVENTO)) {
            eventoId = inserirEventosIniciais(psEvento, eventoId, 1, "VG", "TRIAGEM", 16, 12);
            eventoId = inserirEventosIniciais(psEvento, eventoId, 2, "LP", "RECEPCAO", 14, 12);
            eventoId = inserirEventosIniciais(psEvento, eventoId, 3, "CR", "CONSULTORIO", 19, 12);
            eventoId = inserirEventosIniciais(psEvento, eventoId, 4, "IT", "RECEPCAO", 24, 12);
            eventoId = inserirEventosIniciais(psEvento, eventoId, 6, "PR", "TRIAGEM", 17, 12);
            eventoId = inserirEventosIniciais(psEvento, eventoId, 7, "CJ", "RECEPCAO", 14, 12);
            eventoId = inserirEventosIniciais(psEvento, eventoId, 8, "JC", "CONSULTORIO", 18, 12);
            eventoId = inserirEventosIniciais(psEvento, eventoId, 9, "JT", "TRIAGEM", 25, 12);
            eventoId = inserirEventosIniciais(psEvento, eventoId, 10, "SA", "RECEPCAO", 22, 12);

            eventoId = inserirEventosIniciais(psEvento, eventoId, 11, "HM", "TRIAGEM", 18, 12);
            eventoId = inserirEventosIniciais(psEvento, eventoId, 12, "HC", "TRIAGEM", 28, 12);
            eventoId = inserirEventosIniciais(psEvento, eventoId, 13, "HV", "RECEPCAO", 15, 12);
            eventoId = inserirEventosIniciais(psEvento, eventoId, 14, "HG", "CONSULTORIO", 23, 12);
            eventoId = inserirEventosIniciais(psEvento, eventoId, 15, "HJ", "TRIAGEM", 18, 12);
            eventoId = inserirEventosIniciais(psEvento, eventoId, 16, "HT", "CONSULTORIO", 26, 12);
            eventoId = inserirEventosIniciais(psEvento, eventoId, 17, "HL", "TRIAGEM", 25, 12);
            eventoId = inserirEventosIniciais(psEvento, eventoId, 18, "HD", "RECEPCAO", 15, 12);
            eventoId = inserirEventosIniciais(psEvento, eventoId, 19, "HS", "CONSULTORIO", 20, 12);

            eventoId = inserirEventosIniciais(psEvento, eventoId, 21, "UB", "RECEPCAO", 14, 12);
            eventoId = inserirEventosIniciais(psEvento, eventoId, 22, "UC", "RECEPCAO", 14, 12);
            eventoId = inserirEventosIniciais(psEvento, eventoId, 23, "UI", "CONSULTORIO", 14, 12);
            eventoId = inserirEventosIniciais(psEvento, eventoId, 25, "US", "TRIAGEM", 17, 12);
            eventoId = inserirEventosIniciais(psEvento, eventoId, 26, "UM", "RECEPCAO", 14, 12);
            eventoId = inserirEventosIniciais(psEvento, eventoId, 27, "UV", "TRIAGEM", 16, 12);
            eventoId = inserirEventosIniciais(psEvento, eventoId, 28, "UE", "RECEPCAO", 16, 12);
            eventoId = inserirEventosIniciais(psEvento, eventoId, 29, "UG", "CONSULTORIO", 14, 12);
        }

        stmt.execute("ALTER SEQUENCE SEQ_UPA_EVENTO RESTART WITH " + eventoId);
    }

    private void inserirUnidadeInicial(PreparedStatement ps, long id, String slug, String nome, String tipo,
                                       String endereco, String bairro, double latitude, double longitude,
                                       String descricao, String horarioFuncionamento, double distanciaKm,
                                       int tempoDeslocamentoMin, String statusIntegracao,
                                       String dataUltimaAtualizacao) throws SQLException {
        ps.setLong(1, id);
        ps.setString(2, slug);
        ps.setString(3, nome);
        ps.setString(4, tipo);
        ps.setString(5, endereco);
        ps.setString(6, bairro);
        ps.setString(7, "São Paulo");
        ps.setDouble(8, latitude);
        ps.setDouble(9, longitude);
        ps.setString(10, descricao);
        ps.setString(11, horarioFuncionamento);
        ps.setDouble(12, distanciaKm);
        ps.setInt(13, tempoDeslocamentoMin);
        ps.setInt(14, 15);
        ps.setInt(15, 45);
        ps.setString(16, statusIntegracao);
        if (dataUltimaAtualizacao != null) {
            ps.setString(17, dataUltimaAtualizacao);
        } else {
            ps.setNull(17, Types.VARCHAR);
        }
        ps.executeUpdate();
    }

    private long inserirEventosIniciais(PreparedStatement ps, long eventoId, long idUnidade, String prefixo,
                                        String etapa, int emitidas, int chamadas) throws SQLException {
        for (int i = 1; i <= emitidas; i++) {
            int minutoTotal = i * 2;
            String dataHora = String.format("2026-11-15T%02d:%02d:00", 20 + (minutoTotal / 60), minutoTotal % 60);
            ps.setLong(1, eventoId++);
            ps.setLong(2, idUnidade);
            ps.setString(3, etapa);
            ps.setString(4, "SENHA_EMITIDA");
            ps.setString(5, i % 3 == 0 ? "PREFERENCIAL" : "GERAL");
            ps.setString(6, i % 4 == 0 ? "AMARELO" : "VERDE");
            ps.setString(7, String.format("HASH-%s-%03d", prefixo, i));
            ps.setString(8, dataHora);
            ps.executeUpdate();
        }
        for (int i = 1; i <= chamadas; i++) {
            int minutoTotal = 20 + i * 3;
            String dataHora = String.format("2026-11-15T%02d:%02d:00", 20 + (minutoTotal / 60), minutoTotal % 60);
            ps.setLong(1, eventoId++);
            ps.setLong(2, idUnidade);
            ps.setString(3, etapa);
            ps.setString(4, "SENHA_CHAMADA");
            ps.setString(5, i % 3 == 0 ? "PREFERENCIAL" : "GERAL");
            ps.setString(6, i % 4 == 0 ? "AMARELO" : "VERDE");
            ps.setString(7, String.format("HASH-%s-%03d", prefixo, i));
            ps.setString(8, dataHora);
            ps.executeUpdate();
        }
        return eventoId;
    }
}
