package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Date;



@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        
    }

    @AfterAll
    private static void tearDown(){

    }

    @Test
    public void testParkingACar() {
    	// GIVEN
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        // WHEN
        parkingService.processIncomingVehicle();

        // THEN
        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        assertNotNull(ticket, "The ticket must be saved in the database.");
        assertEquals("ABCDEF", ticket.getVehicleRegNumber());
        assertNotNull(ticket.getInTime(), "The entry time must be entered.");

        ParkingSpot parkingSpot = ticket.getParkingSpot();
        assertNotNull(parkingSpot, "A parking spot must be associated with the ticket.");
        assertFalse(parkingSpot.isAvailable(), "The parking spot must be occupied.");
    }

    @Test
    public void testParkingLotExit() {
    	// GIVEN
        testParkingACar();
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        // WHEN
        parkingService.processExitingVehicle();

        // THEN
        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        assertNotNull(ticket.getOutTime(), "The exit time must be entered.");
        assertTrue(ticket.getOutTime().after(ticket.getInTime()), "The exit time must be after the entry time.");
        assertTrue(ticket.getPrice() > 0, "The price must be calculated.");
    }
    
    @Test
    public void testParkingLotExitRecurringUser() {
    	// GIVEN (First entry)
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();
        
        Ticket firstTicket = ticketDAO.getTicket("ABCDEF");
        assertNotNull(firstTicket, "First ticket must not be null.");
        firstTicket.setInTime(new Date(System.currentTimeMillis() - 60 * 60 * 1000)); // 1h parking time
        firstTicket.setOutTime(new Date());
        ticketDAO.updateTicket(firstTicket);
        
        parkingService.processExitingVehicle();

        // GIVEN (Second entry - Recurring User)
        parkingService.processIncomingVehicle();
        
        Ticket secondTicket = ticketDAO.getTicket("ABCDEF");
        assertNotNull(secondTicket, "Second ticket must not be null.");
        secondTicket.setInTime(new Date(System.currentTimeMillis() - 60 * 60 * 1000)); // 1h parking time
        secondTicket.setOutTime(new Date());
        ticketDAO.updateTicket(secondTicket);

        parkingService.processExitingVehicle();

        // THEN
        Ticket finalTicket = ticketDAO.getTicket("ABCDEF");

        assertNotNull(finalTicket.getOutTime(), "The exit time must be entered.");
        assertTrue(finalTicket.getPrice() > 0, "The price must be calculated.");
        assertTrue(finalTicket.isRecurringUser(), "The ticket must identify a recurring user.");

        // Check that the 5% discount is applied
        double expectedPrice = Fare.CAR_RATE_PER_HOUR * 0.95;
        assertEquals(expectedPrice, finalTicket.getPrice(), 0.01, "A 5% discount must be applied to the price.");
    }
}
