package com.amazon.green.book.service.webapp.config;

import static com.amazon.green.book.service.webapp.constants.ServiceConstants.AAA_OPERATION_SEARCH_STORES;
import static com.amazon.green.book.service.webapp.constants.ServiceConstants.AAA_SERVICE;
import static com.amazon.green.book.service.webapp.constants.ServiceConstants.DEFAULT_MARKETPLACE_ID;
import static com.amazon.green.book.service.webapp.constants.ServiceConstants.TEST_DOMAIN;
import static com.amazon.green.book.service.webapp.constants.UrlMapping.SEARCH_STORES_URL;

import amazon.platform.config.AppConfig;
import com.amazon.cloudauth.CloudAuthRegion;
import com.amazon.core.platform.api.data.descriptor.DataConfigurationState;
import com.amazon.core.platform.api.internationalization.Language;
import com.amazon.core.platform.cloudauth.CloudAuthLocalResource;
import com.amazon.core.platform.cloudauth.configuration.CloudAuthDataSourceConfiguration;
import com.amazon.core.platform.csrf.odin.runtime.OdinCsrfImplementationsFactory;
import com.amazon.core.platform.identity.services.configuration.SessionServiceClientDataConfiguration;
import com.amazon.core.platform.runtime.configuration.PlatformDataSourceDefaultConfiguration;
import com.amazon.core.platform.runtime.configuration.PlatformMarketplaceCustomConfiguration;
import com.amazon.core.platform.runtime.configuration.PlatformPreferencesDefaultConfiguration;
import com.amazon.core.platform.security.runtime.csrf.configuration.CsrfDataConfiguration;
import com.amazon.core.platform.spi.data.descriptor.DataConfiguration;
import com.amazon.core.platform.spi.data.descriptor.StaticGlobalDataSourceDescriptor;
import com.amazon.environment.platform.api.property.EnvironmentStage;
import com.amazon.environment.platform.api.property.EnvironmentType;
import com.amazon.environment.platform.runtime.EmbuSembuDataConfiguration;
import com.amazon.environment.platform.runtime.ImmutableMarketplaceIdentity;
import com.amazonaws.regions.DefaultAwsRegionProviderChain;
import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

/**
 * This class is where you can override existing DataSources and add custom DataSources. If your custom DataSources are configured via an
 * implementation of DataConfiguration  you can chain that configuration into the default PlatformDataSourceDefaultConfiguration. For more
 * information, please go to https://w.amazon.com/bin/view/Horizonte/Dive_Deeper/DataSources/#HOverridingthedefaultplatformconfiguration
 */
public class CustomDataConfigurationOverrides implements DataConfiguration {

    private static final String PARIS_CLIENT_CONFIG_KEY = "PARISService.CloudAuthQualifier";
    private static final String DEFAULT_MARKETPLACE_NAME = "US Marketplace (static override)";
    private static final List<String> DEFAULT_MERCHANTS = Arrays.asList("A1OHOT6VONX3KA");
    private static final String DEFAULT_LANGUAGE = "en-US";
    private static final List<String> DEFAULT_LANGUAGES = Arrays.asList(DEFAULT_LANGUAGE);
    private static final String CSRF_SERVICE_ID = "0x84";

    private final DataConfiguration innerConfiguration;

    /**
     * Additional DataConfiguration classes can be chained using .before(new CustomDataConfiguration()).
     */
    public CustomDataConfigurationOverrides() {
        innerConfiguration = new PlatformDataSourceDefaultConfiguration()
                .before(getCloudAuthDataSourceConfiguration())
                .before(SessionServiceClientDataConfiguration.builder().build())
                .before(EmbuSembuDataConfiguration.builder().build())
                // Override Horizonte's default i18n resolution by disabling SEMBU calls for I18nPreferencesDataSourceDescriptor
                // See: https://w.amazon.com/bin/view/Horizonte/Get_Started/Integrations/SembuEmbu/#HShoppingPortalCustomers
                .before(PlatformPreferencesDefaultConfiguration.getBuilder()
                        .setUseI18n(false)
                        .build());
    }


    @Override
    public void configure(DataConfigurationState dataConfigurationState) {
        innerConfiguration.configure(dataConfigurationState);

        //CSRF requires a name and the serial for an Odin material set. By default this template has place holders in
        //the file /configuration/system-properties/008CsrfKey.properties, there is no requirement to use these default
        //property keys, or even to use system properties to supply these values. The CSRF Sevice ID can be varied as
        //well, but will be used in tracking metrics so it must meet those requirements.
        //You can find more information on the Horizonte wiki, https://w.amazon.com/bin/view/Horizonte/Dive_Deeper/Security/
        CsrfDataConfiguration.newGlobalStaticMapBuilder()
                .addOrReplace(CSRF_SERVICE_ID,
                        OdinCsrfImplementationsFactory.sessionIdCustomerIdScoped0x84Impl(
                                System.getProperty("amazon.csrf.material.set.name.0x84"),
                                Integer.getInteger("amazon.csrf.material.set.serial.0x84")).get())
                .withDefaultId(CSRF_SERVICE_ID).build().configure(dataConfigurationState);

        PlatformMarketplaceCustomConfiguration.builder()
                .withPARISMarketplaceSubDataSourceDescriptor(
                        EnumSet.allOf(EnvironmentType.class), EnumSet.allOf(EnvironmentStage.class))
                .withPARISCloudAuth(AppConfig.findString(PARIS_CLIENT_CONFIG_KEY))
                .withStaticMarketplaceDataSourceDescriptor(
                        EnumSet.allOf(EnvironmentType.class), EnumSet.allOf(EnvironmentStage.class))
                .withStaticMarketplaceIdentity(
                        ImmutableMarketplaceIdentity.builder()
                                .marketplaceName(DEFAULT_MARKETPLACE_NAME)
                                .encryptedMarketplaceId(DEFAULT_MARKETPLACE_ID)
                                .marketplaceOwnerId(DEFAULT_MARKETPLACE_ID)
                                .encryptedMerchantCustomerIds(DEFAULT_MERCHANTS)
                                .availableLanguages(DEFAULT_LANGUAGES)
                                .build())
                .build()
                .configure(dataConfigurationState);

        // Override Horizonte's default locale resolution by binding to the Language API
        // See: https://w.amazon.com/bin/view/Horizonte/Advanced/DataSourcesAdv/#HStep5:Addenvironmentorstage-specificdataconfiguration
        final StaticGlobalDataSourceDescriptor<String> newDescriptor = (key) -> DEFAULT_LANGUAGE;
        dataConfigurationState.bind(Language.KEY, newDescriptor);

        // Configure additional DataSource overrides below
    }

    private CloudAuthDataSourceConfiguration getCloudAuthDataSourceConfiguration() {
        final String qualifier = String.format("native.%s.%s", AppConfig.getDomain(), AppConfig.getRealm());

        final CloudAuthDataSourceConfiguration.Builder builder = CloudAuthDataSourceConfiguration.builder()
                .withCloudAuthAllCredentialsViaNAWS(CloudAuthRegion.from(new DefaultAwsRegionProviderChain().getRegion()))
                // Disable RAWRequiresCloudAuthRule for RAW widget call because FMW hasn't onboard as CloudAuth client
                // By default, CloudAuth adds RAWRequiresCloudAuthRule in the list of CloudAuth rules and we need to override the rules
                // See here: https://sage.amazon.com/posts/1081578
                .withCloudAuthResultRules(ImmutableList.of())
                .withCloudAuthClientQualifier(qualifier);

        if (TEST_DOMAIN.equals(AppConfig.getDomain())) {
            // Disable CloudAuth for test stage
            builder.withResourceServerDisabled();
        } else {
            builder.withCloudAuthResources(ImmutableList.of(
                    // searchStores is a CloudAuth protected endpoint because ShoppingPortal is a CloudAuth client
                    new CloudAuthLocalResource(SEARCH_STORES_URL, AAA_SERVICE, AAA_OPERATION_SEARCH_STORES)));
        }

        return builder.build();
    }
}
