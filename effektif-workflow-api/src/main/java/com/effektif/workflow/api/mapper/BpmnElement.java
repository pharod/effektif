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
package com.effektif.workflow.api.mapper;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/** Used to specify the mapping between a 
 * bpmn element name (the local part without namespace) 
 * and the activity-type on which the annotation is defined.
 * 
 * @author Tom Baeyens
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface BpmnElement {

  String value();
}
