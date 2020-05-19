package mx.edu.ittepic.ladm_u4_practica1_edgarramirez

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper

class BaseDatos(context : Context,
                nombreBD : String,
                cursorFactory: SQLiteDatabase.CursorFactory?,
                numeroVersion : Int) : SQLiteOpenHelper(context, nombreBD, cursorFactory, numeroVersion) {

    override fun onCreate(db: SQLiteDatabase?) {
        try {
            /*
                 1 -> MENSAJE PARA PERSONAS AGRADABLES
                 2 -> MENSAJE PARA PERSONAS NO AGRADABLES

                 1 -> TELEFONOS DE PERSONAS AGRADABLES
                 2 -> TELEFONOS DE PERSONAS NO AGRADABLES
             */
            db!!.execSQL("CREATE TABLE MENSAJES(ID INTEGER PRIMARY KEY AUTOINCREMENT, MENSAJE VARCHAR(400))")
            db.execSQL("CREATE TABLE TELEFONOS(ID INTEGER PRIMARY KEY AUTOINCREMENT, TELEFONO VARCHAR(15), NOMBRE VARCHAR (25), TIPO VARCHAR(1))")
            db.execSQL("CREATE TABLE LLAMADASPERDIDAS(ID INTEGER PRIMARY KEY AUTOINCREMENT, TELEFONO VARCHAR(15), STATUS BOOLEAN)")


        } catch (error : SQLiteException){ }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }
}