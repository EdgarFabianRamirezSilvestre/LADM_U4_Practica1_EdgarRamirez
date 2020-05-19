package mx.edu.ittepic.ladm_u4_practica1_edgarramirez

import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteException
import android.os.Build
import android.os.Bundle
import android.text.style.TtsSpan
import android.widget.ArrayAdapter
import android.telephony.SmsManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.util.jar.Manifest
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    val nombreBD = "autocontestadora"
    var hiloControl : HiloControl?=null

    var REQUEST_PERMISOS = 111

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Funcion que solicita permisos en dado caso de no haber sido otorgados.
        solicitarPermisos()

        hiloControl = HiloControl(this)
        hiloControl?.start()

        verificarBD()
        llamadasPerdidas()

        btnRegistro.setOnClickListener {
            var otraVentana = Intent(this, Main2Activity::class.java)
            startActivity(otraVentana)
        }//btnRegistro

    }//onCreate


    private fun solicitarPermisos() {
        var permisoReadCall = ActivityCompat.checkSelfPermission(this,android.Manifest.permission.READ_CALL_LOG)
        var permisoReadState = ActivityCompat.checkSelfPermission(this,android.Manifest.permission.READ_PHONE_STATE)
        var permisoSMS = ActivityCompat.checkSelfPermission(this,android.Manifest.permission.SEND_SMS)
        if(permisoReadCall != PackageManager.PERMISSION_GRANTED || permisoReadState != PackageManager.PERMISSION_GRANTED || permisoSMS != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_CALL_LOG,android.Manifest.permission.READ_PHONE_STATE,android.Manifest.permission.SEND_SMS),REQUEST_PERMISOS)
        }
    }//solicitarPermisos

    fun actualizarStat(ID : String){
        try {
            var baseDatos = BaseDatos(this, nombreBD, null, 1)
            var actualizar = baseDatos.writableDatabase
            var SQL = "UPDATE LLAMADASPERDIDAS SET STATUS='true' WHERE ID=?"
            var parametros = arrayOf(ID)

            actualizar.execSQL(SQL, parametros)
            actualizar.close()
            baseDatos.close()
        } catch (error : SQLiteException) {
            mensaje(error.message.toString())
        }
    }//actualizarStat

    fun statTelefono(tel : String) : String {
        try {
            var baseDatos = BaseDatos(this, nombreBD, null, 1)
            var select = baseDatos.readableDatabase
            var SQL = "SELECT * FROM TELEFONOS"
            var cursor = select.rawQuery(SQL, null)

            if(cursor.count > 0) {
                cursor.moveToFirst()
                var cantidad = cursor.count - 1

                (0..cantidad).forEach {
                    if(cursor.getString(1) == tel) {
                        if(cursor.getString(3) == "1") {
                            return "AGRADABLE"
                        } else if(cursor.getString(3) == "2") {
                            return "NO AGRADABLE"
                        }
                    }
                    cursor.moveToNext()
                }
            }
            select.close()
            baseDatos.close()
        } catch (error : SQLiteException){
            mensaje(error.message.toString())
        }
        return "IGNORADA"
    }//statTelefono


    fun obtenerMensaje(tipo : Int) : String {

            // 1 = PARA MENSAJE TIPO AGRADABLE
            // 2 = PARA MENSAJE TIPO NO AGRADABLE
        try {
            var baseDatos = BaseDatos(this, nombreBD, null, 1)
            var select = baseDatos.readableDatabase
            var SQL = "SELECT * FROM MENSAJES WHERE ID = ?"
            var parametros = arrayOf(tipo.toString())
            var cursor = select.rawQuery(SQL, parametros)

            if(cursor.moveToFirst()){
                return cursor.getString(1)
            }
            select.close()
            baseDatos.close()
        } catch (error : SQLiteException){ }
        return "ERROR AL OBTENER EL MENSAJE"
    }//obtenerMensaje

    fun llamadasPerdidas() {
        try {
            var i = 0
            var baseDatos = BaseDatos(this, nombreBD, null, 1)
            var select = baseDatos.readableDatabase
            var SQL = "SELECT * FROM LLAMADASPERDIDAS ORDER BY ID DESC"
            var cursor = select.rawQuery(SQL, null)

            if(cursor.count > 0) {
                var arreglo = ArrayList<String>()
                cursor.moveToFirst()
                var cantidad = cursor.count-1
                (0..cantidad).forEach {
                    i++
                    if((i % 2) == 0){
                        var data = "Telefono: ${cursor.getString(1)} \nLlamada tipo: ${statTelefono(cursor.getString(1))}"
                        arreglo.add(data)
                    }
                    cursor.moveToNext()
                }
                listLlamadas.adapter = ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, arreglo)
            } else if(cursor.count == 0){
                var noHay = ArrayList<String>()
                var data = "NO HAY LLAMADAS PERDIDAS"

                noHay.add(data)
                listLlamadas.adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,noHay)
            }

            select.close()
            baseDatos.close()
        } catch (error : SQLiteException){
            mensaje(error.message.toString())
        }
    }//llamadasPerdidas

    fun agregarMensaje() {
        var baseDatos = BaseDatos(this, nombreBD, null, 1)
        var insertar = baseDatos.writableDatabase
        var SQL = "INSERT INTO MENSAJES VALUES(NULL, '')"

        insertar.execSQL(SQL)
        insertar.close()
        baseDatos.close()
    }//agregarMensaje

    fun verificarBD(){
        try {
            var baseDatos = BaseDatos(this, nombreBD, null, 1)
            var select = baseDatos.readableDatabase
            var SQL = "SELECT * FROM MENSAJES WHERE ID = 1"
            var cursor = select.rawQuery(SQL, null)

            if(cursor.moveToFirst()){
            } else {
                agregarMensaje()
                agregarMensaje()
            }

            select.close()
            baseDatos.close()
        } catch (error : SQLiteException){ }
    }//verificarBD

    fun enviarSMS() {
        try {
            var j = 0
            var baseDatos = BaseDatos(this, nombreBD, null, 1)
            var select = baseDatos.readableDatabase
            var SQL = "SELECT * FROM LLAMADASPERDIDAS"
            var cursor = select.rawQuery(SQL, null)

            if(cursor.count > 0) {
                cursor.moveToFirst()
                var cantidad = cursor.count - 1

                (0..cantidad).forEach {
                    j++
                    if(cursor.getString(2) == "false" && (j % 2) == 0) {
                        if(statTelefono(cursor.getString(1)) == "AGRADABLE") {
                            actualizarStat(cursor.getString(0))
                            actualizarStat((cursor.getInt(0) + 1).toString())
                            SmsManager.getDefault().sendTextMessage(cursor.getString(1), null, obtenerMensaje(1), null, null)
                        } else if(statTelefono(cursor.getString(1)) == "NO AGRADABLE") {
                            actualizarStat(cursor.getString(0))
                            actualizarStat((cursor.getInt(0) + 1).toString())
                            SmsManager.getDefault().sendTextMessage(cursor.getString(1), null, obtenerMensaje(2), null, null)
                        }
                    }
                    cursor.moveToNext()
                }//foeEach
            }//if
            select.close()
            baseDatos.close()
        } catch (error : SQLiteException){
            mensaje(error.message.toString())
        }//catch
    }//enviarSMS

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == REQUEST_PERMISOS){
            llamadasPerdidas()
        }
    }//onRequestPermissionsResult

    fun mensaje(mensaje : String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
    }//mensaje

}//class
