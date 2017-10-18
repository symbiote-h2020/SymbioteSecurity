[![Build Status](https://api.travis-ci.org/symbiote-h2020/SymbIoTeSecurity.svg?branch=staging)](https://api.travis-ci.org/symbiote-h2020/SymbIoTeSecurity)
[![](https://jitpack.io/v/symbiote-h2020/SymbIoTeSecurity.svg)](https://jitpack.io/#symbiote-h2020/SymbIoTeSecurity)
[![codecov.io](https://codecov.io/github/symbiote-h2020/SymbIoTeSecurity/branch/staging/graph/badge.svg)](https://codecov.io/github/symbiote-h2020/SymbIoTeSecurity)
# SymbIoTe Security
This repository contains SymbIoTe security layer interfaces, payloads, helper methods and a thin client named the SecurityHandler used throughout different components and different layers.

## How to include this library in your code
The codes will be transiently available using SymbioteLibraries dependency. However, should one want to include it directly, then
[Jitpack](https://jitpack.io/) can be used to easily import SymbIoTe Security in your code. In Jitpack's website you can find guidelines about how to include repositories for different build automation systems. In the symbIoTe project which utilizes [gradle](https://gradle.org/), developers have to add the following in the *build.gradle*:

1. Add jitpack in your root build.gradle at the end of repositories:
```gradle
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```
2. Add the dependency:
```
compile('com.github.symbiote-h2020:SymbIoTeSecurity:develop-SNAPSHOT')
```
As you notice above, during development (i.e. feature and develop branches of component repositories) the ***develop*** branch of the SymbIoTeSecurity needs to be used, in order to make sure that the latest version is always retrieved. In the official releases (i.e. master branches of Component repositories), this dependecy will be changed to:

```
compile('com.github.symbiote-h2020:SymbIoTeSecurity:{tag}')
```
by the **SymbIoTe Security Team**.

## Instructions for java developers
#### End-user Security Handler

Security handler class provides some useful methods:
  - `Map<String, AAM> getAvailableAAMs()` - returns map of all currently available security entrypoints to symbiote (getCertificate, login, token
 validation) as obtained by the core AAM
  - `Map<String, AAM> getAvailableAAMs(AAM aam)` - returns map of all currently available security entrypoints to symbiote (getCertificate, login, token
validation) for AAM specified specified in parameter
 - `Token login(AAM aam)` - returns home token for your account in a given AAM
 - `Map<AAM, Token> login(List<AAM> foreignAAMs, String homeToken)` - allows you to login to foreign AAMs (you don't have account in) using home token.
 - `Token loginAsGuest(AAM aam)` - returns guest token that allows access to all public resources in symbIoTe
 - `ValidationStatus validate(AAM validationAuthority, String token,
                                  Optional<String> clientCertificate,
                                  Optional<String> clientCertificateSigningAAMCertificate,
                                  Optional<String> foreignTokenIssuingAAMCertificate)` - validates your token to the specified AAM
 - `Certificate getComponentCertificate(String componentIdentifier, String platformIdentifier)` - returns component with specified identifier for the platform
 - `Certificate getCertificate(AAM homeAAM,
                                   String username,
                                   String password,
                                   String clientId)` - method used to acquire a certificate(PKI) for this client from the home AAM
This private key will be used to sign-off the request to AAM
- `buildCredentialsWallet()` - reads all certificates in the keystore and populate the credentialsWallet


See [SecurityHandler.java](https://github.com/symbiote-h2020/SymbIoTeSecurity/blob/develop/src/main/java/eu/h2020/symbiote/security/handler/SecurityHandler.java) 

At the beginning of an integration with Sybiote Security Layer as end-user you have to receive an implementation of [ISecurityHandler.java](https://github.com/symbiote-h2020/SymbIoTeSecurity/blob/develop/src/main/java/eu/h2020/symbiote/security/handler/ISecurityHandler.java) using [SecurityHandlerFactory.java](https://github.com/symbiote-h2020/SymbIoTeSecurity/blob/develop/src/main/java/eu/h2020/symbiote/security/ClientSecurityHandlerFactory.java).
```java
/**
     * Creates an end-user security handler
     *
     * @param coreAAMAddress   Symbiote Core AAM address which is available 
     *                         on the symbiote security webpage
     * @param keystorePath     where the keystore will be stored
     * @param keystorePassword needed to access security credentials
     * @return the security handler ready to talk with Symbiote Security Layer
     * @throws SecurityHandlerException on creation error (e.g. problem with the wallet)
     */
SecurityHandler securityHandler = ClientSecurityHandlerFactory.getSecurityHandler(
        coreAAMAddress, keystorePath, keystorePassword, userId);
```


In order to find the certificate of the component you communicate with, please use the following table:

| Component name | Component certificate key in the AAM collection |
| ------ | ------ |
| Core search | search |
| Core registry | registry |
| Registration handler | reghandler |
| ResourceAccessProxy | rap |
| CoreResourceMonitor | crm |
| CoreResourceAccessMonitor | cram |

#### Component Security Handler
If you want to manage components, create ComponentSecurityHandler object with  [ComponentSecurityHandlerFactory](https://github.com/symbiote-h2020/SymbIoTeSecurity/blob/develop/src/main/java/eu/h2020/symbiote/security/handler/ComponentSecurityHandler.java) class.
```java
/**
     * Creates an end-user component security handler
     *
     * @param coreAAMAddress                 Symbiote Core AAM address which is available 
     *                                       on the symbiote security webpage
     * @param keystorePath                   where the keystore will be stored
     * @param keystorePassword               needed to access security credentials
     * @param clientId                       name of the component in the form of "componentId@platformId"
     * @param localAAMAddress                when using only local AAM for SecurityRequest validation
     * @param alwaysUseLocalAAMForValidation when wanting to use local AAM for SecurityRequest validation
     * @param componentOwnerUsername         AAMAdmin credentials for core components 
     *                                       and platform owner credentials for platform components
     * @param componentOwnerPassword         AAMAdmin credentials for core components 
     *                                       and platform owner credentials for platform components
     * @return the component security handler ready to talk with Symbiote components
     * @throws SecurityHandlerException on creation error (e.g. problem with the wallet)
     */
ComponentSecurityHandler componentSecurityHandler = 
    ComponentSecurityHandlerFactory.getComponentSecurityHandler(
            coreAAMAddress, keystorePath, keystorePassword, clientId, localAAMAddress, 
            alwaysUseLocalAAMForValidation, componentOwnerUsername, componentOwnerPassword);
```

Component Security Handler provides following methods:
 - `Set<String> getSatisfiedPoliciesIdentifiers(Map<String, IAccessPolicy> accessPolicies,
                                                    SecurityRequest securityRequest)` - returns a set of identifiers of policies (e.g. resources identifiers) whose access policies are satisfied with the given SecurityRequest
 - `boolean isReceivedServiceResponseVerified(String serviceResponse,                                                  
                                                  String componentIdentifier,
                                                  String platformIdentifier)` - is used by a component to verify that the other components response was legitimate... e.g. to handle the service response encapsulated in a JWS. Returns true if the service is genuine.
 - `SecurityRequest generateSecurityRequestUsingCoreCredentials()` - is used by a component to generate the SecurityRequest needed to authorize operations in the Symbiote Core to be attached to the business query
             so that the service can confirm that the client should posses provided tokens. Returns the required payload for client's authentication and authorization.
 - `String generateServiceResponse()` - returns the required payload that should be attached next to the components API business response so that the client can verify that the service is legitimate.  
 - `ISecurityHandler getSecurityHandler()` - returns Security Handler if the component owner wants to use it directly
#### SecurityRequest and API
The SecurityRequest (available here [SecurityRequest.java](https://github.com/symbiote-h2020/SymbIoTeSecurity/blob/develop/src/main/java/eu/h2020/symbiote/security/communication/payloads/SecurityRequest.java)) is split into the following HTTP security headers for REST communication. We also offer convenience converters on how to consume the SecurityRequest on your business API and how to prepare one for attaching to a REST request.
```java
// timestamp header
public static final String SECURITY_CREDENTIALS_TIMESTAMP_HEADER = "x-auth-timestamp";
// SecurityCredentials set size header
public static final String SECURITY_CREDENTIALS_SIZE_HEADER = "x-auth-size";
// each SecurityCredentials entry header prefix, they are number 1..size
public static final String SECURITY_CREDENTIALS_HEADER_PREFIX = "x-auth-";
```
whereas the ServiceResponseJWS is in communication just a String and should be transport in the following header
```java
public static final SECURITY_RESPONSE_HEADER = "x-auth-response";
```
The headers are available in the [SecurityConstants.java](https://github.com/symbiote-h2020/SymbIoTeSecurity/blob/develop/src/main/java/eu/h2020/symbiote/security/commons/SecurityConstants.java)

#### SecurityRequest and Guest token
The reference Java code to create the SecurityRequest with a GUEST token is provided in [SecurityRequest.java](https://github.com/symbiote-h2020/SymbIoTeSecurity/blob/develop/src/main/java/eu/h2020/symbiote/security/communication/payloads/SecurityRequest.java) constructor
```java
public SecurityRequest(String guestToken) {
    this.timestamp = ZonedDateTime.now().toInstant().toEpochMilli();
    this.securityCredentials = new HashSet<>();
    securityCredentials.add(new SecurityCredentials(guestToken));
}
```

#### Proxy client for access to AAM Services
In addition to the [IComponentSecurityHandler](https://github.com/symbiote-h2020/SymbIoTeSecurity/blob/develop/src/main/java/eu/h2020/symbiote/security/handler/IComponentSecurityHandler.java) that was released, there's an utility class for REST clients using Feign. In case you are using it, it is created a client that will manage automatically the authentication headers and validate the server response (with respect to security). This class is [SymbioteAuthorizationClient](https://github.com/symbiote-h2020/SymbIoTeSecurity/blob/develop/src/main/java/eu/h2020/symbiote/security/communication/SymbioteAuthorizationClient.java).

So let's say that you have a Feign client named MyServiceFeignClient. Normally you would instantiate it like:

```java
Feign.builder().decoder(new JacksonDecoder())
                 .encoder(new JacksonEncoder())
                 .target(MyServiceFeignClient.class, url);
```

So now, if you want it to manage the security headers automatically, all you have to do is:


  1. Get an instance of the IComponentSecurityHandler:
   ```java

IComponentSecurityHandler secHandler = ComponentSecurityHandlerFactory
                                           .getComponentSecurityHandler(
                                               coreAAMAddress, keystorePath, keystorePassword,
                                               clientId, localAAMAddress, false,
                                               username, password );
```
  2. Create an instance of the [SymbioteAuthorizationClient](https://github.com/symbiote-h2020/SymbIoTeSecurity/blob/develop/src/main/java/eu/h2020/symbiote/security/communication/SymbioteAuthorizationClient.java) 
  passing the Security Handler instance and the target service (serviceComponentIdentifier of the service this client is used to communicate with and servicePlatformIdentifier to which the service belongs ([CORE_AAM_INSTANCE_ID](https://github.com/symbiote-h2020/SymbIoTeSecurity/blob/develop/src/main/java/eu/h2020/symbiote/security/commons/SecurityConstants.java#L20)) for Symbiote core components). So for example, the Registration Handler wanting to communicate with the Registry will pass `registry` in the first parameter and [CORE_AAM_INSTANCE_ID](https://github.com/symbiote-h2020/SymbIoTeSecurity/blob/develop/src/main/java/eu/h2020/symbiote/security/commons/SecurityConstants.java#L20) for the latter. This will allow the Security Handler to get the correct certificate to validate responses.
   ```java
Client client = new SymbioteAuthorizationClient(
    secHandler, serviceComponentIdentifier,servicePlatformIdentifier, new Client.Default(null, null));
```

And now you can pass it on your Feign client creation:
```java
MyServiceFeignClient jsonclient = Feign.builder()
                 .decoder(new JacksonDecoder())
                 .encoder(new JacksonEncoder())
                 .client(client)
                 .target(InterworkingInterfaceService.class, url);
```
From now on, all methods call to jsonclient will generate REST requests with valid authentication headers and the responses will be validated as well for integrity, so in case of a challenge-response failure it will return a 400 error message.

## Instructions for non java developers
### Acquiring client certificates needed to get authorization credentials
The following image depicts in general how to get a symbiote authentication certificate:
![Certificate acquisition procedure](media/acquire_user_cert.png)

0. Make sure you have an account in the platform you want to access resources (using Home credentials).
1. Generate a new keystore with your **ECDSA** keypair using **secp256r1** curve.
2. Using that keypair generate a CSR (signed using **SHA256withECDSA**) for your client with the following CN
   - for ordinary user client:
     - CN=username@clientId@platformId (or SymbIoTe_Core_AAM for a core user)
     - CSR's format in REGEX: ^(CN=)(([\w-])+)(@)(([\w-])+)(@)(([\w-])+)$ 
   - for your component
     - CN=componentId@platform_id
     - CSR's format in REGEX: ^(CN=)(([\w-])+)(@)(([\w-])+)$
   - for a platform AAM
     - CN=platformId
     - CSR's format in REGEX: ^(CN=)(([\w-])+)$
3. submit the CSR along with other credentials to your HOME AAM on the following REST endpoint, to receive your signed Certificate:
```
https://<coreInterfaceAdress>/sign_certificate_request
```
or
```
https://<platformInterworkingInterface>/paam/sign_certificate_request
```

CSR is located in body of the request, sent with POST method.

[CertificateRequest](https://github.com/symbiote-h2020/SymbIoTeSecurity/blob/docs/src/main/java/eu/h2020/symbiote/security/communication/payloads/CertificateRequest.java) example:

```
{
  "username" : "testApplicationUsername",
  "password" : "testApplicationPassword",
  "clientId" : "clientId",
  "clientCSRinPEMFormat" : "-----BEGIN CERTIFICATE REQUEST-----\r\nMIH4MIGfAgEAMD0xOzA5BgNVBAMMMnRlc3RBcHBsaWNhdGlvblVzZXJuYW1lQGNs\r\naWVudElkQFN5bWJJb1RlX0NvcmVfQUFNMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcD\r\nQgAE6hgbq/xGIGp9aRPuzHY1MPuxAA3dmhZ/RQRD/F7t+fhjUaOboWDIkmeAMfw6\r\nc9X+3JZVcJlklBvjWFM+tm96qaAAMAoGCCqGSM49BAMCA0gAMEUCICLGFyqGDt+u\r\nekgxkwpulG53JEMVoQ+OJp9dmf608a76AiEAved+JWfNmA6TBlwq/lllrVthE3rO\r\nru1m7ZyKHBdaoEQ=\r\n-----END CERTIFICATE REQUEST-----\r\n"
}
```

4. Home AAM verifies the request and response containing signed Certificate in body or error status should be received. Signed certificate is added to the HomeAAM's database for the user which send the request and given client's ID.
Response body example:
```
-----BEGIN CERTIFICATE-----
MIIBgjCCASigAwIBAgIBATAKBggqhkjOPQQDAjBJMQ0wCwYDVQQHEwR0ZXN0MQ0w
CwYDVQQKEwR0ZXN0MQ0wCwYDVQQLEwR0ZXN0MRowGAYDVQQDDBFTeW1iSW9UZV9D
b3JlX0FBTTAeFw0xNzEwMTIxMDQ5MDVaFw0xODEwMTIxMDQ5MDVaMD0xOzA5BgNV
BAMMMnRlc3RBcHBsaWNhdGlvblVzZXJuYW1lQGNsaWVudElkQFN5bWJJb1RlX0Nv
cmVfQUFNMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE22XrQOVU5dOYI7nWWE+M
3xNc//0kvpiT/tqOA3AL6Jj1ZZbsui8pQKKJzWjhOTgw0NHULbnqDO8eZ9F63b7D
MKMNMAswCQYDVR0TBAIwADAKBggqhkjOPQQDAgNIADBFAiEA5MwiGcLYFj/9x/80
CR6oAmE3HVBkstwcAaYUsy3kUIECIAPGWPh++7In2oM/PirBFZoR8xKqufypo1lm
61fUd+FJ
-----END CERTIFICATE-----
```

5. User should store received signed Certificate in the Keystore with his private key.

From now the user can log in to his Home AAM and acquire home tokens from it.

### Authorization Token acquisition
To acquire access to any resource, actor needs to acquire authorization credentials containing tokens ([JSON Web Token](https://jwt.io/introduction/)).

Actor needs to know **coreInterfaceAdress**. 
In case of acquiring tokens from other platform, their **platformInterworkingInterfaces** 
can be accessed, sending GET request on:
```
https://<coreInterfaceAdress>/get_available_aams
```
In return, response with json containing [AvailableAAMsCollection](https://github.com/symbiote-h2020/SymbIoTeSecurity/blob/develop/src/main/java/eu/h2020/symbiote/security/communication/payloads/AvailableAAMsCollection.java) should be received.

#### Guest Token
Guest Token is a authorization token, for which no registration is required.
However, it can give access only to public resources. 

To acquire such token, empty HTTP POST request has to be sent on:
```
https://<coreInterfaceAdress>/get_guest_token
```
or
```
https://<platformInterworkingInterface>/paam/get_guest_token
```
depending from which platform we want to acquire Guest Token.
In return, headers with *x-auth-token* containing Guest Token should be received.
#### Home Token 
Home Token is a authorization token, for registered actors only. It can give access to public and private resources (depending on actors privileges).

To log in into a service and acquire Home Token, actor has to generate and send Login Request to the Local AAM in which he is registered. 
Login Request is a [JSON Web Token](https://jwt.io/introduction/), with right claims, wrapped into JWS. In *iss*, actor's unique identifier is sent, *sub* contains one of the actor's client identifier. 
Issue (“iat”) and expiration date (“exp”) limit the validity of the token. Login Request can be created for registered actors with issued certificate in local AAM or for guest.

![Login Request structure](media/home-acquisition.png "Login request token format and content in symbIoTe.")

To acquire such token, HTTP POST request with proper headers has to be sent. 
Required request should look like this:
 ```
 x-auth-token: {token}
 ```
 where {token} is Login Request.
 Request should be send on:
```
https://<coreInterfaceAdress>/get_home_token
```
or
```
https://<platformInterworkingInterface>/paam/get_home_token
```
depending from which platform we want to acquire Home Token.
In return, headers with *x-auth-token* containing Home Token should be received.

#### Structure of *sub* and *iss* claim
There are two kinds of *sub* claim, depending on for who Login Request is created. 

For ordinary user or Platform Owner:
```
ISS: username
SUB: clientId
```
For symbiote components acting on behalf of the CoreAAMOwner or PlatformOwner [Note: R3 version, will be changed in R3.1]:
```
ISS: AAMOwnerUsername/PlatformOwnerUsername
SUB: componentId@PlatformId
```
where platformId is be **Symbiote_Core_AAM** for core components.

##### Structure of *sign* claim
For the sign generation, we use of ECDSA algorithm, by leveraging elliptic curve public keys 256-bits long 
(being 256 the recommended length for EC keys, equivalent to a security level of 128 bits).

Let T<sub>U</sub> be the token (excluding the sign field), H a generic hash function, and P<sub>V,Actor</sub> the private key of the actor that issues the token. 
Then, the sign is computed as:

 sign =  SIGN-ECDSA<sub>256</sub> (H(T<sub>U</sub>), P<sub>V,Actor</sub>)
 
An AAM that would like to verify the authenticity and integrity of the token T<sub>U</sub> needs to gather the public key 
of the actor, namely P<sub>B,Actor</sub> and verify that:

H(T<sub>U</sub>) = VERIFY-ECDSA<sub>256</sub> (H(T<sub>U</sub>), P<sub>B,Actor</sub>)

In case the equation is verified, the token is valid, i.e. it is authentic and integral.

##### Example

Actor wants to acquire Home Token for one of the registrated clients. To do so, he has to generate JWS message, modified JWT from his username, client id, actual data as issue_date and expiration_date (issue_date + 60s). All the information is signed, using actor's private key complementary with the public key for registrated client. From all of this components (data + JWS), text chain is generated and sent to AAM. 
Example login request JWS compact token:

```
eyJhbGciOiJFUzI1NiJ9.eyJpc3MiOiJ0ZXN0dXNlcm5hbWUiLCJzdWIiOiJ0ZXN0Y2xpZW50aWQiLCJpYXQiOjE1MDE1MDk3ODIsImV4cCI6MTUwMTUwOTg0Mn0.SGNpyl3zRA_ptRhA0lFH0o7-nhf3mpxE95ss37_jHYbCnwlRb4zDvVaYCj9DlpppU4U0y3vIPEqM44vV2UZ5Iw
```

Full JSON:

HEADER:
```json
{
    "alg": "ES256"
}
```
PAYLOAD:
```json
{
    "iss": "testusername",
    "sub": "testclientid",
    "iat": 1501509782,
    "exp": 1501509842
}
```
One can compare the above using: https://jwt.io/

##### Signature credentials
Actor's client Public Key (one that the actor stores in the AAM-bound certificate).
```
-----BEGIN PUBLIC KEY-----
MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE7u8bg5nOOsxZvkdmK+Zcvx+byi93
iQ+lMWHsAcOaOAwbmcSU3lKEXKu3gp/ymiXUhIyFuw2Pkxfe7T1e4HSmqA==
-----END PUBLIC KEY-----
```
Actor's client Private Key - known  only to the actor
```
-----BEGIN EC PRIVATE KEY-----
MHcCAQEEIG4jKF3TUcXuKFeyZ0QucJDF6i9SB/i10lnK5pLBVdGqoAoGCCqGSM49
AwEHoUQDQgAE7u8bg5nOOsxZvkdmK+Zcvx+byi93iQ+lMWHsAcOaOAwbmcSU3lKE
XKu3gp/ymiXUhIyFuw2Pkxfe7T1e4HSmqA==
-----END EC PRIVATE KEY-----
```
AAM is converting that message to acquire actor's username and client_id, checks if "token" is valid, authentic and integral using public key from database. 
If everything is ok, AAM sends back Home Authorization Token.
### Authentication and Authorization payloads
Challenge

![Challenge payload structure](media/challenge-payload.PNG)

All the claims marked with T means values of claims from AuthorizationToken for which challenge is made.

•	jti, random number,

•	iss, actor's unique identifier is sent,

•	sub contains the unique identifier of the token,

•	ipk is the public key of the actor.
 
•	hash contains a SHA256 hash of the token and timestamp

•	Issue (“iat”) and expiration date (“exp”) limit the validity of the challenge token.

•	hash claim contains a SHA256 hash of the authorization token compact form String concatenated with the challenge timestamp1

#### Service Response payload

![Service response structure](media/service-response.png)

Claims description:

•	hash claim contains a SHA256 hash of the timestamp2

•	timestamp claim contains the timestamp2

##### Example:
An Application wants to demonstrate the token ownership through the Challenge-Response procedure. To do so, he has to generate a JWS message, signed using actor's private key complementary with its public key. From all of this components (data + JWS), text chain is generated and sent to a Service. 


Example Challenge JWS compact token:
```
eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJ0ZXN0Y2xpZW50aWQiLCJzdWIiOiJBdXRob3JpemF0aW9uVG9rZW5KVEkiLCJpcGsiOiJNRmt3RXdZSEtvWkl6ajBDQVFZSUtvWkl6ajBEQVFjRFFnQUU3dThiZzVuT09zeFp2a2RtSytaY3Z4K2J5aTkzaVErbE1XSHNBY09hT0F3Ym1jU1UzbEtFWEt1M2dwL3ltaVhVaEl5RnV3MlBreGZlN1QxZTRIU21xQT09IiwiaGFzaCI6ImVjNTNkYmEwZjkzNzYyMzEwMzVjNWM1ZjFmNDIwM2UzNDgyNDcwOWUwOTkyZDU3NTZhYmY3N2VhNjc2ZWJkNjQiLCJpYXQiOjE1MDE1MDk3ODIsImV4cCI6MTUwMTUwOTg0Mn0.HjomIkzFXbTjokKDwGTgdHOsU19HdM3xXZFRoHqqIdY
```
 Full Challenge JSON:
```json
{
  "alg": "ES256",
  "typ": "JWT"
}
{
  "iss": "testclientid",
  "sub": "AuthorizationTokenJTI",
  "ipk": "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE7u8bg5nOOsxZvkdmK+Zcvx+byi93iQ+lMWHsAcOaOAwbmcSU3lKEXKu3gp/ymiXUhIyFuw2Pkxfe7T1e4HSmqA==",
  "hash" : "ec53dba0f9376231035c5c5f1f4203e34824709e0992d5756abf77ea676ebd64",
  "iat": 1501509782,
  "exp": 1501509842
}
```
If token ownership is satisfied, Service sends back the Response Token.
Example Response JWS compact token:
```
eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9.eyJoYXNoIjoiYWVkNTE3OTI4OTk4MjM4MDkzNDk3MzRkMDU4ZjdhYzIyODliZjE4OTU0NzEyMmIzMmMyMzBiZjAxMDAwYWExNyIsInRpbWVzdGFtcCI6MTUwNDc3MTMzNzAwMH0.2Oj6Dx4rzg5poB19z9opdEPquQvqg9l65HVnG_C-dU4
```
Full Response JSON:

```json
{
  "alg": "ES256",
  "typ": "JWT"
}
```

```
{
  "hash" : "aed51792899823809349734d058f7ac2289bf189547122b32c230bf01000aa17",
  "timestamp": 1504771337000
}
```
#### Communication details
The SecurityRequest (available [here](https://github.com/symbiote-h2020/SymbIoTeSecurity/blob/develop/src/main/java/eu/h2020/symbiote/security/communication/payloads/SecurityRequest.java))
 can be split into the following HTTP security headers for communication.
 ```
 // timestamp header
 public static final String SECURITY_CREDENTIALS_TIMESTAMP_HEADER = "x-auth-timestamp";
 // SecurityCredentials set size header
 public static final String SECURITY_CREDENTIALS_SIZE_HEADER = "x-auth-size";
 // each SecurityCredentials entry header prefix, they are number 1..size
 public static final String SECURITY_CREDENTIALS_HEADER_PREFIX = "x-auth-";
 ```
 whereas the ServiceResponseJWS is in communication just a String and should be transport in the following header.
 ```
 public static final SECURITY_RESPONSE_HEADER = "x-auth-response";
 ```
 The headers are available in the [SecurityConstants](https://github.com/symbiote-h2020/SymbIoTeSecurity/blob/develop/src/main/java/eu/h2020/symbiote/security/commons/SecurityConstants.java)