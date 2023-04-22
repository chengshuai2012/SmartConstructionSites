
package com.aoecloud.smartconstructionsites.network




class Network {

    var mainPageService = ServiceCreator.create(MainPageService::class.java)
        private set

    companion object {

        @Volatile
        private var INSTANCE: Network? = null

        fun getInstance(): Network = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Network()
        }
    }
}