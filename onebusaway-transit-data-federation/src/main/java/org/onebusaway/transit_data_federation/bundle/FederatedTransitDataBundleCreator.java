package org.onebusaway.transit_data_federation.bundle;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.onebusaway.collections.MappingLibrary;
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
 * {@link #setContextPaths(List)}). The core config is kept in the resource:
 * 
 * {@value #BUNDLE_RESOURCE}
 * 
 * @author bdferris
 * @see FederatedTransitDataBundleCreatorMain
 */
public class FederatedTransitDataBundleCreator {

  private static final String BUNDLE_RESOURCE = "classpath:org/onebusaway/transit_data_federation/bundle/application-context-bundle-creator.xml";

  private List<File> _contextPaths = new ArrayList<File>();

  private Map<String, BeanDefinition> _contextBeans = new HashMap<String, BeanDefinition>();

  private File _outputPath;

  private Set<String> _skipTasks = new HashSet<String>();

  private Set<String> _onlyTasks = new HashSet<String>();

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

    List<String> contextPaths = getPrimaryApplicatonContextPaths();
    Map<String, BeanDefinition> contextBeans = getPrimaryBeanDefintions();

    runTasks(contextPaths, contextBeans, true, true, _skipTasks, _onlyTasks);

  }

  private void runTasks(List<String> applicationContextPaths,
      Map<String, BeanDefinition> beans, boolean checkDatabase,
      boolean clearCacheFiles, Set<String> skipTasks, Set<String> onlyTasks)
      throws UnknownTaskException {

    ConfigurableApplicationContext context = ContainerLibrary.createContext(
        applicationContextPaths, beans);

    List<TaskDefinition> taskDefinitions = getTaskList(context);
    Set<String> taskNames = getReducedTaskList(taskDefinitions, skipTasks,
        onlyTasks);

    if (checkDatabase)
      createOrUpdateDatabaseSchemaAsNeeded(context, taskNames);

    if (clearCacheFiles) {
      FederatedTransitDataBundle bundle = context.getBean(FederatedTransitDataBundle.class);
      clearExistingCacheFiles(bundle);
    }

    for (TaskDefinition def : taskDefinitions) {
      String taskName = def.getTaskName();
      if (taskNames.contains(taskName)) {
        System.out.println("== " + taskName + " =====>");
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
          task.run();
        }
      }
    }

    // We don't need this context anymore
    context.stop();
    context.close();
    context = null;
  }

  private Runnable getTask(ApplicationContext context, Runnable task,
      String taskBeanName) {

    if (task == null && taskBeanName != null) {
      task = context.getBean(taskBeanName, Runnable.class);
    }

    return task;
  }

  private void createOrUpdateDatabaseSchemaAsNeeded(
      ConfigurableApplicationContext context, Set<String> taskNames) {

    LocalSessionFactoryBean factory = context.getBean("&sessionFactory",
        LocalSessionFactoryBean.class);

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
    paths.add(BUNDLE_RESOURCE);
    for (File contextPath : _contextPaths)
      paths.add("file:" + contextPath);
    return paths;
  }

  private Map<String, BeanDefinition> getPrimaryBeanDefintions() {
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

  private List<TaskDefinition> getTaskList(ApplicationContext context)
      throws UnknownTaskException {

    Map<String, TaskDefinition> taskDefinitions = context.getBeansOfType(TaskDefinition.class);
    Map<String, TaskDefinition> taskDefinitionsByTaskName = new HashMap<String, TaskDefinition>();

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

    List<TaskDefinition> taskDefinitionsInOrder = new ArrayList<TaskDefinition>();
    for (String taskName : taskNames)
      taskDefinitionsInOrder.add(taskDefinitionsByTaskName.get(taskName));
    return taskDefinitionsInOrder;
  }

  private Set<String> getReducedTaskList(List<TaskDefinition> taskDefinitions,
      Set<String> skipTasks, Set<String> onlyTasks) throws UnknownTaskException {

    // Check task names first
    List<String> tasks = MappingLibrary.map(taskDefinitions, "taskName");

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

    return new HashSet<String>(tasks);
  }
}
