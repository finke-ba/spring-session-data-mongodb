/*
 * Copyright 2014-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.session.data.mongo;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;

import org.assertj.core.api.Assertions;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.ReflectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.DBObject;

/**
 * @author Jakub Kubrynski
 * @author Greg Turnquist
 */
public class JacksonMongoSessionConverterTest extends AbstractMongoSessionConverterTest {

	JacksonMongoSessionConverter mongoSessionConverter = new JacksonMongoSessionConverter();

	@Override
	AbstractMongoSessionConverter getMongoSessionConverter() {
		return this.mongoSessionConverter;
	}

	@Test
	public void shouldSaveIdField() {

		// given
		MongoSession session = new MongoSession();

		// when
		DBObject convert = this.mongoSessionConverter.convert(session);

		// then
		assertThat(convert.get("_id")).isEqualTo(session.getId());
		assertThat(convert.get("id")).isNull();
	}

	@Test
	public void shouldQueryAgainstAttribute() throws Exception {

		// when
		Query cart = this.mongoSessionConverter.getQueryForIndex("cart", "my-cart");

		// then
		assertThat(cart.getQueryObject().get("attrs.cart")).isEqualTo("my-cart");
	}

	@Test
	public void shouldAllowCustomObjectMapper() {

		// given
		ObjectMapper myMapper = new ObjectMapper();

		// when
		JacksonMongoSessionConverter converter = new JacksonMongoSessionConverter(myMapper);

		// then
		Field objectMapperField = ReflectionUtils.findField(JacksonMongoSessionConverter.class, "objectMapper");
		ReflectionUtils.makeAccessible(objectMapperField);
		ObjectMapper converterMapper = (ObjectMapper) ReflectionUtils.getField(objectMapperField, converter);

		assertThat(converterMapper).isEqualTo(myMapper);
	}

	@Test
	public void shouldNotAllowNullObjectMapperToBeInjected() {

		Assertions.assertThatIllegalArgumentException().isThrownBy(() -> {
			new JacksonMongoSessionConverter((ObjectMapper) null);
		});
	}

	@Test
	public void shouldSaveExpireAtAsDate() {

		//given
		MongoSession session = new MongoSession();

		//when
		DBObject convert = this.mongoSessionConverter.convert(session);

		//then
		assertThat(convert.get("expireAt")).isInstanceOf(Date.class);
		assertThat(convert.get("expireAt")).isEqualTo(session.getExpireAt());
	}

	@Test
	public void shouldLoadExpireAtFromDocument() {

		// given
		Date now = new Date();
		HashMap<String, Object> data = new HashMap<>();

		data.put("expireAt", now);
		data.put("@class", MongoSession.class.getName());
		data.put("_id", new ObjectId().toString());
		
		Document document = new Document(data);

		// when
		MongoSession convertedSession = this.mongoSessionConverter.convert(document);

		// then
		assertThat(convertedSession).isNotNull();
		assertThat(convertedSession.getExpireAt()).isEqualTo(now);
	}

}
