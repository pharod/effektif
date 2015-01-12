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
package com.effektif.mongo;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import com.effektif.workflow.api.WorkflowEngine;
import com.effektif.workflow.impl.WorkflowEngineConfiguration;
import com.effektif.workflow.impl.job.JobType;
import com.effektif.workflow.impl.plugin.ActivityType;
import com.effektif.workflow.impl.type.DataType;
import com.effektif.workflow.impl.util.Lists;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;


public class MongoWorkflowEngineConfiguration extends WorkflowEngineConfiguration {

  public static List<ServerAddress> DEFAULT_SERVER_ADDRESSES = Lists.of(createServerAddress("localhost", null));
  
  public static class FieldNames {
    public WorkflowFields workflow = new WorkflowFields(); 
    public WorkflowVersionsFields workflowVersions = new WorkflowVersionsFields(); 
    public WorkflowVersionsLockFields workflowVersionsLock = new WorkflowVersionsLockFields(); 
    public WorkflowInstanceFields workflowInstance = new WorkflowInstanceFields(); 
    public JobFields job = new JobFields(); 
    public JobExecutionFields jobExecution = new JobExecutionFields(); 
  }
  
  public static class WorkflowFields {
    public String _id = "_id";
    public String name = "name";
    public String deployedTime = "deployedTime";
    public String deployedBy = "deployedBy";
    public String organizationId = "organizationId";
    public String workflowId = "workflowId";
    public String version = "version";
  }

  public static class WorkflowVersionsFields {
    public String _id = "_id";
    public String workflowName = "workflowName";
    public String versionIds = "versionIds";
    public String lock = "lock";
  }

  public static class WorkflowVersionsLockFields {
    public String owner = "owner";
    public String time = "time";
  }

  public static class WorkflowInstanceFields {
    public String _id = "_id";
    public String organizationId = "organizationId";
    public String workflowId = "workflowId";
    public String start = "start";
    public String end = "end";
    public String duration = "duration";
    public String activityInstances = "activities";
    public String archivedActivityInstances = "archivedActivities";
    public String variableInstances = "variables";
    public String parent = "parent";
    public String variableId = "variableId";
    public String value = "value";
    public String activityId = "activityId";
    public String lock = "lock";
    public String time = "time";
    public String owner= "owner";
    public String updates = "updates";
    public String workState = "workState";
    public String work = "work";
    public String workAsync = "workAsync";
    public String callerWorkflowInstanceId = "callerWorkflowInstanceId";
    public String callerActivityInstanceId = "callerActivityInstanceId";
    public String calledWorkflowInstanceId = "calledWorkflowInstanceId";
  }

  public static class JobFields {
    public String _id = "_id";
    public String key = "key";
    public String duedate = "duedate";
    public String lock = "lock";
    public String executions= "executions";
    public String retries = "retries";
    public String retryDelay = "retryDelay";
    public String done = "done";
    public String dead = "dead";
    public String organizationId = "organizationId";
    public String processId = "processId";
    public String workflowId = "workflowId";
    public String workflowInstanceId = "workflowInstanceId";
    public String lockWorkflowInstance = "lockWorkflowInstance";
    public String activityInstanceId = "activityInstanceId";
    public String taskId = "taskId";
    public String error = "error";
    public String logs = "logs";
    public String time = "time";
    public String duration = "duration";
    public String owner = "owner";
    public String jobType = "jobType";
  }
  
  public static class JobExecutionFields {
    
  }

  
  protected List<ServerAddress> serverAddresses;
  protected String databaseName = "effektif";
  protected List<MongoCredential> credentials;
  protected String workflowsCollectionName = "workflows";
  protected String workflowInstancesCollectionName = "workflowInstances";
  protected String jobsCollectionName = "jobs";
  protected boolean isPretty = true;
  protected MongoClientOptions.Builder optionBuilder = new MongoClientOptions.Builder();
  protected FieldNames fieldNames = new FieldNames();
  protected WriteConcern writeConcernInsertWorkflow;
  protected WriteConcern writeConcernInsertWorkflowInstance;
  protected WriteConcern writeConcernFlushUpdates;
  protected WriteConcern writeConcernJobs;
  
  public MongoWorkflowEngineConfiguration() {
    registerService(new MongoWorkflowStore());
    registerService(new MongoWorkflowInstanceStore());
    registerService(new MongoTaskService());
    registerService(new MongoJobs());
  }
  
  @Override
  public WorkflowEngine buildWorkflowEngine() {
    MongoClient mongoClient = new MongoClient(
            getServerAddresses(), 
            getCredentials(), 
            getOptionBuilder().build());
    registerService(mongoClient);
    
    DB db = mongoClient.getDB(getDatabaseName());
    registerService(db);
    
    return super.buildWorkflowEngine();
  }



  public MongoWorkflowEngineConfiguration server(String host) {
    if (serverAddresses==null) {
      serverAddresses = new ArrayList<>();
    }
    serverAddresses.add(createServerAddress(host, null));
    return this;
  }

  public MongoWorkflowEngineConfiguration server(String host, int port) {
    if (serverAddresses==null) {
      serverAddresses = new ArrayList<>();
    }
    serverAddresses.add(createServerAddress(host, port));
    return this;
  }

  protected static ServerAddress createServerAddress(String host, Integer port) {
    try {
      if (port!=null) {
        return new ServerAddress(host, port);
      }
      return new ServerAddress(host);
    } catch (UnknownHostException e) {
      throw new RuntimeException(e);
    }
  }
  
  public List<ServerAddress> getServerAddresses() {
    return serverAddresses!=null ? serverAddresses : DEFAULT_SERVER_ADDRESSES;
  }

  public MongoWorkflowEngineConfiguration authentication(String userName, String database, char[] password) {
    if (credentials==null) {
      credentials = new ArrayList<>();
    }
    credentials.add(MongoCredential.createMongoCRCredential(userName, database, password));
    return this;
  }
  
  public FieldNames getFieldNames() {
    return fieldNames;
  }
  
  public void setFieldNames(FieldNames fieldNames) {
    this.fieldNames = fieldNames;
  }

  public MongoWorkflowEngineConfiguration writeConcernInsertProcessDefinition(WriteConcern writeConcernInsertProcessDefinition) {
    this.writeConcernInsertWorkflow = writeConcernInsertProcessDefinition;
    return this;
  }

  public MongoWorkflowEngineConfiguration writeConcernInsertProcessInstance(WriteConcern writeConcernInsertProcessInstance) {
    this.writeConcernInsertWorkflowInstance = writeConcernInsertProcessInstance;
    return this;
  }

  public MongoWorkflowEngineConfiguration writeConcernFlushUpdates(WriteConcern writeConcernFlushUpdates) {
    this.writeConcernFlushUpdates = writeConcernFlushUpdates;
    return this;
  }
  
  public MongoWorkflowEngineConfiguration writeConcernJobs(WriteConcern writeConcernJobs) {
    this.writeConcernJobs = writeConcernJobs;
    return this;
  }
  
  public void workflowInstancesCollectionName(String processInstancesCollectionName) {
    this.workflowInstancesCollectionName = processInstancesCollectionName;
  }

  public void workflowsCollectionName(String workflowsCollectionName) {
    this.workflowsCollectionName = workflowsCollectionName;
  }

  public void jobsCollectionName(String jobsCollectionName) {
    this.jobsCollectionName = jobsCollectionName;
  }

  @Override
  public MongoWorkflowEngineConfiguration id(String id) {
    super.id(id);
    return this;
  }
  
  @Override
  public MongoWorkflowEngineConfiguration registerService(Object service) {
    super.registerService(service);
    return this;
  }

  @Override
  public MongoWorkflowEngineConfiguration registerJavaBeanType(Class< ? > javaBeanType) {
    super.registerJavaBeanType(javaBeanType);
    return this;
  }

  @Override
  public MongoWorkflowEngineConfiguration registerActivityType(ActivityType activityType) {
    super.registerActivityType(activityType);
    return this;
  }

  @Override
  public MongoWorkflowEngineConfiguration registerDataType(DataType dataType) {
    super.registerDataType(dataType);
    return this;
  }
  
  @Override
  public MongoWorkflowEngineConfiguration registerJobType(Class< ? extends JobType> jobTypeClass) {
    super.registerJobType(jobTypeClass);
    return this;
  }

  public void setServerAddresses(List<ServerAddress> serverAddresses) {
    this.serverAddresses = serverAddresses;
  }
  
  public String getDatabaseName() {
    return databaseName;
  }
  
  public void setDatabaseName(String databaseName) {
    this.databaseName = databaseName;
  }
  
  public List<MongoCredential> getCredentials() {
    return credentials;
  }
  
  public void setCredentials(List<MongoCredential> credentials) {
    this.credentials = credentials;
  }

  public void setWriteConcernInsertWorkflow(WriteConcern writeConcernInsertProcessDefinition) {
    this.writeConcernInsertWorkflow = writeConcernInsertProcessDefinition;
  }
  
  public void setWriteConcernInsertWorkflowInstance(WriteConcern writeConcernInsertWorkflowInstance) {
    this.writeConcernInsertWorkflowInstance = writeConcernInsertWorkflowInstance;
  }
  
  public void setWriteConcernJobs(WriteConcern writeConcernJobs) {
    this.writeConcernJobs = writeConcernJobs;
  }

  public void setWriteConcernFlushUpdates(WriteConcern writeConcernFlushUpdates) {
    this.writeConcernFlushUpdates = writeConcernFlushUpdates;
  }
  
  public String getWorkflowInstancesCollectionName() {
    return workflowInstancesCollectionName;
  }
  
  public void setWorkflowInstancesCollectionName(String processInstancesCollectionName) {
    this.workflowInstancesCollectionName = processInstancesCollectionName;
  }
 
  public String getWorkflowsCollectionName() {
    return workflowsCollectionName;
  }

  public void setWorkflowsCollectionName(String processDefinitionsCollectionName) {
    this.workflowsCollectionName = processDefinitionsCollectionName;
  }
  
  public String getJobsCollectionName() {
    return jobsCollectionName;
  }
  
  public void setJobsCollectionName(String jobsCollectionName) {
    this.jobsCollectionName = jobsCollectionName;
  }

  public boolean isPretty() {
    return isPretty;
  }
  
  public void setPretty(boolean isPretty) {
    this.isPretty = isPretty;
  }
  
  public void setOptionBuilder(MongoClientOptions.Builder optionBuilder) {
    this.optionBuilder = optionBuilder;
  }

  public MongoClientOptions.Builder getOptionBuilder() {
    return optionBuilder;
  }
  
  public WriteConcern getWriteConcernInsertWorkflow(DBCollection dbCollection) {
    return getWriteConcern(dbCollection, writeConcernInsertWorkflow);
  }
  
  public WriteConcern getWriteConcernInsertWorkflowInstance(DBCollection dbCollection) {
    return getWriteConcern(dbCollection, writeConcernInsertWorkflowInstance);
  }
  
  public WriteConcern getWriteConcernJobs(DBCollection dbCollection) {
    return getWriteConcern(dbCollection, writeConcernJobs);
  }

  public WriteConcern getWriteConcernFlushUpdates(DBCollection dbCollection) {
    return getWriteConcern(dbCollection, writeConcernFlushUpdates);
  }
  
  public static WriteConcern getWriteConcern(DBCollection dbCollection, WriteConcern configuredWriteConcern) {
    return configuredWriteConcern!=null ? configuredWriteConcern : dbCollection.getWriteConcern();
  }
}