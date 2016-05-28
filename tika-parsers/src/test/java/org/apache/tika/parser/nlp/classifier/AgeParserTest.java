/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tika.parser.nlp.classifier;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;

import org.apache.tika.Tika;
import org.apache.tika.TikaTest;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.CompositeParser;
import org.junit.Assert;
import org.junit.Test;

import edu.usc.ir.nlp.classifier.AgeClassifier;

public class AgeParserTest extends TikaTest {

    private static final String CONFIG_FILE = "tika-config.xml";
    private static final String TEST_TEXT = "I am student at University of Southern California (USC)," +
            " located in Los Angeles . USC's football team is called by name Trojans." +
            " Mr. John McKay was a head coach of the team from 1960 - 1975";
    private static final String TEST_AGE = "25-27";
    
    static{
    	/**
    	 * Injecting mock AgeClassifer into AgeParser to generate test response
    	 */
    	AgeClassifier mockAgeClassifier = mock(AgeClassifier.class);
    	
		when(mockAgeClassifier.predictAuthorAge(TEST_TEXT)).thenReturn(TEST_AGE);
    	AgeParser.setAgePredictorClient(mockAgeClassifier);
    }
    @Test
    public void testAgeParser() throws Exception {

        //test config is added to resources directory
        TikaConfig config = new TikaConfig(getClass().getResourceAsStream(CONFIG_FILE));
        Tika tika = new Tika(config);
        
        Metadata md = new Metadata();
        tika.parse(new ByteArrayInputStream(TEST_TEXT.getBytes(Charset.defaultCharset())), md);
        System.out.println(md);
        Assert.assertArrayEquals("Age Parser not invoked.",new String[] {CompositeParser.class.getCanonicalName(), 
        		AgeParser.class.getCanonicalName()} , md.getValues("X-Parsed-By"));
        Assert.assertArrayEquals("Wrong Age predicted.", new String[] {TEST_AGE} , md.getValues(AgeParser.MD_KEY_ESTIMATED_AGE));
        
        
    }

}