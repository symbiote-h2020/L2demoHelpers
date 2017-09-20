# L2demo
In main.java.demo_steps classes used to configurations can be found. 
They are named P[x]-[description], where x is a step number, in which order they should be run. Description is a short overview what will happen.

* P1_[description] TODO
* P2_GeneratePlatformKeyStore - generates platform keyStore. Working CoreAAM is required! See:[CoreAAM](https://github.com/symbiote-h2020/AuthenticationAuthorizationManager/tree/L2-demo-CORE ).
KeyStore should be placed in platformAAM in main.resources. See:[Platform1AAM](https://github.com/symbiote-h2020/AuthenticationAuthorizationManager/tree/L2-demo-PLATFORM1 ).
* TODO
* P4_AcquireTokensAndCheckAgainstAccessPolicies - acquires local home token from platform1AAM and verify it against federation access policies. 
Acquire federated token from CoreAAM and than again check it against policies. Working CoreAAM and platform1AAM are required