/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2011 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.transit_data_federation.bundle;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import org.onebusaway.collections.CollectionsLibrary;
import org.onebusaway.container.ContainerLibrary;
import org.onebusaway.container.spring.PropertyOverrideConfigurer;
import org.onebusaway.transit_data_federation.bundle.model.GtfsBundle;
import org.onebusaway.transit_data_federation.bundle.model.StatusMessages;
import org.onebusaway.transit_data_federation.bundle.model.TaskDefinition;
import org.onebusaway.transit_data_federation.impl.DirectedGraph;
import org.onebusaway.transit_data_federation.services.FederatedTransitDataBundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * The primary method for building a new federated transit data bundle, which is
 * the collection of resources and optimized data structures necessary to power
 * a federated transit data bundle instance. The bundle is constructed from a
 * number of input resources, including {@link GtfsBundle} objects and street
 * network resources.
 * 
 * While this class can be accessed directly, it is mostly designed to be run
 * from the command line using the {@link FederatedTransitDataBundleCreatorMain}
 * runner.
 * 
 * Various stages of the bundle building process can be conditionally enabled
 * and disabled by passing {@link Stages} arguments to methods like
 * {@link #setStageToSkip(Stages)}. All stages are enabled by default.
 * 
 * The build process is configured using Spring and additional context config
 * paths can be specified to add to the Spring container (see
 * {@link #setContextPaths(List)}). The core config is kept in the resource:
 * 
 * {@value #BUNDLE_RESOURCE}
 * 
 * @author bdferris
 * @see FederatedTransitDataBundleCreatorMain
 */
public class FederatedTransitDataBundleCreator {

  private static final String BUNDLE_RESOURCE = "classpath:org/onebusaway/transit_data_federation/bundle/application-context-bundle-creator.xml";

  private ConfigurableApplicationContext _context;

  private List<String> _contextPaths = new ArrayList<String>();

  private Map<String, BeanDefinition> _contextBeans = new HashMap<String, BeanDefinition>();

  private Properties _additionalBeanPropertyOverrides;

  private File _outputPath;

  private Set<String> _skipTasks = new HashSet<String>();

  private Set<String> _includeTasks = new HashSet<String>();

  private Set<String> _onlyTasks = new HashSet<String>();

  private String _skipToTask;

  private boolean _randomizeCacheDir = false;

  private String _bundleKey;
  
  private StatusMessages _status = new StatusMessages();

  /**
   * 
   * @param context
   */
  @Autowired
  public void setContext(ConfigurableApplicationContext context) {
    _context = context;
  }

  /**
   * Additional context paths that will be added when constructing the Spring
   * container that controls the build process. See
   * {@link ContainerLibrary#createContext(Iterable)}.
   * 
   * @param contextPaths additional Spring context paths to add to the container
   */
  public void setContextPaths(List<String> paths) {
    _contextPaths = paths;
  }

  public void setContextBeans(Map<String, BeanDefinition> contextBeans) {
    _contextBeans = contextBeans;
  }

  /**
   * 
   * @param outputPath the output path of the bundle
   */
  public void setOutputPath(File outputPath) {
    _outputPath = outputPath;
  }

  public void setBundleKey(String key) {
    _bundleKey = key;
  }

  public void addTaskToOnlyRun(String onlyTask) {
    _onlyTasks.add(onlyTask);
  }

  public void addTaskToSkip(String taskToSkip) {
    _skipTasks.add(taskToSkip);
  }

  public void addTaskToInclude(String taskToInclude) {
    _includeTasks.add(taskToInclude);
  }

  public void setSkipToTask(String taskName) {
    _skipToTask = taskName;
  }

  public void setRandomizeCacheDir(boolean randomizeCacheDir) {
    _randomizeCacheDir = randomizeCacheDir;
  }

  public void setAdditionalBeanPropertyOverrides(Properties props) {
    _additionalBeanPropertyOverrides = props;
  }
  
  public StatusMessages getStatusMessages() {
    return this._status;
  }

  /**
   * Build the bundle!
   * 
   * @throws IOException
   * @throws ClassNotFoundException
   * @throws UnknownTaskException
   */
  public void run() throws IOException, ClassNotFoundException,
      UnknownTaskException {

    _outputPath.mkdirs();

    setSystemProperties();

    ConfigurableApplicationContext context = _context;
    boolean closeContextOnCompletion = false;

    if (context == null) {
      List<String> contextPaths = getPrimaryApplicatonContextPaths();
      Map<String, BeanDefinition> contextBeans = getPrimaryBeanDefintions();
      context = ContainerLibrary.createContext(contextPaths, contextBeans);
      closeContextOnCompletion = true;
    }

    List<TaskDefinition> taskDefinitions = getTaskList(context);
    Set<String> taskNames = getReducedTaskList(taskDefinitions);

    // Clear cache files
    FederatedTransitDataBundle bundle = context.getBean(FederatedTransitDataBundle.class);
    clearExistingCacheFiles(bundle);
    int taskSize = taskNames.size();
    int i = 0;
    for (TaskDefinition def : taskDefinitions) {
      String taskName = def.getTaskName();
      if (taskNames.contains(taskName)) {
        i++;
        System.out.println("== " + taskName + " =====>");
        _status.addMessage("running task " + taskName + " (" + i + "/" + taskSize + ")");
        Runnable task = getTask(context, def.getTask(), def.getTaskBeanName());
        if (task == null)
          throw new IllegalStateException("unknown task bean with name: "
              + taskName);
        task.run();
      } else {
        Runnable task = getTask(context, def.getTaskWhenSkipped(),
            def.getTaskWhenSkippedBeanName());
        if (task != null) {
          System.out.println("== skipping " + taskName + " =====>");
          _status.addMessage("skipping task " + taskName);
          task.run();
        }
      }
    }

    // We don't need this context anymore
    if (closeContextOnCompletion) {
      context.stop();
      context.close();
      context = null;
    }
  }

  /****
   * Private Methods
   ****/

  private void setSystemProperties() {

    /**
     * Set the bundlePath system property. Will be used by "${bundle_path}"
     * references in the Spring config
     */
    System.setProperty("bundlePath", _outputPath.getAbsolutePath());

    String cacheDir = _outputPath.getAbsolutePath() + File.separator + "cache";
    if (_randomizeCacheDir)
      cacheDir += "-" + UUID.randomUUID().toString();
    System.setProperty("bundleCacheDir", cacheDir);

    boolean resetDatabase = isDatabaseResetNeeded();
    if (resetDatabase) {
      System.setProperty("hibernate.hbm2ddl.auto", "create");
    } else {
      System.setProperty("hibernate.hbm2ddl.auto", "update");
    }
  }

  private boolean isDatabaseResetNeeded() {
    if (_skipToTask != null)
      return "start".equals(_skipToTask);
    if (!_onlyTasks.isEmpty())
      return _onlyTasks.contains("start");
    if (_skipTasks.contains("start"))
      return false;
    return true;
  }

  private List<String> getPrimaryApplicatonContextPaths() {
    List<String> paths = new ArrayList<String>();
    paths.add(BUNDLE_RESOURCE);
    for (String contextPath : _contextPaths)
      paths.add(contextPath);
    return paths;
  }

  private Map<String, BeanDefinition> getPrimaryBeanDefintions() {
    Map<String, BeanDefinition> beans = new HashMap<String, BeanDefinition>();
    beans.putAll(_contextBeans);

    Properties p = new Properties();
    if (_bundleKey != null) {
      p.setProperty("bundle.key", _bundleKey);
    }

    if (_additionalBeanPropertyOverrides != null)
      p.putAll(_additionalBeanPropertyOverrides);

    BeanDefinitionBuilder propertyOverrides = BeanDefinitionBuilder.genericBeanDefinition(PropertyOverrideConfigurer.class);
    propertyOverrides.addPropertyValue("properties", p);
    beans.put("myCustomPropertyOverrides",
        propertyOverrides.getBeanDefinition());

    return beans;
  }

  private void clearExistingCacheFiles(FederatedTransitDataBundle bundle) {

    File cacheDir = bundle.getCachePath();

    if (!cacheDir.exists())
      return;

    File[] files = cacheDir.listFiles();
    if (files == null)
      return;

    for (File file : files) {
      if (file.isFile())
        file.delete();
    }
  }

  private Runnable getTask(ApplicationContext context, Runnable task,
      String taskBeanName) {

    if (task == null && taskBeanName != null) {
      task = context.getBean(taskBeanName, Runnable.class);
    }

    return task;
  }

  private List<TaskDefinition> getTaskList(ApplicationContext context)
      throws UnknownTaskException {

    Map<String, TaskDefinition> taskDefinitions = context.getBeansOfType(TaskDefinition.class);
    Map<String, TaskDefinition> taskDefinitionsByTaskName = getTaskDefinitionsByName(taskDefinitions);

    DirectedGraph<String> graph = new DirectedGraph<String>();

    for (TaskDefinition taskDefinition : taskDefinitions.values()) {

      String taskName = taskDefinition.getTaskName();

      graph.addNode(taskName);

      String before = taskDefinition.getBeforeTaskName();
      if (before != null) {
        if (!taskDefinitionsByTaskName.containsKey(before))
          throw new UnknownTaskException(before);
        graph.addEdge(taskName, before);
      }

      List<String> afters = taskDefinition.getAfterTaskNames();
      if (!CollectionsLibrary.isEmpty(afters)) {
        for (String after : afters) {
          if (!taskDefinitionsByTaskName.containsKey(after))
            throw new UnknownTaskException(after);
          graph.addEdge(after, taskName);
        }
      }
    }

    List<String> taskNames = graph.getTopologicalSort(null);

    List<TaskDefinition> taskDefinitionsInOrder = new ArrayList<TaskDefinition>();
    for (String taskName : taskNames) {
      TaskDefinition task = taskDefinitionsByTaskName.get(taskName);
      taskDefinitionsInOrder.add(task);
    }
    return taskDefinitionsInOrder;
  }

  private Map<String, TaskDefinition> getTaskDefinitionsByName(
      Map<String, TaskDefinition> taskDefinitions) {
    Map<String, TaskDefinition> taskDefinitionsByTaskName = new HashMap<String, TaskDefinition>();

    for (TaskDefinition taskDefinition : taskDefinitions.values()) {

      String taskName = taskDefinition.getTaskName();

      if (taskName == null || taskName.trim().length() == 0) {
        throw new IllegalStateException(
            "no taskName property defined for task definition: "
                + taskDefinition);
      }

      taskDefinitionsByTaskName.put(taskName, taskDefinition);

    }
    return taskDefinitionsByTaskName;
  }

  private Set<String> getReducedTaskList(List<TaskDefinition> taskDefinitions)
      throws UnknownTaskException {

    // Check task names first
    List<String> tasks = new ArrayList<String>();

    for (TaskDefinition taskDef : taskDefinitions) {
      if (!isTaskEnabled(taskDef))
        continue;
      String taskName = taskDef.getTaskName();
      tasks.add(taskName);
    }

    for (String task : _onlyTasks) {
      if (!tasks.contains(task))
        throw new UnknownTaskException(task);
    }

    for (String task : _skipTasks) {
      if (!tasks.contains(task))
        throw new UnknownTaskException(task);
    }

    if (_skipToTask != null) {
      int index = tasks.indexOf(_skipToTask);
      if (index == -1)
        throw new UnknownTaskException(_skipToTask);

      for (int i = 0; i < index; i++)
        tasks.remove(0);
    }

    if (!_onlyTasks.isEmpty())
      tasks.retainAll(_onlyTasks);

    tasks.removeAll(_skipTasks);

    return new HashSet<String>(tasks);
  }

  private boolean isTaskEnabled(TaskDefinition taskDef) {
    return _onlyTasks.contains(taskDef.getTaskName())
        || _includeTasks.contains(taskDef.getTaskName()) || taskDef.isEnabled();
  }
}
