package com.amazon.green.book.service.webapp.api;

import static com.amazon.green.book.service.webapp.constants.BrandConstants.AFS_BRAND_ID;
import static com.amazon.green.book.service.webapp.constants.MetricsConstants.ALM_STORE_LOCATION_NOT_FOUND;
import static com.amazon.green.book.service.webapp.constants.MetricsConstants.ALM_STORE_SERVICE;
import static com.amazon.green.book.service.webapp.constants.MetricsConstants.DISCOVER_IN_STORE_STORES_NOT_FOUND;
import static com.amazon.green.book.service.webapp.constants.MetricsConstants.DISCOVER_IN_STORE_STORES_OPERATION;
import static com.amazon.green.book.service.webapp.constants.MetricsConstants.DISCOVER_PICKUP_STORES_NOT_FOUND;
import static com.amazon.green.book.service.webapp.constants.MetricsConstants.DISCOVER_PICKUP_STORES_OPERATION;
import static com.amazon.green.book.service.webapp.constants.MetricsConstants.FIND_STORES_FOR_BRAND_OPERATION;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import com.amazon.alm.stores.da.service.model.FindStoresForBrandRequest;
import com.amazon.alm.stores.da.service.model.FindStoresForBrandResponse;
import com.amazon.alm.stores.discovery.instore.service.model.DiscoverInStoreStoresRequest;
import com.amazon.alm.stores.discovery.instore.service.model.DiscoverInStoreStoresResponse;
import com.amazon.alm.stores.discovery.instore.service.model.DiscoveredInStoreStoresContext;
import com.amazon.alm.stores.discovery.pickup.service.model.DiscoverPickupStoresRequest;
import com.amazon.alm.stores.discovery.pickup.service.model.DiscoverPickupStoresResponse;
import com.amazon.alm.stores.discovery.pickup.service.model.DiscoveredPickupStoresContext;
import com.amazon.alm.stores.service.ALMStoresServiceClient;
import com.amazon.alm.stores.service.model.ALMCustomer;
import com.amazon.alm.stores.service.model.ALMLocationCriteria;
import com.amazon.alm.stores.service.model.ALMProgramEnum;
import com.amazon.alm.stores.service.model.ALMStore;
import com.amazon.alm.stores.service.model.ALMStoreLocationInfo;
import com.amazon.alm.stores.service.model.ALMStoreOperationalDailyHours;
import com.amazon.alm.stores.service.model.ALMStoreOperationalInfo;
import com.amazon.alm.stores.service.model.ALMStoreReachabilityInfo;
import com.amazon.alm.stores.service.model.ALMStoreRoleEnum;
import com.amazon.alm.stores.service.model.OperationExecutionInfo;
import com.amazon.green.book.service.model.StoreInformation;
import com.amazon.green.book.service.model.StoreInformation.StoreInformationBuilder;
import com.amazon.green.book.service.model.StoreOperationalDailyHours;
import com.amazon.green.book.service.model.StoreOperationalHours;
import com.amazon.green.book.service.webapp.exceptions.GreenBookDependencyException;
import com.amazon.green.book.service.webapp.utils.MetricsEmitter;
import com.amazon.metrics.declarative.servicemetrics.Availability;
import com.amazon.metrics.declarative.servicemetrics.Latency;
import com.amazon.metrics.declarative.servicemetrics.ServiceMetric;
import com.amazon.metrics.declarative.servicemetrics.Timeout;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import java.time.Instant;
import java.util.List;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Log4j2
public class AlmStoresServiceApi {

    private static final Double RADIUS_MILES = 50.0;
    private static final List<String> BUILD_COMPLEX_ATTRIBUTES = ImmutableList.of("OPERATIONAL_DAILY_HOURS");

    private final ALMStoresServiceClient almClient;
    private final MetricsEmitter metricsEmitter;

    /**
     * Discover nearby ALM in-store stores from EITHER the given customer info and ip address OR from postal Code.
     *
     * <p>ALM will internally retrieve geocode from Rancor by customerId and other customer info.
     * Once ALM determines a geocode it will be used for store discovery.
     *
     * <p>See: https://tiny.amazon.com/gapxo1uj/codeamazpackALMSblobfcfdsrc
     *
     * @param almBrandId    an entity that owns regional stores (ALM Stores) like WFM, AFS, Sprouts etc
     * @param marketplaceId obfuscated marketplace id
     * @param customerId    customer id
     * @param sessionId     customer's session id
     * @param ipAddress     customer's ip address
     * @param postalCode    the postalCode used to find nearby stores, null if finding stores based on customer info and ip address instead
     * @return list of store info
     */
    @ServiceMetric(serviceName = ALM_STORE_SERVICE, operation = DISCOVER_IN_STORE_STORES_OPERATION)
    @Latency
    @Availability
    @Timeout
    public List<StoreInformation> discoverGeoInStoreStores(final String almBrandId,
                                                           final String marketplaceId,
                                                           final String customerId,
                                                           final String sessionId,
                                                           final String ipAddress,
                                                           final String postalCode) {
        final DiscoverInStoreStoresRequest request = buildDiscoverInStoreStoresRequest(
                almBrandId, marketplaceId, customerId, sessionId, ipAddress, postalCode);
        final DiscoverInStoreStoresResponse response = almClient.newDiscoverGeoInStoreStoresCall().call(request);
        log.info("Response for newDiscoverGeoInStoreStoresCall call: {}", response);

        final List<String> errorResponse = buildErrorInfo(response.getContext(), DiscoveredInStoreStoresContext::getExecutionInfo);
        if (!errorResponse.isEmpty()) {
            log.error("Dependency error occurred when calling newDiscoverGeoInStoreStoresCall Api. Errors: {}.", errorResponse);
            throw new GreenBookDependencyException("ALM dependency error");
        }

        if (isEmpty(response.getAlmStores())) {
            log.info("Stores were not found when calling newDiscoverGeoInStoreStoresCall Api. Request: {}", request);
            metricsEmitter.emitCount(DISCOVER_IN_STORE_STORES_NOT_FOUND, 1);
        }

        return buildStoreInfoList(response.getAlmStores());
    }

    /**
     * Discover nearby ALM pickup stores from EITHER the given customer info and location OR from postal Code.
     *
     * @param almBrandId    an entity that owns regional stores (ALM Stores) like WFM, AFS, Sprouts etc
     * @param marketplaceId obfuscated marketplace id
     * @param customerId    customer id
     * @param sessionId     customer's session id
     * @param ipAddress     customer's ip address
     * @param postalCode    the postalCode used to find nearby stores, null if finding stores based on customer info and ip address instead
     * @return list of store info
     */
    @ServiceMetric(serviceName = ALM_STORE_SERVICE, operation = DISCOVER_PICKUP_STORES_OPERATION)
    @Latency
    @Availability
    @Timeout
    public List<StoreInformation> discoverGeoPickupStores(final String almBrandId,
                                                          final String marketplaceId,
                                                          final String customerId,
                                                          final String sessionId,
                                                          final String ipAddress,
                                                          final String postalCode) {
        final DiscoverPickupStoresRequest request = buildDiscoverPickupStoresRequest(
                almBrandId, marketplaceId, customerId, sessionId, ipAddress, postalCode);
        final DiscoverPickupStoresResponse response = almClient.newDiscoverGeoPickupStoresCall().call(request);
        log.info("Response for newDiscoverGeoPickupStoresCall call: {}", response);

        final List<String> errorResponse = buildErrorInfo(response.getContext(), DiscoveredPickupStoresContext::getExecutionInfo);
        if (!errorResponse.isEmpty()) {
            log.error("Dependency error occurred when calling newDiscoverGeoPickupStoresCall Api. Errors: {}.", errorResponse);
            throw new GreenBookDependencyException("ALM dependency error");
        }

        if (isEmpty(response.getAlmStores())) {
            log.info("Stores were not found when calling newDiscoverGeoPickupStoresCall Api. Request: {}", request);
            metricsEmitter.emitCount(DISCOVER_PICKUP_STORES_NOT_FOUND, 1);
        }

        return buildStoreInfoList(response.getAlmStores());
    }

    /**
     * Gets all Stores for an AlmBrandId.
     *
     * @param almBrandId an entity that owns regional stores (ALM Stores) like WFM, AFS, Sprouts etc
     * @param marketPlaceId obfuscated marketplace id
     * @return list of store info
     */
    @ServiceMetric(serviceName = ALM_STORE_SERVICE, operation = FIND_STORES_FOR_BRAND_OPERATION)
    @Latency
    @Availability
    @Timeout
    public List<StoreInformation> findAllStoresForBrand(final String almBrandId, final String marketPlaceId) {
        final FindStoresForBrandRequest request = buildFindStoresForBrandRequest(almBrandId, marketPlaceId);
        final FindStoresForBrandResponse response = almClient.newFindStoresForBrandCall().call(request);
        log.info("Response for newFindStoresForBrandCall call: {}", response);
        return buildStoreInfoList(response.getAlmStores());
    }

    @VisibleForTesting
    DiscoverInStoreStoresRequest buildDiscoverInStoreStoresRequest(final String almBrandId,
                                                                   final String marketplaceId,
                                                                   final String customerId,
                                                                   final String sessionId,
                                                                   final String ipAddress,
                                                                   final String postalCode) {
        return DiscoverInStoreStoresRequest
                .builder()
                .withAlmProgram(ALMProgramEnum.UFG)
                .withAlmBrandIds(ImmutableList.of(almBrandId))
                .withMarketplaceId(marketplaceId)
                .withBuildComplexAttributes(BUILD_COMPLEX_ATTRIBUTES)
                .withCustomerInfo(ALMCustomer.builder()
                        .withCustomerId(customerId)
                        .withSessionId(sessionId)
                        .withIpAddress(ipAddress)
                        .build())
                .withLocationCriteria(ALMLocationCriteria.builder()
                        .withPostalCode(postalCode)
                        .withRadius(RADIUS_MILES)
                        .build())
                .build();
    }

    @VisibleForTesting
    DiscoverPickupStoresRequest buildDiscoverPickupStoresRequest(final String almBrandId,
                                                                 final String marketplaceId,
                                                                 final String customerId,
                                                                 final String sessionId,
                                                                 final String ipAddress,
                                                                 final String postalCode) {
        return DiscoverPickupStoresRequest
                .builder()
                .withAlmProgram(ALMProgramEnum.UFG)
                .withAlmBrandIds(ImmutableList.of(almBrandId))
                .withMarketplaceId(marketplaceId)
                .withBuildComplexAttributes(BUILD_COMPLEX_ATTRIBUTES)
                .withCustomerInfo(ALMCustomer.builder()
                        .withCustomerId(customerId)
                        .withSessionId(sessionId)
                        .withIpAddress(ipAddress)
                        .build())
                .withLocationCriteria(ALMLocationCriteria.builder()
                        .withPostalCode(postalCode)
                        .withRadius(RADIUS_MILES)
                        .build())
                .build();
    }

    @VisibleForTesting
    FindStoresForBrandRequest buildFindStoresForBrandRequest(final String almBrandId,
                                                             final String marketplaceId) {
        return FindStoresForBrandRequest.builder()
                .withAlmProgram(ALMProgramEnum.UFG)
                .withAlmBrandId(almBrandId)
                .withMarketplaceId(marketplaceId)
                .withBuildComplexAttributes(BUILD_COMPLEX_ATTRIBUTES)

                // Either AFS or WFM brandId could reach here
                .withRole(AFS_BRAND_ID.equals(almBrandId) ? ALMStoreRoleEnum.INSTORE : ALMStoreRoleEnum.PICKUP)
                .build();
    }

    private <T> List<String> buildErrorInfo(final T responseContext,
                                            final Function<T, OperationExecutionInfo> getExecutionInfo) {
        // ALM request could fail on its dependency calls and still return us a response without throwing
        // an exception. Instead, ALM is adding errors in ExecutionInfo.
        // We need to check this to differentiate with actual case where no store is found.
        return ofNullable(responseContext)
                .map(getExecutionInfo)
                .map(OperationExecutionInfo::getErrors)
                .orElse(ImmutableList.of());
    }

    private <T extends ALMStore> List<StoreInformation> buildStoreInfoList(final List<T> stores) {
        return ofNullable(stores)
                .orElse(ImmutableList.of())
                .stream()
                .map(this::buildStoreInfo)
                .collect(ImmutableList.toImmutableList());
    }

    private StoreInformation buildStoreInfo(final ALMStore almStore) {
        final StoreInformationBuilder storeInfoBuilder = StoreInformation.builder()
                .storeId(almStore.getPhysicalStoreId())
                .libbyStoreId(almStore.getReferencesInfo().getLibbyStoreId())
                .name(almStore.getStoreName())
                .operationalDailyHours(buildOperationalDailyHours(almStore.getOperationalInfo()))

                // almStore.getReachabilityInfo() would be null for findAllStoresForBrand operation
                // doing this to avoid NullPointerException for almStore.getReachabilityInfo().getDistanceInMiles()
                .distanceInMiles(ofNullable(almStore.getReachabilityInfo()).map(ALMStoreReachabilityInfo::getDistanceInMiles).orElse(null));

        final ALMStoreLocationInfo locationInfo = almStore.getLocationInfo();
        // Location data can be null for some ALMStores: https://tiny.amazon.com/1dhs5wzy/codeamazpackALMSblob6812src
        if (locationInfo == null) {
            log.info("ALMStore location not found. storeId: {}", almStore.getPhysicalStoreId());
            metricsEmitter.emitCount(ALM_STORE_LOCATION_NOT_FOUND, 1);
        } else {
            storeInfoBuilder.addressLines(locationInfo.getAddressLines())
                    .city(locationInfo.getCity())
                    .state(locationInfo.getState())
                    .postalCode(locationInfo.getPostalCode())
                    // TimeZone is required for interpreting store hours data.
                    .timezone(locationInfo.getTimeZone())
                    .longitude(locationInfo.getGeocode().getLongitude())
                    .latitude(locationInfo.getGeocode().getLatitude());
        }

        return storeInfoBuilder.build();
    }

    /**
     * ALM only returns store hours for the next few days because hours might change.
     *
     * <p>For example: "operationalHours":
     *       {
     *         "dailyHoursList":
     *         [
     *           {"date": "2021-05-07T07:00:00Z",
     *            "hoursList":
     *             [
     *               {"endTime": "2021-05-08T03:00:00Z",
     *                "startTime": "2021-05-07T14:00:00Z"}
     *             ]
     *           },
     *           {"date": "2021-05-08T07:00:00Z",
     *            "hoursList":
     *             [
     *               {"endTime": "2021-05-09T03:00:00Z",
     *                "startTime": "2021-05-08T15:00:00Z"}
     *             ]
     *           },
     *         ],
     */
    private List<StoreOperationalDailyHours> buildOperationalDailyHours(final ALMStoreOperationalInfo operationalInfo) {
        final List<ALMStoreOperationalDailyHours> almStoreOperationalDailyHours = ofNullable(operationalInfo)
                .map(ALMStoreOperationalInfo::getOperationalDailyHours).orElse(ImmutableList.of());

        return almStoreOperationalDailyHours.stream().map(dailyHour -> StoreOperationalDailyHours.builder()
                .date(Instant.parse(dailyHour.getDate()))
                .operationalHours(dailyHour.getOperationalHours().stream().map(
                        hour -> StoreOperationalHours.builder()
                                .startTime(Instant.parse(hour.getStartTime()))
                                .endTime(Instant.parse(hour.getEndTime()))
                                .build()).collect(toList()))
                .build())
                .collect(toList());
    }
}
