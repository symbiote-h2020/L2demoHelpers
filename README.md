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
  * **Prerequisite:** A running instance of `CoreAAM` See:[CoreAAM](https://github.com/symbiote-h2020/AuthenticationAuthorizationManager/tree/L2-demo-CORE ).
* P2_Platform1AAMKeyStoreGeneration - generates platformAAM keyStore.   
Generated _platform_1.p12_ file should be placed in PLATFORMAAM's `src/main/resources` folder
* P3_FederationInitialization - initializes federation in CoreAAM (removes existing one and adds new)
##### Following steps run on PLATFORMAAM 
* P4_UserRegistrationInPlatform1 - registers test user in Platform1AAM database.
  * **Prerequisite:** A running instance of `Platform1AAM`
* P5_L2demoClient - acquires local home token from Platform1AAM
 and verify it against federation access policies in Platform2AAM. 
 Acquire federated token from CoreAAM and then 
 again check it against policies.
 Check it against policies after federation update.
Working CoreAAM and platform1AAM are required
* P6_FederationUpdate - updates federation in CoreAAM (removes existing one and adds new).
### Running the L2Demo

In order for the Demo to run it **It is _required_** to have 
*Rabbit-MQ* server and *MongoDB* running in the background
What's more, please clone git repositories containing CoreAAM and PlatformAAM:
* [CoreAAM](https://github.com/symbiote-h2020/AuthenticationAuthorizationManager/tree/L2-demo-CORE )
* [Platform1AAM](https://github.com/symbiote-h2020/AuthenticationAuthorizationManager/tree/L2-demo-PLATFORM1)

To run L2demo:
1. Run Core Authentication and Authorization Manager using e.g. gradle bootRun command and wait until a message that confirms it is up and running, port 8801
It should be left running in the background.
2. Run three steps from L2Demo associated with Core AAM Operations (as described in point above)
   1. P1_PlatformsRegistrationInCoreAAM  
   2. P2_Platform1AAMKeyStoreGeneration 
      - After performing this step remember to move generated `platform_1.p12` file to PLATFORMAAMS's `src/main/resources` folder
   3. P3_FederationRegistration 
3. Run Platform Authentication and Authorization Manager using e.g. gradle bootRun and wait until it is up and running. This one should be left running in the background as well, port 8802
4. Run remaining steps associated with Platform AAM Operations.
   1. P4_UserRegistrationInPlatform1
   2. P5_L2demoClient - it stops with message: "Waiting for federation update". Then P6 should be performed.
   3. P6_FederationUpdate
   4. P5_L2demoClient should be continued passing any character to console.
5. If performed correctly, follwing message should appear: 
> SecurityRequest using federated token didn't pass Access Policy as the token was revoked
