package br.edu.ulbra.election.election.service;

import br.edu.ulbra.election.election.client.CandidateClientService;
import br.edu.ulbra.election.election.exception.GenericOutputException;
import br.edu.ulbra.election.election.input.v1.VoteInput;
import br.edu.ulbra.election.election.model.Election;
import br.edu.ulbra.election.election.model.Vote;
import br.edu.ulbra.election.election.output.v1.CandidateOutput;
import br.edu.ulbra.election.election.output.v1.GenericOutput;
import br.edu.ulbra.election.election.output.v1.VoteOutput;
import br.edu.ulbra.election.election.repository.ElectionRepository;
import br.edu.ulbra.election.election.repository.VoteRepository;
import feign.FeignException;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.List;

@Service
public class VoteService {

    private final VoteRepository voteRepository;
    private final ElectionRepository electionRepository;
    private final CandidateClientService candidateClientService;
    private final ModelMapper modelMapper;
    
    @Autowired
    public VoteService(VoteRepository voteRepository, ElectionRepository electionRepository, CandidateClientService candidateClientService, ModelMapper modelMapper){
        this.voteRepository = voteRepository;
        this.electionRepository = electionRepository;
        this.candidateClientService = candidateClientService;
        this.modelMapper = modelMapper;
    }

    public List<VoteOutput> getByVoterId(Long voterId) {
    	Type voteOutputListType = new TypeToken<List<VoteOutput>>(){}.getType();
        return modelMapper.map(voteRepository.findByVoterId(voterId), voteOutputListType);
    }
    
    public List<VoteOutput> getByElectionId(Long electionId) {
    	Type voteOutputListType = new TypeToken<List<VoteOutput>>(){}.getType();
        return modelMapper.map(voteRepository.findByElectionId(electionId), voteOutputListType);
    }
    
    public GenericOutput electionVote(VoteInput voteInput){

        Election election = validateInput(voteInput.getElectionId(), voteInput);
        
        Vote voted = voteRepository.findFirstByVoterIdAndElectionId(voteInput.getVoterId(), voteInput.getElectionId());
        if (voted != null) {
        	throw new GenericOutputException("Voter already voted in this election");
        }
        
        Vote vote = new Vote();
        vote.setElection(election);
        vote.setVoterId(voteInput.getVoterId());

        if (voteInput.getCandidateNumber() == null){
            vote.setBlankVote(true);
            vote.setNullVote(false);
        } else {
            vote.setBlankVote(false);
            
            try {
            	CandidateOutput candidateOutput = candidateClientService.getFirstByNumberElectionAndElectionId(voteInput.getCandidateNumber(), voteInput.getElectionId());
            	
            	if (candidateOutput == null) {
            		vote.setNullVote(true);
            	} else {
            		vote.setCandidateId(voteInput.getCandidateNumber());
            		vote.setNullVote(false);
            	}
            	
            } catch (FeignException e){
                if (e.status() == 500) {
                    throw new GenericOutputException("Invalid Candidate");
                }
            }
        }

        voteRepository.save(vote);

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
