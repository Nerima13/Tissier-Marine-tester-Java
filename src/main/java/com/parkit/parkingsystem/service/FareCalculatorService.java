package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {
	
	private final TicketDAO dao;

    public FareCalculatorService(TicketDAO ticketDAO) {
        this.dao = ticketDAO;
    }
    
    public void calculateFare(Ticket ticket) {
        if (ticket == null || ticket.getInTime() == null || ticket.getOutTime() == null) {
            throw new IllegalArgumentException("Ticket and in/out times must not be null");
        }

        if (ticket.getOutTime().before(ticket.getInTime())) {
            throw new IllegalArgumentException("Out time provided is before in time: " + ticket.getOutTime());
        }

        long durationMs = ticket.getOutTime().getTime() - ticket.getInTime().getTime();
        double durationHours = durationMs / (1000.0 * 60 * 60);
        
        if (durationHours <= 0.5) {
            ticket.setPrice(0.0);
            return;
        }
        
        double ratePerHour;
        switch (ticket.getParkingSpot().getParkingType()) {
            case CAR:
                ratePerHour = Fare.CAR_RATE_PER_HOUR;
                break;
            case BIKE:
                ratePerHour = Fare.BIKE_RATE_PER_HOUR;
                break;
            default:
                throw new IllegalArgumentException("Unknown Parking Type: " + ticket.getParkingSpot().getParkingType());
        }
        
        double price = 0;
        boolean discount = ticket.isRecurringUser();
        if (discount) {
            price = durationHours * ratePerHour * 0.95;
        } else {
            price = durationHours * ratePerHour;
        }
        ticket.setPrice(price);
    }
}