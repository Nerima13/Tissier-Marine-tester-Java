package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;

public class FareCalculatorServiceTest {

    private static FareCalculatorService fareCalculatorService;
    
    @Mock
    private static TicketDAO ticketDAO;
    private Ticket ticket;
    
    private static ParkingSpot parkingSpotCarNotAvailable;
    private static ParkingSpot parkingSpotBikeNotAvailable;
    private static ParkingSpot parkingSpotEmptyNotAvailable;

    @BeforeAll
    private static void setUp() {
    	ticketDAO = mock(TicketDAO.class);
        fareCalculatorService = new FareCalculatorService(ticketDAO);
        parkingSpotCarNotAvailable = new ParkingSpot(1, ParkingType.CAR, false);
        parkingSpotBikeNotAvailable = new ParkingSpot(1, ParkingType.BIKE, false);
        parkingSpotEmptyNotAvailable = new ParkingSpot(1, null, false);
        
    }

    @BeforeEach
    private void setUpPerTest() {
        ticket = new Ticket();
    }

    @Test
    public void calculateFareCar(){
    	// GIVEN
    	Date inTime = new Date(System.currentTimeMillis() - (60 * 60 * 1000)); // 1 hour parking time ago
        Date outTime = new Date();

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpotCarNotAvailable);

        when(ticketDAO.getTicket(ticket.getVehicleRegNumber())).thenReturn(null);

        // WHEN
        fareCalculatorService.calculateFare(ticket);

        // THEN
        assertEquals(Fare.CAR_RATE_PER_HOUR, ticket.getPrice());
    }

    @Test
    void calculateFareBike() {
    	// GIVEN
        Date inTime = new Date(System.currentTimeMillis() - 60 * 60 * 1000); // 1 hour parking time ago
        Date outTime = new Date();
        
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpotBikeNotAvailable);

        when(ticketDAO.getTicket(ticket.getVehicleRegNumber())).thenReturn(null);

        // WHEN
        fareCalculatorService.calculateFare(ticket);

        // THEN
        assertEquals(Fare.BIKE_RATE_PER_HOUR, ticket.getPrice());
    }

    @Test
    void calculateFareUnkownType() {
        // GIVEN
        Date inTime = new Date(System.currentTimeMillis() - 60 * 60 * 1000); // 1 hour parking time ago
        Date outTime = new Date();
        
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpotEmptyNotAvailable);

        // WHEN + THEN
        assertThrows(NullPointerException.class, () -> fareCalculatorService.calculateFare(ticket));
    }

    @Test
    void calculateFareCarWithFutureInTime() {
        // GIVEN
        Date inTime = new Date(System.currentTimeMillis() + 60 * 60 * 1000); // 1 hour parking time later
        Date outTime = new Date();
        
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpotCarNotAvailable);

        // WHEN + THEN
        assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket));
    }
    
    @Test
    void calculateFareBikeWithFutureInTime() {
        // GIVEN
        Date inTime = new Date(System.currentTimeMillis() + 60 * 60 * 1000); // 1 hour parking time later
        Date outTime = new Date();
        
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpotBikeNotAvailable);

        // WHEN + THEN
        assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket));
    }

    @Test
    void calculateFareCarWithLessThanOneHourParkingTime() {
        // GIVEN
        Date inTime = new Date(System.currentTimeMillis() - 45 * 60 * 1000); // 45 minutes parking time ago
        Date outTime = new Date();
        
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpotCarNotAvailable);

        when(ticketDAO.getTicket(ticket.getVehicleRegNumber())).thenReturn(null);

        // WHEN
        fareCalculatorService.calculateFare(ticket);

        // THEN
        assertEquals(0.75 * Fare.CAR_RATE_PER_HOUR, ticket.getPrice());
    }
    
    @Test
    void calculateFareBikeWithLessThanOneHourParkingTime() {
        // GIVEN
        Date inTime = new Date(System.currentTimeMillis() - 45 * 60 * 1000); // 45 minutes parking time ago
        Date outTime = new Date();
        
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpotBikeNotAvailable);

        when(ticketDAO.getTicket(ticket.getVehicleRegNumber())).thenReturn(null);

        // WHEN
        fareCalculatorService.calculateFare(ticket);

        // THEN
        assertEquals(0.75 * Fare.BIKE_RATE_PER_HOUR, ticket.getPrice());
    }

    @Test
    void calculateFareCarWithMoreThanADayParkingTime() {
        // GIVEN
        Date inTime = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000); // 24 hours parking time ago
        Date outTime = new Date();
        
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpotCarNotAvailable);

        when(ticketDAO.getTicket(ticket.getVehicleRegNumber())).thenReturn(null);

        // WHEN
        fareCalculatorService.calculateFare(ticket);

        // THEN
        assertEquals(24 * Fare.CAR_RATE_PER_HOUR, ticket.getPrice());
    }
    
    @Test
    void calculateFareBikeWithMoreThanADayParkingTime() {
        // GIVEN
        Date inTime = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000); // 24 hours parking time ago
        Date outTime = new Date();
        
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpotBikeNotAvailable);

        when(ticketDAO.getTicket(ticket.getVehicleRegNumber())).thenReturn(null);

        // WHEN
        fareCalculatorService.calculateFare(ticket);

        // THEN
        assertEquals(24 * Fare.BIKE_RATE_PER_HOUR, ticket.getPrice());
    }
    
    @Test
    void calculateFareCarWithLessThanThirtyMinParkingTime() {
        // GIVEN
        Date inTime = new Date(System.currentTimeMillis() - 15 * 60 * 1000); // 15 minutes parking time ago
        Date outTime = new Date();
        
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpotCarNotAvailable);

        // WHEN
        fareCalculatorService.calculateFare(ticket);

        // THEN
        assertEquals(0.0, ticket.getPrice());
    }
    
    @Test
    void calculateFareBikeWithLessThanThirtyMinParkingTime() {
        // GIVEN
        Date inTime = new Date(System.currentTimeMillis() - 15 * 60 * 1000); // 15 minutes parking time ago
        Date outTime = new Date();
        
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpotBikeNotAvailable);

        // WHEN
        fareCalculatorService.calculateFare(ticket);

        // THEN
        assertEquals(0.0, ticket.getPrice());
    }
    
    @Test
    public void calculateFareCarWithDiscount() {
        // GIVEN
        Date inTime = new Date(System.currentTimeMillis() - 60 * 60 * 1000); // 1 hour parking time ago
        Date outTime = new Date();
        
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpotCarNotAvailable);
        
        when(ticketDAO.getTicket(ticket.getVehicleRegNumber())).thenReturn(ticket);

        // WHEN
        fareCalculatorService.calculateFare(ticket);

        // THEN
        assertEquals(Fare.CAR_RATE_PER_HOUR * 0.95, ticket.getPrice());
    }
    
    @Test
    public void calculateFareBikeWithDiscount() {
        // GIVEN
    	Date outTime = new Date(System.currentTimeMillis() + (60 * 60 * 1000)); // 1 hour parking time ago
        
        
        
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpotBikeNotAvailable);
        
        when(ticketDAO.getTicket(ticket.getVehicleRegNumber())).thenReturn(ticket);

        // WHEN
        fareCalculatorService.calculateFare(ticket);

        // THEN
        assertEquals(Fare.BIKE_RATE_PER_HOUR * 0.95, ticket.getPrice());
    }
}
