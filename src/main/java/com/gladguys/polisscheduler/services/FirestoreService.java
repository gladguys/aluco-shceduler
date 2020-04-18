package com.gladguys.polisscheduler.services;

import com.gladguys.polisscheduler.model.Partido;
import com.gladguys.polisscheduler.model.Politico;
import com.gladguys.polisscheduler.model.Proposicao;
import com.gladguys.polisscheduler.model.Tramitacao;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class FirestoreService {

	private final Firestore db;

	public FirestoreService(Firestore firestore) {
		this.db = firestore;
	}

	public void addPolitico(Politico politico) {
		db.collection("politicos").document(politico.getId()).set(politico);
	}

	public List<Politico> getPoliticos() throws InterruptedException, ExecutionException {
		List<Politico> politicos = new ArrayList<>();
		ApiFuture<QuerySnapshot> future = db.collection("politicos").get();
		List<QueryDocumentSnapshot> documents = future.get().getDocuments();
		for (DocumentSnapshot document : documents) {
			politicos.add(document.toObject(Politico.class));
		}
		return politicos;
	}

	public void salvarPartidos(List<Partido> partidos) {
		partidos.forEach(p -> {
			db.collection("partidos").document(p.getId()).set(p);
		});
	}

	public void salvarProposicao(Proposicao proposicao) {

		db.collection("atividades").document(proposicao.getIdPoliticoAutor()).collection("atividadesPolitico")
				.document(proposicao.getId()).set(proposicao);
	}

	public void deleteAllProposicoes() {

		try {
			List<String> politicosId = getPoliticos().stream().map(p -> p.getId()).collect(Collectors.toList());
			politicosId.forEach(p -> {

				db.collection("atividades").document(p).collection("atividadesPolitico").listDocuments()
						.forEach(d -> d.delete());
			});
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}

	}

	public void salvarTramitacoesProposicao(List<Tramitacao> tramitacoes, String id) {

		db.collection("tramitacoes").document(id).delete();

		tramitacoes.forEach(t -> db.collection("tramitacoes").document(id).collection("tramitacoesProposicao").add(t));
	}

	public void updateHashCodeSyncPartidos() {
		String hash = LocalDateTime.now().toString();
		db.collection("sync_log").document("PARTIDOSYNC").update("hash", hash);
	}

	public void updateHashCodeSyncPoliticos() {
		String hash = LocalDateTime.now().toString();
		db.collection("sync_log").document("POLITICOSYNC").update("hash", hash);
	}
}
