package de.fhg.iee.bacnet;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jan.lapp@iee.fraunhofer.de
 */
public class BacnetDateTest {
    
    public BacnetDateTest() {
    }

    @Test
    public void testParsing() {
        assertNotEquals(new BacnetDate(119, 3, 15, 5), new BacnetDate(119, 3, 15, 6));
        assertNotEquals(new BacnetDate(119, 3, 15, 5), new BacnetDate(119, 3, 16, 5));
        assertNotEquals(new BacnetDate(119, 3, 15, 5), new BacnetDate(119, 4, 15, 5));
        assertNotEquals(new BacnetDate(119, 3, 15, 5), new BacnetDate(118, 3, 15, 5));
        
        testRoundtrip(new BacnetDate(119, 3, 15, 5));
        testRoundtrip(new BacnetDate(119, 3, 15, BacnetDate.ANY));
        testRoundtrip(new BacnetDate(119, BacnetDate.ANY, 15, BacnetDate.ANY));
        testRoundtrip(new BacnetDate(119, 3, BacnetDate.ANY, BacnetDate.ANY));
        testRoundtrip(new BacnetDate(BacnetDate.ANY, 3, 15, BacnetDate.ANY));
        testRoundtrip(new BacnetDate(BacnetDate.ANY, 3, BacnetDate.ANY, BacnetDate.ANY));
        testRoundtrip(new BacnetDate(BacnetDate.ANY, BacnetDate.ANY, 15, BacnetDate.ANY));
        
        testRoundtrip(new BacnetDate(BacnetDate.ANY, BacnetDate.MONTH_EVEN, 15, BacnetDate.ANY));
        testRoundtrip(new BacnetDate(BacnetDate.ANY, BacnetDate.MONTH_EVEN, BacnetDate.DAY_OF_MONTH_LAST, BacnetDate.ANY));
        
        testRoundtrip(new BacnetDate(BacnetDate.ANY, BacnetDate.MONTH_ODD, BacnetDate.DAY_OF_MONTH_ODD, BacnetDate.ANY));
        testRoundtrip(new BacnetDate(BacnetDate.ANY, BacnetDate.MONTH_ODD, BacnetDate.DAY_OF_MONTH_EVEN, BacnetDate.ANY));
        testRoundtrip(new BacnetDate(BacnetDate.ANY, BacnetDate.MONTH_ODD, BacnetDate.DAY_OF_MONTH_LAST, BacnetDate.ANY));
        
        assertEquals(new BacnetDate(BacnetDate.ANY, BacnetDate.ANY, BacnetDate.ANY, BacnetDate.ANY), BacnetDate.parse("*"));
    }
    
    private void testRoundtrip(BacnetDate d) {
        System.out.println(d);
        assertEquals(d, BacnetDate.parse(d.toString()));
    }
    
}
