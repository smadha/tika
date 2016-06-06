/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright owlocationNameEntitieship.
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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;


/**
 * Stores URL for AgePredictor 
 */
public class TextFeatureParserConfig {

	private static final Logger LOG = Logger.getLogger(TextFeatureParserConfig.class.getName());
    private String ageRestEndpoint = null;

	public TextFeatureParserConfig() {
		init(this.getClass().getResourceAsStream("TextFeatureParserConfig.properties"));
	}
	
	/**
     * Initialize configurations from property files
     * @param stream InputStream for GeoTopicConfig.properties
     */
    private void init(InputStream stream) {
        if (stream == null) {
            return;
        }
        Properties props = new Properties();

        try {
            props.load(stream);
        } catch (IOException e) {
        	LOG.warning("GeoTopicConfig.properties not found in class path");
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ioe) {
                	LOG.severe("Unable to close stream: " + ioe.getMessage());
                }
            }
        }
        setAgeRestEndpoint(props.getProperty("age.rest.api", "http://localhost:5007"));
    }
    /**
     * @return REST endpoint for Age-Predictor
     */
    public String getAgeRestEndpoint() {
		return ageRestEndpoint;
	}
    /**
     * Configure REST endpoint for Age-Predictor
     * @param AgeRestEndpoint
     */
    public void setAgeRestEndpoint(String AgeRestEndpoint) {
		this.ageRestEndpoint = AgeRestEndpoint;
	}
}
