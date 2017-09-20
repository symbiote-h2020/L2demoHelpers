# L2Demo

### Overview

This repository contains demonstration of Level 2 SymbIoTe functionality.
All classes under **main.java.demo_steps** are responsible for different kind of configuration.
Their naming convention is as follows:  
`P<number>_<description>` where  
`number` indicates order in which those classes should be ran.  
Accordingly, `description` is short overview of the class' functionality

Currently available classes / functionalities are available:
##### Following steps run on COREAAM 
* P1_PlatformsRegistrationInCoreAAM - registers platform owners and platforms.
  * **Prerequisite:** A running instance of `CoreAAM`
* P2_Platform1AAMKeyStoreGeneration - generates platform keyStore.   
See:[CoreAAM](https://github.com/symbiote-h2020/AuthenticationAuthorizationManager/tree/L2-demo-CORE ).
KeyStore should be placed in platformAAM in main.resources. See:[Platform1AAM](https://github.com/symbiote-h2020/AuthenticationAuthorizationManager/tree/L2-demo-PLATFORM1 ).  
Generated _.p12_ file should be placed in COREAAM's `Resources` folder
* P3_FederationRegistration - registers federation in CoreAAM
##### Following steps run on PLATFORMAAM 
* P4_UserRegistrationInPlatform1
  * **Prerequisite:** A running instance of `Platform1AAM`
* P5_L2demoClient - acquires local home token from Platform1AAM
 and verify it against federation access policies in Platform2. 
 Acquire federated token from CoreAAM and then 
 again check it against policies. 
Working CoreAAM and platform1AAM are required
### Running the L2Demo

In order for the Demo to run it **It is _required_** to have 
*Rabbit-MQ* server and *MongoDB* running in the background

1. Run Core Authentication and Authorization Manager and wait until a message that confirms it is up and running.
It should be left running in the background.
2. Run three steps from L2Demo associated with Core AAM Operations (as described in point above)
   1. P1_PlatformsRegistrationInCoreAAM  
   2. P2_Platform1AAMKeyStoreGeneration 
      - After performing this step remember to move generated `.p12` file to COREAAM's `Resources` folder
   3. P3_FederationRegistration 
3. Run Platform Authentication and Authorization Manager and wait until it is up and running. This one should be left running in the background as well.
4. Run two remaining steps associated with Platform AAM Operations.
   1. P4_UserRegistrationInPlatform1
   2. P5_L2demoClient 
5. If performed correctly, follwing message should appear: 
> SecurityRequest using federated token passed Access Policy.
