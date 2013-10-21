package morphr;
/*
 * INESC-ID, Instituto de Engenharia de Sistemas e Computadores Investigação e Desevolvimento em Lisboa
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

import eu.cloudtm.autonomicManager.commons.ForecastParam;
import eu.cloudtm.autonomicManager.commons.Param;
import eu.cloudtm.autonomicManager.commons.ReplicationProtocol;
import eu.cloudtm.autonomicManager.oracles.InputOracle;
import eu.cloudtm.autonomicManager.oracles.OutputOracle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import utils.PropertyReader;

/**
 * @author Diego Didona, didona@gsd.inesc-id.pt
 *         Date: 27/08/13
 */
public class OutputOracleMorphR implements OutputOracle {
   private final static Log log = LogFactory.getLog(OutputOracleMorphR.class);
   //TODO clean up
   private static String modelFilenameAbort2PC = PropertyReader.getString("modelFilenameAbort2PC", "/confMorphR/MorphR.properties");
   private static String modelFilenameThroughput2PC = PropertyReader.getString("modelFilenameThroughput2PC", "/confMorphR/MorphR.properties");
   private static String modelFilenameReadOnly2PC = PropertyReader.getString("modelFilenameReadOnly2PC", "/confMorphR/MorphR.properties");
   private static String modelFilenameWrite2PC = PropertyReader.getString("modelFilenameWrite2PC", "/confMorphR/MorphR.properties");
   private static String modelFilenameAbortPB = PropertyReader.getString("modelFilenameAbortPB", "/confMorphR/MorphR.properties");
   private static String modelFilenameThroughputPB = PropertyReader.getString("modelFilenameThroughputPB", "/confMorphR/MorphR.properties");
   private static String modelFilenameReadOnlyPB = PropertyReader.getString("modelFilenameReadOnlyPB", "/confMorphR/MorphR.properties");
   private static String modelFilenameWritePB = PropertyReader.getString("modelFilenameWritePB", "/confMorphR/MorphR.properties");
   private static String modelFilenameAbortTO = PropertyReader.getString("modelFilenameAbortTO", "/confMorphR/MorphR.properties");
   private static String modelFilenameThroughputTO = PropertyReader.getString("modelFilenameThroughputTO", "/confMorphR/MorphR.properties");
   private static String modelFilenameReadOnlyTO = PropertyReader.getString("modelFilenameReadOnlyTO", "/confMorphR/MorphR.properties");
   private static String modelFilenameWriteTO = PropertyReader.getString("modelFilenameWriteTO", "/confMorphR/MorphR.properties");
   private MorphR morphr;
   private ReplicationProtocol replicationProtocol;
   private String query;
   private InputOracle input;

   public OutputOracleMorphR(MorphR m, String q, InputOracle inputO) {
      morphr = m;
      replicationProtocol = extractReplicationProtocol(inputO);
      query = q;
      input = inputO;
   }

   @Override
   public double responseTime(int transactionalClass) {
      if (transactionalClass == 0) {
         if (replicationProtocol.compareTo(ReplicationProtocol.TWOPC) == 0)
            morphr.initiateCubist(modelFilenameReadOnly2PC);
         else if (replicationProtocol.compareTo(ReplicationProtocol.PB) == 0)
            morphr.initiateCubist(modelFilenameReadOnlyPB);
         else
            morphr.initiateCubist(modelFilenameReadOnlyTO);
      } else {
         if (replicationProtocol.compareTo(ReplicationProtocol.TWOPC) == 0)
            morphr.initiateCubist(modelFilenameWrite2PC);
         else if (replicationProtocol.compareTo(ReplicationProtocol.PB) == 0)
            morphr.initiateCubist(modelFilenameWritePB);
         else
            morphr.initiateCubist(modelFilenameWriteTO);
      }
      return morphr.getPrediction(query);
   }

   @Override
   public double throughput(int i) {
      log.trace("Asking xput for xact class " + i);
      double txPercentage = txClassPercentage(i);
      if (replicationProtocol.compareTo(ReplicationProtocol.TWOPC) == 0) {
         log.trace("TPC initing model");
         morphr.initiateCubist(modelFilenameThroughput2PC);
         log.trace("TPC model inited");
      } else if (replicationProtocol.compareTo(ReplicationProtocol.PB) == 0) {
         log.trace("PB initing model");
         morphr.initiateCubist(modelFilenameThroughputPB);
         log.trace("PB model inited");
      } else {
         log.trace("TO initing model");
         morphr.initiateCubist(modelFilenameThroughputTO);
         log.trace("To model inited");
      }
      //This works only if retry-on-abort is enabled
      log.trace("Querying...");
      double prediction = morphr.getPrediction(query);
      log.trace("Query returned " + prediction);
      return prediction * txPercentage;
   }

   @Override
   public double abortRate(int i) {
      if (i == 0) {
         return 0.0;
      }
      if (replicationProtocol.compareTo(ReplicationProtocol.TWOPC) == 0)
         morphr.initiateCubist(modelFilenameAbort2PC);
      else if (replicationProtocol.compareTo(ReplicationProtocol.PB) == 0)
         morphr.initiateCubist(modelFilenameAbortPB);
      else
         morphr.initiateCubist(modelFilenameAbortTO);
      return morphr.getPrediction(query);
   }

   @Override
   public double getConfidenceThroughput(int i) {
      double txPercentage = txClassPercentage(i);
      if (replicationProtocol.compareTo(ReplicationProtocol.TWOPC) == 0)
         morphr.initiateCubist(modelFilenameThroughput2PC);
      else if (replicationProtocol.compareTo(ReplicationProtocol.PB) == 0)
         morphr.initiateCubist(modelFilenameThroughputPB);
      else
         morphr.initiateCubist(modelFilenameThroughputTO);
      //This works only if retry-on-abort is enabled
      return morphr.getPredictionAndError(query)[1] * txPercentage;
   }

   @Override
   public double getConfidenceAbortRate(int i) {
      if (i == 0) {
         return 0.0;
      }
      if (replicationProtocol.compareTo(ReplicationProtocol.TWOPC) == 0)
         morphr.initiateCubist(modelFilenameAbort2PC);
      else if (replicationProtocol.compareTo(ReplicationProtocol.PB) == 0)
         morphr.initiateCubist(modelFilenameAbortPB);
      else
         morphr.initiateCubist(modelFilenameAbortTO);
      return morphr.getPredictionAndError(query)[1];
   }

   @Override
   public double getConfidenceResponseTime(int transactionalClass) {
      if (transactionalClass == 0) {
         if (replicationProtocol.compareTo(ReplicationProtocol.TWOPC) == 0)
            morphr.initiateCubist(modelFilenameReadOnly2PC);
         else if (replicationProtocol.compareTo(ReplicationProtocol.PB) == 0)
            morphr.initiateCubist(modelFilenameReadOnlyPB);
         else
            morphr.initiateCubist(modelFilenameReadOnlyTO);
      } else {
         if (replicationProtocol.compareTo(ReplicationProtocol.TWOPC) == 0)
            morphr.initiateCubist(modelFilenameWrite2PC);
         else if (replicationProtocol.compareTo(ReplicationProtocol.PB) == 0)
            morphr.initiateCubist(modelFilenameWritePB);
         else
            morphr.initiateCubist(modelFilenameWriteTO);
      }
      return morphr.getPredictionAndError(query)[1];
   }

   private ReplicationProtocol extractReplicationProtocol(InputOracle input) {
      return ((ReplicationProtocol) input.getForecastParam(ForecastParam.ReplicationProtocol));
   }

   private double txClassPercentage(int clazz) {
      double wr = ((Number) input.getParam(Param.PercentageSuccessWriteTransactions)).doubleValue();
      log.trace("Asking for percentage of class " + clazz);
      double ret = -1;
      if (clazz == 0)
         ret = 1 - wr;
      if (clazz == 1)
         ret = wr;
      log.trace("Returning " + ret);
      if (ret != -1)
         return ret;
      throw new IllegalArgumentException("Percentage for transactional class with id " + clazz + " is not available with param " + Param.PercentageSuccessWriteTransactions);
   }
}
