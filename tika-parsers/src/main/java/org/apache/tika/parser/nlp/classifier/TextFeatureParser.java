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
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AbstractParser;
import org.apache.tika.parser.ParseContext;
import org.xml.sax.ContentHandler;

import edu.usc.ir.nlp.classifier.AgeClassifier;
import edu.usc.ir.nlp.classifier.AgePredictorResponse;

/**
 * Parser for extracting features from text. Below features are extracted <br/>
 * <li>Author Age</li>
 */
public class TextFeatureParser extends AbstractParser {

	private static final long serialVersionUID = 1108439049093046832L;

	private static final Logger LOG = Logger.getLogger(TextFeatureParser.class.getName());

	public static final String MD_KEY_ESTIMATED_AGE_RANGE = "Estimated-Author-Age-Range";
	public static final String MD_KEY_ESTIMATED_AGE = "Estimated-Author-Age";

	private static AgeClassifier ageClassifier;
    private TextFeatureParserConfig config = new TextFeatureParserConfig();

	private static final MediaType MEDIA_TYPE = MediaType.TEXT_PLAIN;
	private static final Set<MediaType> SUPPORTED_TYPES = Collections.singleton(MEDIA_TYPE);
	public Tika secondaryParser;
	private static volatile boolean available = false;

	public TextFeatureParser() {
		try {
			secondaryParser = new Tika(new TikaConfig());
			available = true;
		} catch (Exception e) {
			available = false;
			LOG.log(Level.SEVERE, "Unable to initialize secondary parser");
		}
	}

	@Override
	public Set<MediaType> getSupportedTypes(ParseContext parseContext) {
		return SUPPORTED_TYPES;
	}

	/**
	 * USED in test cases to mock response of AgeClassifier
	 */
	protected static void setAgePredictorClient(AgeClassifier ageClassifier) {
		if (TextFeatureParser.ageClassifier == null) {
			TextFeatureParser.ageClassifier = ageClassifier;
		}
	}

	public AgeClassifier getAgePredictorClient() {
		if (ageClassifier == null) {
			ageClassifier = new AgeClassifier(config.getAgeRestEndpoint());
		}
		return ageClassifier;
	}

	@Override
	public void parse(InputStream inputStream, ContentHandler handler, Metadata metadata, ParseContext context) throws IOException {
		/**
		 * Check Availability of Age-Predictor. Unavailability could be due to Age-Predictor server not started 
		 */
		this.config = context.get(TextFeatureParserConfig.class, config);
		if (!available) {
			LOG.log(Level.SEVERE, "Parser Unavailable, check your configuration");
			return;
		}
		
		/**
		 * If content is not plain text use Tika to extract text out of content. 
		 */
		Reader reader;
		if (MediaType.TEXT_PLAIN.toString().equals(metadata.get(Metadata.CONTENT_TYPE))) {
			reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
		} else {
			reader = secondaryParser.parse(inputStream);
		}
		
		/**
		 * Call AgeClassifier to get predicted Age
		 */
		try {
			AgePredictorResponse predictAuthorAge = getAgePredictorClient().predictAuthorAge(IOUtils.toString(reader));
			
			int predictedAge = predictAuthorAge.getPredictedAge();
			String predictedAgeRange = predictAuthorAge.getPredictedAgeRange();
			
			metadata.add(MD_KEY_ESTIMATED_AGE, Integer.toString(predictedAge) );
			if (predictedAgeRange != null && !predictedAgeRange.trim().isEmpty()) {
				metadata.add(MD_KEY_ESTIMATED_AGE_RANGE, predictedAgeRange);
			}
		} catch (Exception e) {
			LOG.log(Level.SEVERE, "Age Predcitor server not avaialble. Tried with URL {0}",config.getAgeRestEndpoint());
			return;
		}
		
	}

}
