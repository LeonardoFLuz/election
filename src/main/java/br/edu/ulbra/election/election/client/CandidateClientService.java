package br.edu.ulbra.election.election.client;

import java.util.List;

import br.edu.ulbra.election.election.output.v1.CandidateOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Service
public class CandidateClientService {
	private final CandidateClient candidateClient;

    @Autowired
    public CandidateClientService(CandidateClient candidateClient) {
        this.candidateClient = candidateClient;
    }

    public List<CandidateOutput> getByElectionId(Long electionId) {
        return this.candidateClient.getByElectionId(electionId);
    }
    
    public CandidateOutput getFirstByNumberElectionAndElectionId(Long numberElection, Long electionId) {
        return this.candidateClient.getFirstByNumberElectionAndElectionId(numberElection, electionId);
    }

    public CandidateOutput getById(Long id){
		return this.candidateClient.getById(id);
	}
    
    @FeignClient(value="candidate-service", url="${url.candidate-service}")
    private interface CandidateClient {

        @GetMapping("/v1/candidate/{numberElection}/{electionId}")
        CandidateOutput getFirstByNumberElectionAndElectionId(@PathVariable(name = "numberElection") Long numberElection, @PathVariable(name = "electionId") Long electionId);
    
        @GetMapping("/v1/candidate/election/{electionId}")
        List<CandidateOutput> getByElectionId(@PathVariable(name = "electionId") Long electionId);

        @GetMapping("/v1/candidate/{candidateId}")
		CandidateOutput getById(@PathVariable(name = "candidateId") Long candidateId);
    }
}
