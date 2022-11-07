package com.amazon.green.book.service.webapp.config;

import static com.amazon.green.book.service.webapp.constants.ServiceConstants.PROD_DOMAIN;
import static com.amazon.green.book.service.webapp.constants.ServiceConstants.TEST_DOMAIN;
import static com.amazon.green.book.service.webapp.constants.ServiceConstants.US_REALM;

import amazon.platform.config.AppConfig;
import com.amazon.alm.stores.service.ALMStoresServiceClient;
import com.amazon.cloudauth.CloudAuthRegion;
import com.amazon.cloudauth.client.CloudAuthCredentials.RegionalAwsCredentials;
import com.amazon.coral.client.CallAttachmentVisitor;
import com.amazon.coral.client.Calls;
import com.amazon.coral.client.ClientBuilder;
import com.amazon.coral.client.cloudauth.CloudAuthDefaultCredentialsVisitor;
import com.amazon.coral.retry.RetryStrategy;
import com.amazon.coral.retry.strategy.ExponentialBackoffAndJitterBuilder;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.DefaultAwsRegionProviderChain;
import com.google.common.collect.ImmutableMap;
import org.springframework.context.annotation.Bean;

public class AlmStoresServiceClientConfig {

    // Realm and domain values are based on setup in configuration/apollo-shim.d/ShimCustomReplaceValues
    // CloudAuth Qualifiers are based on ALMStoresServiceDefaults.config: https://code.amazon.com/packages/ALMStoresServiceClientConfig/blobs/f0bf22e6bbdb196dcf1e5ed5cdeab6767e5c470c/--/coral-config/ALMStoresServiceDefaults.config#L45
    private static final ImmutableMap<String, ImmutableMap<String, String>> ALM_STORES_SERVICE_QUALIFIERS = ImmutableMap.of(
            TEST_DOMAIN, ImmutableMap.of(US_REALM, "CloudAuth.test.NAAmazon"),
            PROD_DOMAIN, ImmutableMap.of(US_REALM, "CloudAuth.prod.NAAmazon"));
    private static final int RETRY_INITIAL_INTERVAL_MS = 300;
    private static final int RETRY_EXPONENTIAL_FACTOR = 2;
    private static final int RETRY_MAX_ATTEMPTS = 3;
    private static final int RETRY_MAX_ELAPSED_MS = 4000;
    private static final double RETRY_RANDOMIZATION_FACTOR = 0.1;

    /**
     * Creates an ALMStoreService client based on service domain and realm.
     *
     * @return ALMStoreService client
     */
    @Bean
    public ALMStoresServiceClient getAlmStoresServiceClient() {
        return new ClientBuilder()
                .remoteOf(ALMStoresServiceClient.class)
                .withConfiguration(ALM_STORES_SERVICE_QUALIFIERS.get(AppConfig.getDomain()).get(AppConfig.getRealm().name()))
                .withCallVisitors(getCloudAuthCredentialsVisitor(), getCallAttachmentVisitor())
                .newClient();
    }

    /**
     * Returns the AWS Credentials needed for CloudAuth.
     *
     * @return the AWS credentials based on the region
     */
    private CloudAuthDefaultCredentialsVisitor getCloudAuthCredentialsVisitor() {
        final RegionalAwsCredentials regionalAwsCredentials =
                new RegionalAwsCredentials(
                        DefaultAWSCredentialsProviderChain.getInstance(),
                        CloudAuthRegion.from(new DefaultAwsRegionProviderChain().getRegion()));
        return new CloudAuthDefaultCredentialsVisitor(regionalAwsCredentials);
    }

    /**
     * Returns a CallVisitor that attaches the supplied CallAttachment to a Call object.
     *
     * @return the call attachment visitor
     */
    private CallAttachmentVisitor getCallAttachmentVisitor() {
        final RetryStrategy<Void> retryStrategy = new ExponentialBackoffAndJitterBuilder()
                // Common Coral recoverable exceptions are defined here: https://tiny.amazon.com/1iioyia8q/codeamazpackCorablobd97fsrc
                .retryOn(Calls.getCommonRecoverableThrowables())
                .withInitialIntervalMillis(RETRY_INITIAL_INTERVAL_MS)
                .withExponentialFactor(RETRY_EXPONENTIAL_FACTOR)
                .withMaxAttempts(RETRY_MAX_ATTEMPTS)
                .withMaxElapsedTimeMillis(RETRY_MAX_ELAPSED_MS)
                .withRandomizationFactor(RETRY_RANDOMIZATION_FACTOR)
                .newStrategy();

        return new CallAttachmentVisitor(Calls.retry(retryStrategy));
    }
}
