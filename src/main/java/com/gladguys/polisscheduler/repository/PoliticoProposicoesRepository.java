package com.gladguys.polisscheduler.repository;

import com.gladguys.polisscheduler.model.PoliticoProposicao;
import com.gladguys.polisscheduler.model.PoliticoProposicaoRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PoliticoProposicoesRepository {

    final JdbcTemplate template;

    final String INSERT_QUERY = "insert into politico_proposicao (politico, proposicao, atualizacao) values (?, ?, ?)";
    final String SELECT_QUERY = "select * from politico_proposicao";
    final String UPDATE_ATUALIZACAO = "update politico_proposicao SET atualizacao = ? where politico = ? and proposicao = ?;";

    public PoliticoProposicoesRepository(JdbcTemplate template) {
        this.template = template;
    }

    public int inserirRelacaoPoliticoProposicao(String politicoId, String proposicaoId, String dataAtualizacao) {
        return template.update(INSERT_QUERY, politicoId, proposicaoId, dataAtualizacao);
    }

    public List<PoliticoProposicao> getTodos() {
        return template.query(SELECT_QUERY, new PoliticoProposicaoRowMapper());
    }

    public void updateDataAtualizacao(PoliticoProposicao politicoProposicao, String dataHora) {
        template.update(UPDATE_ATUALIZACAO, dataHora, politicoProposicao.getPolitico(), politicoProposicao.getProposicao());
    }
}