package org.onebusaway.transit_data_federation.bundle;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.onebusaway.container.ContainerLibrary;
import org.onebusaway.transit_data_federation.bundle.model.FederatedTransitDataBundle;
import org.onebusaway.transit_data_federation.bundle.model.GtfsBundle;
import org.onebusaway.transit_data_federation.bundle.model.TaskDefinition;
import org.onebusaway.transit_data_federation.impl.DirectedGraph;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;

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
 * {@link #setPreBundleContextPaths(List)}). The core config is kept in the
 * resource:
 * 
 * {@value #PRE_BUNDLE_RESOURCE}
 * 
 * @author bdferris
 * @see FederatedTransitDataBundleCreatorMain
 */
public class FederatedTransitDataBundleCreator {

  private static final String PRE_BUNDLE_RESOURCE = "classpath:org/onebusaway/transit_data_federation/bundle/application-context-creator-pre-bundle.xml";

  private static final String POST_BUNDLE_RESOURCE = "classpath:org/onebusaway/transit_data_federation/bundle/application-context-creator-post-bundle.xml";

  private List<File> _contextPaths = new ArrayList<File>();

  private Map<String, BeanDefinition> _contextBeans = new HashMap<String, BeanDefinition>();

  private List<File> _preBundleContextPaths = new ArrayList<File>();

  private List<File> _postBundleContextPaths = new ArrayList<File>();

  private File _outputPath;

  private Set<String> _skipTasks = new HashSet<String>();

  private Set<String> _onlyTasks = new HashSet<String>();

  private Set<String> _postBundleSkipTasks = new HashSet<String>();

  private Set<String> _postBundleOnlyTasks = new HashSet<String>();

  private String _skipToTask;

  private boolean _skipApplied = false;

  private Set<String> _visitedTasks = new HashSet<String>();

  /**
   * Additional context paths that will be added when constructing the Spring
   * container that controls the build process. See
   * {@link ContainerLibrary#createContext(Iterable)}.
   * 
   * @param contextPaths additional Spring context paths to add to the container
   */

  public void setContextPaths(List<File> paths) {
    _contextPaths = paths;
  }

  public void setContextBeans(Map<String, BeanDefinition> contextBeans) {
    _contextBeans = contextBeans;
  }

  /**
   * Additional pre-bundle-construction context paths that will be added when
   * constructing the Spring container that controls the build process. See
   * {@link ContainerLibrary#createContext(Iterable)}.
   * 
   * @param contextPaths additional Spring context paths to add to the container
   */
  public void setPreBundleContextPaths(List<File> contextPaths) {
    _preBundleContextPaths = contextPaths;
  }

  /**
   * Additional post-bundle-construction context paths that will be added when
   * constructing the Spring container that controls the build process. See
   * {@link ContainerLibrary#createContext(Iterable)}.
   * 
   * @param contextPaths additional Spring context paths to add to the container
   */
  public void setPostBundleContextPaths(List<File> contextPaths) {
    _postBundleContextPaths = contextPaths;
  }

  /**
   * 
   * @param outputPath the output path of the bundle
   */
  public void setOutputPath(File outputPath) {
    _outputPath = outputPath;
  }

  public void addTaskToOnlyRun(String onlyTask) {
    _onlyTasks.add(onlyTask);
  }

  public void addTaskToSkip(String taskToSkip) {
    _skipTasks.add(taskToSkip);
  }

  public void setSkipToTask(String taskName) {
    _skipToTask = taskName;
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

    /**
     * Set the bundlePath system property. Will be used by "${bundle_path}"
     * references in the Spring config
     */
    System.setProperty("bundlePath", _outputPath.getAbsolutePath());

    List<String> preBundlePaths = getPrimaryApplicatonContextPaths();
    Map<String, BeanDefinition> preBundleBeans = getPrimaryBeanDefintions();
    
    runTasks(preBundlePaths, preBundleBeans, true, true, _skipTasks, _onlyTasks);

    List<String> postBundlePaths = getPostBundleApplicatonContextPaths();
    Map<String, BeanDefinition> postBundleBeans = getPostBundleBeanDefintions();
    runTasks(postBundlePaths, postBundleBeans, false, true, _postBundleSkipTasks,
        _postBundleOnlyTasks);
  }

  private void runTasks(List<String> applicationContextPaths,
      Map<String, BeanDefinition> beans, boolean checkDatabase, boolean clearCacheFiles, Set<String> skipTasks,
      Set<String> onlyTasks) throws UnknownTaskException {

    ConfigurableApplicationContext context = ContainerLibrary.createContext(applicationContextPaths,beans);

    Map<String, TaskDefinition> taskDefinitionsByTaskName = new HashMap<String, TaskDefinition>();
    List<String> taskNames = getTaskList(context, taskDefinitionsByTaskName,
        skipTasks, onlyTasks);

    if (checkDatabase)
      createOrUpdateDatabaseSchemaAsNeeded(context, taskNames);

    if (clearCacheFiles) {
      FederatedTransitDataBundle bundle = context.getBean(FederatedTransitDataBundle.class);
      clearExistingCacheFiles(bundle);
    }

    for (String taskName : taskNames) {
      System.out.println("== " + taskName + " =====>");
      TaskDefinition def = taskDefinitionsByTaskName.get(taskName);
      Runnable task = def.getTask();
      if (task == null)
        throw new IllegalStateException("unknown task bean with name: "
            + taskName);
      task.run();
    }

    // We don't need this context anymore
    context.stop();
    context.close();
    context = null;
  }

  private void createOrUpdateDatabaseSchemaAsNeeded(
      ConfigurableApplicationContext context, List<String> taskNames) {
    LocalSessionFactoryBean factory = context.getBean(LocalSessionFactoryBean.class);

    if (taskNames.contains("gtfs")) {
      factory.dropDatabaseSchema();
      factory.createDatabaseSchema();
    } else {
      factory.updateDatabaseSchema();
    }
  }

  /****
   * Private Methods
   ****/

  private List<String> getPrimaryApplicatonContextPaths() {
    List<String> paths = new ArrayList<String>();
    paths.add(PRE_BUNDLE_RESOURCE);
    for (File contextPath : _contextPaths)
      paths.add("file:" + contextPath);
    for (File contextPath : _preBundleContextPaths)
      paths.add("file:" + contextPath);
    return paths;
  }

  private Map<String, BeanDefinition> getPrimaryBeanDefintions() {
    Map<String, BeanDefinition> beans = new HashMap<String, BeanDefinition>();
    beans.putAll(_contextBeans);
    return beans;
  }

  private List<String> getPostBundleApplicatonContextPaths() {

    List<String> paths = new ArrayList<String>();

    paths.add("classpath:org/onebusaway/transit_data_federation/application-context-services.xml");
    paths.add(PRE_BUNDLE_RESOURCE);
    paths.add(POST_BUNDLE_RESOURCE);
    for (File contextPath : _contextPaths)
      paths.add("file:" + contextPath);
    for (File contextPath : _postBundleContextPaths)
      paths.add("file:" + contextPath);

    return paths;
  }

  private Map<String, BeanDefinition> getPostBundleBeanDefintions() {
    Map<String, BeanDefinition> beans = new HashMap<String, BeanDefinition>();
    beans.putAll(_contextBeans);
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

  private List<String> getTaskList(ApplicationContext context,
      Map<String, TaskDefinition> taskDefinitionsByTaskName,
      Set<String> skipTasks, Set<String> onlyTasks) throws UnknownTaskException {

    Map<String, TaskDefinition> taskDefinitions = context.getBeansOfType(TaskDefinition.class);

    DirectedGraph<String> graph = new DirectedGraph<String>();

    for (TaskDefinition taskDefinition : taskDefinitions.values()) {
      String taskName = taskDefinition.getTaskName();

      if (!_visitedTasks.add(taskName))
        continue;

      taskDefinitionsByTaskName.put(taskName, taskDefinition);
      graph.addNode(taskName);

      String before = taskDefinition.getBeforeTaskName();
      if (before != null)
        graph.addEdge(taskName, before);

      String after = taskDefinition.getAfterTaskName();
      if (after != null)
        graph.addEdge(after, taskName);
    }

    List<String> taskNames = graph.getTopologicalSort(null);
    return getReducedTaskList(taskNames, skipTasks, onlyTasks);
  }

  private List<String> getReducedTaskList(List<String> tasks,
      Set<String> skipTasks, Set<String> onlyTasks) throws UnknownTaskException {

    // Check task names first

    for (String task : onlyTasks) {
      if (!tasks.contains(task))
        throw new UnknownTaskException(task);
    }

    for (String task : skipTasks) {
      if (!tasks.contains(task))
        throw new UnknownTaskException(task);
    }

    if (_skipToTask != null) {
      int index = tasks.indexOf(_skipToTask);
      if (index == -1) {
        if (!_skipApplied)
          tasks.clear();
      } else {
        for (int i = 0; i < index; i++)
          tasks.remove(0);
        _skipApplied = true;
      }

    }

    if (!onlyTasks.isEmpty())
      tasks.retainAll(onlyTasks);

    tasks.removeAll(skipTasks);

    return tasks;
  }
}
