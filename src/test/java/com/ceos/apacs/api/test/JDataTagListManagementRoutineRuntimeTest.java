/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
*/

package com.ceos.apacs.api.test;

import com.ceos.apacs.api.Japx;
import com.ceos.apacs.api.Japxapi;
import com.sun.jna.NativeLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestCase;
import com.sun.jna.Pointer;

/**
 *
 * @author cgarcia
 */
public class JDataTagListManagementRoutineRuntimeTest extends TestCase {


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        short EC = 0;
        // Siempre se deben inicializar las API
        EC = Japxapi.InitializeAPI();
        assertEquals("Japxapi.InitializeAPI(): "+Japx.getErrorString(EC)+" ",Japx.ER_Success, EC);

        //En este caso en particular se deben inicializar las NIM a P500
        //que es la maquina de pruebas a ser utilizada.
        //No debe ser necesario con la tarjeta MBUS.
        EC = Japxapi.InitializeNimNameTable();
        assertEquals("Japxapi.InitializeNimNameTable(): "+Japx.getErrorString(EC)+" ",Japx.ER_Success, EC);

        //En este caso en particular se deben inicializar las NIM a P500
        //que es la maquina de pruebas a ser utilizada.
        EC = Japxapi.AddNimName("P500");
        assertEquals("Japxapi.InitializeNimNameTable(): "+Japx.getErrorString(EC)+" ",Japx.ER_Success, EC);

        //Inicializamos las rutinas de Datos
        EC = Japxapi.InitializeDataServices("CEOS");

    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        short EC = 0;

        //Primero paramos los servicios de datos.
        Japxapi.ShutDownDataServices();

        // Finalizando las API
        EC = Japxapi.ShutDownAPI();
        assertEquals("Japxapi.ShutDownAPI(): "+Japx.getErrorString(EC)+" ",Japx.ER_Success, EC);
    }



   /**
     * Crea los grupos y variables para los Apacs conectados a la maquina.
     * El formato general de un tag es IEC:
     * <i>ResourceName.ProgramName.DerivedName.<...>TC1100.PV</i>
     */
    public void testCrearYBorrarTagsReales()  {
        short EC = 0;
        int DTC = 0;
        Pointer DTL; //Puntero a la lista de tags;
        Pointer DT1,DT2,DT3;
        NativeLong MIL = new NativeLong();

        //Creo la primera lista de Tags
        DTL = Japxapi.CreateDataTagList();

        //Creo los tags
        DT1 = Japxapi.AddDataTag(DTL, "RESPALDO_IP.RESOURCE_BLOCKS.RESOURCE_STATUS.PROG_T");
        assertNotNull("No se pudo crear RESPALDO_IP.RESOURCE_BLOCKS.RESOURCE_STATUS.PROG_T",DT1);
        DT2 = Japxapi.AddDataTag(DTL, "RESPALDO_IP.RESOURCE_BLOCKS.RESOURCE_STATUS.PCTPRG");
        assertNotNull("No se pudo crear RESPALDO_IP.RESOURCE_BLOCKS.RESOURCE_STATUS.PCTPRG",DT2);
        DT3 = Japxapi.AddDataTag(DTL, "RESPALDO_IP.RESOURCE_BLOCKS.RESOURCE_STATUS.WFLAG");
        assertNotNull("No se pudo crear RESPALDO_IP.RESOURCE_BLOCKS.RESOURCE_STATUS.WFLAG",DT3);

        //Forzo una lectura del driver
        Japxapi.DoPolling();
        try {
            //Duermo un rato el programa
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            Logger.getLogger(JDataTagListManagementRoutinesTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        MIL.setValue(50000);
        EC = Japxapi.WaitForResponse(MIL);
        //assertEquals("Japxapi.WaitForResponse(MIL): "+Japx.getErrorString(EC),Japx.ER_Success,EC);

        int i =0;

        do {
            EC = Japxapi.ReadTagList(DTL);
            //assertEquals("Lectura de lista de tags: "+Japx.getErrorString(EC),Japx.ER_Success,EC);
           // EC = Japxapi.WaitForResponse(MIL);
            //assertEquals("Japxapi.WaitForResponse(MIL): "+Japx.getErrorString(EC),Japx.ER_Success,EC);
            
            Japxapi.DoPolling();
            System.out.println(i);
            i += 1;
        } while (EC != Japx.ER_Success);


        //Forzo una lectura del driver


        EC = Japxapi.ReadTagList(DTL);
        assertEquals("Lectura de lista de tags: "+Japx.getErrorString(EC),Japx.ER_Success,EC);

        //Forzo una lectura del driver
        Japxapi.DoPolling();


        //Aqui puedo probar la validación del tipo
        DTC = Japxapi.GetDataType(DT1);
        assertEquals("Tipo tiempo: ",Japx.DTYPE_TIME,DTC);

        DTC = Japxapi.GetDataType(DT2);
        assertEquals("Tipo real: ",Japx.DTYPE_REAL,DTC);

        DTC = Japxapi.GetDataType(DT3);
        assertEquals("Tipo boolean: ",Japx.DTYPE_BOOL,DTC);

        //Paso 1 reseteo la lista de tags
        // colocando el puntero al inicio de la lista
        Japxapi.ResetList(DTL);

        //
        DT1 = Japxapi.NextTag(DTL);
        assertNotNull(" DT = Japxapi.NextTag(DTL): "+DT1+" ",DT1);

        do {
            do {
                EC = Japxapi.RemoveDataTag(DTL, DT1);
                if ((EC != Japx.ER_TagNotRemoved) && (EC != Japx.ER_Success)) {
                        System.out.println("EC = Japxapi.RemoveDataTag(DTL, DT): "+Japx.getErrorString(EC));
                        fail();
                }
               } while(EC != Japx.ER_Success);

                    DT1 = Japxapi.NextTag(DTL);
                //assertNotNull(" DT = Japxapi.NextTag(DTL): "+DT+" ",DT);
            } while(DT1 != null) ;

        Japxapi.DeleteDataTagList(DTL);

       }

 }
