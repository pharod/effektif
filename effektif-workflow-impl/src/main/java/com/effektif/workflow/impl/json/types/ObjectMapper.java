/* Copyright (c) 2014, Effektif GmbH.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. */
package com.effektif.workflow.impl.json.types;

import java.lang.reflect.Type;
import java.util.Map;

import com.effektif.workflow.impl.json.JsonReader;
import com.effektif.workflow.impl.json.JsonTypeMapper;
import com.effektif.workflow.impl.json.JsonWriter;


/**
 * Maps a JavaBean to a {@link Map} field for JSON serialisation and deserialisation.
 *
 * @author Tom Baeyens
 */
public class ObjectMapper<T extends Object> implements JsonTypeMapper<T> {

  public static final ObjectMapper INSTANCE = new ObjectMapper();

  @Override
  public Class<T> getMappedClass() {
    return (Class<T>) Object.class;
  }

  @Override
  public T read(Object jsonValue, Type type, JsonReader jsonReader) {
    return (T) jsonReader.readObject(jsonValue, type);
  }

  @Override
  public void write(T objectValue, JsonWriter jsonWriter) {
    jsonWriter.writeObject(objectValue);
  }
}
