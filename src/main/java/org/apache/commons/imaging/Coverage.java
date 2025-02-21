/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

 package org.apache.commons.imaging;

 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.HashMap;
 
 public class Coverage {
 
     public static final int numBranches=74; //num of branches in the function
     private static String filepath="coverage.txt";
 
     public static final HashMap<Integer, Boolean> coverageMap = new HashMap<>();
 
     // runs one instance in the beginning
     static{
         for (int i= 1; i <= numBranches; i++) { 
             coverageMap.put(i,false); 
         }
     }
 
     /**
      * Calculate coverage and write report 
      */
     public static void generateCoverageReport() {
 
         try (FileWriter writer = new FileWriter(filepath)) {
             writer.write("Branch Coverage Report:\n");
             writer.write("Function preprocess in BasicCParser:\n");
             int numBranchesCovered=0;
             for (Integer branchNum : coverageMap.keySet()) {
                 boolean taken= coverageMap.get(branchNum);
                 if (taken){
                     numBranchesCovered+=1; 
                 }
                 
                 writer.write("branch "+ branchNum + (taken? " taken \n": " not taken\n" ));
 
             }
             double branchCoveragePercentage=((double)numBranchesCovered/numBranches)*100; //calculte coverage
             writer.write("Final branch coverage is "+ branchCoveragePercentage + " for function preprocess");
         } catch (IOException e) {
             e.printStackTrace();
          }   
     }
 }