Branches Workflow:

Definitions:
original: The original repository github.com/kohsuke/github-api master branch
my_fork: Fork github.com/germanysources/github-api

1. master the code which should be merge in in the original repository
2. organization the code which should be used for the RepositorySearch Server
   Is the top of all branches with all features. 
3. original_merge The original code should be merged in this branch. Later one
(after verified no bugs are contained and merge conflicts are resolved) should
be merged in the organization branch
   Development is done in this branch!
