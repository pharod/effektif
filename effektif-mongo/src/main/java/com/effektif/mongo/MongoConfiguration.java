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

import com.effektif.workflow.impl.configuration.DefaultConfiguration;
import com.effektif.workflow.impl.memory.MemoryTaskService;
import com.effektif.workflow.impl.util.Lists;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;


public class MongoConfiguration extends DefaultConfiguration {

  public static List<ServerAddress> DEFAULT_SERVER_ADDRESSES = Lists.of(createServerAddress("localhost", null));
  
  protected List<ServerAddress> serverAddresses;
  protected String databaseName = "effektif";
  protected List<MongoCredential> credentials;
  protected String workflowsCollectionName = "workflows";
  protected String workflowInstancesCollectionName = "workflowInstances";
  protected String jobsCollectionName = "jobs";
  protected boolean isPretty;
  protected MongoClientOptions.Builder optionBuilder = new MongoClientOptions.Builder();
  protected WriteConcern writeConcernInsertWorkflow;
  protected WriteConcern writeConcernInsertWorkflowInstance;
  protected WriteConcern writeConcernFlushUpdates;
  protected WriteConcern writeConcernJobs;
  protected boolean storeWorkflowIdsAsStrings = false;

  public MongoConfiguration() {
    brewery.ingredient(this);
    brewery.supplier(new MongoClientFactory(), MongoClient.class);
    brewery.supplier(new MongoDbFactory(), DB.class);
    brewery.ingredient(new MongoWorkflowStore());
    brewery.ingredient(new MongoWorkflowInstanceStore());
    brewery.ingredient(new MemoryTaskService());
    brewery.ingredient(new MongoJobs());
  }
  
  public MongoConfiguration server(String host) {
    if (serverAddresses==null) {
      serverAddresses = new ArrayList<>();
    }
    serverAddresses.add(createServerAddress(host, null));
    return this;
  }

  public MongoConfiguration server(String host, int port) {
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

  public MongoConfiguration authentication(String userName, String database, char[] password) {
    if (credentials==null) {
      credentials = new ArrayList<>();
    }
    credentials.add(MongoCredential.createMongoCRCredential(userName, database, password));
    return this;
  }
  
  public MongoConfiguration writeConcernInsertProcessDefinition(WriteConcern writeConcernInsertProcessDefinition) {
    this.writeConcernInsertWorkflow = writeConcernInsertProcessDefinition;
    return this;
  }

  public MongoConfiguration writeConcernInsertProcessInstance(WriteConcern writeConcernInsertProcessInstance) {
    this.writeConcernInsertWorkflowInstance = writeConcernInsertProcessInstance;
    return this;
  }

  public MongoConfiguration writeConcernFlushUpdates(WriteConcern writeConcernFlushUpdates) {
    this.writeConcernFlushUpdates = writeConcernFlushUpdates;
    return this;
  }
  
  public MongoConfiguration writeConcernJobs(WriteConcern writeConcernJobs) {
    this.writeConcernJobs = writeConcernJobs;
    return this;
  }
  
  public MongoConfiguration workflowInstancesCollectionName(String processInstancesCollectionName) {
    this.workflowInstancesCollectionName = processInstancesCollectionName;
    return this;
  }

  public MongoConfiguration workflowsCollectionName(String workflowsCollectionName) {
    this.workflowsCollectionName = workflowsCollectionName;
    return this;
  }

  public MongoConfiguration jobsCollectionName(String jobsCollectionName) {
    this.jobsCollectionName = jobsCollectionName;
    return this;
  }
  
  public MongoConfiguration prettyPrint() {
    this.isPretty = true;
    return this;
  }
  
  @Override
  public MongoConfiguration id(String id) {
    super.id(id);
    return this;
  }
  
  public MongoConfiguration storeWorkflowIdsAsStrings() {
    this.storeWorkflowIdsAsStrings = true;
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

  public boolean getStoreWorkflowIdsAsString() {
    return this.storeWorkflowIdsAsStrings;
  }
  
  public void setStoreWorkflowIdsAsString(boolean storeWorkflowIdsAsStrings) {
    this.storeWorkflowIdsAsStrings = storeWorkflowIdsAsStrings;
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

  @Override
  public MongoConfiguration synchronous() {
    super.synchronous();
    return this;
  }

  public MongoConfiguration databaseName(String databaseName) {
    this.databaseName = databaseName;
    return this;
  }
}
