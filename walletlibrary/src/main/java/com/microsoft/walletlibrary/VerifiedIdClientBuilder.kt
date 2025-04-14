internal class VerifiedIdClientBuilder {

    var keychainAccessGroupIdentifier: String? = null

    private var correlationHeader: VerifiedIdCorrelationHeader? = null

    private var urlSession: URLSession = URLSession.shared

    private var logger: WalletLibraryLogger

    private var requestResolvers: MutableList<RequestResolving> = mutableListOf()

    private var requestProcessors: MutableList<RequestProcessing> = mutableListOf()

    private var rootOfTrustResolver: RootOfTrustResolver? = null

    private var extensions: MutableList<VerifiedIdExtendable> = mutableListOf()

    private var previewFeatureFlagsSupported: MutableList<String> = mutableListOf()

    private var identifiers: MutableList<HolderIdentifier> = mutableListOf()

    private val identifierRepository: HolderIdentifierRepository

    private var testClientCode: String? = null

    constructor() {
        logger = WalletLibraryLogger()
        this.identifierRepository = IdentifierRepository()
    }

    internal constructor(identifierRepository: HolderIdentifierRepository) {
        this.logger = WalletLibraryLogger()
        this.identifierRepository = identifierRepository
    }

    fun build(): VerifiedIdClient {

        val previewFeatureFlags = PreviewFeatureFlags(previewFeatureFlagsSupported)
        val vcLogConsumer = WalletLibraryVCSDKLogConsumer(logger)
        VerifiableCredentialSDK.initialize(vcLogConsumer, keychainAccessGroupIdentifier)

        val walletLibraryNetworking = WalletLibraryNetworking(
            urlSession,
            logger,
            correlationHeader
        )

        val identifiers = getAllIdentifiers(previewFeatureFlags)

        val configuration = LibraryConfiguration(
            logger = logger,
            mapper = Mapper(),
            networking = walletLibraryNetworking,
            verifiedIdDecoder = VerifiedIdDecoder(),
            verifiedIdEncoder = VerifiedIdEncoder(),
            previewFeatureFlags = previewFeatureFlags,
            identifiers = identifiers
        )

        registerSupportedResolvers(configuration)
        registerSupportedRequestProcessors(configuration)
        registerVerifiedIdExtensions(configuration)

        val requestResolverFactory = RequestResolverFactory(requestResolvers)
        val requestHandlerFactory = RequestProcessorFactory(requestProcessors)
        return VerifiedIdClient(
            requestResolverFactory,
            requestHandlerFactory,
            configuration
        )
    }

    private fun getAllIdentifiers(previewFeatureFlags: PreviewFeatureFlags): List<HolderIdentifier> {
        if (previewFeatureFlags.isPreviewFeatureSupported(PreviewFeatureFlags.FIPSCompliantIdentifier)) {
            val holderIdentifier = getMainHolderIdentifier()
            if (holderIdentifier != null) {
                identifiers.add(holderIdentifier)
            }
        } else {
            val defaultIdentifier = try {
                VerifiableCredentialSDK.identifierService.fetchOrCreateMasterIdentifier()
            } catch (e: Exception) {
                null
            }

            val holderIdentifier = try {
                defaultIdentifier?.toHolderIdentifier(CryptoOperations())
            } catch (e: Exception) {
                null
            }

            if (holderIdentifier != null) {
                identifiers.add(holderIdentifier)
            } else {
                logger.logError("Unable to load default Identifiers.")
            }
        }

        return identifiers
    }

    private fun getMainHolderIdentifier(): HolderIdentifier? {
        return try {
            identifierRepository.getMainHolderIdentifier()
        } catch (e: Exception) {
            logger.logError("Unable to get main Holder Identifier from repository, ${e.message}")
            null
        }
    }

    fun with(identifier: HolderIdentifier): VerifiedIdClientBuilder {
        identifiers.add(identifier)
        return this
    }

    fun with(previewFeatureFlags: List<String>): VerifiedIdClientBuilder {
        previewFeatureFlagsSupported.addAll(previewFeatureFlags)
        return this
    }

    fun with(rootOfTrustResolver: RootOfTrustResolver): VerifiedIdClientBuilder {
        this.rootOfTrustResolver = rootOfTrustResolver
        return this
    }

    fun with(logConsumer: WalletLibraryLogConsumer): VerifiedIdClientBuilder {
        logger.add(logConsumer)
        return this
    }

    fun with(verifiedIdCorrelationHeader: VerifiedIdCorrelationHeader): VerifiedIdClientBuilder {
        this.correlationHeader = verifiedIdCorrelationHeader
        return this
    }

    fun with(urlSession: URLSession): VerifiedIdClientBuilder {
        this.urlSession = urlSession
        return this
    }

    fun with(testClientCode: String): VerifiedIdClientBuilder {
        this.testClientCode = testClientCode
        return this
    }

    fun with(keychainAccessGroupIdentifier: String): VerifiedIdClientBuilder {
        this.keychainAccessGroupIdentifier = keychainAccessGroupIdentifier
        return this
    }

    fun with(verifiedIdExtension: VerifiedIdExtendable): VerifiedIdClientBuilder {
        this.extensions.add(verifiedIdExtension)
        return this
    }

    private fun registerSupportedResolvers(configuration: LibraryConfiguration) {
        println(testClientCode)
        val presentationService = OpenIdPresentationRequestValidator(
            correlationHeader,
            rootOfTrustResolver,
            urlSession
        )
        val openIdURLResolver = OpenIdURLRequestResolver(
            validator = presentationService,
            configuration = configuration
        )
        requestResolvers.add(openIdURLResolver)
    }

    private fun registerSupportedRequestProcessors(configuration: LibraryConfiguration) {
        val issuanceService = IssuanceService(
            correlationHeader,
            rootOfTrustResolver,
            configuration.identifierFactory,
            configuration.logger,
            urlSession
        )

        val openIdProcessor = OpenIdRequestProcessor(
            configuration = configuration,
            manifestResolver = issuanceService,
            verifiableCredentialRequester = issuanceService
        )
        requestProcessors.add(openIdProcessor)

        val credMetadataProcessor = SignedCredentialMetadataProcessor(
            configuration = configuration,
            rootOfTrustResolver = rootOfTrustResolver
        )
        val openId4VCIProcessor = OpenId4VCIProcessor(
            configuration = configuration,
            signedMetadataProcessor = credMetadataProcessor
        )
        requestProcessors.add(openId4VCIProcessor)
    }

    private fun registerVerifiedIdExtensions(conf: LibraryConfiguration) {
        val extConfig = conf.createExtensionConfiguration()
        val allProcessorExtensions: MutableList<RequestProcessorExtendable> = mutableListOf()
        val allPreferHeadersFromExtensions: MutableList<String> = mutableListOf()
        for (ext in extensions) {
            allPreferHeadersFromExtensions.addAll(ext.prefer)
            val processorExtensions = ext.createRequestProcessorExtensions(extConfig)
            allProcessorExtensions.addAll(processorExtensions)
        }

        for (processorExtension in allProcessorExtensions) {
            addExtensionToProcessors(processorExtension)
        }

        for (resolver in requestResolvers) {
            resolver.preferHeaders.addAll(allPreferHeadersFromExtensions)
        }
    }

    private fun <Ext : RequestProcessorExtendable> addExtensionToProcessors(ext: Ext) {
        for (processor in requestProcessors) {
            if (processor::class == ext.requestProcessorClass) {
                processor.requestProcessorExtensions.add(ext)
            }
        }
    }
}