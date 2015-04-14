/*
 * Copyright 2014 Effektif GmbH.
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
 * limitations under the License.
 */
package com.effektif.workflow.api.form;

import com.effektif.workflow.api.mapper.JsonReadable;
import com.effektif.workflow.api.mapper.JsonReader;
import com.effektif.workflow.api.mapper.JsonWritable;
import com.effektif.workflow.api.mapper.JsonWriter;




/**
 * @author Tom Baeyens
 */
public class AbstractForm implements JsonReadable, JsonWritable {

  protected String description;
  // TODO Map<String,String> descriptionI18n;

  public AbstractForm() {
  }
  
  /** shallow copy constructor */
  public AbstractForm(AbstractForm other) {
    this.description = other.description;
  }
  
  @Override
  public void readJson(JsonReader r) {
    description = r.readObject("description");
  }

  @Override
  public void writeJson(JsonWriter w) {
    w.writeString("description", description);
  }

  public String getDescription() {
    return this.description;
  }
  public void setDescription(String description) {
    this.description = description;
  }
  public AbstractForm description(String description) {
    this.description = description;
    return this;
  }
}
