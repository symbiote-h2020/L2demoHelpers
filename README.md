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

#### Setting up the demo:
1. Delete the *symbiote-aam-tests-database* and *symbiote-platform-aam-tests-database*
2. Run Core Authentication and Authorization Manager using *gradle bootRun* command and wait until there is a message that confirms it is up and running at port 8801. It should be left running in the background.
3. Register the platform in Core AAM (i.e. *P1_PlatformsRegistrationInCoreAAM*)
4. Generate the platform keystore (i.e. *P2_Platform1AAMKeyStoreGeneration*).  
5. Move generated `platform_1.p12` file to PLATFORMAAMS's `src/main/resources` folder
6. Run the Platform AAM use using *gradle bootRun* and wait until there is a message that confirms it is up and running at port 8802. It should be left running in the background.

#### Access the resource without registering to the PAAM
1. Run *P5_L2demoClient*. It stops with message: *"USER_NOT_REGISTERED_IN_REPOSITORY"*, which showcases that you cannot access resources without an account in the PAAM

#### Registered user tries to access without federation creation
1. Register a user in PAAM by running *P4_UserRegistrationInPlatform1*. 
2. Run *P5_L2demoClient*.  
    - Access with ***HOME TOKEN*** fails
    - ***FOREIGN TOKEN*** cannot be acquired without federation creation

#### Registered user tries to access after federation creation
1. Create the federation by running *P3_FederationRegistration*. 
2. Run *P5_L2demoClient*.  
    - Access with ***HOME TOKEN*** fails
    - ***FOREIGN TOKEN*** is acquired and access to the resource is successful
    - Press enter to continue the *L2DemoClient*. It stops with message: *"Access to federated resource using the cached FOREIGN token from CoreAAM was GRANTED. It should not."*
    
#### Registered user tries to access after federation deletion
1. Run *P5_L2demoClient*.  
    - Access with ***HOME TOKEN*** fails
    - ***FOREIGN TOKEN*** is acquired and access to the resource is successful
2. Delete the federation by running *P6_FederationUpdate*
3. Press enter to continue the *L2DemoClient*. It stops with message: *"Access to federated resource using the cached FOREIGN token was denied as the token was revoked by the issuer due to platform1 no longer being in the federation"*
  
#### Registered user tries to access after federation deletion again
1. Register a user in PAAM by running *P4_UserRegistrationInPlatform1*. 
2. Run *P5_L2demoClient*.  
    - Access with ***HOME TOKEN*** fails
    - ***FOREIGN TOKEN*** cannot be acquired without federation creation
