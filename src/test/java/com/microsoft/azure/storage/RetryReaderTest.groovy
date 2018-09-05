/*
 * Copyright Microsoft Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.azure.storage

import com.microsoft.azure.storage.blob.*
import com.microsoft.azure.storage.blob.models.StorageErrorException
import com.microsoft.azure.storage.blob.BlockBlobURL

import com.microsoft.rest.v2.util.FlowableUtil
import io.reactivex.Flowable
import spock.lang.Unroll

class RetryReaderTest extends APISpec {
    BlockBlobURL bu

    def setup() {
        bu = cu.createBlockBlobURL(generateBlobName())
        bu.upload(Flowable.just(defaultData), defaultText.length(), null, null,
                null).blockingGet()
    }

    /*
    This shouldn't really be different from anything else we're doing in the other tests. Just a sanity check against
    a real use case.
     */
    def "Network call"() {
        expect:
<<<<<<< HEAD
        FlowableUtil.collectBytesInBuffer(bu.download(null, null, false).blockingGet().body(null))
                .blockingGet() == defaultData
=======
        FlowableUtil.collectBytesInBuffer(new RetryReader(bu.download(null, null, false, null), info, options, new Function<RetryReader.HTTPGetterInfo, Single<? extends RestResponse<?, Flowable<ByteBuffer>>>>() {
            @Override
            Single<? extends RestResponse<?, Flowable<ByteBuffer>>> apply(RetryReader.HTTPGetterInfo httpGetterInfo) {
                bu.download(new BlobRange(httpGetterInfo.offset, httpGetterInfo.count), null, false, null)
            }
        })).blockingGet() == defaultData

        // Go DownloadResponse, Download, DownloadResponse.body

        // Test with the different kinds of errors that are retryable: Timeout, IOException, 500, 503--assert that the data at the end is still the same - Use the RetryTestFactory (or similar)
        // Another policy which returns a custom flowable that injects an error after a certain amount of data.
        // Different values of options. Valid and invalid. See Adam's comment on CR about count and offset.
        // Null options and info parameters and null internal fields (null count)
>>>>>>> Generated with context feature
    }

    @Unroll
    def "Successful"() {
        setup:
        def flowable = new DownloadResponseMockFlowable(scenario)
        def info = new HTTPGetterInfo()
                .withOffset(0)
                .withCount(flowable.getScenarioData().remaining())

        def options = new ReliableDownloadOptions()
        .withMaxRetryRequests(5)

        def mockRawResponse = flowable.getter(info).blockingGet().rawResponse()

        when:
        DownloadResponse response = new DownloadResponse(mockRawResponse, info, { HTTPGetterInfo newInfo ->
            flowable.getter(newInfo)
        })

        then:
        FlowableUtil.collectBytesInBuffer(response.body(options)).blockingGet() == flowable.getScenarioData()
        flowable.getTryNumber() == tryNumber


        where:
        scenario                                                            | tryNumber | provideInitialResponse
        DownloadResponseMockFlowable.RR_TEST_SCENARIO_SUCCESSFUL_ONE_CHUNK       | 1         | false
        DownloadResponseMockFlowable.RR_TEST_SCENARIO_SUCCESSFUL_MULTI_CHUNK     | 1         | false
        DownloadResponseMockFlowable.RR_TEST_SCENARIO_SUCCESSFUL_STREAM_FAILURES | 4         | false
    }

    @Unroll
    def "Failure"() {
        setup:
        def flowable = new DownloadResponseMockFlowable(scenario)

        def options = new ReliableDownloadOptions()
        .withMaxRetryRequests(5)

        def mockRawResponse = flowable.getter(null).blockingGet().rawResponse()

        when:
        DownloadResponse response = new DownloadResponse(mockRawResponse, null, { HTTPGetterInfo newInfo ->
            flowable.getter(newInfo)
        })
        response.body(options).blockingSubscribe()

        then:
        def e = thrown(Throwable) // Blocking subscribe will sometimes wrap the IOException in a RuntimeException.
        if (e.getCause() != null) {
            e = e.getCause()
        }
        exceptionType.isInstance(e)
        flowable.getTryNumber() == tryNumber

        /*
        tryNumber is 7 because the initial request is the first try, then it will fail when retryCount>maxRetryCount,
        which is when retryCount=6 and therefore tryNumber=7
         */
        where:
        scenario                                                      | exceptionType         | tryNumber
        DownloadResponseMockFlowable.RR_TEST_SCENARIO_MAX_RETRIES_EXCEEDED | IOException           | 7
        DownloadResponseMockFlowable.RR_TEST_SCENARIO_NON_RETRYABLE_ERROR  | Exception             | 1
        DownloadResponseMockFlowable.RR_TEST_SCENARIO_ERROR_GETTER_MIDDLE  | StorageErrorException | 2
    }

    @Unroll
    def "Info null IA"() {
        setup:
        def flowable = new DownloadResponseMockFlowable(
                DownloadResponseMockFlowable.RR_TEST_SCENARIO_SUCCESSFUL_ONE_CHUNK)

        when:
        new DownloadResponse(flowable.getter(info).blockingGet().rawResponse(), info,
                { HTTPGetterInfo newInfo ->
                    flowable.getter(newInfo)
                })


        then:
        thrown(IllegalArgumentException)

        where:
        info                                | _
        null                                | _
        new HTTPGetterInfo().withETag(null) | _
    }

    def "Options IA"() {
        setup:
        def flowable = new DownloadResponseMockFlowable(DownloadResponseMockFlowable.RR_TEST_SCENARIO_SUCCESSFUL_ONE_CHUNK)

        def options = new ReliableDownloadOptions()
        .withMaxRetryRequests(-1)

        when:
        def response = new DownloadResponse(flowable.getter(new HTTPGetterInfo()).blockingGet()
                .rawResponse(), new HTTPGetterInfo(), { HTTPGetterInfo info ->
            flowable.getter(info)
        })

        response.body(options)

        then:
        thrown(IllegalArgumentException)
    }

    def "Getter IA"() {
        setup:
        def flowable = new DownloadResponseMockFlowable(DownloadResponseMockFlowable.RR_TEST_SCENARIO_SUCCESSFUL_ONE_CHUNK)

        when:
        def response = new DownloadResponse(flowable.getter(new HTTPGetterInfo()).blockingGet()
                .rawResponse(), new HTTPGetterInfo(), null)
        response.body(null).blockingSubscribe()

        then:
        thrown(IllegalArgumentException)
    }

    def "Info"() {
        setup:
        def flowable = new DownloadResponseMockFlowable(DownloadResponseMockFlowable.RR_TEST_SCENARIO_INFO_TEST)
        def info = new HTTPGetterInfo()
        .withOffset(20)
        .withCount(10)
        .withETag("etag")
        def options = new ReliableDownloadOptions()
        options.withMaxRetryRequests(5)

        when:
        def response = new DownloadResponse(flowable.getter(info).blockingGet().rawResponse(), info,
                { HTTPGetterInfo newInfo ->
                    return flowable.getter(newInfo)
                })
        response.body(options).blockingSubscribe()

        then:
        flowable.tryNumber == 3
    }

    def "Info IA"() {
        when:
        def info = new HTTPGetterInfo().withCount(-1)

        then:
        thrown(IllegalArgumentException)
    }
}
