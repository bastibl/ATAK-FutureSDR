package com.atakmap.android.futuresdr

import com.atakmap.coremap.log.Log
import com.atakmap.android.cot.CotMapComponent
import com.atakmap.coremap.cot.event.CotEvent

class AtakBridge {
    fun onReceive(data: ByteArray) {
        Log.i(TAG, "received message")
        val str = data.decodeToString(0, data.size - 2)
        Log.i(TAG, str)
        try {
            val event = CotEvent.parse(str)
            CotMapComponent.getInternalDispatcher().dispatch(event)

            // send out to network?
            // CotMapComponent.getExternalDispatcher().dispatch(event)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse/send CoT", e)
        }
    }

    companion object {
        private const val TAG = "AtakBridge"
    }
}