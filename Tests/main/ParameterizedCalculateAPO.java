package main;

import helper.SensorData;
import helper.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;
@RunWith(Parameterized.class)
public class ParameterizedCalculateAPO {

    private User mockUser;

    @Parameterized.Parameter
    public int medicalType ;

    @Parameterized.Parameter(1)
    public int aqi;

    @Parameterized.Parameter(2)
    public int expected_result;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                //When the aqi is in 1->50
                { 3, 1, 90 }, { 7, 35, 210 }, { 2, 50, 60 },

                //When the aqi is in 51->100
                { 1, 51, 15}, { 3, 74, 45 }, { 2, 100, 30},

                //When the aqi is in 101->150
                { 4, 101, 40}, { 10, 125, 100}, { 2, 150, 20 },

                //When the aqi is >150
                { 3, 200, 15}, { 2, 200, 10}, { 1, 200, 5}
        });
    }

    @Before
    public void setUp(){
        SensorData mockSensor = new SensorData("abc", "B", 30, aqi);
        mockUser = new User(medicalType, new int[]{34, 35},90, 90, mockSensor, 0, false,false);
    }

    @Test
    public void calculateapoThreshhold_WhenCalled_ReturnAPOThreshold() {
        assertEquals(ContextManager.calculateapoThreshhold(mockUser), expected_result,0);
    }
}