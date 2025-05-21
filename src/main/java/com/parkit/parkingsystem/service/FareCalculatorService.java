package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket) {
    	calculateFare(ticket, false);
    }
    
    public void calculateFare(Ticket ticket, boolean discount) {
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        long inTimeMs = ticket.getInTime().getTime();
        long outTimeMs = ticket.getOutTime().getTime();
        long durationMs = outTimeMs - inTimeMs;
        
        double durationHours = durationMs / (1000.0 * 60 * 60);
        
        if (durationHours < 0.5) {
        	ticket.setPrice(0.0);
        	return;
        }
        
        double price;

        switch (ticket.getParkingSpot().getParkingType()){
            case CAR: {
                price = durationHours * Fare.CAR_RATE_PER_HOUR;
                break;
            }
            case BIKE: {
                price = durationHours * Fare.BIKE_RATE_PER_HOUR;
                break;
            }
            default: throw new IllegalArgumentException("Unkown Parking Type");
        }
        
        if (discount) {
        	price = price * 0.95;
        	ticket.setDiscount(true);
        }
        
        ticket.setPrice(price);
    }
}