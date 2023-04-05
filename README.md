# Microsoft Entra Wallet Library
![badge-privatepreview]
![badge-packagemanagers-supported] 
![badge-package-version] 
![badge-languages] 
![badge-platforms]
![badge-license]
![badge-azure-pipline]

## Introduction
The Microsoft Entra Wallet Library for Android gives your app the ability to begin using the Microsoft Entra Verified Id platform by supporting the issuance and presentation of Verified Ids in accordance with OpenID Connect, Presentation Exchange, Verifiable Credentials, and more up and coming industry standards.

---
## Installation

Add to your app's build.gradle to add Wallet Library as a dependency:
```kotlin
dependencies {
    implementation 'com.microsoft.entra.verifiedid:walletlibrary:0.0.1'
}
```

## Quick Start
Here is a simple example of how to use the library. For more in-depth examples, check out the sample app.

```kotlin
// Create a verifiedIdClient
val verifiedIdClient = VerifiedIdClientBuilder(context).build()

// Create a VerifiedIdRequestInput using a OpenId Request Uri.
val verifiedIdRequestUrl = VerifiedIdRequestURL(Uri.parse("openid-vc://..."))
val verifiedIdRequestResult: Result<VerifiedIdRequest<*>> = verifiedIdClient.createRequest(verifiedIdRequestUrl)

// Every external method's return value is wrapped in a Result object to ensure proper error handling.
if (verifiedIdRequestResult.isSuccess) {
    val verifiedIdRequest = verifiedIdRequestResult.getOrNull()
    val presentationRequest = verifiedIdRequest?.let {
        verifiedIdRequest as VerifiedIdPresentationRequest
    }
} else {
    // If an exception occurs, its value can be accessed here.
    val exception = verifiedIdRequestResult.exceptionOrNull()
}
```

At the time of publish, we support the following requirements on a request:
| Requirement                  	| Description 	| Supported on Request 	|
|------------------------------	|-------------	|------------------------------	|
| GroupRequirement             	| A verifier/issuer could request multiple requirements. If more than one requirement is requested, a GroupRequirement contains a list of the requirements.        	| Issuance/Presentation        	|
| VerifiedIdRequirement        	| A verifier/issuer can request a VerifiedId. See below for helper methods to fulfill the requirement.       	| Issuance/Presentation        	|
| SelfAttestedClaimRequirement 	| An issuer might require a self-attested claim that is simply a string value.        	| Issuance                     	|
| PinRequirement               	| An issuer might require a pin from user.         	| Issuance                     	|
| AccessTokenRequirement       	| An issuer might request an Access Token. An Access Token must be retrieved using an external library.        	| Issuance                     	|
| IdTokenRequirement           	| An issuer might request an Id Token. If the Id Token is not already injected into the request, an Id Token must be retrieved using an external library.       	| Issuance                     	|

To fulfill a requirement, cast it to the correct Requirement type and use the `fulfill` method.
```kotlin
val verifiedIdRequirement = presentationRequest.requirement as VerifiedIdRequirement
verifiedIdRequirement.fulfill(verifiedId)
```

VerifiedIdRequirement contains a helper function `getMatches` that will filter all of the VerifiedId that satisfies the constraints on the VerifiedIdRequirement from a list of VerifiedIds.
```kotlin
val matchingVerifiedIds = verifiedIdRequirement.getMatches(verifiedIds: List<VerifiedId>)
```

You can also validate a requirement to ensure the requirement has been fulfilled.
```kotlin
val validationResult = verifiedIdRequirement.validate()
```

Once all of the requirements are fulfilled, you can double check that the request has been satisfied by calling the `isSatisfied` method on the request object.
```kotlin
val isSatisfied = presentationRequest.isSatisfied()
```

Then, complete the request using the complete method.
- The `complete` method on a `VerifiedIdIssuanceRequest` returns a successful result that contains the issued `VerifiedId`, or if an error occurs, returns a failure result with the error.
- The `complete` method on a `VerifiedIdPresentationRequest` returns an empty successful result or if an error occurs, returns a failure result with the error.
```kotlin
val result = presentationRequest.complete()
```

---
## VerifiedId
A Verified Id is a verifiable piece of information that contains claims about an entity.

### Style
Issuers have the ability to customize the style of a Verified Id. We support `BasicVerifiedIdStyle` which contains basic traits like name, issuer, background color, text color, and logo that can be used to represent the look and feel of a Verified Id.

### Storing VerifiedIds
It is the responsibility of the app developer to store the VerifiedIds. We have included helper functions to encode/decode VerifiedIds to easily store the VerifiedIds in a database as a primitive type.

```kotlin
// Encode a VerifiedId into String.
val encodedVerifiedIdString = verifiedIdClient.encode(verifiedId)
// Decode a VerifiedId from String.
val verifiedId = verifiedIdClient.decodeVerifiedId(encodedVerifiedIdString)
```

## Sample App
1. Clone this repository.
2. Open the WalletLibrary workspace in Android Studio.
3. Run the following command from the location where repository was cloned. It initializes the git submodules. 
```
     git submodule update --init --recursive 
```
4. Create a test device. 
5. Run the `walletlibarydemo` app. 

## Log Injection
You can inject your own log consumer into the Wallet Library by creating a class that conforms to the [Wallet Library Log Consumer Interface](./walletlibrary/src/main/java/com/microsoft/walletlibrary/util/WalletLibraryLogger.kt) and injecting it into the `VerifiedIdClientBuilder`.

```Kotlin
val client = VerifiedIdClientBuilder(context)
    .with(<Your Log Consumer>)
    .build()
```

## Documentation

* [External Architecture](https://github.com/microsoft/entra-verifiedid-wallet-library-ios/blob/dev/Docs/LibraryArchitecture.md)
* [Microsoft Docs](https://learn.microsoft.com/en-us/azure/active-directory/verifiable-credentials/)

## Contributing

This project welcomes contributions and suggestions.  Most contributions require you to agree to a
Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us
the rights to use your contribution. For details, visit https://cla.opensource.microsoft.com.

When you submit a pull request, a CLA bot will automatically determine whether you need to provide
a CLA and decorate the PR appropriately (e.g., status check, comment). Simply follow the instructions
provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/).
For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or
contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.

## Trademarks

This project may contain trademarks or logos for projects, products, or services. Authorized use of Microsoft 
trademarks or logos is subject to and must follow 
[Microsoft's Trademark & Brand Guidelines](https://www.microsoft.com/en-us/legal/intellectualproperty/trademarks/usage/general).
Use of Microsoft trademarks or logos in modified versions of this project must not cause confusion or imply Microsoft sponsorship.
Any use of third-party trademarks or logos are subject to those third-party's policies.

[badge-package-version]: https://img.shields.io/maven-central/v/com.microsoft.entra.verifiedid/walletlibrary
[badge-packagemanagers-supported]: https://img.shields.io/badge/supports-Maven%20Central-yellow.svg
[badge-languages]: https://img.shields.io/badge/languages-Kotlin%20Java-blue.svg
[badge-platforms]: https://img.shields.io/badge/platforms-Android-lightgrey.svg
[badge-license]: https://img.shields.io/github/license/microsoft/entra-verifiedid-wallet-library-android
[badge-azure-pipline]: https://decentralized-identity.visualstudio.com/Core/_apis/build/status/Android%20Wallet%20Library?branchName=dev
[badge-privatepreview]: https://img.shields.io/badge/status-Private%20Preview-red.svg