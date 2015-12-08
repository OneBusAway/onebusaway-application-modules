# Contribute to OneBusAway

OneBusAway is open-source software, which means we'd love to have developers like you get involved by working with the code and contributing changes.  This document is a general guide to working with OneBusAway source-code.  Note that some individual OneBusAway projects and modules may have more specific developer documentation as well.

## Individual Contributor License Agreement (ICLA)

To ensure that the app source code remains fully open-source under a common license, we require that contributors sign [an electronic ICLA](https://docs.google.com/forms/d/12jV-ByyN186MuPotMvxJtNKtSaGGTnEHm8rXomM2bm4/viewform) before contributions can be merged.

## Code of Conduct

This project adheres to the [Open Code of Conduct](http://todogroup.org/opencodeofconduct/#OneBusAway/conduct@onebusaway.org). By participating, you are expected to honor this code.

## Getting Help

If you have trouble working with OneBusAway source-code, there are a couple of ways to get help. 

First, check out our [Troubleshooting page](https://github.com/OneBusAway/onebusaway/wiki/Troubleshooting) to see if we have a known solution.  

You can also ask questions in the following place:
* The [onebusaway-developers](https://groups.google.com/group/onebusaway-developers) mailing list
* Other mailing lists: see the full lists of mailing lists on the [Contact Us page](https://github.com/OneBusAway/onebusaway/wiki/Contact-Us).
* IRC: #onebusaway on Freenode
* Slack - Sign up at https://onebusaway.herokuapp.com

## Working with the Code

All the source-code for the various OneBusAway modules, libraries, and application are hosted in a number of Git repositories belonging to the OneBusAway organization on GitHub:

https://github.com/OneBusAway/

For a more general overview of the various projects that make up OneBusAway, check out our [Features](https://github.com/OneBusAway/onebusaway/wiki/Features) page.

Generally, we use the following technologies for development and project management:

* [GitHub](https://github.com) and Git for source-code versioning.
* [Apache Maven](http://maven.apache.org/) for managing the build.
* [Eclipse](http://www.eclipse.org/) for working with code.  You are of course free to edit code any which way you like, but much of the documentation will be geared towards Eclipse.

**NOTE:**  If you're thinking about making changes/improvements to the OneBusAway code, we **STRONGLY** recommend you create your own copy of the project on Github by creating a Github account, and then clicking the `Fork` button in the upper right-hand corner of the [`onebusaway-application-modules`](https://github.com/OneBusAway/onebusaway-application-modules/wiki) page (or the page of the project you're interested in modifying).  This will make it easier for you to contribute any changes (via ["Pull Requests"](https://help.github.com/articles/using-pull-requests)) back to the main OBA project later.  See below for more on contributing changes.

For specific instructions on importing OneBusAway projects into Eclipse, check out [Import Source-Code into Eclipse](https://github.com/OneBusAway/onebusaway/wiki/Importing-source-code-into-Eclipse).

## Code Style

Before you submit pull requests, you'll need to make sure your code changes/additions match the same style as the existing source code.  Please see our [Code Style page](https://github.com/OneBusAway/onebusaway/wiki/Code-Style) for details on the code style.

## Contributing Changes

As mentioned above, we recommend that you fork the main OBA project by clicking on the `Fork` button in the upper right-hand corner of the [`onebusaway-application-modules`](https://github.com/OneBusAway/onebusaway-application-modules/wiki) page so you can use Github's [Pull Request](https://help.github.com/articles/using-pull-requests) feature to submit changes.

You can contribute code changes in a number of ways:

* Send a GitHub [Pull Request](https://help.github.com/articles/using-pull-requests) (Preferred, since it makes it easier to do code-reviews).
* Send a patch over the mailing list.

When you are submitting code to OneBusAway, here are a few guidelines to go by:

* If you're looking at fixing a few specific problems (e.g., to fix a bug), we suggest you create a Git `branch` in your repository for each specific fix.  This allows you to submit multiple pull requests at a time, and separates commits you don't want to share (on your master branch) with ones you do.  See [this page](https://openshift.redhat.com/community/wiki/github-workflow-for-submitting-pull-requests) for a general suggested process.
* Try to keep your copy of OBA in sync with the main OBA repository.  This reduces the chance of conflicts between the two copies of code.  See the "Keeping updated" section on [this page](https://openshift.redhat.com/community/wiki/github-workflow-for-submitting-pull-requests) for details.
* For each feature request, bug, or change, someone should create an issue in the GitHub issue tracker for the appropriate project.
* For each commit to the repository, reference the issue number in the commit message.  Something like `Issue #5: Adding this cool feature`.  This will automatically link your commit to the issue and make it easier to track changes to the codebase.
* Code style: check out the [Code Style](https://github.com/OneBusAway/onebusaway/wiki/Code-Style) documentation for details on how configure your IDE to match the OneBusAway code-style conventions.  In general, try to match the existing code style when adding new code.
* Write unit tests.
* Don't break the build.  You can run `mvn verify` to run all the unit tests and checks for a Maven project to verify that your changes haven't broken anything.  Also keep an eye on our [Continuous Integration Server](http://ci.onebusaway.org/).  Some projects also have Travis CI added, which provides an additional reference for builds.
* Code reviews.  If you are new to OneBusAway development or if you are working on a core piece of OneBusAway, it's a good idea to get someone to do a codereview of your change before committing it to the master repository.  GitHub makes codereviews pretty simple with their [Pull Request](https://help.github.com/articles/using-pull-requests) feature.

## Commit Access

What does it take to get commit access to a project?

* Make yourself know to the community by introducing yourself on the [mailing list](https://groups.google.com/group/onebusaway-developers).
* Have two or more changes to the code base submitted and accepted.  They don't need to be big changes, but just enough to give us an idea of how you work with the code.

For more info on the rights and responsibilities of commit access, check out [Project Governance](https://github.com/OneBusAway/onebusaway/wiki/Governance).

## Licensing

All OneBusAway source is provided under the [Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0).

OneBusAway is open source software in order to encourage the contributions of others to making OBA better, and also to reflect the contributions of the open source community at large in the wealth of existing code, tools, and frameworks that OBA builds on.
