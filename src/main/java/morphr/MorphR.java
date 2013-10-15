/*
 * INESC-ID, Instituto de Engenharia de Sistemas e Computadores Investiga��o e Desevolvimento em Lisboa
 * Copyright 2013 INESC-ID and/or its affiliates and other
 * contributors as indicated by the @author tags. All rights reserved.
 * See the copyright.txt in the distribution for a full listing of
 * individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

/**
 * @author Maria Couceiro <mcouceiro@gsd.inesc-id.pt>
 * @version 1.0
 * @since 2013-07-24
 */


package morphr;


import eu.cloudtm.autonomicManager.commons.ForecastParam;
import eu.cloudtm.autonomicManager.commons.Param;
import eu.cloudtm.autonomicManager.oracles.InputOracle;
import eu.cloudtm.autonomicManager.oracles.Oracle;
import eu.cloudtm.autonomicManager.oracles.OutputOracle;
import eu.cloudtm.autonomicManager.oracles.exceptions.OracleException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import utils.PropertyReader;

import java.util.Arrays;


public class MorphR implements Oracle {
   private static final Log log = LogFactory.getLog(MorphR.class);
   private static String cubistLibraryFilename = PropertyReader.getString("cubistLibraryFilename", "/config/MorphR/MorphR.properties");

   public native void initiateCubist(String filename);

   public native double getPrediction(String query);

   public native double[] getPredictionAndError(String query);

   static {
      try {
         System.loadLibrary(cubistLibraryFilename);
      } catch (Exception e) {
         log.error(e);
         log.error(Arrays.toString(e.getStackTrace()));
      }
      log.trace(cubistLibraryFilename + " loaded");
   }

   @Override
   public OutputOracle forecast(InputOracle input) throws OracleException {

      final String query = buildQueryString(input);

      return new OutputOracleMorphR(this, query, input);

   }

   private String buildQueryString(InputOracle input) {
      String s = input.getParam(Param.MemoryInfo_used) + "," +
              input.getParam(Param.AvgGetsPerROTransaction) + "," +
              input.getParam(Param.AvgGetsPerWrTransaction) + "," +
              input.getParam(Param.AvgPutsPerWrTransaction) + "," +
              input.getParam(Param.LocalReadOnlyTxLocalServiceTime) + "," +
              input.getParam(Param.LocalUpdateTxLocalServiceTime) + "," +
              input.getParam(Param.AvgClusteredGetCommandReplySize) + "," +
              input.getParam(Param.AvgPrepareCommandSize) + "," +
              input.getParam(Param.PercentageWriteTransactions) + "," +
              input.getForecastParam(ForecastParam.NumNodes) + "," +
              input.getForecastParam(ForecastParam.ReplicationDegree) + ",?";
      log.trace("Query string " + s);
      return s;

   }


}


