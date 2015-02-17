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
package com.effektif.workflow.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.model.TypedValue;
import com.effektif.workflow.api.types.Type;
import com.effektif.workflow.api.workflow.Binding;
import com.effektif.workflow.api.workflow.Element;
import com.effektif.workflow.api.workflow.MultiInstance;
import com.effektif.workflow.api.workflow.ParseIssue.IssueType;
import com.effektif.workflow.api.workflow.ParseIssues;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.impl.activity.InputParameter;
import com.effektif.workflow.impl.data.DataType;
import com.effektif.workflow.impl.data.DataTypeService;
import com.effektif.workflow.impl.data.TypedValueImpl;
import com.effektif.workflow.impl.json.SerializedWorkflow;
import com.effektif.workflow.impl.script.ExpressionService;
import com.effektif.workflow.impl.workflow.ActivityImpl;
import com.effektif.workflow.impl.workflow.BindingImpl;
import com.effektif.workflow.impl.workflow.MultiInstanceImpl;
import com.effektif.workflow.impl.workflow.ScopeImpl;
import com.effektif.workflow.impl.workflow.TransitionImpl;
import com.effektif.workflow.impl.workflow.WorkflowImpl;


/** Validates and wires process definition after it's been built by either the builder api or json deserialization. */
public class WorkflowParser {
  
  public static final String PROPERTY_LINE = "line";
  public static final String PROPERTY_COLUMN = "column";
  
  public static final Logger log = LoggerFactory.getLogger(WorkflowParser.class);
  
  public Configuration configuration;
  public WorkflowImpl workflow;
  public LinkedList<String> path;
  public ParseIssues issues;
  public Stack<ParseContext> contextStack;
  public Set<String> activityIds = new HashSet<>();
  public Set<String> variableIds = new HashSet<>();
  public Set<String> transitionIds = new HashSet<>();
  public boolean isSerialized;
  
  private class ParseContext {
    ParseContext(String property, Object element, Integer index) {
      this.property = property;
      this.element = element;
      
      String indexText = null;
      if (element instanceof Element) {
        indexText = ((Element)element).getId();
      }
      if (indexText==null && index!=null) {
        indexText = Integer.toString(index);
      }
    }
    Object element;
    String property;
    String index;
    public String toString() {
      if (index!=null) {
        return property+"["+index+"]";
      } else {
        return property;
      }
    }
    public Long getLine() {
      if (element instanceof Element) {
        Number line = (Number) ((Element)element).getProperty(PROPERTY_LINE);
        return line!=null ? line.longValue() : null;
      }
      return null;
    }
    public Long getColumn() {
      if (element instanceof Element) {
        Number column = (Number) ((Element)element).getProperty(PROPERTY_COLUMN);
        return column!=null ? column.longValue() : null;
      }
      return null;
    }
  }

  /** parses the content of workflowApi into workflowImpl and 
   * adds any parse issues to workflowApi.
   * Use one parser for each parse.
   * By returning the parser itself you can access the  */
  public static WorkflowParser parse(Configuration configuration, Workflow workflowApi) {
    WorkflowParser parser = new WorkflowParser(configuration);
    parser.pushContext("workflow", workflowApi, null);
    parser.workflow = new WorkflowImpl();
    parser.isSerialized = workflowApi instanceof SerializedWorkflow;
    parser.workflow.parse(workflowApi, parser);
    parser.popContext();
    return parser;
  }

  public WorkflowParser(Configuration configuration) {
    this.configuration = configuration;
    this.path = new LinkedList<>();
    this.contextStack = new Stack<>();
    this.issues = new ParseIssues();
  }

  public void pushContext(String property, Object element, Integer index) {
    this.contextStack.push(new ParseContext(property, element, index));
  }
  
  public void popContext() {
    this.contextStack.pop();
  }
  
  protected String getPathText() {
    StringBuilder pathText = new StringBuilder();
    String dot = null;
    for (ParseContext validationContext: contextStack) {
      if (dot==null) {
        dot = ".";
      } else {
        pathText.append(dot);
      }
      pathText.append(validationContext.toString());
    }
    return pathText.toString();
  }

  public String getExistingActivityIdsText(ScopeImpl scope) {
    List<Object> activityIds = new ArrayList<>();
    if (scope.activities!=null) {
      for (ActivityImpl activity: scope.activities.values()) {
        if (activity.id!=null) {
          activityIds.add(activity.id);
        }
      }
    }
    return (!activityIds.isEmpty() ? "Should be one of "+activityIds : "No activities defined in this scope");
  }

//  public Map<String, BindingImpl> parseInputBindings(Map<String, Binding> inputBindings, Activity activityApi, Map<String, InputParameter> inputParameters) {
//    return parseInputBindings(inputBindings, activityApi, inputParameters, false);
//  }
//  
//  public Map<String, BindingImpl> parseInputBindings(Map<String, Binding> inputBindings, Activity activityApi, Map<String, InputParameter> inputParameters, boolean deserialize) {
//  }

  public <T> BindingImpl<T> parseBinding(Binding<T> binding, InputParameter inputParameter) {
    return parseBinding(binding, inputParameter, false);
  }

  public <T> BindingImpl<T> parseBinding(Binding<T> binding, InputParameter inputParameter, boolean deserialize) {
    Type type = inputParameter.getType();
    boolean isRequired = inputParameter!=null && inputParameter.isRequired();
    String bindingName = inputParameter.getKey();
    return parseBinding(binding, type, isRequired, bindingName, deserialize);
  }

  public <T> BindingImpl<T> parseBinding(Binding<T> binding, Type type, boolean isRequired, String bindingName, boolean deserialize) {
    BindingImpl<T> bindingImpl = parseBinding(binding, type, deserialize);
    int values = 0;
    if (bindingImpl!=null) {
      if (bindingImpl.value!=null) values++;
      if (bindingImpl.variableId!=null) values++;
      if (bindingImpl.expression!=null) values++;
      if (bindingImpl.bindings!=null) values++;
    }
    if (isRequired && values==0) {
      addError("Binding '%s' required and not specified", bindingName);
    } else if (values>1) {
      addWarning("Multiple values specified for binding '%s'", bindingName);
    }
    return bindingImpl;
  }

  public <T> BindingImpl<T> parseBinding(Binding<T> binding, Type type, boolean deserialize) {
    if (binding==null) {
      return null;
    }
    BindingImpl<T> bindingImpl = new BindingImpl<>(configuration);
    if (binding.getValue()!=null) {
      bindingImpl.value = binding.getValue();
      if (deserialize && isSerialized) {
        DataTypeService dataTypeService = configuration.get(DataTypeService.class);
        DataType dataType = dataTypeService.createDataType(type);
        bindingImpl.value = (T) dataType.convertJsonToInternalValue(bindingImpl.value);
      }
    }
    if (binding.getVariableId()!=null) {
      // TODO check if the variable exists and add an error if not
      bindingImpl.variableId = binding.getVariableId();
    }
    if (binding.getFields()!=null) {
      // TODO check if the fields exist and add errors if not
      bindingImpl.fields = binding.getFields(); 
    }
    if (binding.getExpression()!=null) {
      ExpressionService expressionService = configuration.get(ExpressionService.class);
      try {
        bindingImpl.expression = expressionService.compile(binding.getExpression(), this);
      } catch (Exception e) {
        addError("Expression '%s' couldn't be compiled: %s", binding.getExpression(), e.getMessage());
      }
    }
    List<Binding<T>> bindings = binding.getBindings();
    if (bindings!=null && !bindings.isEmpty()) {
      bindingImpl.bindings = new ArrayList<>();
      for (Binding<T> elementBinding: bindings) {
        BindingImpl<T> elementBindingImpl = parseBinding(elementBinding, type, deserialize);
        bindingImpl.bindings.add(elementBindingImpl);
      }
    }
    return bindingImpl;
  }

  protected TypedValueImpl parseTypedValue(TypedValue typedValue) {
    if (typedValue==null) {
      return null;
    }
    DataTypeService dataTypeService = configuration.get(DataTypeService.class);
    DataType type = dataTypeService.createDataType(typedValue.getType());
    return new TypedValueImpl(type, typedValue.getValue());
  }

  public void addError(String message, Object... messageArgs) {
    ParseContext currentContext = contextStack.peek();
    issues.addIssue(IssueType.error, getPathText(), currentContext.getLine(), currentContext.getColumn(), message, messageArgs);
  }

  public void addWarning(String message, Object... messageArgs) {
    ParseContext currentContext = contextStack.peek();
    issues.addIssue(IssueType.warning, getPathText(), currentContext.getLine(), currentContext.getColumn(), message, messageArgs);
  }
  
  public ParseIssues getIssues() {
    return issues;
  }

  public WorkflowParser checkNoErrors() {
    issues.checkNoErrors();
    return this;
  }

  public WorkflowParser checkNoErrorsAndNoWarnings() {
    issues.checkNoErrorsAndNoWarnings();
    return this;
  }

  public boolean hasErrors() {
    return issues.hasErrors();
  }

  public WorkflowImpl getWorkflow() {
    return workflow;
  }

  public <T> T getConfiguration(Class<T> type) {
    return configuration.get(type);
  }

  public List<ActivityImpl> getStartActivities(ScopeImpl scope) {
    if (scope.activities==null) {
      return null;
    }
    List<ActivityImpl> startActivities = new ArrayList<>(scope.activities.values());
    if (scope.transitions!=null) {
      for (TransitionImpl transition: scope.transitions) {
        startActivities.remove(transition.to);
      }
    }
    if (startActivities.isEmpty()) {
      this.addWarning("No start activities in %s", scope.id);
    }
    return startActivities;
  }

  public MultiInstanceImpl parseMultiInstance(MultiInstance multiInstance) {
    if (multiInstance==null) {
      return null;
    }
    MultiInstanceImpl multiInstanceImpl = new MultiInstanceImpl();
    pushContext("multiInstance", multiInstance, null);
    multiInstanceImpl.parse(multiInstance, this);
    popContext();
    return multiInstanceImpl;
  }
}
