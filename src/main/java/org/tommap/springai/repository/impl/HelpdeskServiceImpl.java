package org.tommap.springai.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.tommap.springai.model.entity.HelpdeskTicket;
import org.tommap.springai.model.request.TicketRequest;
import org.tommap.springai.repository.HelpdeskTicketRepository;
import org.tommap.springai.service.IHelpdeskService;

import java.time.LocalDateTime;
import java.util.List;

import static org.tommap.springai.model.entity.TicketStatus.OPEN;

@Service
@RequiredArgsConstructor
public class HelpdeskServiceImpl implements IHelpdeskService {
    private final HelpdeskTicketRepository helpdeskTicketRepository;

    @Override
    public HelpdeskTicket createTicket(TicketRequest request, String username) {
        var ticket = HelpdeskTicket.builder() //transient state
                .issue(request.issue())
                .username(username)
                .status(OPEN)
                .createdAt(LocalDateTime.now())
                .eta(LocalDateTime.now().plusDays(7))
                .build();

        return helpdeskTicketRepository.save(ticket); //move to persistent context -> manged by entity manager -> after transaction ends the EM will be closed -> no longer manged by EM -> entity becomes detached
    }

    @Override
    public List<HelpdeskTicket> getByUsername(String username) {
        return helpdeskTicketRepository.findByUsername(username);
    }
}
