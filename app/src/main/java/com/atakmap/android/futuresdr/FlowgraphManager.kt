package com.atakmap.android.futuresdr

import com.atakmap.coremap.log.Log

class FlowgraphManager: HardwareManager.Listener {
    private companion object {
        const val TAG = "FlowgraphManager"

        @JvmStatic
        external fun runFg(deviceArgs: String)
    }

    override fun onHwReady(deviceArgs: String) {
        Log.i(TAG, "onHwReady(${deviceArgs})")
        runFg(deviceArgs)
    }

    override fun onHwDetached() {
        Log.i(TAG, "onHwDetached()")
    }

    override fun onPermissionDenied() {
        Log.i(TAG, "onPermissionDenied()")
    }
}