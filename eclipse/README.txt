Eclipse 3.3.X instructions

== Configure Eclipse Environment==

  All relative paths are relative to the source repository's
  '/eclipse/trunk' folder.

------------ Output Filtering -------------

Window->Preferences->Java->Compiler->Building
Make sure "Filtered Resources" includes ".svn/"

---------- Code style/formatting ----------

Window->Preferences->Java->Code Style->Formatter->Import...
  code-style/edu.washington.cs.rse-format.xml

----------- Import organization -----------

Window->Preferences->Java->Code Style->Organize Imports->Import...
  code-style/edu.washington.cs.rse.importorder

------------ Member sort order ------------

Window->Preferences->Java->Appearance->Members Sort Order
There is no import here, so make your settings match:
  code-style/sort-order.png

First, members should be sorted by category.
1) Types
2) Static Fields
3) Static Initializers
4) Static Methods
5) Fields
6) Initializers
7) Constructors
8) Methods

Second, members in the same category should be sorted by visibility.
1) Public
2) Protected
3) Default
4) Private

Third, within a category/visibility combination, members should be sorted
alphabetically.
