# Gitlet Design Document

**Name**: May Liu

## Classes and Data Structures
1. Blob  

   _Blobs contain the content of the file._
   
   * Fields
      * content of the file. Serialized. 
      * SHA-1 of the content.   
 
3. Commit

    _Commit is basically a snapshot of a version of the project._ 
    
    * Fields
      * File commitFile: the file that contains the information of all the data in the commit
        * File name is the SHA1 code of the commit. 
      * Metadata: 
        * **String** message: a string that represents log message. 
        * The timeStamp then the commit is added to the Branch. 
      * **Commit** parent: the parent(s) of this commit.
      * all the files
      * SHA-1 of everything above. 
   
5. Branch

   _Branch is basically a tree or linkedList of commits._ 
   
   * Fields
     *  **Commit** HeadPointer: the commit that the head pointer currently points to. 
     *  **String** name: the name of the branch
     
6. Repository
  
    _A Repository contains a stage, a working directory, and a .gitLet file that contains all the branches and stuff?_
   
    _Really don't know if this is necessary. But we'll see._
    
    * Fields 
      * **HashSet of File** WorkingDirectory: the working directory of the repository.
        * I think I can make it a Heap so that it saves time.  
      * **DotFile** gitRepo: where you have all the branches.  
      
    
8. DotFile

   _A very _interesting and_ ~~Magical~~ place where all the branches are located._ 
   
   * Fields
     * **BranchGraph** allBranches: A . 
       * **Commit** initialCommit: the _VERY_ initial commit. 
       * Two important pointers:
         * The default master branch
         * HEAD: a pointer to the commit that we checked out. 
           * it should be under branch tree because HEAD can point to other branch commit later in the future. 
     * **HashMap** addingStage: the staging area? 
     * **HashMap** removingStage: the removing stage. 
     * **Stack** blobs: a Stack of blobs! 
       



## Algorithms

1. Main Class

  
   * addFiles(String[] args)
   * makeCommit(String[] args)
1. Commit
   * Generat the timeStemp
   
* **Init**: create a new repository, with a new DotFile and Working space. All these should start off empty. 
     * In Main method: 
       * initRepository(String[] args)
       * create a repo file with the name in the parameter under CDW, and create a Repository object, _initializedRepo_.
       * Call initializedRepo.init()
     * In the Repository class:
       * _dotGitLet.initializeDefault();
     * In the DotFile class:
       * Create a new BranchGraph that has a head pointer and a master branch
       * Read the content of the BranchGraph into the DotFile.
           
* **Add**: add a file to the stage area so that it's ready to be commit.
     * In Main Method: 
       * First, it has to make sure that the file is in the working directory.
       * Call _initializedRepo_.add()  
     * In the Repository class:
       * Call _dotGitLet.addFile()
     * In the DotFile class: 
       * Create a key of type String, this is the file path in String format
       * Create a Blob by pushing and peaking a Blob representing the content of the file
       * Add a map of the above Key and Blob value to the addingState. 
       
* Commit: 
      * First, create a clone of the current head commit. 
      * Change the timeStemp and the messege. 
      * Look at the adding stage, copy all the maps in there to the _file in the Commit class. 
      * Look at the removing stage, remove the existing one.
        * How? I don't know for now but we'll figure it out. 
      * Delete everything currently existed in the Stage.
      * Generate a SHA code for all these?? 
      
* **Log**:

* **rm**: add the file to the removing stage so that it's ready to be committed
   * In Main Method: 
     * Call _initializedRepo_.remove(file)
     * And then delete the file from CWD if it hasn't already done so.
   * In the Repository class:
     * Call _dotFitLet.removeFile(file)
   * In the DotFile class:
     * If the file is in the adding stage, remove it.
     * If the file is tracked in the current head commit, add it onto the _removingStage.     
     
3. Stage

   * Honestly I feel like Stage is 
   * addNewFile(): append the file onto the staging area.  
   * hasStagedItem(): return boolean of whether or there are stuff stages on it. 
4. DotFile

   * addFile(File file, Blob blob): add the map<file, blob> onto the adding stage. 
     * Maybe I should implement the compareTo method?  
   * commit(): calls currBranch.makeNewCommit()
   * showLogs(): show the log in the format provided in the spec. 
   * checkOut()
   * reset()
   * rmBranch()
   * merge() 
   * status()
   * closestAncestor(): honestly idk if this should really be here tho. 
5. Branch

   * makeNewCommit(): make a new Commit object. Set the head to it by calling setHead(). 
   * setHead(Commit cmt): set the head of the branch into CMT. 

6. Blob

   * isIdentical(Blob blob): check if the two blobs are identical by comparing the two SHA code.  


## Persistence



