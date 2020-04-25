package main;

import helper.SensorData;
import helper.User;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import support.LocationDetails;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class ContextManagerTest {
    private ContextManager.ContextManagerWorkerI workerI;
    private SensorData mockSensor;
    private User mockUser;

    @Before
    public void setUpContextManagerBeforeTest() throws Exception{

        //Create new object ContextManagerWorkerI
        workerI = new ContextManager.ContextManagerWorkerI();

//        //Create the communicator of context manager
//        ContextManager.communicator = com.zeroc.Ice.Util.initialize(new String[] {"[Ljava.lang.String;@2530c12"});
//
//        //Get the preference
//        ContextManager.iniPreferenceWorker();
//
//        //Get the locationWorker
//        ContextManager.iniLocationMapper();

        //Get the cityinfo of the context manager
        ContextManager.cityInfo = ContextManager.readCityInfo();

        //CurrentWeather set to 0
        ContextManager.currentWeather = 0;

        //Create sensor data and user to add in the users list
        mockSensor = new SensorData("David", "D", 30, 100);
        mockUser = new User(3, new int[]{27, 30},90, 45, mockSensor, 0, false,false);
        ContextManager.users.put("David", mockUser);

    }

    @Test
    @Ignore
    public void addUser_Whencalled_addUserInUsersList(){
        workerI.addUser("abc",null);
        assertTrue(ContextManager.users.containsKey("abc"));
    }

    @Test
    public void searchInfo_ItemIsInTheCityInfo_ReturnfInfoOfItem(){
        String result = workerI.searchInfo("Dam Sen Parklands", null);
        assertEquals("The Dam Sen Parklands area was created as part of the rejuvenation of the industrial upgrade undertaken for World Expo 1988. The Parklands area is spacious with plenty of green and spaces for all ages. A big lake promenade stretches the area of Dam Sen Parklands.",
                result);
    }

    @Test
    public void searchInfo_ItemIsNotInTheCityInfo_ReturnNull(){
        assertNull(workerI.searchInfo("haha", null));
    }

    @Test
    public void searchInfo_TheCityInfoDoesNotContainAnyElement_ReturnNull(){
        ContextManager.cityInfo.clear();
        assertNull(workerI.searchInfo("David",null));
    }

    @Test
    public void searchItems_LocationIsInCityInfo_ReturnListOfItems(){
        //Users list contains David who has location D
        assertEquals(Arrays.toString(workerI.searchItems("David", null)), Collections.singletonList("Ho Chi Minh City, Downtown").toString());
        assertNotEquals(Arrays.toString(workerI.searchItems("David", null)), Collections.singletonList("Crescent Mall").toString());
        assertNotEquals(Arrays.toString(workerI.searchItems("David", null)), Collections.singletonList("abc").toString());
        assertNotEquals(Arrays.toString(workerI.searchItems("David",null)), Collections.singletonList("").toString());
    }

    @Test
    public void searchItems_LocationIsNotInCityInfo_ReturnNull(){
        mockUser.sensorData.location = "E";
        assertEquals(Arrays.toString(workerI.searchItems("David",null)), Collections.singletonList("").toString());
    }

    @Test
    public void searchItems_NoElementInTheCityInfoList_ReturnNull(){
        ContextManager.cityInfo.clear();
        assertEquals(Arrays.toString(workerI.searchItems("David",null)), Collections.singletonList("").toString());
    }

    @Test
    public void readCityInfo_WhenCalled_returnListOfLocationDetails(){
        List<LocationDetails> locationList = ContextManager.readCityInfo();
        assertEquals(4, locationList.size());
        assertEquals(locationList.get(3).getServices(), Arrays.asList("restaurants", "shops", "market", "bowling"));
        assertNotEquals(locationList.get(2).getServices(), Arrays.asList("cinema", "restaurants", "shops"));
        assertEquals(locationList.get(0).getName(), "Vivo City Shopping Centre");
        assertNotEquals(locationList.get(1).getName(), "wed");
    }

    @Test
    @Ignore
    public void getLocationsByService_ServiceIsNotInTheList_ReuturnListOfService(){
        List<String> resultList= ContextManager.getLocationsByService("cinema");
        assertEquals(resultList, Arrays.asList("Vivo City Shopping Centre", "Crescent Mall"));
        assertNotEquals(resultList, Arrays.asList("abcefg", "Hung Vuong Plaza"));
        assertNotEquals(resultList, Arrays.asList("Dam Sen Parklands", "Ho Chi Minh City, Downtown"));
        assertNotEquals(resultList, Collections.singletonList("Vivo City Shopping Centre"));
    }

    @Test
    public void resetClock_WhenCalled_ClockShouldBeResetTo0(){
        //Currently clock is 45
        //David is mockUser , is in the users list
        ContextManager.resetClock("David");
        assertEquals(mockUser.clock , 0);

    }

    @Test
    public void checkAPOReached_ClockEqualsTheAPOThreshold_ReturnTrue(){
        List<int[]> parameter = Arrays.asList(new int[] {3, 35, 90}, new int[] {5, 70, 75}, new int[] {20, 135, 200},
                                                new int[] {2, 150, 20}, new int [] {100, 168, 500 }, new int[]{30, 250, 150});
        for (int i = 0 ; i < parameter.size(); i++){
            mockUser.medicalConditionType = parameter.get(i)[0];
            mockUser.sensorData.aqi = parameter.get(i)[1];
            mockUser.clock = parameter.get(i)[2];
            //Calculate APO threshold
            mockUser.apoThreshhold = ContextManager.calculateapoThreshhold(mockUser);

            assertTrue(ContextManager.checkapoReached(mockUser));
        }
    }

    @Test
    public void checkAPOReached_ClockSmallerThanTheAPOThreshold_ReturnFalse(){
        List<int[]> parameter = Arrays.asList(new int[] {3, 35, 300}, new int[] {5, 70, 80},
                                                new int[] {2, 150, 21}, new int[] {100, 168, 10000});
        for (int i = 0 ; i < parameter.size(); i++){
            mockUser.medicalConditionType = parameter.get(i)[0];
            mockUser.sensorData.aqi = parameter.get(i)[1];
            mockUser.clock = parameter.get(i)[2];
            //Calculate APO threshold
            mockUser.apoThreshhold = ContextManager.calculateapoThreshhold(mockUser);

            assertFalse(ContextManager.checkapoReached(mockUser));
        }
        mockUser.sensorData.aqi = 3;
        mockUser.clock = 20;
        assertFalse(ContextManager.checkapoReached(mockUser));
    }

    @Test
    public void checkTempReached_TemperatureEqualsTemperatureThreshold(){
        //Temperature is 27
        mockUser.sensorData.temperature = 27;
        assertTrue(ContextManager.checkTempReached(mockUser));

        //Temperature is 30
        mockUser.sensorData.temperature = 30;
        assertTrue(ContextManager.checkTempReached(mockUser));
    }

    @Test
    public void checkTempReached_TemperatureHigherThanALlTemperatureThreshold(){
        //Temperature is 32
        mockUser.sensorData.temperature = 32;
        assertTrue(ContextManager.checkTempReached(mockUser));
    }

    @Test
    public void checkTempReached_TemperatureSmallerThanAllTemperatureThreshold(){
        //Temperature is
        mockUser.sensorData.temperature = 27;
        assertTrue(ContextManager.checkTempReached(mockUser));
    }

    @Test
    public void checkTempReached_TemperatureBetweenTemperatureThresholds(){
        //Temperature is 28
        mockUser.sensorData.temperature = 27;
        assertTrue(ContextManager.checkTempReached(mockUser));
    }


}
