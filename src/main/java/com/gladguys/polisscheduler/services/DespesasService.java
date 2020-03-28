package com.gladguys.polisscheduler.services;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import com.gladguys.polisscheduler.model.Despesa;
import com.gladguys.polisscheduler.model.Politico;
import com.gladguys.polisscheduler.model.RetornoDespesas;
import com.gladguys.polisscheduler.utils.DataUtil;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class DespesasService {

    private static final String URI_POLITICOS = "https://dadosabertos.camara.leg.br/api/v2/deputados/";

    private final RestTemplate restTemplate;
    private final FirestoreService firestoreService;

    public DespesasService(RestTemplateBuilder restTemplateBuilder, FirestoreService firestoreService) {
        this.restTemplate = restTemplateBuilder.build();
        this.firestoreService = firestoreService;
    }

    //@Scheduled(cron = "0 48 05 * * ?")
    public void salvarDespesasDoDia() throws InterruptedException, ExecutionException {
        List<Politico> politicos = firestoreService.getPoliticosIds();
        politicos.forEach(p -> {
            int numeroMes = DataUtil.getNumeroMes();
            String urlParaDespesasPolitico = URI_POLITICOS + p.getId() + "/despesas?ano=2020&mes=" + numeroMes
                    + "&ordem=ASC&ordenarPor=ano";

            List<Despesa> despesasDeHoje = this.restTemplate
                    .getForObject(urlParaDespesasPolitico, RetornoDespesas.class).getDados().stream()
                    .filter(despesa -> DataUtil.ehHoje(despesa.getDataDocumento())).collect(Collectors.toList());

            despesasDeHoje.forEach(d -> {
                d.setIdPolitico(p.getId());
                d.setNomePolitico(p.getNomeEleitoral());
                d.setSiglaPartido(p.getSiglaPartido());
            });

            firestoreService.salvarDespesas(despesasDeHoje, p.getId());
        });
    }

}