package org.tommap.springai.service;

import org.tommap.springai.model.entity.HelpdeskTicket;
import org.tommap.springai.model.request.TicketRequest;

import java.util.List;

public interface IHelpdeskService {
    HelpdeskTicket createTicket(TicketRequest request, String username);
    List<HelpdeskTicket> getByUsername(String username);
}
