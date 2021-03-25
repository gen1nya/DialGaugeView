package com.android.vehicleDataService

import org.junit.Test

class SimpleUnitTest {

    @Test
    fun testDataGeneration() {
        var counter = 0.0
        while(counter <= Math.PI * 2 * 10) { //
            counter += 0.001
            val data = DataGenerator.getData(0.0)
            assert(data[0] >= 0)
            assert(data[0] <= 280)
            assert(data[1] >= 0)
            assert(data[1] <= 10000)
        }
    }
}