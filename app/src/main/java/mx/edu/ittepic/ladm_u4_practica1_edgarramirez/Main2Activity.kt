package mx.edu.ittepic.ladm_u4_practica1_edgarramirez

import android.database.sqlite.SQLiteException
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.widget.ArrayAdapter
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main2.*

class Main2Activity : AppCompatActivity() {

    val nombreBD = "autocontestadora"
    var listaID = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        cargarLista()
        cargarMensaje()

        chkBoxMsj.setOnClickListener{
            cargarMensaje()
        }

        btnActualizarMensaje.setOnClickListener {
            if(txtMensaje.text.isEmpty()) {
                mensaje("ES NECESARIO ESCRIBIR EL MENSAJE")
                return@setOnClickListener
            }
            actualizarMensaje(txtMensaje.text.toString())
        }//btnActualizarMensaje

        btnGuardarDatos.setOnClickListener {
            if(txtTelefono.text.isEmpty() || txtNombre.text.isEmpty()){
                mensaje("NUMERO DE TELEFONO Y/O NOMBRE INGRESADO NO VALIDO")
                return@setOnClickListener
            }
            agregarTelefono(txtTelefono.text.toString(), txtNombre.text.toString())
            txtTelefono.setText("")
            txtNombre.setText("")
            cargarLista()
            txtTelefono.requestFocus()
        }//btnGuardarDatos
    }//onCreate

    fun cargarMensaje() {
        try {
            var baseDatos = BaseDatos(this, nombreBD, null, 1)
            var select = baseDatos.readableDatabase
            var SQL1 = "SELECT * FROM MENSAJES WHERE ID = 1"
            var SQL2 = "SELECT * FROM MENSAJES WHERE ID = 2"
            var cursor1 = select.rawQuery(SQL1, null)
            var cursor2 = select.rawQuery(SQL2, null)

            if(chkBoxMsj.isChecked){
                if(cursor1.moveToFirst()){
                    //SI HAY RESULTADO
                    txtMensaje.setText(cursor1.getString(1))
                } else {
                    //NO HAY RESULTADO
                }
            }else if (chkBoxMsj.isChecked == false){
                if(cursor2.moveToFirst()){
                    //SI HAY RESULTADO
                    txtMensaje.setText(cursor2.getString(1))
                } else {
                    //NO HAY RESULTADO
                }
            }

            select.close()
            baseDatos.close()
        } catch (error : SQLiteException){ }
    }//cargarMensaje

    fun cargarLista() {
        try {
            var baseDatos = BaseDatos(this, nombreBD, null, 1)
            var select = baseDatos.readableDatabase
            var SQL = "SELECT * FROM TELEFONOS"
            var cursor = select.rawQuery(SQL, null)

            if(cursor.count > 0) {
                var arreglo = ArrayList<String>()
                this.listaID.clear()
                cursor.moveToFirst()
                var cantidad = cursor.count-1

                (0..cantidad).forEach {
                    var data = "Telefono: ${cursor.getString(1)} \nNombre: ${cursor.getString(2)}" +
                            "\nTipo: ${statTelefono(cursor.getString(1))}"
                    arreglo.add(data)
                    listaID.add(cursor.getString(0))
                    cursor.moveToNext()
                }

                listaA.adapter = ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, arreglo)
                listaA.setOnItemClickListener { parent, view, position, id ->
                    AlertDialog.Builder(this)
                        .setTitle("ATENCIÓN")
                        .setMessage("¿Desea eliminar el telefono seleccionado?")
                        .setPositiveButton("Eliminar") {d, i ->
                            eliminarTelefono(listaID[position])
                        }
                        .setNegativeButton("Cancelar") {d, i -> }
                        .show()
                }
            }

            select.close()
            baseDatos.close()
        } catch (error : SQLiteException){
            mensaje(error.message.toString())
        }
    }//cargarLista

    fun eliminarTelefono(id : String) {
        try {
            var baseDatos = BaseDatos(this, nombreBD, null, 1)
            var eliminar = baseDatos.writableDatabase
            var SQL = "DELETE FROM TELEFONOS WHERE ID = ?"
            var parametros = arrayOf(id)

            eliminar.execSQL(SQL,parametros)
            eliminar.close()
            baseDatos.close()
            mensaje("ELIMINADO CORRECTAMENTE")
            cargarLista()
        } catch (error : SQLiteException) {
            mensaje(error.message.toString())
        }
    }//eliminarTelefono

    fun agregarTelefono(numero : String, nombre : String) {
        var baseDatos = BaseDatos(this, nombreBD, null, 1)
        var insertar = baseDatos.writableDatabase
        var SQL1 = "INSERT INTO TELEFONOS VALUES(NULL, '${numero}','${nombre}', '1')"
        var SQL2 = "INSERT INTO TELEFONOS VALUES(NULL, '${numero}','${nombre}', '2')"

        if(chkBoxDatos.isChecked){
            insertar.execSQL(SQL1)
            mensaje("AGRADABLE")
        }else if(!chkBoxDatos.isChecked){
            insertar.execSQL(SQL2)
        }
        insertar.close()
        baseDatos.close()

        mensaje("SE INSERTO CORRECTAMENTE EL TELEFONO")
    }//agregarTelefono

    fun statTelefono(tel : String) : String {
        try {
            var baseDatos = BaseDatos(this, nombreBD, null, 1)
            var select = baseDatos.readableDatabase
            var SQL = "SELECT * FROM TELEFONOS"
            var cursor = select.rawQuery(SQL, null)

            if(cursor.count > 0) {
                cursor.moveToFirst()
                var cantidad = cursor.count-1

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
        } catch (error : SQLiteException){ }
        return "IGNORADA"
    }//statTelefono

    fun actualizarMensaje(mensaje : String) {
        try {
            var baseDatos = BaseDatos(this, nombreBD, null, 1)
            var actualizar = baseDatos.writableDatabase
            var SQL = "UPDATE MENSAJES SET MENSAJE='${mensaje}' WHERE ID=?"
            var parametros1 = arrayOf(1)
            var parametros2 = arrayOf(2)

            if(chkBoxMsj.isChecked){
                actualizar.execSQL(SQL, parametros1)
            }else if(chkBoxMsj.isChecked == false) {
                actualizar.execSQL(SQL, parametros2)
            }

            actualizar.close()
            baseDatos.close()

            mensaje("SE ACTUALIZO CORRECTAMENTE EL MENSAJE")
        } catch (error : SQLiteException) {
            mensaje(error.message.toString())
        }
    }//actualizarMensaje

    fun mensaje(mensaje : String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG)
            .show()
    }//mensaje

}//class