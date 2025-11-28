package com.atakmap.android.futuresdr

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager
import androidx.core.content.ContextCompat
import com.atakmap.coremap.log.Log

class HardwareManager(
    ctx: Context,
) {
    private companion object {
        const val TAG = "HardwareManager"
        const val MY_USB_INTENT = "com.atakmap.android.FutureSDR.USB_PERMISSION"
    }

    interface Listener {
        fun onHackrfReady(conn: UsbDeviceConnection)
        fun onHackrfDetached()
        fun onPermissionDenied()
    }

    private val appCtx: Context = ctx.applicationContext ?: ctx

    private val usb: UsbManager =
        ctx.getSystemService(Context.USB_SERVICE) as UsbManager

    private var permissionPI: PendingIntent? = null
    private var receiver: BroadcastReceiver? = null
    private var listener: Listener? = null

    // active sdr /dev/bus/usb/xxx/yyy
    @Volatile
    private var activeDevice: String? = null

    fun setListener(l: Listener) {
        listener = l
    }

    fun start() {
        val filter = IntentFilter().apply {
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
            addAction(MY_USB_INTENT)
        }

        receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context, i: Intent) {
                val a = i.action
                Log.i(
                    TAG,
                    "onReceive action=$a thread=${Thread.currentThread().name}"
                )

                when (a) {
                    UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                        val d: UsbDevice? = i.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                        if (d != null) {
                            Log.i(TAG, "ATTACHED: ${devStr(d)}")
                        }
                        probeNow()
                    }

                    UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                        val d: UsbDevice? = i.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                        if (d != null) {
                            Log.w(TAG, "DETACHED: ${devStr(d)}")
                        }
                        if (d != null && isHydraSDR(d) && devNameEqActive(d)) {
                            listener?.onHackrfDetached()
                            activeDevice = null
                        } else {
                            Log.i(
                                TAG,
                                "DETACHED ignored (not active HackRF)"
                            )
                        }
                    }

                    MY_USB_INTENT -> {
                        handlePermissionResult(i)
                    }
                }
            }
        }

        ContextCompat.registerReceiver(appCtx, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)

        val intent = Intent(MY_USB_INTENT)
        permissionPI = PendingIntent.getBroadcast(
            appCtx,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        probeNow()
    }

    fun stop() {
        try {
            receiver?.let { appCtx.unregisterReceiver(it) }
        } catch (_: Throwable) {
        }
        receiver = null
        permissionPI = null
        activeDevice = null
    }

    fun probeNow() {
        Log.i(TAG, "probeNow()")

        // if (FlowgraphEngine.get().isBusy()) {
        //     Log.i(TAG, "Engine busy; skip probe")
        //     return
        // }

        for (d in usb.deviceList.values) {
            Log.i(TAG, "  check ${devDump(d)}")
            Log.i(TAG, "  is hydra ${isHydraSDR(d)}")
            if (!isHydraSDR(d)) continue

            Log.i(
                TAG,
                "  isHackrf=true hasPermission=${usb.hasPermission(d)}"
            )

            if (usb.hasPermission(d)) {
                openAndNotify(d)
            } else {
                permissionPI?.let { usb.requestPermission(d, it) }
            }
            break
        }
    }

    private fun handlePermissionResult(i: Intent) {
        val d: UsbDevice? = i.getParcelableExtra(UsbManager.EXTRA_DEVICE)
        val granted = i.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
        if (d == null) return
        if (!granted) {
            listener?.onPermissionDenied()
            return
        }
        openAndNotify(d)
    }

    private fun openAndNotify(d: UsbDevice) {
        //if (FlowgraphEngine.get().isBusy()) {
        //    Log.w(
        //        TAG,
        //        "Engine busy; skip open ${d.deviceName}"
        //    )
        //    return
        //}
        Log.i(TAG, "openAndNotify OPEN ${devStr(d)}")

        val c: UsbDeviceConnection? = usb.openDevice(d)
        if (c == null) {
            Log.w(TAG, "openDevice returned null")
            return
        }

        Log.i(
            TAG,
            "openDevice OK, fd=${c.fileDescriptor} conn=$c"
        )
        activeDevice = d.deviceName

        listener?.onHackrfReady(c)
    }

    private fun devNameEqActive(d: UsbDevice): Boolean {
        val name = d.deviceName
        return activeDevice != null && activeDevice == name
    }

    private fun isHydraSDR(d: UsbDevice?): Boolean {
        return d != null && d.vendorId == 0x1D50 && d.productId == 0x60A1
    }

    private fun devStr(d: UsbDevice?): String {
        if (d == null) return "null"
        return "name=${d.deviceName} vid=0x${d.vendorId.toString(16)} pid=0x${
            d.productId.toString(
                16
            )
        }"
    }

    private fun devDump(d: UsbDevice?): String {
        if (d == null) return "null"
        val sb = StringBuilder()
        sb.append("dev=").append(devStr(d))
            .append(" class=").append(d.deviceClass)
            .append(" sub=").append(d.deviceSubclass)
            .append(" proto=").append(d.deviceProtocol)
            .append(" ifaceCount=").append(d.interfaceCount)
        for (i in 0 until d.interfaceCount) {
            val inf: UsbInterface = d.getInterface(i)
            sb.append(" [if#").append(inf.id)
                .append(" cls=").append(inf.interfaceClass)
                .append(" sub=").append(inf.interfaceSubclass)
                .append(" ep=").append(inf.endpointCount).append("]")
        }
        return sb.toString()
    }
}