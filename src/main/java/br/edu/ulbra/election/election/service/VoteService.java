package br.edu.ulbra.election.election.service;

import br.edu.ulbra.election.election.client.ElectionClientService;
import br.edu.ulbra.election.election.client.VoterClientService;
import br.edu.ulbra.election.election.exception.GenericOutputException;
import br.edu.ulbra.election.election.input.v1.VoteInput;
import br.edu.ulbra.election.election.model.Election;
import br.edu.ulbra.election.election.model.Vote;
import br.edu.ulbra.election.election.output.v1.ElectionOutput;
import br.edu.ulbra.election.election.output.v1.GenericOutput;
import br.edu.ulbra.election.election.output.v1.VoteOutput;
import br.edu.ulbra.election.election.output.v1.VoterOutput;
import br.edu.ulbra.election.election.repository.ElectionRepository;
import br.edu.ulbra.election.election.repository.VoteRepository;
import feign.FeignException;
import org.apache.commons.lang.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VoteService {

	private final VoteRepository voteRepository;
	private final ElectionRepository electionRepository;
	private final ElectionClientService electionClientService;
	private final VoterClientService voterClientService;
	private final Vote vote;
	

	private final ModelMapper modelMapper;

	private static final String MESSAGE_INVALID_ID = "Invalid id";
	private static final String MESSAGE_INVALID_ELECTION_ID = "Invalid Election Id";
	private static final String MESSAGE_CANDIDATE_NOT_FOUND = "Candidate not found";

	@Autowired
	public VoteService(VoteRepository voteRepository, ElectionRepository electionRepository, ModelMapper modelMapper, ElectionClientService electionClientService, VoterClientService voterClientService,Vote vote){
		this.voteRepository = voteRepository;
		this.electionRepository = electionRepository;
		this.modelMapper = modelMapper;
		this.electionClientService = electionClientService;
		this.voterClientService = voterClientService;
		this.vote = vote;
	}

	public GenericOutput electionVote(VoteInput voteInput){

		Election election = validateInput(voteInput.getElectionId(), voteInput);
		Vote vote = new Vote();
		vote.setElection(election);
		vote.setVoterId(voteInput.getVoterId());

		if (voteInput.getCandidateNumber() == null){
			vote.setBlankVote(true);
		} else {
			vote.setBlankVote(false);
		}

		// TODO: Validate null candidate
		vote.setNullVote(false);

		voteRepository.save(vote);

		return new GenericOutput("OK");
	}

	public GenericOutput multiple(List<VoteInput> voteInputList){
		for (VoteInput voteInput : voteInputList){
			this.electionVote(voteInput);
		}
		return new GenericOutput("OK");
	}

	public Election validateInput(Long electionId, VoteInput voteInput){
		Election election = electionRepository.findById(electionId).orElse(null);
		if (election == null){
			throw new GenericOutputException("Invalid Election");
		}
		if (voteInput.getVoterId() == null){
			throw new GenericOutputException("Invalid Voter");
		}
		try {
        	voterClientService.getById(voteInput.getVoterId());
        } catch (FeignException e){
            if (e.status() == 500) {
                throw new GenericOutputException("Invalid Voter");
            }
        }
		if (voteInput.getElectionId() == null){
			throw new GenericOutputException("Invalid Election");
		}
		
		try {
			electionClientService.getById(voteInput.getElectionId());
		} catch (FeignException e){
			if (e.status() == 500) {
				throw new GenericOutputException("Invalid Election");
			}
		}
		// TODO: Validate voter

		return election;
	}
	 public VoteOutput toCVoteOutput(Vote vote){
	        VoteOutput voteOutput = modelMapper.map(vote, VoteOutput.class);
	        ElectionOutput electionOutput = electionClientService.getById(voteOutput.getElectionId());
	        voteOutput.setElectionOutput(electionOutput);
	        
	        VoterOutput voterOutput = voterClientService.getById(vote.getVoterId());
	        voteOutput.setVoterOutput(voterOutput);
	        return voteOutput;
	    }
	
}
