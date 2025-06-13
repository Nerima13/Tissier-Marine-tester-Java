package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
        dataBasePrepareService.clearDataBaseEntries();    
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
		assertNotNull(ticketDAO.getTicket("ABCDEF"));
		assertEquals(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR), 2);

	}

    @Test
	public void testParkingLotExit() {
		// GIVEN
    	testParkingACar();
		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		
		// WHEN
		parkingService.processExitingVehicle();

		// THEN
		assertNotNull(ticketDAO.getTicket("ABCDEF"));
		assertEquals(ticketDAO.getTicket("ABCDEF").getPrice(), 0);
		assertNotNull(ticketDAO.getTicket("ABCDEF").getOutTime());
    }
    
    @Test
    void testParkingLotExitRecurringUser() throws Exception {
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        // First entry - Register User
        parkingService.processIncomingVehicle();
        parkingService.processExitingVehicle();
        
        // Second entry - Recurring User
        parkingService.processIncomingVehicle();
        
        Date outTime = new Date(System.currentTimeMillis() + (60 * 60 * 1000)); // 1 hour parking time later
        
        
        parkingService.processExitingVehicle();
        
        // Check if the discount has been applied
        Ticket updatedTicket = ticketDAO.getTicket("ABCDEF");
        assertNotNull(updatedTicket.getOutTime());
        
        double expectedPrice = Fare.CAR_RATE_PER_HOUR * 0.95;
        assertEquals(expectedPrice, updatedTicket.getPrice(), 0.01);
    }
}