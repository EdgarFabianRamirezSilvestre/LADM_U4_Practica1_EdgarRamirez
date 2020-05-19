package mx.edu.ittepic.ladm_u4_practica1_edgarramirez

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log


class CallReceiver : BroadcastReceiver() {

    var cursor : Context ?= null
    var contestar = true
    var j = 0
    val nombreBD = "autocontestadora"

    override fun onReceive(context : Context, intent: Intent?) {
        try {
            cursor = context
            val tmgr = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

            val PhoneListener = MyPhoneStateListener()

            tmgr.listen(PhoneListener, PhoneStateListener.LISTEN_CALL_STATE)

        } catch (e: Exception) {
            Log.e("Phone Receive Error", " $e")
        }
    }

    private inner class MyPhoneStateListener : PhoneStateListener() {

        override fun onCallStateChanged(state: Int, incomingNumber: String) {
            Log.d("MyPhoneListener", "$state   incoming no:$incomingNumber")

            if(state == 2){
                contestar = false
            }

            if (state == 0 && contestar == true) {
                val num = "$incomingNumber"
                Log.d("LLamadaPerdida", num)
                j++
                try {
                    if(!num.isEmpty()) {
                        var baseDatos = BaseDatos(cursor!!, nombreBD, null, 1)
                        var insertar = baseDatos.writableDatabase
                        var SQL = "INSERT INTO LLAMADASPERDIDAS VALUES (NULL ,'${num}', 'false')"

                        insertar.execSQL(SQL)
                        baseDatos.close()
                        Log.d("Inserta", "SE HA INSERTADO CORRECTAMENTE" + j)
                    }
                } catch (err : Exception) {

                }
            }
        }
    }
}