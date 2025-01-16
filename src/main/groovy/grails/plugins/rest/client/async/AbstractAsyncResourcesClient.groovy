/* Copyright (C) 2013 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package grails.plugins.rest.client.async

import grails.async.Promise
import grails.plugins.rest.client.RequestCustomizer
import grails.plugins.rest.client.RestBuilder
import grails.plugins.rest.client.RestResponse
import groovy.transform.CompileStatic
import static grails.async.Promises.*

/**
 * Abstract base class for Asynchronous resource clients
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@CompileStatic
abstract class AbstractAsyncResourcesClient<T> {

    String url
    AsyncRestBuilder restBuilder

    protected String templatedUrl
    protected Closure customizer

    /**
     * @param url The base URL. Example http://localhost:8080/books
     */
    AbstractAsyncResourcesClient(String url) {
        this.url = url
        this.templatedUrl = "${url}/{id}"
        this.restBuilder = new AsyncRestBuilder()
    }


    AbstractAsyncResourcesClient(String url, AsyncRestBuilder restBuilder) {
        this(url)
        this.restBuilder = restBuilder
    }

    void customize(@DelegatesTo( RequestCustomizer ) Closure customizer) {
        this.customizer = customizer
    }

    /**
     * Issues a GET request for the given id
     *
     * @param id The id
     * @param acceptContentType The content type to pass in the ACCEPT header
     *
     * @return The result
     */
    Promise<T> get(Object id, String acceptContentType = getAcceptContentType()) {
        task {
            RestBuilder syncRestBuilder = restBuilder.restBuilder
            final response = syncRestBuilder.get(templatedUrl, [id: id]) {
                accept getAcceptType(), acceptContentType
                if (customizer) {
                    this.customizer.delegate = delegate
                    this.customizer.call()
                }
            }
            return (T)response.body
        }
    }

    /**
     * Issues a DELETE request for the given id
     *
     * @param id The id
     * @return The result
     */
    Promise<RestResponse> delete(Object id) {
        restBuilder.delete(templatedUrl, [id: id]) {
            if(customizer) {
                this.customizer.delegate = delegate
                this.customizer.call()
            }
        }
    }


    /**
     * Issues a HEAD request for the given id
     *
     * @param id The id
     * @return The result
     */
    Promise<RestResponse> head(Object id) {
        restBuilder.head(templatedUrl, [id: id]) {
            if(customizer) {
                this.customizer.delegate = delegate
                this.customizer.call()
            }
        }
    }

    /**
     * Issues a HEAD request for the given id
     *
     * @param id The id
     * @return The result
     */
    Promise<RestResponse> head() {
        restBuilder.head(url) {
            if(customizer) {
                this.customizer.delegate = delegate
                this.customizer.call()
            }
        }
    }

    /**
     * Issues a OPTIONS request for the given id
     *
     * @param id The id
     * @return The result
     */
    Promise<RestResponse> options(Object id) {
        restBuilder.options(templatedUrl, [id: id]) {
            if(customizer) {
                this.customizer.delegate = delegate
                this.customizer.call()
            }
        }
    }

    /**
     * Issues a OPTIONS request for the given id
     *
     * @param id The id
     * @return The result
     */
    Promise<RestResponse> options() {
        restBuilder.options(url) {
            if(customizer) {
                this.customizer.delegate = delegate
                this.customizer.call()
            }
        }
    }

    /**
     * Issues a POST request for the given id
     *
     * @param requestBody The requestBody of the request
     * @return The result
     */
    Promise<RestResponse> post(T requestBody, String bodyContentType = getAcceptContentType()) {
        restBuilder.post(url) {
            body requestBody
            contentType bodyContentType
            if(customizer) {
                this.customizer.delegate = delegate
                this.customizer.call()
            }
        }
    }

    /**
     * Issues a PUT request for the given id
     *
     * @param requestBody The requestBody of the request
     * @return The result
     */
    Promise<RestResponse> put(Object id, String requestBody, String bodyContentType = getAcceptContentType()) {
        restBuilder.put(templatedUrl, [id:id]) {
            body requestBody
            contentType bodyContentType
            if(customizer) {
                this.customizer.delegate = delegate
                this.customizer.call()
            }
        }
    }


    /**
     * Issues a PATCH request for the given id
     *
     * @param requestBody The requestBody of the request
     * @return The result
     */
    Promise<RestResponse> patch(Object id, String requestBody, String bodyContentType = getAcceptContentType()) {
        restBuilder.patch(templatedUrl, [id:id]) {
            body requestBody
            contentType bodyContentType
            if(customizer) {
                this.customizer.delegate = delegate
                this.customizer.call()
            }
        }
    }

    /**
     * Issues a POST request for the given id
     *
     * @param requestBody The requestBody of the request
     * @return The result
     */
    Promise<RestResponse> post(String requestBody, String bodyContentType = getAcceptContentType()) {
        restBuilder.post(url) {
            body requestBody
            contentType bodyContentType
            if(customizer) {
                this.customizer.delegate = delegate
                this.customizer.call()
            }
        }
    }

    /**
     * Issues a PUT request for the given id
     *
     * @param requestBody The requestBody of the request
     * @return The result
     */
    Promise<RestResponse> put(Object id, T requestBody, String bodyContentType = getAcceptContentType()) {
        restBuilder.put(templatedUrl, [id:id]) {
            body requestBody
            contentType bodyContentType
            if(customizer) {
                this.customizer.delegate = delegate
                this.customizer.call()
            }
        }
    }

    /**
     * Issues a PATCH request for the given id
     *
     * @param requestBody The requestBody of the request
     * @return The result
     */
    Promise<RestResponse> patch(Object id, T requestBody, String bodyContentType = getAcceptContentType()) {
        restBuilder.patch(templatedUrl, [id:id]) {
            body requestBody
            contentType bodyContentType
            if(customizer) {
                this.customizer.delegate = delegate
                this.customizer.call()
            }
        }
    }



    /**
     * Issues a GET request to the configured URL
     *
     * @param acceptContentType The content type to pass in the ACCEPT header
     *
     * @return The result
     */
    Promise<T> getAll(String acceptContentType = getAcceptContentType()) {
        task {
            final syncRestBuilder = restBuilder.restBuilder
            final response = syncRestBuilder.get(url) {
                accept getAcceptType(), acceptContentType
                if(customizer) {
                    this.customizer.delegate = delegate
                    this.customizer.call()
                }
            }

            return (T)response.body

        }
    }

    /**
     * Subclasses should implement to provide the conversion to the target representation (JSON, XML etc.)
     * @param restResponse The RestResponse
     * @return The converted type
     */
    Class getAcceptType() { String }

    /**
     * @return Subclasses should implement to provide the default content type used to exchange resources
     */
    abstract String getAcceptContentType()
}