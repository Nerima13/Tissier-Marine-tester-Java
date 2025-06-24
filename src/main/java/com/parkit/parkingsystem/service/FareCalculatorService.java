package com.parkit.parkingsystem.service;

import java.util.Date;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {
    private TicketDAO ticketDAO;

    public FareCalculatorService(TicketDAO ticketDAO) {
        this.ticketDAO = ticketDAO;
    }

    public void calculateFare(Ticket ticket) {
        if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
            throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString()
                    + " In time : " + ticket.getInTime().toString());
        }

        Date inDate = ticket.getInTime();
        Date outDate = ticket.getOutTime();

        long inTime = inDate.getTime();
        long outTime = outDate.getTime();

        float duration = outTime - inTime; // Duration in millisecond
        float durationInHour = duration / 1000 / 3600; // Conversion in hour

        if (durationInHour <= 0.5) {
        	ticket.setPrice(0);
        }

        else {
            double discount = 1;
            if (ticketDAO.hasVisited(ticket.getVehicleRegNumber())) {
                discount = 0.95;
            }
            switch (ticket.getParkingSpot().getParkingType()) {
                case CAR: {
                    ticket.setPrice(durationInHour * Fare.CAR_RATE_PER_HOUR * discount);
                    break;
                }

                case BIKE: {
                    ticket.setPrice(durationInHour * Fare.BIKE_RATE_PER_HOUR * discount);
                    break;
                }

                default:
                    throw new IllegalArgumentException("Unknown Parking Type");
            }

        }
        ticketDAO.updateTicket(ticket);
    }
}